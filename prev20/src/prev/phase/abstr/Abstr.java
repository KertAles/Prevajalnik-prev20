package prev.phase.abstr;

import prev.phase.*;
import prev.data.ast.tree.*;

/**
 * Abstract syntax tree construction.
 */
public class Abstr extends Phase {

	// === STATIC ===

	/** The abstract syntax tree. */
	public static AstTree tree;
	
	// ==============
	
	/**
	 * Phase construction.
	 */
	public Abstr() {
		super("abstr");
	}

}
