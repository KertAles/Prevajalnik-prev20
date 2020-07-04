package prev.phase.imcgen;

import prev.data.ast.tree.expr.*;
import prev.data.ast.tree.stmt.*;
import prev.data.ast.attribute.*;
import prev.data.ast.visitor.*;
import prev.data.imc.code.expr.*;
import prev.data.imc.code.stmt.*;
import prev.phase.*;

/**
 * Intermediate code generation.
 */
public class ImcGen extends Phase implements AstVisitor<Object, Object> {

	/** Maps statements to intermediate code. */
	public static final AstAttribute<AstStmt, ImcStmt> stmtImc = new AstAttribute<AstStmt, ImcStmt>(0);

	/** Maps expressions to intermediate code. */
	public static final AstAttribute<AstExpr, ImcExpr> exprImc = new AstAttribute<AstExpr, ImcExpr>(0);

	/**
	 * Constructs a new phase for computing layout.
	 */
	public ImcGen() {
		super("imcgen");
	}

}
