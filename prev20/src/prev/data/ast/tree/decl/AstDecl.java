package prev.data.ast.tree.decl;

import prev.data.ast.tree.*;

/**
 * Abstract declaration.
 */
public interface AstDecl extends AstTree {

	@Override
	public abstract AstDecl clone();

}
