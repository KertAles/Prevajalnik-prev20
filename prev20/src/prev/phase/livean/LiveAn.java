package prev.phase.livean;

import prev.data.mem.*;
import prev.data.asm.*;
import prev.phase.*;
import prev.phase.asmgen.*;
import java.util.*;

/**
 * Liveness analysis.
 */
public class LiveAn extends Phase {

	public LiveAn() {
		super("livean");
	}

	public void analysis() {
        for(Code code: AsmGen.codes) {
            analysisCode(code);
        }
	}
	
	/*
	repeat
    forall n:
        old_in(n) = in(n)
        old_out(n) = out(n)
        in(n) = use(n) union [ out(n) minus def(n) ]
        out(n) = union_{ n' = naslednik n-ja } [ in(n') ]
    until old_in(n) = in(n) and old_out(n) = out(n), forall n
    */
	public void analysisCode(Code code) {
        HashMap<MemLabel, AsmInstr> labels = new HashMap<MemLabel, AsmInstr>();
	
        for(AsmInstr instr: code.instrs) {
            instr.clearInOut();
        
        }
	
	
        boolean hasChange = true;
        
        while(hasChange) {
            int i = 0;
            hasChange = false;
            
            for(AsmInstr instr: code.instrs) {
                HashSet<MemTemp> old_in = instr.in();
                HashSet<MemTemp> old_out = instr.out();
                HashSet<MemTemp> in = new HashSet<MemTemp>();
                HashSet<MemTemp> out = new HashSet<MemTemp>();
                
                in.addAll(old_out);
                in.removeAll(instr.defs());
                in.addAll(instr.uses());
                
                if(i+1 < code.instrs.size()) {
                    if(code.instrs.get(i+1) instanceof AsmOPER) {
                        out.addAll(code.instrs.get(i+1).in());
                    }
                }
                
                if(instr instanceof AsmLABEL) {
                    labels.put(((AsmLABEL)instr).label(), instr);
                }
                
                if(instr instanceof AsmOPER) {
                    for(MemLabel jmpLabel: instr.jumps()) {
                        AsmInstr labIn = labels.get(jmpLabel);
                        if(labIn != null) {
                            out.addAll(labIn.in());
                        }
                    }
                }
                
                if(!(old_in.equals(in) && old_out.equals(out))) {
                    instr.addInTemps(in);
                    instr.addOutTemp(out);
                    
                    hasChange = true;
                }
                
                i++;
            }
        }
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
				logger.addAttribute("code", instr.toString());
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
