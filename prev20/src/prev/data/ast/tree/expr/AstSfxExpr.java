package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Suffix expression.
 */
public class AstSfxExpr extends AstNode implements AstExpr {

	/** Operators. */
	public enum Oper {
		PTR
	};

	/** The operator. */
	private Oper oper;

	/** The subexpression. */
	private AstExpr expr;

	/**
	 * Constructs a suffix expression.
	 * 
	 * @param location The location.
	 * @param oper     The operator.
	 * @param expr     The subexpression.
	 */
	public AstSfxExpr(Location location, Oper oper, AstExpr expr) {
		super(location);
		this.oper = oper;
		this.expr = expr;
	}

	/**
	 * Returns the operator.
	 * 
	 * @return The operator.
	 */
	public final Oper oper() {
		return oper;
	}

	/**
	 * Returns the subexpression.
	 * 
	 * @return The subexpression.
	 */
	public final AstExpr expr() {
		return expr;
	}

	@Override
	public AstSfxExpr clone() {
		AstSfxExpr ast = (AstSfxExpr) super.clone();
		ast.oper = oper;
		ast.expr = expr == null ? null : expr.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
