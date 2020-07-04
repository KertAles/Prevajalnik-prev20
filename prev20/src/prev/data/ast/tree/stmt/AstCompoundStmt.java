package prev.data.ast.tree.stmt;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.visitor.*;

/**
 * Compound statement.
 */
public class AstCompoundStmt extends AstNode implements AstStmt {

	/** The statements. */
	private AstTrees<AstStmt> stmts;

	/**
	 * Constructs a compound statement.
	 * 
	 * @param location The location.
	 * @param stmts    The statements.
	 */
	public AstCompoundStmt(Location location, AstTrees<AstStmt> stmts) {
		super(location);
		this.stmts = stmts;
	}

	/**
	 * Returns the statements.
	 * 
	 * @return The statements.
	 */
	public final AstTrees<AstStmt> stmts() {
		return stmts;
	}

	public AstCompoundStmt clone() {
		AstCompoundStmt ast = (AstCompoundStmt) super.clone();
		this.stmts = stmts == null ? null : stmts.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
