package prev.data.ast.tree.stmt;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;

/**
 * Assignment statement.
 */
public class AstAssignStmt extends AstNode implements AstStmt {

	/** The destination. */
	private AstExpr dst;

	/** The source. */
	private AstExpr src;

	/**
	 * Construct an assignment statement.
	 * 
	 * @param location The location.
	 * @param dst      The destination.
	 * @param src      The source.
	 */
	public AstAssignStmt(Location location, AstExpr dst, AstExpr src) {
		super(location);
		this.dst = dst;
		this.src = src;
	}

	/**
	 * Returns the destination.
	 * 
	 * @return The destination.
	 */
	public final AstExpr dst() {
		return dst;
	}

	/**
	 * Returns the source.
	 * 
	 * @return The source.
	 */
	public final AstExpr src() {
		return src;
	}

	public AstAssignStmt clone() {
		AstAssignStmt ast = (AstAssignStmt) super.clone();
		this.dst = dst == null ? null : dst.clone();
		this.src = src == null ? null : src.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
