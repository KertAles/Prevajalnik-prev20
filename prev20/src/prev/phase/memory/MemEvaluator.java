package prev.phase.memory;

import prev.data.ast.tree.*;
import prev.data.ast.tree.decl.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.tree.type.*;
import prev.data.ast.visitor.*;
import prev.data.semtype.*;
import prev.data.mem.*;
import prev.phase.seman.*;



/**
 * Computing memory layout: frames and accesses.
 */
public class MemEvaluator extends AstFullVisitor<Object, MemEvaluator.Context> {

    private static int memSize = 8;
    
	/**
	 * The context {@link MemEvaluator} uses while computing function frames and
	 * variable accesses.
	 */
	protected abstract class Context {
	}

	/**
	 * Functional context, i.e., used when traversing function and building a new
	 * frame, parameter acceses and variable acceses.
	 */
	private class FunContext extends Context {
		public int depth = 0;
		public long locsSize = 0;
		public long argsSize = 0;
		public long parsSize = new SemPointer(new SemVoid()).size();
	}

	/**
	 * Record context, i.e., used when traversing record definition and computing
	 * record component acceses.
	 */
	private class RecContext extends Context {
		public long compsSize = 0;
	}

	
	public Object visit(AstTrees<?> trees, Context context) {
		for (AstTree tree : trees) {
            if (tree != null) {
                FunContext funCtx = new FunContext();
                if(context != null) {
                    if(tree instanceof AstFunDecl) {
                        funCtx.depth = ((FunContext)context).depth;
                    } else {
                        funCtx = (FunContext)context;
                    }
                }
                
				tree.accept(this, funCtx);
				
				/*if(context != null) {
                    if(tree instanceof AstFunDecl) {
                        if(funCtx.parsSize > ((FunContext)context).argsSize) {
                            ((FunContext)context).argsSize = funCtx.parsSize;
                        }
                    } 
                }*/
            }
		}
		
		return null;
	}
	
	// DECLARATIONS
	
	@Override
	public Object visit(AstFunDecl funDecl, Context context) {
		
		((FunContext)context).depth++;
        funDecl.type().accept(this, context);
		funDecl.expr().accept(this, context);
		
		for (AstParDecl par : funDecl.pars()) {
            par.accept(this, context);
		}		
		
		if(((FunContext)context).depth <= 1) {
            Memory.frames.put(funDecl, new MemFrame(new MemLabel(funDecl.name()), ((FunContext)context).depth, ((FunContext)context).locsSize, ((FunContext)context).argsSize));
		}
		else {
            Memory.frames.put(funDecl, new MemFrame(new MemLabel(), ((FunContext)context).depth, ((FunContext)context).locsSize, ((FunContext)context).argsSize));
		}
        
		return null;
	}
	
	@Override
	public Object visit(AstParDecl parDecl, Context context) {
		
		FunContext funCtx = (FunContext) context;
		
        SemType semType = SemAn.isType.get(parDecl.type()).actualType();
		MemRelAccess memAccess = new MemRelAccess(semType.size(), funCtx.parsSize, funCtx.depth);
		((FunContext)context).parsSize += semType.size();
        
        Memory.accesses.put(parDecl, memAccess);
        
		return null;
	}
	
	
    @Override
	public Object visit(AstVarDecl varDecl, Context context) {
		
		varDecl.type().accept(this, context);
		
		MemAccess memAccess = null;
		
		if(((FunContext)context).depth == 0) { //if var outside of function
            SemType semType = SemAn.isType.get(varDecl.type()).actualType();
            memAccess = new MemAbsAccess(semType.size(), new MemLabel(varDecl.name()));
            }
        else { //if local var inside function
            SemType semType = SemAn.isType.get(varDecl.type()).actualType();
            ((FunContext)context).locsSize += semType.size();
            memAccess = new MemRelAccess(semType.size(), -((FunContext)context).locsSize , ((FunContext)context).depth);
        }
        
        Memory.accesses.put(varDecl, memAccess);
        
		return null;
	}
	
	@Override
	public Object visit(AstCompDecl compDecl, Context context) {
		
        SemType semType = SemAn.isType.get(compDecl.type()).actualType();
		MemRelAccess memAccess = new MemRelAccess(semType.size(), ((RecContext)context).compsSize, 0);
		((RecContext)context).compsSize += semType.size();
        
        Memory.accesses.put(compDecl, memAccess);
        
		return null;
	}
	
	//TYPES
	
	@Override
	public Object visit(AstRecType recType, Context context) {
		
		MemAccess memAccess = null;
		
		RecContext recCtx = new RecContext();
		
		for (AstCompDecl comp : recType.comps()) {
            comp.accept(this, recCtx);
		}
        
		return null;
	}
	
	
	// EXPRESSIONS
	
    @Override
	public Object visit(AstAtomExpr atomExpr, Context context) {
        
        //MemAbsAccess memAccess = null;
        SemType semType = SemAn.ofType.get(atomExpr);
        
        switch(atomExpr.type()) {
            /*case VOID: memAccess = new MemAbsAccess(semType.size(), new MemLabel("void"), "null"); break;
            case CHAR: memAccess = new MemAbsAccess(semType.size(), new MemLabel("char"), atomExpr.value()); break;
            case INTEGER: memAccess = new MemAbsAccess(semType.size(), new MemLabel("integer"), atomExpr.value()); break;
            case BOOLEAN: memAccess = new MemAbsAccess(semType.size(), new MemLabel("boolean"), atomExpr.value()); break;
            case POINTER: memAccess = new MemAbsAccess(semType.size(), new MemLabel("pointer"), atomExpr.value()); break;*/
            case STRING: MemAbsAccess memAccess = new MemAbsAccess(semType.size(), new MemLabel(), atomExpr.value()); 
                        Memory.strings.put(atomExpr, memAccess); break;
        }
        
        return null;
	}
	
    @Override
	public Object visit(AstCallExpr callExpr, Context context) {
        
        callExpr.args().accept(this, context);
        
        long locArgSize = new SemPointer(new SemVoid()).size();;
        
        for(AstExpr arg : callExpr.args()) {
            locArgSize += SemAn.ofType.get(arg).size();
        }
        
        /*if(locArgSize == 0) {
            locArgSize = new SemPointer(new SemVoid()).size();
        }*/
        
        if(locArgSize > ((FunContext)context).argsSize) {
            ((FunContext)context).argsSize = locArgSize;
        }
        
        return null;
	}
}
