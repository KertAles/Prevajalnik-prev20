package prev.data.ast.tree.stmt;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;

/**
 * While statement.
 */
public class AstWhileStmt extends AstNode implements AstStmt {

	/** The condition. */
	private AstExpr cond;

	/** The body statement. */
	private AstStmt bodyStmt;

	/**
	 * Constructs a while statement.
	 * 
	 * @param location The location.
	 * @param cond     The condition.
	 * @param bodyStmt The body statement.
	 */
	public AstWhileStmt(Location location, AstExpr cond, AstStmt bodyStmt) {
		super(location);
		this.cond = cond;
		this.bodyStmt = bodyStmt;
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
	 * Returns the body statement.
	 * 
	 * @return The body statement.
	 */
	public final AstStmt bodyStmt() {
		return bodyStmt;
	}

	public AstWhileStmt clone() {
		AstWhileStmt ast = (AstWhileStmt) super.clone();
		this.cond = cond == null ? null : cond.clone();
		this.bodyStmt = bodyStmt == null ? null : bodyStmt.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
