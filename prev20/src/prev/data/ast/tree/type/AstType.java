package prev.data.ast.tree.type;

import prev.data.ast.tree.*;

/**
 * Abstract type.
 */
public interface AstType extends AstTree {

	@Override
	public abstract AstType clone();

}
