package prev.data.ast.tree.decl;

import prev.common.report.*;
import prev.data.ast.tree.type.*;

/**
 * Any variable declaration.
 */
public abstract class AstMemDecl extends AstNameDecl {

	/** The type of this variable. */
	private AstType type;

	/**
	 * Constructs a variable declaration.
	 * 
	 * @param location The location.
	 * @param name     The name of this variable.
	 * @param type     The type of this variable.
	 */
	protected AstMemDecl(Location location, String name, AstType type) {
		super(location, name);
		this.type = type;
	}

	/**
	 * Returns the type of this variable.
	 * 
	 * @return The type of this variable.
	 */
	public final AstType type() {
		return type;
	}

	@Override
	public AstMemDecl clone() {
		AstMemDecl ast = (AstMemDecl) super.clone();
		ast.type =  type == null ? null : type.clone();
		return ast;
	}

}
