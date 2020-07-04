package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Record component access expression.
 */
public class AstRecExpr extends AstNode implements AstExpr {

	/** The record. */
	private AstExpr rec;

	/** The component. */
	private AstNameExpr comp;

	/**
	 * Constructs a record component access expression.
	 * 
	 * @param location The location.
	 * @param rec      The record.
	 * @param comp     The component.
	 */
	public AstRecExpr(Location location, AstExpr rec, AstNameExpr comp) {
		super(location);
		this.rec = rec;
		this.comp = comp;
	}

	/**
	 * Returns the record.
	 * 
	 * @return The record.
	 */
	public final AstExpr rec() {
		return rec;
	}

	/**
	 * Returns the component.
	 * 
	 * @return The component.
	 */
	public final AstNameExpr comp() {
		return comp;
	}

	@Override
	public AstRecExpr clone() {
		AstRecExpr ast = (AstRecExpr) super.clone();
		ast.rec = rec == null ? null : rec.clone();
		ast.comp = comp == null ? null : comp.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
