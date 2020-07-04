package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Binary expression.
 */
public class AstBinExpr extends AstNode implements AstExpr {

	/** Operators. */
	public enum Oper {
		OR, AND, EQU, NEQ, LTH, GTH, LEQ, GEQ, ADD, SUB, MUL, DIV, MOD
	};

	/** The operator. */
	private Oper oper;

	/** The first subexpression. */
	private AstExpr fstExpr;

	/** The second subexpression. */
	private AstExpr sndExpr;

	/**
	 * Constructs a binary expression.
	 * 
	 * @param location The location.
	 * @param oper     The operator.
	 * @param fstExpr  The first subexpression.
	 * @param sndExpr  The second subexpression.
	 */
	public AstBinExpr(Location location, Oper oper, AstExpr fstExpr, AstExpr sndExpr) {
		super(location);
		this.oper = oper;
		this.fstExpr = fstExpr;
		this.sndExpr = sndExpr;
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
	 * Returns the first subexpression.
	 * 
	 * @return The first subexpression.
	 */
	public final AstExpr fstExpr() {
		return fstExpr;
	}

	/**
	 * Returns the second subexpression.
	 * 
	 * @return The second subexpression.
	 */
	public final AstExpr sndExpr() {
		return sndExpr;
	}

	@Override
	public AstBinExpr clone() {
		AstBinExpr ast = (AstBinExpr) super.clone();
		ast.oper = oper;
		ast.fstExpr = fstExpr == null ? null : fstExpr.clone();
		ast.sndExpr = sndExpr == null ? null : sndExpr.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
