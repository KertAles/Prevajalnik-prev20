package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Array access expression.
 */
public class AstArrExpr extends AstNode implements AstExpr {

	/** The array. */
	private AstExpr arr;

	/** The index. */
	private AstExpr idx;

	/**
	 * Constructs an array access expression.
	 * 
	 * @param location The location.
	 * @param arr      The array.
	 * @param index The index.
	 */
	public AstArrExpr(Location location, AstExpr arr, AstExpr idx) {
		super(location);
		this.arr = arr;
		this.idx = idx;
	}

	/**
	 * Returns the array.
	 * 
	 * @return The array.
	 */
	public final AstExpr arr() {
		return arr;
	}

	/**
	 * Returns the index.
	 * 
	 * @return The index.
	 */
	public final AstExpr idx() {
		return idx;
	}

	@Override
	public AstArrExpr clone() {
		AstArrExpr ast = (AstArrExpr) super.clone();
		ast.arr = arr == null ? null : arr.clone();
		ast.idx = idx == null ? null : ast.idx();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
