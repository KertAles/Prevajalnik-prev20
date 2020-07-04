package prev.phase.asmgen;

import java.util.*;
import prev.data.mem.*;
import prev.data.imc.code.expr.*;
import prev.data.imc.visitor.*;
import prev.data.asm.*;
import prev.common.report.*;
import prev.Compiler;

/**
 * Machine code generator for expressions.
 */
public class ExprGenerator implements ImcVisitor<MemTemp, Vector<AsmInstr>> {

    public int get_num_of_regs() {
        int num_of_regs;
        String num_regs_string = Compiler.cmdLineArgValue("--num-regs");
		if(num_regs_string != null) {
            num_of_regs = Integer.parseInt(num_regs_string);
		}
		else {
            num_of_regs = 5;
		}
    
        return num_of_regs;
    }
   

	public MemTemp visit(ImcBINOP imcBinop, Vector<AsmInstr> instrs) {
        MemTemp temp = new MemTemp();
        Vector<MemTemp> uses = new Vector<MemTemp>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        
        defs.add(temp);
        
        uses.add(imcBinop.fstExpr.accept(this, instrs));
        uses.add(imcBinop.sndExpr.accept(this, instrs));

        switch(imcBinop.oper) {            
        case ADD: instrs.add(new AsmOPER("ADD `d0,`s0,`s1", uses, defs, null)); break;
        case SUB: instrs.add(new AsmOPER("SUB `d0,`s0,`s1", uses, defs, null)); break;
        case MUL: instrs.add(new AsmOPER("MUL `d0,`s0,`s1", uses, defs, null));break;
        case DIV: instrs.add(new AsmOPER("DIV `d0,`s0,`s1", uses, defs, null)); break;
        case MOD: instrs.add(new AsmOPER("DIV `d0,`s0,`s1", uses, defs, null));
                                instrs.add(new AsmOPER("GET `d0,rR", null, defs, null)); break;
        
        case OR: instrs.add(new AsmOPER("OR `d0,`s0,`s1", uses, defs, null)); break;
        case AND: instrs.add(new AsmOPER("AND `d0,`s0,`s1", uses, defs, null)); break;
        
        case EQU: instrs.add(new AsmOPER("CMP `d0,`s0,`s1", uses, defs, null));
                                instrs.add(new AsmOPER("ZSZ `d0,`s0,1", defs, defs, null)); break;
        case NEQ: instrs.add(new AsmOPER("CMP `d0,`s0,`s1", uses, defs, null)); 
                                instrs.add(new AsmOPER("ZSNZ `d0,`s0,1", defs, defs, null)); break;
        case LTH: instrs.add(new AsmOPER("CMP `d0,`s0,`s1", uses, defs, null));
                                instrs.add(new AsmOPER("ZSN `d0,`s0,1", defs, defs, null)); break;
        case LEQ: instrs.add(new AsmOPER("CMP `d0,`s0,`s1", uses, defs, null)); 
                                instrs.add(new AsmOPER("ZSNP `d0,`s0,1", defs, defs, null)); break;
        case GTH: instrs.add(new AsmOPER("CMP `d0,`s0,`s1", uses, defs, null));
                                instrs.add(new AsmOPER("ZSP `d0,`s0,1", defs, defs, null)); break;
        case GEQ: instrs.add(new AsmOPER("CMP `d0,`s0,`s1", uses, defs, null)); 
                                instrs.add(new AsmOPER("ZSNN `d0,`s0,1", defs, defs, null)); break;
                                
        default: throw new Report.InternalError();
        }
        
		return temp;
	}
	
