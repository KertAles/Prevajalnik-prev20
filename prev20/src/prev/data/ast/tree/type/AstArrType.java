package prev.data.ast.tree.type;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;

/**
 * Array type.
 */
public class AstArrType extends AstNode implements AstType {

	/** Element type. */
	private AstType elemType;

	/** Number of elements. */
	private AstExpr numElems;

	/**
	 * Constructs an array type.
	 * 
	 * @param location The location.
	 * @param elemType The element type.
	 * @param numElems The number of elements.
	 */
	public AstArrType(Location location, AstType elemType, AstExpr numElems) {
		super(location);
		this.elemType = elemType;
		this.numElems = numElems;
	}

	/**
	 * Returns the element type.
	 * 
	 * @return The element type.
	 */
	public final AstType elemType() {
		return elemType;
	}

	/**
	 * Returns the number of elements.
	 * 
	 * @return The number of elements.
	 */
	public final AstExpr numElems() {
		return numElems;
	}

	@Override
	public AstArrType clone() {
		AstArrType ast = (AstArrType) super.clone();
		ast.elemType = elemType == null ? null : elemType.clone();
		ast.numElems = numElems == null ? null : numElems.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
