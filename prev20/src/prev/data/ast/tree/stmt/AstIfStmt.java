package prev.data.ast.tree.stmt;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;

/**
 * If statement.
 */
public class AstIfStmt extends AstNode implements AstStmt {

	/** The condition. */
	private AstExpr cond;

	/** The statement in the then branch. */
	private AstStmt thenStmt;

	/** The statement in the else branch. */
	private AstStmt elseStmt;

	/**
	 * Constructs an if statement.
	 * 
	 * @param location The location.
	 * @param cond     The condition.
	 * @param thenStmt The statement in the then branch.
	 * @param elseStmt The statement in the else branch.
	 */
	public AstIfStmt(Location location, AstExpr cond, AstStmt thenStmts, AstStmt elseStmts) {
		super(location);
		this.cond = cond;
		this.thenStmt = thenStmts;
		this.elseStmt = elseStmts;
	}

	/**
	 * Returns the condition.
	 * 
	 * @return The condition.
	 */
	public final AstExpr cond() {
		return cond;
	}

	/**
	 * Returns the statement in the then branch.
	 * 
	 * @return The statement in the then branch.
	 */
	public final AstStmt thenStmt() {
		return thenStmt;
	}

	/**
	 * Returns the statement in the else branch.
	 * 
	 * @return The statement in the else branch.
	 */
	public final AstStmt elseStmt() {
		return elseStmt;
	}

	public AstIfStmt clone() {
		AstIfStmt ast = (AstIfStmt) super.clone();
		this.cond = cond == null ? null : cond.clone();
		this.thenStmt = thenStmt == null ? null : thenStmt.clone();
		this.elseStmt = elseStmt == null ? null : elseStmt.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
