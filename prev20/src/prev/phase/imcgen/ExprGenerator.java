package prev.phase.imcgen;

import java.util.*;

import prev.data.ast.tree.*;
import prev.data.ast.tree.decl.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.tree.type.*;
import prev.data.ast.tree.stmt.*;
import prev.data.imc.code.expr.*;
import prev.data.imc.code.stmt.*;
import prev.data.ast.visitor.*;
import prev.data.semtype.*;
import prev.phase.seman.*;
import prev.data.mem.*;
import prev.phase.memory.*;

public class ExprGenerator extends AstFullVisitor<ImcExpr, Stack<MemFrame>> {

   /* @Override
	public ImcExpr visit(AstTrees<?> trees, Stack<MemFrame> frames) {
		if (frames == null)
			frames = new Stack<MemFrame>();
		for (AstTree tree : trees)
			if (tree instanceof AstFunDecl)
				((AstFunDecl) tree).accept(this, frames);
		return null;
	}
    
    @Override
	public ImcExpr visit(AstFunDecl funDecl, Stack<MemFrame> frames) {
		frames.push(Memory.frames.get(funDecl));
		funDecl.expr().accept(new ExprGenerator(), frames);
		frames.pop();
		return null;
	}*/
	
	@Override
	public ImcExpr visit(AstAtomExpr atomExpr, Stack<MemFrame> frames) {
		
        SemType semType = SemAn.ofType.get(atomExpr);
        long atomVal = 0;
        
        switch(atomExpr.type()) {
            case VOID: atomVal = 0; break;
            case CHAR: atomVal = (long) atomExpr.value().charAt(1); break;
            case INTEGER: atomVal =  (long) Integer.parseInt(atomExpr.value()); break;
            case BOOLEAN: if(atomExpr.value().equals("true"))
                            atomVal = 1;
                          else
                            atomVal = 0;
                          break;
            case POINTER: if(atomExpr.value().equals("nil")) atomVal = 0; break;
            case STRING: break;
        }
        
        if(atomExpr.type() == AstAtomExpr.Type.STRING) {
            ImcGen.exprImc.put(atomExpr, new ImcNAME(Memory.strings.get(atomExpr).label));
        }
        else {
            ImcGen.exprImc.put(atomExpr, new ImcCONST(atomVal));   
        }
		
		return null;
	}
	
	
	@Override
	public ImcExpr visit(AstNameExpr nameExpr, Stack<MemFrame> frames) {
	
        MemAccess memAcs = Memory.accesses.get((AstMemDecl)SemAn.declaredAt.get(nameExpr));
        
        if(memAcs instanceof MemAbsAccess) {
            ImcGen.exprImc.put(nameExpr, new ImcMEM(new ImcNAME(((MemAbsAccess)memAcs).label))); 
        }
        else if(memAcs instanceof MemRelAccess) {
            if(SemAn.declaredAt.get(nameExpr) instanceof AstCompDecl) {
                ImcGen.exprImc.put(nameExpr, new ImcCONST(((MemRelAccess) memAcs).offset));
            }
            else {
                long varDepth = ((MemRelAccess) memAcs).depth;
                
                int i = frames.size() - 1;
                
                ImcExpr framePointer = new ImcTEMP(frames.peek().FP);
                
                MemFrame frame = frames.get(i);
                
                //System.out.print(frame.depth);
                //System.out.print(varDepth);
                
                while(frame.depth > varDepth) {
                    i--;
                    frame = frames.get(i);
                    //System.out.print(frame.depth);
                    
                    framePointer = new ImcMEM(framePointer);
                }
                
                ImcExpr expr = new ImcMEM(new ImcBINOP(ImcBINOP.Oper.ADD, framePointer, new ImcCONST(((MemRelAccess)memAcs).offset)));
                
                ImcGen.exprImc.put(nameExpr, expr);
            }
        }
		
		return null;
	}
	
	
	@Override
    public ImcExpr visit(AstBinExpr binExpr, Stack<MemFrame> frames) {
		
        binExpr.fstExpr().accept(this, frames);
        binExpr.sndExpr().accept(this, frames);
        
        ImcBINOP.Oper oper; 

        switch(binExpr.oper()) {
            case OR:  oper = ImcBINOP.Oper.OR;  break;
            case AND: oper = ImcBINOP.Oper.AND; break;
            case EQU: oper = ImcBINOP.Oper.EQU; break;
            case NEQ: oper = ImcBINOP.Oper.NEQ; break;
            case LTH: oper = ImcBINOP.Oper.LTH; break;
            case GTH: oper = ImcBINOP.Oper.GTH; break;
            case LEQ: oper = ImcBINOP.Oper.LEQ; break;
            case GEQ: oper = ImcBINOP.Oper.GEQ; break;
            case ADD: oper = ImcBINOP.Oper.ADD; break;
            case SUB: oper = ImcBINOP.Oper.SUB; break;
            case MUL: oper = ImcBINOP.Oper.MUL; break;
            case DIV: oper = ImcBINOP.Oper.DIV; break;
            //case MOD: oper = ImcBINOP.Oper.MOD; break;
            default:  oper = ImcBINOP.Oper.MOD;
        }
        
        ImcGen.exprImc.put(binExpr, new ImcBINOP(oper, ImcGen.exprImc.get(binExpr.fstExpr()), ImcGen.exprImc.get(binExpr.sndExpr())));
        
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstArrExpr arrExpr, Stack<MemFrame> frames) {
		
        arrExpr.arr().accept(this, frames);
        arrExpr.idx().accept(this, frames);
        
        ImcBINOP arrAdr = new ImcBINOP(ImcBINOP.Oper.ADD, ((ImcMEM)ImcGen.exprImc.get(arrExpr.arr())).addr, new ImcBINOP(ImcBINOP.Oper.MUL, ImcGen.exprImc.get(arrExpr.idx()), new ImcCONST(((SemArray)SemAn.ofType.get(arrExpr.arr())).elemType().size())));
        
        ImcGen.exprImc.put(arrExpr, new ImcMEM(arrAdr));
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstRecExpr recExpr, Stack<MemFrame> frames) {
		
        recExpr.rec().accept(this, frames);
        recExpr.comp().accept(this, frames);
        
        ImcBINOP recAdr = new ImcBINOP(ImcBINOP.Oper.ADD, ImcGen.exprImc.get(recExpr.rec()), ImcGen.exprImc.get(recExpr.comp()));
        
        ImcGen.exprImc.put(recExpr, new ImcMEM(recAdr));
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstCallExpr callExpr, Stack<MemFrame> frames) {
		
		Vector<ImcExpr> args = new Vector<ImcExpr>();
		Vector<Long> offs = new Vector<Long>();
		
		AstFunDecl funDecl = (AstFunDecl)SemAn.declaredAt.get(callExpr);
		
		long funDepth = Memory.frames.get(funDecl).depth;
		long currDepth = frames.peek().depth;
		ImcExpr slTemp = new ImcTEMP(frames.peek().FP);
		
		while(currDepth >= funDepth) {
            currDepth--;
            slTemp = new ImcMEM(slTemp);
		}
		
		args.add(slTemp);
		offs.add(Long.valueOf(0));
		
        for(AstExpr arg: callExpr.args()) {
            arg.accept(this, frames);
            
            args.add(ImcGen.exprImc.get(arg));
        }
        
        for(AstParDecl par: funDecl.pars()) {
            offs.add(Long.valueOf(((MemRelAccess)Memory.accesses.get(par)).offset));
        }
        
        MemLabel funLab;
        
        funLab = Memory.frames.get(funDecl).label;
        
        ImcCALL callImc = new ImcCALL(funLab, offs, args);
    
        ImcGen.exprImc.put(callExpr, callImc);
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstPfxExpr pfxExpr, Stack<MemFrame> frames) {
		
        pfxExpr.expr().accept(this, frames);

        ImcExpr expr = ImcGen.exprImc.get(pfxExpr.expr());
        
        switch(pfxExpr.oper()) {
            case ADD: /*ImcGen.exprImc.put(pfxExpr, new ImcUNOP(ImcUNOP.Oper.ADD, expr));*/
                        ImcGen.exprImc.put(pfxExpr, expr); break;
            case SUB: ImcGen.exprImc.put(pfxExpr, new ImcUNOP(ImcUNOP.Oper.NEG, expr)); break;
            case NOT: ImcGen.exprImc.put(pfxExpr, new ImcUNOP(ImcUNOP.Oper.NOT, expr)); break;
            case PTR: if( expr instanceof ImcMEM ) {
                        ImcMEM memAcc = (ImcMEM) expr;
                        ImcGen.exprImc.put(pfxExpr, memAcc.addr);
                    }
                    else {
                        ImcGen.exprImc.put(pfxExpr, expr);
                    }
                    break;
            case NEW: ImcGen.exprImc.put(pfxExpr, new ImcNEW(expr)); break;
            case DEL: ImcGen.exprImc.put(pfxExpr, new ImcCONST(0)); break;
        }        
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstSfxExpr sfxExpr, Stack<MemFrame> frames) {
		
        sfxExpr.expr().accept(this, frames);
        
        ImcGen.exprImc.put(sfxExpr, new ImcMEM(ImcGen.exprImc.get(sfxExpr.expr())));        
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstCastExpr castExpr, Stack<MemFrame> frames) {
		
		castExpr.type().accept(this, frames);
        castExpr.expr().accept(this, frames);
        
        if(!(SemAn.isType.get(castExpr.type()) instanceof SemChar)) {
            ImcGen.exprImc.put(castExpr, ImcGen.exprImc.get(castExpr.expr()));   
        } else {
            ImcGen.exprImc.put(castExpr, new ImcBINOP(ImcBINOP.Oper.MOD, ImcGen.exprImc.get(castExpr.expr()), new ImcCONST(256)));   
        }
        
		return null;
	}
	
		
	@Override
    public ImcExpr visit(AstWhereExpr whereExpr, Stack<MemFrame> frames) {
		
		whereExpr.decls().accept(new CodeGenerator(), frames);
		whereExpr.expr().accept(this, frames);
		
		ImcGen.exprImc.put(whereExpr, ImcGen.exprImc.get(whereExpr.expr()));
		
		return null;
	}
	
	@Override
    public ImcExpr visit(AstStmtExpr stmtExpr, Stack<MemFrame> frames) {
		
		//stmtExpr.stmts().accept(new StmtGenerator(), frames);
		
		ImcStmt lastElement = null;
		Vector<ImcStmt> vecStmts = new Vector<ImcStmt>();
		
		for(AstStmt stmt: stmtExpr.stmts()) {
            stmt.accept(new StmtGenerator(), frames);
            lastElement = ImcGen.stmtImc.get(stmt);
            
            vecStmts.add(lastElement);
        }
        
        ImcExpr expr;
        
        if(lastElement instanceof ImcESTMT) {
            vecStmts.remove(lastElement);
            expr = ((ImcESTMT)lastElement).expr;
        }
        else {
            expr = new ImcCONST(0);
        }
        
        ImcGen.exprImc.put(stmtExpr, new ImcSEXPR(new ImcSTMTS(vecStmts), expr));
		
		return null;
	} 
}
