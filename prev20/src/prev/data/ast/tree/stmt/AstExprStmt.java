package prev.data.ast.tree.stmt;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;

/**
 * Expression statement.
 */
public class AstExprStmt extends AstNode implements AstStmt {

	/** The expression. */
	private AstExpr expr;

	/**
	 * Constructs an expression statement.
	 * 
	 * @param location The location.
	 * @param expr     The expression.
	 */
	public AstExprStmt(Location location, AstExpr expr) {
		super(location);
		this.expr = expr;
	}

	/**
	 * Returns the expression.
	 * 
	 * @return The expression.
	 */
	public final AstExpr expr() {
		return expr;
	}

	public AstExprStmt clone() {
		AstExprStmt ast = (AstExprStmt) super.clone();
		this.expr = expr == null ? null : expr.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
