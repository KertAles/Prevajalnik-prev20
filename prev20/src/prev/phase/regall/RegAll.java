package prev.phase.regall;

import java.util.*;

import prev.data.mem.*;
import prev.data.asm.*;
import prev.phase.*;
import prev.phase.asmgen.*;
import prev.phase.livean.*;
import prev.Compiler;


/**
 * Register allocation.
 */
public class RegAll extends Phase {

    class ColorNode {
        private HashSet<MemTemp> neighbors;
        private MemTemp temp;
        private int color;
        
        public ColorNode(MemTemp temp) {
            neighbors = new HashSet<MemTemp>();
            this.temp = temp;
            color = -1;
        }
        
        public int color() {
            return color;
        }
        
        public MemTemp temp() {
            return temp;
        }
        
        public int numNeigh() {
            return neighbors.size();
        }
        
        public void addNeighbor(MemTemp neigh) {
            neighbors.add(neigh);
            neighbors.remove(temp);
        }
        
        public void addAll(HashSet<MemTemp> neighs) {
            neighbors.addAll(neighs);
            neighbors.remove(temp);
        }
        
        public boolean hasNeighbor(MemTemp neigh) {
            return neighbors.contains(neigh);
        }
        
        public void removeNeighbor(ColorNode neigh) {
            neighbors.remove(neigh.temp());
        }
        
        public void removeNeighbor(MemTemp neigh) {
            neighbors.remove(neigh);
        }
        
        public boolean tryColoring(int maxColor) {
            boolean isColored = false;
            
            for(int i = 0; i < maxColor && !isColored; i++) {
                boolean validColor = true;
                
                for(MemTemp neighbor: neighbors) {
                    if(tempToReg.containsKey(neighbor)) {
                        if(tempToReg.get(neighbor).intValue() == i) {
                            validColor = false;
                            break;
                        }
                    }
                }
                
                if(validColor) {
                    isColored = true;
                    this.color = i;
                }
            }
            
            return isColored;
        }
    }

	
	/** Mapping of temporary variables to registers. */
	public static final HashMap<MemTemp, Integer> tempToReg = new HashMap<MemTemp, Integer>();
	
	public final HashMap<MemTemp, ColorNode> colors = new HashMap<MemTemp, ColorNode>();
	public final HashMap<MemTemp, Long> offsets = new HashMap<MemTemp, Long>();
	

	public int num_of_regs = 6;
	
	public RegAll() {
		super("regall");
		
		String num_regs_string = Compiler.cmdLineArgValue("--num-regs");
		if(num_regs_string != null) {
            num_of_regs = Integer.parseInt(num_regs_string);
		}
		else {
            num_of_regs = 5;
		}
	}

	public void allocate() {
        
        for(Code code: AsmGen.codes) {
            while(!allocateCode(code));
            
            offsets.clear();
        }
	}
	
	public Vector<AsmInstr> storeNumber(long offset, MemTemp temp) {
        Vector<MemTemp> defs = new Vector<MemTemp>();
        Vector<AsmInstr> storeNum = new Vector<AsmInstr>();
        defs.add(temp);
        
        long value = Math.abs(offset);
        
        if(value >= 0) {
            storeNum.add(new AsmOPER("SETL `d0," + Integer.toString(((short) value & 0xffff)), null, defs, null));
            value >>= 16;
        }
        
        if(value > 0) {
            storeNum.add(new AsmOPER("INCML `d0," + Integer.toString(((short) value & 0xffff)), defs, defs, null));
            value >>= 16;
        }
        
        if(value > 0) {
            storeNum.add(new AsmOPER("INCMH `d0," + Integer.toString(((short) value & 0xffff)), defs, defs, null));
            value >>= 16;
        }
        
        if(value > 0) {
            storeNum.add(new AsmOPER("INCH `d0," + Integer.toString(((short) value & 0xffff)), defs, defs, null));
            value >>= 16;
        }
        
        if (offset < 0) {
            storeNum.add(new AsmOPER("NEG `d0,0,`s0", defs, defs, null));
        }
	
        return storeNum;
	}
	
