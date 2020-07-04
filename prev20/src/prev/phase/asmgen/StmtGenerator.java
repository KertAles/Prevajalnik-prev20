package prev.phase.asmgen;

import java.util.*;
import prev.data.imc.code.*;
import prev.data.imc.code.expr.*;
import prev.data.imc.code.stmt.*;
import prev.data.imc.visitor.*;
import prev.data.mem.*;
import prev.data.asm.*;
import prev.common.report.*;

/**
 * Machine code generator for statements.
 */
public class StmtGenerator implements ImcVisitor<Vector<AsmInstr>, Object> {
	
	public Vector<AsmInstr> visit(ImcLABEL imcLabel, Object obj) {
        Vector<AsmInstr> instrs = new Vector<AsmInstr>();
        
        instrs.add(new AsmLABEL(imcLabel.label));
        
		return instrs;
	}
	
	public Vector<AsmInstr> visit(ImcJUMP imcJump, Object obj) {
        Vector<AsmInstr> instrs = new Vector<AsmInstr>();
        
        Vector<MemLabel> jumps = new Vector<MemLabel>();
        jumps.add(imcJump.label);
        
        instrs.add(new AsmOPER("JMP " + imcJump.label.name, null, null, jumps));
        
		return instrs;
	}
	
	public Vector<AsmInstr> visit(ImcCJUMP imcCjump, Object obj) {
        Vector<AsmInstr> instrs = new Vector<AsmInstr>();

        MemTemp temp = imcCjump.cond.accept(new ExprGenerator(), instrs);
        Vector<MemTemp> uses = new Vector<MemTemp>();
        uses.add(temp);
        
        Vector<MemLabel> jumps = new Vector<MemLabel>();
        jumps.add(imcCjump.negLabel);
        //jumps.add(imcCjump.posLabel);
        
        instrs.add(new AsmOPER("BZ `s0," + imcCjump.negLabel.name, uses, null, jumps));        
        
		return instrs;
	}
	
	public Vector<AsmInstr> visit(ImcESTMT imcEstmt, Object obj) {
        Vector<AsmInstr> instrs = new Vector<AsmInstr>();
        
        imcEstmt.expr.accept(new ExprGenerator(), instrs);
        
		return instrs;
	}
	
	public Vector<AsmInstr> visit(ImcMOVE imcMove, Object obj) {
        Vector<AsmInstr> instrs = new Vector<AsmInstr>();
        Vector<MemTemp> uses = new Vector<MemTemp>();
        Vector<MemTemp> defs = new Vector<MemTemp>();
        
        
        if(imcMove.dst instanceof ImcTEMP && !(imcMove.src instanceof ImcMEM)) {
            MemTemp dst = imcMove.dst.accept(new ExprGenerator(), instrs);
            MemTemp src = imcMove.src.accept(new ExprGenerator(), instrs);
            
            defs.add(dst);
            uses.add(src);
            
            instrs.add(new AsmMOVE("SET `d0,`s0", uses, defs));
        }
        else if(imcMove.dst instanceof ImcTEMP && imcMove.src instanceof ImcMEM) {
            MemTemp dst = imcMove.dst.accept(new ExprGenerator(), instrs);
            MemTemp src = ((ImcMEM)imcMove.src).addr.accept(new ExprGenerator(), instrs);
        
            defs.add(dst);
            Vector<MemTemp> usesLoad = new Vector<MemTemp>();
            Vector<MemTemp> defsLoad = new Vector<MemTemp>();
            MemTemp loadTemp = new MemTemp();
            
            defsLoad.add(loadTemp);
            usesLoad.add(src);
            
            instrs.add(new AsmOPER("LDO `d0,`s0,0", usesLoad, defsLoad, null));
            
            instrs.add(new AsmMOVE("SET `d0,`s0", defsLoad, defs));
        }
        else if(imcMove.dst instanceof ImcMEM && !(imcMove.src instanceof ImcMEM)) {
            MemTemp dst = ((ImcMEM)imcMove.dst).addr.accept(new ExprGenerator(), instrs);
            MemTemp src = imcMove.src.accept(new ExprGenerator(), instrs);
        
            uses.add(src);
            uses.add(dst);
            
            instrs.add(new AsmOPER("STO `s0,`s1,0", uses, null, null));
        }
        else if(imcMove.dst instanceof ImcMEM && imcMove.src instanceof ImcMEM) {
            MemTemp dst = imcMove.dst.accept(new ExprGenerator(), instrs);
            MemTemp src = imcMove.src.accept(new ExprGenerator(), instrs);
        
            Vector<MemTemp> usesLoad = new Vector<MemTemp>();
            Vector<MemTemp> defsLoad = new Vector<MemTemp>();
            MemTemp loadTemp = new MemTemp();
            
            defsLoad.add(loadTemp);
            usesLoad.add(src);
            
            instrs.add(new AsmOPER("LDO `d0,`s0,0", usesLoad, defsLoad, null));
            
            uses.add(dst);
            uses.addAll(defsLoad);
            
            instrs.add(new AsmOPER("STO `s0,`s1,0", uses, null, null));
        }
        
        
		return instrs;
	}

}
