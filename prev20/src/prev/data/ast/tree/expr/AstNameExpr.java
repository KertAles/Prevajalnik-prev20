package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Variable access or a parameterless subprogram call.
 */
public class AstNameExpr extends AstNode implements AstExpr, AstName {

	/** The name. */
	private String name;

	/**
	 * Constructs a variable access or a parameterless subprogram call.
	 * 
	 * @param location The location.
	 * @param name     The name.
	 */
	public AstNameExpr(Location location, String name) {
		super(location);
		this.name = name;
	}

	/**
	 * Returns the name.
	 * 
	 * @return The name.
	 */
	public final String name() {
		return name;
	}

	@Override
	public AstNameExpr clone() {
		AstNameExpr ast = (AstNameExpr) super.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
