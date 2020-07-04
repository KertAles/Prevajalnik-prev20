package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.type.*;
import prev.data.ast.visitor.*;

/**
 * Cast expression.
 */
public class AstCastExpr extends AstNode implements AstExpr {

	/** The expression. */
	private AstExpr expr;

	/** The type. */
	private AstType type;

	/**
	 * Constructs a cast expression.
	 * 
	 * @param location The location.
	 * @param expr     The expression.
	 * @param type     The type.
	 */
	public AstCastExpr(Location location, AstExpr expr, AstType type) {
		super(location);
		this.expr = expr;
		this.type = type;
	}

	/**
	 * Returns the expr.
	 * 
	 * @return The expr.
	 */
	public final AstExpr expr() {
		return expr;
	}

	/**
	 * Returns the type.
	 * 
	 * @return The type.
	 */
	public final AstType type() {
		return type;
	}

	@Override
	public AstCastExpr clone() {
		AstCastExpr ast = (AstCastExpr) super.clone();
		ast.expr = expr == null ? null : expr.clone();
		ast.type = type == null ? null : type.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
