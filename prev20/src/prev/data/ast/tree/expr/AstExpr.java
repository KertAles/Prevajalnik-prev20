package prev.data.ast.tree.expr;

import prev.data.ast.tree.*;

/**
 * Abstract expression.
 */
public interface AstExpr extends AstExec {

	@Override
	public AstExpr clone();

}
