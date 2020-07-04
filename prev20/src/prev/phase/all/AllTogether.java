package prev.phase.all;

import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import prev.data.mem.*;
import prev.data.asm.*;
import prev.phase.*;
import prev.phase.memory.*;
import prev.phase.asmgen.*;
import prev.phase.livean.*;
import prev.phase.regall.*;
import prev.Compiler;


/**
 * Putting it all together.
 */
public class AllTogether extends Phase{

	public int num_of_regs;
	
	public AllTogether() {
		super("all");
		
		String num_regs_string = Compiler.cmdLineArgValue("--num-regs");
		if(num_regs_string != null) {
            num_of_regs = Integer.parseInt(num_regs_string);
		}
		else {
            num_of_regs = 8;
		}
	}
	
	public void writeCodeToFile() {
        try {
            String filename = Compiler.cmdLineArgValue("--src-file-name");
            filename = filename.replace(".p20", "");
            
            File file = new File(filename + ".mms");
            FileWriter myWriter = new FileWriter(file.getName());
        
            for(Code code: AsmGen.codes) {
                for(AsmInstr instr: code.instrs) {
                    if(instr instanceof AsmLABEL) {
                        myWriter.write(instr.toString());
                    } else if (instr instanceof AsmCRT) {
                        myWriter.write(instr.toString() + "\n");
                    } else {
                        myWriter.write("\t" + instr.toString(RegAll.tempToReg) + "\n");
                    }
                }
            }
            
            myWriter.close();
        } 
        catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }        
	}

	public void proEpiLogue() {
        Vector<Code> proEpiCodes = new Vector<Code>();
	
        for(Code code: AsmGen.codes) {
            if(code.frame.label.name.equals("_putChar")) {
                code.instrs.clear();
                addPutChar(code);
            }
            else if(code.frame.label.name.equals("_getChar")) {
                code.instrs.clear();
                addGetChar(code);
            } else if(code.frame.label.name.equals("_putString")) {
                code.instrs.clear();
                addPutString(code);
            }
            
            proEpiCodes.add(addCodePrologue(code));
            proEpiCodes.add(addCodeEpilogue(code));
        }
        
        AsmGen.codes.addAll(proEpiCodes);
        AsmGen.codes.add(0, addCrt0());
        AsmGen.codes.add(addExit());
	}
	
	public Code addCrt0() {
        Vector<AsmInstr> crt0 = new Vector<AsmInstr>();
        
        crt0.add(new AsmOPER("LOC Data_Segment", null, null, null));
        crt0.add(new AsmOPER("GREG Stack_Segment", null, null, null));
        crt0.add(new AsmOPER("GREG Stack_Segment+16", null, null, null));
        crt0.add(new AsmOPER("GREG Pool_Segment", null, null, null));
        crt0.add(new AsmOPER("GREG @", null, null, null));
        crt0.add(new AsmCRT("CharOut\tBYTE", null, null, null));
        crt0.add(new AsmOPER("BYTE 0", null, null, null));
        
        
        crt0.add(new AsmCRT("InSize\tIS\t100", null, null, null));
        crt0.add(new AsmCRT("InBuffer\tBYTE\t0", null, null, null));
        crt0.add(new AsmCRT("\tLOC\tInBuffer+InSize", null, null, null));
        crt0.add(new AsmCRT("InArgs\tOCTA\tInBuffer,InSize", null, null, null));
        
        
        for(MemAbsAccess string: Memory.strings.values()) {
                if(string != null) {
                    crt0.add(new AsmCRT(string.label.name + "\tBYTE " + string.init + ",10,0", null, null, null));
                }
        }
        
        for(MemAccess memAccs: Memory.accesses.values()) {
            if(memAccs instanceof MemAbsAccess) {
                MemAbsAccess absAcc = (MemAbsAccess)memAccs;
                long memSize = absAcc.size - 8;
                
                crt0.add(new AsmCRT(absAcc.label.name + "\tOCTA", null, null, null));
                
                while(memSize >= 0) {
                    crt0.add(new AsmCRT("\tOCTA", null, null, null));
                    memSize -= 8;
                }
            }
        }
        
        crt0.add(new AsmOPER("LOC Pool_Segment", null, null, null));
        crt0.add(new AsmCRT("Heap\tOCTA", null, null, null));
        
        crt0.add(new AsmOPER("LOC Stack_Segment", null, null, null));
        crt0.add(new AsmCRT("Stack\tBYTE", null, null, null));
        
        crt0.add(new AsmOPER("LOC #100" , null, null, null));
        
        crt0.add(new AsmCRT("Main\tLDA $254,Stack", null, null, null));
        crt0.add(new AsmOPER("ADD $253,$254,16", null, null, null));
        crt0.add(new AsmOPER("LDA $252,Heap", null, null, null));
        crt0.add(new AsmOPER("PUSHJ $"+ num_of_regs +",_main", null, null, null));
        crt0.add(new AsmOPER("JMP Exit", null, null, null));
        
        Code crt0Code = new Code(null, new MemLabel("start"), new MemLabel("main"), crt0);
        
        return crt0Code;
	}
	
	
	public void addPutChar(Code code) {
        Vector<AsmInstr> putChar = new Vector<AsmInstr>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        defs.add(code.frame.RV);
        
        putChar.add(new AsmLABEL(code.entryLabel));
        putChar.add(new AsmOPER("LDA $255,CharOut", null, null, null));
        putChar.add(new AsmOPER("LDO $1,$253,8", null, null, null));
        putChar.add(new AsmOPER("STB $1,$255,0", null, null, null));
        putChar.add(new AsmOPER("TRAP 0,Fputs,StdOut", null, null, null));
        putChar.add(new AsmOPER("SETL `d0,0", null, defs, null));
        putChar.add(new AsmOPER("JMP " + code.exitLabel.name, null, null, null));
        
        code.instrs.addAll(putChar);
	}
	
	public void addGetChar(Code code) {
        Vector<AsmInstr> getChar = new Vector<AsmInstr>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        defs.add(code.frame.RV);
        
        
        getChar.add(new AsmLABEL(code.entryLabel));
        getChar.add(new AsmOPER("LDA $255,InArgs", null, null, null));
        getChar.add(new AsmOPER("TRAP  0,Fgets,StdIn", null, null, null));
        getChar.add(new AsmOPER("LDA $0,InBuffer", null, null, null));
        getChar.add(new AsmOPER("LDB `d0,$0,0", null, defs, null));
        getChar.add(new AsmOPER("JMP " + code.exitLabel.name, null, null, null));
        
        code.instrs.addAll(getChar);
	}
	
	public void addPutString(Code code) {
        Vector<AsmInstr> putString = new Vector<AsmInstr>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        defs.add(code.frame.RV);
        
        
        putString.add(new AsmLABEL(code.entryLabel));
        putString.add(new AsmOPER("LDO $0,$253,8", null, null, null));
        putString.add(new AsmOPER("SET $255,$0", null, null, null));
        putString.add(new AsmOPER("TRAP 0,Fputs,StdOut", null, null, null));
        putString.add(new AsmOPER("SETL `d0,0", null, defs, null));
        putString.add(new AsmOPER("JMP " + code.exitLabel.name, null, null, null));
        
        code.instrs.addAll(putString);
	}
	
	public Code addExit() {
        Vector<AsmInstr> exit = new Vector<AsmInstr>();
        
        exit.add(new AsmCRT("Exit\tTRAP	0,Halt,0", null, null, null));
        
        Code exitCode = new Code(null, new MemLabel("start"), new MemLabel("main"), exit);
        
        return exitCode;
	}
	
	public Vector<AsmInstr> storeNumber(long offset) {
        Vector<AsmInstr> storeNum = new Vector<AsmInstr>();
        
        long value = Math.abs(offset);
        
        if(value >= 0) {
            storeNum.add(new AsmOPER("SETL $0," + Integer.toString(((short) value & 0xffff)), null, null, null));
            value >>= 16;
        }
        
        if(value > 0) {
            storeNum.add(new AsmOPER("INCML $0," + Integer.toString(((short) value & 0xffff)), null, null, null));
            value >>= 16;
        }
        
        if(value > 0) {
            storeNum.add(new AsmOPER("INCMH $0," + Integer.toString(((short) value & 0xffff)), null, null, null));
            value >>= 16;
        }
        
        if(value > 0) {
            storeNum.add(new AsmOPER("INCH $0," + Integer.toString(((short) value & 0xffff)), null, null, null));
            value >>= 16;
        }
        
        if (offset < 0) {
            storeNum.add(new AsmOPER("NEG $0,0,$0", null, null, null));
        }
	
        return storeNum;
	}
	
	public Code addCodePrologue(Code code) {
        Vector<AsmInstr> prolog_instrs = new Vector<AsmInstr>();
        
        prolog_instrs.add(new AsmLABEL(code.frame.label));
        
        // 1.) Ustvarimo klicni zapis
        
        // [SP - odmik old FP] <- FP
        long offs_oldFP = code.frame.locsSize + 8;
        /*prolog_instrs.add(new AsmOPER("SET $0," + offs_oldFP, null, null, null));
        prolog_instrs.add(new AsmOPER("NEG $0,0,$0", null, null, null));*/
        
        prolog_instrs.addAll(storeNumber(-offs_oldFP));
        prolog_instrs.add(new AsmOPER("STO $253,$254,$0", null, null, null));
        
        // FP <- SP
        prolog_instrs.add(new AsmOPER("SET $253,$254", null, null, null));
        
        // SP <- SP - velikost klicnega zapisa
        prolog_instrs.addAll(storeNumber(code.size()));
        prolog_instrs.add(new AsmOPER("SUB $254,$254,$0", null, null, null));
        
        // 2.) Shranimo povratni naslov
        
        //[FP - odmik RA] <- rJ
        long offs_RA = offs_oldFP + 8;
        /*prolog_instrs.add(new AsmOPER("SET $0," + offs_RA, null, null, null));
        prolog_instrs.add(new AsmOPER("NEG $0,0,$0", null, null, null));*/
        prolog_instrs.addAll(storeNumber(-offs_RA));
        prolog_instrs.add(new AsmOPER("GET $1,rJ", null, null, null));
        prolog_instrs.add(new AsmOPER("STO $1,$253,$0", null, null, null));
        
        // 3.) Skok v jedro
        Vector<MemLabel> jumps = new Vector<MemLabel>();
        jumps.add(code.entryLabel);
        prolog_instrs.add(new AsmOPER("JMP " + code.entryLabel.name, null, null, jumps));
        
        Code proCode = new Code(null, code.frame.label, code.entryLabel, prolog_instrs);
        
        return proCode;
	}
	
	public Code addCodeEpilogue(Code code) {
        Vector<AsmInstr> epilog_instrs = new Vector<AsmInstr>();
        
        epilog_instrs.add(new AsmLABEL(code.exitLabel));
        
        // 1.) Shranimo rezultat
        
        //[FP] <- RV
        Vector<MemTemp> uses = new Vector<MemTemp>();
        uses.add(code.frame.RV);
        epilog_instrs.add(new AsmOPER("STO `s0,$253,0", uses, null, null));
        
        
        // 2.) Restavriramo povratni naslov
        
        // rJ <- [FP - odmik RA]
        long offs_RA = code.frame.locsSize + 16;
        /*epilog_instrs.add(new AsmOPER("SET $0," + offs_RA, null, null, null));
        epilog_instrs.add(new AsmOPER("NEG $0,0,$0", null, null, null));*/
        epilog_instrs.addAll(storeNumber(-offs_RA));
        epilog_instrs.add(new AsmOPER("LDO $1,$253,$0", null, null, null));
        epilog_instrs.add(new AsmOPER("PUT rJ,$1", null, null, null));
        
        // 3.) Razgradimo klicni zapis:
        
        // SP <- SP + velikost klicnega zapisa
        epilog_instrs.addAll(storeNumber(code.size()));
        epilog_instrs.add(new AsmOPER("ADD $254,$254,$0", null, null, null));
        
        // FP <- [SP - odmik old FP]
        long offs_oldFP = offs_RA - 8;
        /*epilog_instrs.add(new AsmOPER("SET $0," + offs_oldFP, null, null, null));
        epilog_instrs.add(new AsmOPER("NEG $0,0,$0", null, null, null));*/
        epilog_instrs.addAll(storeNumber(-offs_oldFP));
        epilog_instrs.add(new AsmOPER("LDO $253,$254,$0" , null, null, null));
        
        // 4.) POP
        
        epilog_instrs.add(new AsmOPER("POP " + num_of_regs + ",0", null, null, null));
        //epilog_instrs.add(new AsmOPER("JMP $rJ", null, null, null));

        Code epiCode = new Code(null, code.exitLabel, new MemLabel("$rJ"), epilog_instrs);
        
        return epiCode;
	}
	
	public void log() {
		if (logger == null)
			return;
		for (Code code : AsmGen.codes) {
			logger.begElement("code");
			logger.addAttribute("entrylabel", code.entryLabel.name);
			logger.addAttribute("exitlabel", code.exitLabel.name);
			logger.addAttribute("tempsize", Long.toString(code.tempSize));
			if(code.frame != null) 
                code.frame.log(logger);
			logger.begElement("instructions");
			for (AsmInstr instr : code.instrs) {
				logger.begElement("instruction");
				logger.addAttribute("code", instr.toString(RegAll.tempToReg));
				logger.endElement();
			}
			logger.endElement();
			logger.endElement();
		}
	}
}
