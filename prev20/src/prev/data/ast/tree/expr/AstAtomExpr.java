package prev.data.ast.tree.expr;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Atom expression, i.e., a constant.
 */
public class AstAtomExpr extends AstNode implements AstExpr {

	/** Types. */
	public enum Type {
		VOID, CHAR, INTEGER, BOOLEAN, POINTER, STRING
	};

	/** The type of a constant. */
	private Type type;

	/** The value of a constant. */
	private String value;

	/**
	 * Constructs an atom expression, i.e., a constant.
	 * 
	 * @param location The location.
	 * @param type     The type of a constant.
	 * @param value    The value of a constant.
	 */
	public AstAtomExpr(Location location, Type type, String value) {
		super(location);
		this.type = type;
		this.value = value;
	}

	/**
	 * Returns the type of a constant.
	 * 
	 * @return The type of a constant.
	 */
	public final Type type() {
		return type;
	}

	/**
	 * Returns the value of a constant.
	 * 
	 * @return The value of a constant.
	 */
	public final String value() {
		return value;
	}

	@Override
	public AstAtomExpr clone() {
		AstAtomExpr ast = (AstAtomExpr) super.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