	public MemTemp visit(ImcCALL imcCall, Vector<AsmInstr> instrs) {
        MemTemp temp = new MemTemp();
        
        int i = 0;
        for(ImcExpr arg : imcCall.args()) {
            Vector<MemTemp> uses = new Vector<MemTemp>();
            MemTemp argTemp = arg.accept(this, instrs);
            
            uses.add(argTemp);
            
            instrs.add(new AsmOPER("STO `s0,$254," + Integer.toString(imcCall.offs().get(i).intValue()), uses, null, null));
            
            i++;
        }
        
        Vector<MemLabel> jumps = new Vector<MemLabel>();
        jumps.add(imcCall.label);
        
        instrs.add(new AsmOPER("PUSHJ $"+ this.get_num_of_regs() + "," + imcCall.label.name, null, null, jumps));
        
        Vector<MemTemp> defs = new Vector<MemTemp>();
        defs.add(temp);
        
        //instrs.add(new AsmOPER("POP 1, 0", null, null, null));
        
        instrs.add(new AsmOPER("LDO `d0,$254,0", null, defs, null));
        
		return temp;
	}
	
	public MemTemp visit(ImcCONST imcConst, Vector<AsmInstr> instrs) {
        MemTemp temp = new MemTemp();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        
        defs.add(temp);
        
        long value = Math.abs(imcConst.value);
        
        if(value >= 0) {
            instrs.add(new AsmOPER("SETL `d0," + Integer.toString(((short) value & 0xffff)), null, defs, null));
            value >>= 16;
        }
        
        if(value > 0) {
            instrs.add(new AsmOPER("INCML `d0," + Integer.toString(((short) value & 0xffff)), defs, defs, null));
            value >>= 16;
        }
        
        if(value > 0) {
            instrs.add(new AsmOPER("INCMH `d0," + Integer.toString(((short) value & 0xffff)), defs, defs, null));
            value >>= 16;
        }
        
        if(value > 0) {
            instrs.add(new AsmOPER("INCH `d0," + Integer.toString(((short) value & 0xffff)), defs, defs, null));
            value >>= 16;
        }
        
        if (imcConst.value < 0) {
            instrs.add(new AsmOPER("NEG `d0,0,`s0", defs, defs, null));
        }
        
		return temp;
	}
	
	public MemTemp visit(ImcMEM imcMem, Vector<AsmInstr> instrs) {
        MemTemp temp = new MemTemp();
        MemTemp addr = imcMem.addr.accept(this, instrs);
        Vector<MemTemp> uses = new Vector<MemTemp>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        
        uses.add(addr);
        defs.add(temp);
        
        instrs.add(new AsmOPER("LDO `d0,`s0,0", uses, defs, null));
        
		return temp;
	}
	
	public MemTemp visit(ImcNEW imcNew, Vector<AsmInstr> instrs) {
        MemTemp temp = imcNew.size.accept(this, instrs);
        MemTemp retTemp = new MemTemp();
        
        Vector<MemTemp> uses = new Vector<MemTemp>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        
        uses.add(temp);
        defs.add(retTemp);
        
        instrs.add(new AsmOPER("SET `d0,$252", null, defs, null));
        instrs.add(new AsmOPER("ADD $252,$252,`s0", uses, null, null));
        
		return retTemp;
	}
	
	
	
	public MemTemp visit(ImcNAME imcName, Vector<AsmInstr> instrs) {
        MemTemp temp = new MemTemp();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        
        defs.add(temp);
        
        instrs.add(new AsmOPER("LDA `d0," + imcName.label.name, null, defs, null));
        
		return temp;
	}
	
	public MemTemp visit(ImcTEMP imcTemp, Vector<AsmInstr> instrs) {
		return imcTemp.temp;
	}
	
	public MemTemp visit(ImcUNOP imcUnop, Vector<AsmInstr> instrs) {
        Vector<MemTemp> uses = new Vector<MemTemp>();

        MemTemp temp = imcUnop.subExpr.accept(this, instrs);
        uses.add(temp);

        
        switch(imcUnop.oper) {
        case NEG: instrs.add(new AsmOPER("NEG `d0,0,`s0", uses, uses, null)); break;
        case NOT: instrs.add(new AsmOPER("NOR `d0,`s0,0", uses, uses, null)); break;
        default: throw new Report.InternalError();
        }
        
        
		return temp;
	}

}
