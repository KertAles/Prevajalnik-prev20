package prev.data.ast.tree;

import prev.common.report.*;
import prev.data.ast.visitor.*;

/**
 * Abstract syntax tree.
 */
public interface AstTree extends Cloneable, Locatable {

	/**
	 * Returns the unique id of this node.
	 * 
	 * @return The unique id of this node.
	 */
	public abstract int id();

	public abstract AstTree clone();

	public abstract <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg);

}