    public boolean allocateCode(Code code) {
        colors.clear();
        boolean colored = false;
        
        Stack<ColorNode> colorStack = new Stack<ColorNode>();
        
        //Build interferencni graf
        for(AsmInstr instr: code.instrs) {
            HashSet<MemTemp> allInOut = instr.in();
            allInOut.addAll(instr.out());
            allInOut.addAll(instr.defs());
            
            for(MemTemp temp: allInOut) {
                if(!colors.containsKey(temp)) {
                    colors.put(temp, new ColorNode(temp));
                }
                
                if(tempToReg.containsKey(temp)) {
                    tempToReg.remove(temp);
                }
                
                colors.get(temp).addAll(allInOut);
            }
        }
        
        while(colors.size() > 0) {
            //Simplify
            boolean simplifiable = true;
            
            //System.out.println("Simplifying");
            
            while(simplifiable) {
                simplifiable = false;
                //System.out.println("Here we go");
            
                for(ColorNode colorNode: colors.values()) {
                    if(colorNode.numNeigh() < num_of_regs) {
                        //System.out.println("Found one: " + colorNode.temp().toString());
                        colorStack.push(colorNode);
                        
                        for(ColorNode cNode2: colors.values()) {
                            cNode2.removeNeighbor(colorNode);
                        }
                        
                        colors.remove(colorNode.temp());
                        
                        simplifiable = true;
                        
                        break;
                    }
                }
            }
            
            //Spill
            if(colors.size() > 0) {
                //System.out.println("Spilling the beans");
                for(ColorNode colorNode: colors.values()) {
                    if(colorNode.numNeigh() >= num_of_regs) {
                        //System.out.println("Found one: " + colorNode.temp().toString());
                        colorStack.push(colorNode);
                            
                        for(ColorNode cNode2: colors.values()) {
                            cNode2.removeNeighbor(colorNode);
                        }
                        
                        colors.remove(colorNode.temp());
                            
                        break;
                    }
                }
            }
        }
        
        //Select
        boolean succeeded = true;
        
        ColorNode colorNode = colorStack.peek();
        Stack<ColorNode> secondaryStack = new Stack<ColorNode>();
        secondaryStack.addAll(colorStack);
        
        while(!colorStack.isEmpty() && succeeded) {
            colorNode = colorStack.pop();
            
            if(colorNode.temp() == code.frame.FP) {
                tempToReg.put(colorNode.temp(), Integer.valueOf(253));
                succeeded = true;
            }
            else {
                succeeded = colorNode.tryColoring(num_of_regs);
                
                if(succeeded) {
                    tempToReg.put(colorNode.temp(), Integer.valueOf(colorNode.color()));
                    //System.out.println("Colored " + colorNode.temp().toString());
                }
            }
        }
        
        if(!succeeded) {
            //Put on disk
           
            
            //System.out.println("Here we go again: " + colorNode.temp().toString());
            
            Vector<AsmInstr> modifiedInstrs = new Vector<AsmInstr>();
            modifiedInstrs.addAll(code.instrs);
            
            MemTemp replace = colorNode.temp();
                
            //System.out.println("Replacing " + replace.toString());
                
            /*
                    ADD T5,T3,T2         ADD T5',T3,T2
                                         SET T100,-88 (odmik T5 v klicnem zapisu)
                                         STO T5',FP,T100
                        ...                  ...
                        ...                  ...
                        ...                  ...
                                         SET T101,-88 (odmik T5 v klicnem zapisu)
                                         LDO T5'',FP,T101
                    SUB T7,T5,T4     SUB T7,T5'',T4
                */
                
            for(AsmInstr instr: code.instrs) {
                if(instr.defs().contains(replace)) {
                    int position = modifiedInstrs.indexOf(instr);
                    modifiedInstrs.remove(instr);
                        
                    //System.out.println("Defs " + instr.toString());
                        
                    MemTemp newTemp = new MemTemp();
                    MemTemp offsTemp = new MemTemp();
                    Vector<MemTemp> newDefs = instr.defs();
                    newDefs.remove(replace);
                    newDefs.add(newTemp);
                        
                    Vector<MemTemp> uses = new Vector<MemTemp>();
                    Vector<MemTemp> defs = new Vector<MemTemp>();
                    
                    defs.add(offsTemp);
                        
                    uses.add(newTemp);
                    uses.add(code.frame.FP);
                    uses.add(offsTemp);
                        
                    code.tempSize += 8;
                    long tempOffset = -code.tempSize - code.frame.locsSize - 16;
                        
                    offsets.put(replace, Long.valueOf(tempOffset));                        
                        
                    AsmInstr newInstr = new AsmOPER(instr.instr(), instr.uses(), newDefs, instr.jumps());
                    Vector<AsmInstr> numDefInstrs = storeNumber(tempOffset, offsTemp);
                    
                    //AsmInstr offset = new AsmOPER("SUB `d0,0," + (-tempOffset), numUses, defs, null);
                    AsmInstr store = new AsmOPER("STO `s0,`s1,`s2", uses, null, null);
                        
                        
                    modifiedInstrs.insertElementAt(newInstr, position);
                    int i = 1;
                    
                    for(AsmInstr defInstr: numDefInstrs) {
                        modifiedInstrs.insertElementAt(defInstr, position + i);
                        i++;
                    }
                    
                    modifiedInstrs.insertElementAt(store, position + i);
                }
                else if(instr.uses().contains(replace)) {
                    int position = modifiedInstrs.indexOf(instr);
                    modifiedInstrs.remove(instr);
                        
                    //System.out.println("Uses " + instr.toString());
                        
                    MemTemp newTemp = new MemTemp();
                    MemTemp offsTemp = new MemTemp();
                    Vector<MemTemp> newUses = instr.uses();
                    newUses.remove(replace);
                    newUses.add(newTemp);
                        
                    Vector<MemTemp> uses = new Vector<MemTemp>();
                    Vector<MemTemp> defs = new Vector<MemTemp>();
                    Vector<MemTemp> defsLoad = new Vector<MemTemp>();
                    
                    defs.add(offsTemp);
                        
                    defsLoad.add(newTemp);
                    uses.add(code.frame.FP);
                    uses.add(offsTemp);
                    
                    
                    long tempOffset = offsets.get(replace).longValue();
                        
                    //AsmInstr offset = new AsmOPER("SUB `d0,0," + (-tempOffset), null, defs, null);
                    
                    Vector<AsmInstr> numDefInstrs = storeNumber(tempOffset, offsTemp);
                    
                    AsmInstr load = new AsmOPER("LDO `d0,`s0,`s1", uses, defsLoad, null);
                    AsmInstr newInstr = new AsmOPER(instr.instr(), newUses, instr.defs(), instr.jumps());
                        
                    int i = 0;
                    
                    for(AsmInstr defInstr: numDefInstrs) {
                        modifiedInstrs.insertElementAt(defInstr, position + i);
                        i++;
                    }
                    modifiedInstrs.insertElementAt(load, position + i);
                    modifiedInstrs.insertElementAt(newInstr, position + i + 1);                    
                }
                    
            }
                
            code.instrs.clear();
            //System.out.println("Num of Els" + code.instrs.size());
            code.instrs.addAll(modifiedInstrs);
            
            LiveAn livean = new LiveAn();
            
            livean.analysisCode(code);
            //tempToReg.clear();
            
            colored = false;
            //System.out.println("Failed to color");
        }
        else {
            colored = true;
            //System.out.println("Succesfully colored");
        }
        
        return colored;
    }
	
	public void log() {
		if (logger == null)
			return;
		for (Code code : AsmGen.codes) {
			logger.begElement("code");
			logger.addAttribute("entrylabel", code.entryLabel.name);
			logger.addAttribute("exitlabel", code.exitLabel.name);
			logger.addAttribute("tempsize", Long.toString(code.tempSize));
			code.frame.log(logger);
			logger.begElement("instructions");
			for (AsmInstr instr : code.instrs) {
				logger.begElement("instruction");
				logger.addAttribute("code", instr.toString(tempToReg));
				logger.begElement("temps");
				logger.addAttribute("name", "use");
				for (MemTemp temp : instr.uses()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.begElement("temps");
				logger.addAttribute("name", "def");
				for (MemTemp temp : instr.defs()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.begElement("temps");
				logger.addAttribute("name", "in");
				for (MemTemp temp : instr.in()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.begElement("temps");
				logger.addAttribute("name", "out");
				for (MemTemp temp : instr.out()) {
					logger.begElement("temp");
					logger.addAttribute("name", temp.toString());
					logger.endElement();
				}
				logger.endElement();
				logger.endElement();
			}
			logger.endElement();
			logger.endElement();
		}
	}

}
