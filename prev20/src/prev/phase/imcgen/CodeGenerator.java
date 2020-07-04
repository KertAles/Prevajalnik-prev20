package prev.phase.imcgen;

import java.util.*;

import prev.data.ast.tree.*;
import prev.data.ast.tree.decl.*;
import prev.data.ast.visitor.*;
import prev.data.mem.*;
import prev.phase.memory.*;

public class CodeGenerator extends AstNullVisitor<Object, Stack<MemFrame>> {

	public Object visit(AstTrees<?> trees, Stack<MemFrame> frames) {
		if (frames == null)
			frames = new Stack<MemFrame>();
		for (AstTree tree : trees)
			if (tree instanceof AstFunDecl)
				((AstFunDecl) tree).accept(this, frames);
		return null;
	}

	public Object visit(AstFunDecl funDecl, Stack<MemFrame> frames) {
		frames.push(Memory.frames.get(funDecl));
		funDecl.expr().accept(new ExprGenerator(), frames);
		frames.pop();
		return null;
	}

}
