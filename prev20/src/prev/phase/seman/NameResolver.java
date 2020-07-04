package prev.phase.seman;

import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.decl.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.tree.type.*;
import prev.data.ast.visitor.*;

/**
 * Name resolver.
 * 
 * Name resolver connects each node of a abstract syntax tree where a name is
 * used with the node where it is declared. The only exceptions are a record
 * field names which are connected with its declarations by type resolver. The
 * results of the name resolver are stored in
 * {@link prev.phase.seman.SemAn#declaredAt}.
 */
public class NameResolver extends AstFullVisitor<Object, NameResolver.Mode> {

	public enum Mode {
		HEAD, BODY
	}

	private SymbTable symbTable = new SymbTable();

	// GENERAL PURPOSE

	public Object visit(AstTrees<?> trees, Mode arg) {
		for (AstTree tree : trees) {
            if(tree instanceof AstDecl) {
                AstDecl decl = (AstDecl) tree;
                decl.accept(this, Mode.HEAD);
			}
			else {
                tree.accept(this, null);
			}
		}
		for (AstTree tree : trees) {
			if(tree instanceof AstDecl) {
                AstDecl decl = (AstDecl) tree;
                decl.accept(this, Mode.BODY);
			}
			else {
                tree.accept(this, null);
			}
		}
		return null;
	}

	// DECLARATIONS

	@Override
	public Object visit(AstFunDecl funDecl, Mode mode) {
		switch (mode) {
		case HEAD:
			try {
				symbTable.ins(funDecl.name(), funDecl);
			} catch (SymbTable.CannotInsNameException __) {
				throw new Report.Error(funDecl, "Cannot redefine '" + (funDecl.name()) + "' as a function.");
			}
			for (AstTree tree : funDecl.pars())
				tree.accept(this, Mode.BODY);
			funDecl.type().accept(this, null);
			break;
		case BODY:
			symbTable.newScope();
			for (AstTree tree : funDecl.pars())
				tree.accept(this, Mode.HEAD);
			funDecl.expr().accept(this, null);
			symbTable.oldScope();
		}
		return null;
	}

	@Override
	public Object visit(AstParDecl parDecl, Mode mode) {
		switch (mode) {
		case HEAD:
			try {
				symbTable.ins(parDecl.name(), parDecl);
			} catch (SymbTable.CannotInsNameException __) {
				throw new Report.Error(parDecl, "Cannot redefine '" + (parDecl.name()) + "' as a parameter.");
			}
			break;
		case BODY:
			parDecl.type().accept(this, null);
			break;
		}
		return null;
	}

	@Override
	public Object visit(AstTypeDecl typeDecl, Mode mode) {
		switch (mode) {
		case HEAD:
			try {
				symbTable.ins(typeDecl.name(), typeDecl);
			} catch (SymbTable.CannotInsNameException __) {
				throw new Report.Error(typeDecl, "Cannot redefine '" + (typeDecl.name()) + "' as a type.");
			}
			break;
		case BODY:
			typeDecl.type().accept(this, null);
		}
		return null;
	}

	@Override
	public Object visit(AstVarDecl varDecl, Mode mode) {
		switch (mode) {
		case HEAD:
			try {
				symbTable.ins(varDecl.name(), varDecl);
			} catch (SymbTable.CannotInsNameException __) {
				throw new Report.Error(varDecl, "Cannot redefine '" + (varDecl.name()) + "' as a variable.");
			}
			break;
		case BODY:
			varDecl.type().accept(this, null);
			break;
		}
		return null;
	}

	// EXPRESSIONS

	@Override
	public Object visit(AstCallExpr callExpr, Mode mode) {
		try {
			AstDecl decl = symbTable.fnd(callExpr.name());
			if (!(decl instanceof AstFunDecl))
				throw new Report.Error(callExpr, "'" + (callExpr.name()) + "' is not a name of a function name.");
			SemAn.declaredAt.put(callExpr, decl);
		} catch (SymbTable.CannotFndNameException __) {
			throw new Report.Error(callExpr, "Undefined function '" + (callExpr.name()) + "'.");
		}
		if (callExpr.args() != null)
			callExpr.args().accept(this, mode);
		return null;
	}

	@Override
	public Object visit(AstNameExpr nameExpr, Mode mode) {
		try {
			AstDecl decl = symbTable.fnd(nameExpr.name());
			if (!(decl instanceof AstMemDecl))
				throw new Report.Error(nameExpr, "'" + (nameExpr.name()) + "' is not a name of a variable name.");
			SemAn.declaredAt.put(nameExpr, decl);
		} catch (SymbTable.CannotFndNameException __) {
			throw new Report.Error(nameExpr, "Undefined variable '" + (nameExpr.name()) + "'.");
		}
		return null;
	}
	
	@Override
	public Object visit(AstWhereExpr whereExpr, Mode mode) {
		symbTable.newScope();
		
		whereExpr.decls().accept(this, mode);
		whereExpr.expr().accept(this, mode);
		
		symbTable.oldScope();
		return null;
	}

	@Override
	public Object visit(AstRecExpr recExpr, Mode mode) {
		recExpr.rec().accept(this, mode);
		return null;
	}

	// TYPES

	@Override
	public Object visit(AstNameType nameType, Mode mode) {
		try {
			AstDecl decl = symbTable.fnd(nameType.name());
			if (!(decl instanceof AstTypeDecl))
				throw new Report.Error(nameType, "'" + (nameType.name()) + "' is not a type name.");
			SemAn.declaredAt.put(nameType, decl);
		} catch (SymbTable.CannotFndNameException __) {
			throw new Report.Error(nameType, "Undefined type '" + (nameType.name()) + "'.");
		}
		return null;
	}

	@Override
	public Object visit(AstRecType recType, Mode mode) {
		for (AstTree tree : recType.comps()) {
			AstCompDecl compDecl = (AstCompDecl) tree;
			compDecl.type().accept(this, null);
		}
		return null;
	}

}
