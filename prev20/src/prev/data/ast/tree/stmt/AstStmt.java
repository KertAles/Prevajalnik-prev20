package prev.data.ast.tree.stmt;

import prev.data.ast.tree.*;

/**
 * Abstract statement.
 */
public interface AstStmt extends AstExec {

	@Override
	public AstStmt clone();

}
