package prev.data.ast.tree.decl;

import prev.common.report.*;
import prev.data.ast.tree.type.*;
import prev.data.ast.visitor.*;

/**
 * Parameter declaration.
 */
public class AstParDecl extends AstMemDecl {

	/**
	 * Constructs a parameter declaration.
	 * 
	 * @param location The location.
	 * @param name     The name of this parameter.
	 * @param type     The type of this parameter.
	 */
	public AstParDecl(Location location, String name, AstType type) {
		super(location, name, type);
	}
	
	@Override
	public AstCompDecl clone() {
		AstCompDecl ast = (AstCompDecl) super.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
