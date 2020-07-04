package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Prefix expression.
 */
public class AstPfxExpr extends AstNode implements AstExpr {

	/** Operators. */
	public enum Oper {
		ADD, SUB, NOT, PTR, NEW, DEL
	};

	/** The operator. */
	private Oper oper;

	/** The subexpression. */
	private AstExpr expr;

	/**
	 * Constructs a prefix expression.
	 * 
	 * @param location The location.
	 * @param oper     The operator.
	 * @param expr     The subexpression.
	 */
	public AstPfxExpr(Location location, Oper oper, AstExpr expr) {
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
	public AstPfxExpr clone() {
		AstPfxExpr ast = (AstPfxExpr) super.clone();
		ast.oper = oper;
		ast.expr = expr == null ? null : expr.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
