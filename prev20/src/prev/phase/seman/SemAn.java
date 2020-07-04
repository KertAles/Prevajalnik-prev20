package prev.phase.seman;

import prev.phase.*;
import prev.data.ast.attribute.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.decl.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.tree.type.*;
import prev.data.semtype.*;

/**
 * Semantic analysis.
 */
public class SemAn extends Phase {

	// === STATIC ===

	/** Maps names to declarations. */
	public static final AstAttribute<AstName, AstDecl> declaredAt = new AstAttribute<AstName, AstDecl>(0);

	/** Maps type declarations to semantic representations of types. */
	public static final AstAttribute<AstTypeDecl, SemName> declaresType = new AstAttribute<AstTypeDecl, SemName>(0);

	/** Maps syntax types to semantic representations of types. */
	public static final AstAttribute<AstType, SemType> isType = new AstAttribute<AstType, SemType>(0);

	/** Maps syntax expressions to semantic representations of types. */
	public static final AstAttribute<AstExec, SemType> ofType = new AstAttribute<AstExec, SemType>(0);

	/** Indicates which syntax expressions denote lvalues. */
	public static final AstAttribute<AstExpr, Boolean> isAddr = new AstAttribute<AstExpr, Boolean>(0);

	// ==============

	/**
	 * Phase construction.
	 */
	public SemAn() {
		super("seman");
	}

}
