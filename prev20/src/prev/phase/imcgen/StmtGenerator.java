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

public class StmtGenerator extends AstFullVisitor<ImcStmt, Stack<MemFrame>> {
	
	@Override
    public ImcStmt visit(AstAssignStmt assStmt, Stack<MemFrame> frames) {
		
		assStmt.dst().accept(new ExprGenerator(), frames);
		assStmt.src().accept(new ExprGenerator(), frames);
		
		ImcGen.stmtImc.put(assStmt, new ImcMOVE(ImcGen.exprImc.get(assStmt.dst()), ImcGen.exprImc.get(assStmt.src())));
		
		return null;
	}
	
	@Override
    public ImcStmt visit(AstExprStmt exprStmt, Stack<MemFrame> frames) {
		
		exprStmt.expr().accept(new ExprGenerator(), frames);
		
		ImcGen.stmtImc.put(exprStmt, new ImcESTMT(ImcGen.exprImc.get(exprStmt.expr())));
		
		return null;
	}
	
    @Override
    public ImcStmt visit(AstIfStmt ifStmt, Stack<MemFrame> frames) {
		
		ifStmt.cond().accept(new ExprGenerator(), frames);
		ifStmt.thenStmt().accept(this, frames);
		ifStmt.elseStmt().accept(this, frames);
		
		Vector<ImcStmt> stmts = new Vector<ImcStmt>();
		
		ImcLABEL ifDo = new ImcLABEL(new MemLabel());
		ImcLABEL ifDont = new ImcLABEL(new MemLabel());
		ImcLABEL rest = new ImcLABEL(new MemLabel());
		
		stmts.add(new ImcCJUMP(ImcGen.exprImc.get(ifStmt.cond()), ifDo.label, ifDont.label));
		stmts.add(ifDo);
		stmts.add(ImcGen.stmtImc.get(ifStmt.thenStmt()));
		stmts.add(new ImcJUMP(rest.label));
		stmts.add(ifDont);
		stmts.add(ImcGen.stmtImc.get(ifStmt.elseStmt()));	
		stmts.add(new ImcJUMP(rest.label));
		stmts.add(rest);
		
		ImcGen.stmtImc.put(ifStmt, new ImcSTMTS(stmts));
		
		return null;
 	}
	
    @Override
    public ImcStmt visit(AstWhileStmt whileStmt, Stack<MemFrame> frames) {
    
		whileStmt.cond().accept(new ExprGenerator(), frames);
		whileStmt.bodyStmt().accept(this, frames);
		
		Vector<ImcStmt> stmts = new Vector<ImcStmt>();
		
		ImcLABEL whileCond = new ImcLABEL(new MemLabel());
		ImcLABEL whileDo = new ImcLABEL(new MemLabel());
		ImcLABEL whileDont = new ImcLABEL(new MemLabel());
		
		stmts.add(new ImcJUMP(whileCond.label));
		stmts.add(whileCond);
		stmts.add(new ImcCJUMP(ImcGen.exprImc.get(whileStmt.cond()), whileDo.label, whileDont.label));
		stmts.add(whileDo);
		stmts.add(ImcGen.stmtImc.get(whileStmt.bodyStmt()));
		stmts.add(new ImcJUMP(whileCond.label));
		stmts.add(whileDont);
		
		ImcGen.stmtImc.put(whileStmt, new ImcSTMTS(stmts));
		
		return null;
 	}
	
}
