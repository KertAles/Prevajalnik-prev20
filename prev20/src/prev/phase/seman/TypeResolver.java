package prev.phase.seman;

import java.util.*;
import prev.common.report.*;
import prev.data.ast.tree.*;
import prev.data.ast.tree.decl.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.tree.type.*;
import prev.data.ast.tree.stmt.*;
import prev.data.ast.visitor.*;
import prev.data.semtype.*;


public class TypeResolver extends AstFullVisitor<SemType, TypeResolver.Mode> {

	public enum Mode {
		HEAD, BODY
	}

	private HashMap<SemRecord, SymbTable> symbTables = new HashMap<SemRecord, SymbTable>();

	
	// GENERAL PURPOSE

	public SemType visit(AstTrees<?> trees, Mode arg) {
		for (AstTree tree : trees) {
            if(tree instanceof AstTypeDecl) {
                AstTypeDecl decl = (AstTypeDecl) tree;
                SemAn.declaresType.put(decl, new SemName(decl.name()));
			}
		}
		
		for (AstTree tree : trees) {
			if(tree instanceof AstTypeDecl) {
                AstTypeDecl decl = (AstTypeDecl) tree;
                
                decl.accept(this, arg);
                /*
                SemName semName = SemAn.declaresType.get(decl);
                SemType semType = SemAn.isType.get(decl.type());
                
                semName.define(semType);*/
			}
		}
		
		for (AstTree tree : trees) {
			if(tree instanceof AstVarDecl) {
                AstVarDecl decl = (AstVarDecl) tree;
                
                decl.accept(this, arg);
                
                /*decl.type().accept(this, arg);
                SemType semType = SemAn.isType.get(decl.type());
                
                SemAn.isType.put(decl.type(), semType);*/
			}
		}
		
		for (AstTree tree : trees) {
			if(tree instanceof AstFunDecl) {
                AstFunDecl decl = (AstFunDecl) tree;
                
                
                for (AstParDecl par : decl.pars()) {
                    par.accept(this, arg);
                }
                
                decl.type().accept(this, arg);
                SemType semType = SemAn.isType.get(decl.type()).actualType();
                
                if(semType instanceof SemVoid |
                    semType instanceof SemBoolean |
                    semType instanceof SemInteger |
                    semType instanceof SemChar |
                    semType instanceof SemPointer) {
                    
                    SemAn.isType.put(decl.type(), semType);
                }
                else {
                    throw new Report.Error(decl, "Invalid function type");
                }
                
			}
		}
		
		for (AstTree tree : trees) {
			if(tree instanceof AstFunDecl) {
                AstFunDecl decl = (AstFunDecl) tree;
                
                decl.expr().accept(this, arg);
                SemType exprSemType = SemAn.ofType.get(decl.expr()).actualType();
                SemType typeSemType = SemAn.isType.get(decl.type()).actualType();
                
                if(!(exprSemType.getClass().equals(typeSemType.getClass()))) {
                    throw new Report.Error(decl, "Invalid return type");
                }                    
			}
		}
		
		return null;
	}
	
	// DECLARATIONS
	
	@Override
	public SemType visit(AstTypeDecl typeDecl, Mode mode) {
		
		typeDecl.type().accept(this, mode);
		
		SemName semName = SemAn.declaresType.get(typeDecl);
		SemType semType = SemAn.isType.get(typeDecl.type());
		semName.define(semType);
		
		//SemAn.isType.put(typeDecl.type(), semName);
		               
		return null;
	}
	
	@Override
	public SemType visit(AstVarDecl varDecl, Mode mode) {
		
		varDecl.type().accept(this, mode);
		
		SemType semType = SemAn.isType.get(varDecl.type());
		if(!(semType instanceof SemVoid)) {
            SemName semName = new SemName(varDecl.name());
            semName.define(semType);
            
            SemAn.isType.put(varDecl.type(), semType);
        }
        else {
            throw new Report.Error(varDecl, "Variable can't be a void type");
        }
        
		return null;
	}
	
	@Override
	public SemType visit(AstParDecl parDecl, Mode mode) {
		
		parDecl.type().accept(this, mode);
		
        SemType semType = SemAn.isType.get(parDecl.type()).actualType();
		if(semType instanceof SemBoolean |
            semType instanceof SemInteger |
            semType instanceof SemChar |
            semType instanceof SemPointer) {
            
            SemAn.isType.put(parDecl.type(), semType);
        }
        else {
            throw new Report.Error(parDecl, "Invalid parameter type");
        }
        
		return null;
	}
	
	@Override
	public SemType visit(AstCompDecl compDecl, Mode mode) {
		
		compDecl.type().accept(this, mode);
        
		return null;
	}
	
	
	
	// TYPES
	
    @Override
	public SemType visit(AstNameType nameType, Mode mode) {
		
		AstTypeDecl decl = (AstTypeDecl) SemAn.declaredAt.get(nameType);
		
		SemType semType = (SemType) SemAn.declaresType.get(decl);
		
		SemAn.isType.put(nameType, semType);
		
		return null;
	}
	
    @Override
	public SemType visit(AstAtomType atomType, Mode mode) {
	
        SemType semType = null;
        
        switch(atomType.type()) {
            case VOID: semType = new SemVoid(); break;
            case CHAR: semType = new SemChar(); break;
            case INTEGER: semType = new SemInteger(); break;
            case BOOLEAN: semType = new SemBoolean(); break;
            default: throw new Report.Error(atomType, "Invalid type.");
        }
		
		SemAn.isType.put(atomType, semType);
		
		return null;
	}
	
    @Override
	public SemType visit(AstPtrType ptrType, Mode mode) {
        
        ptrType.baseType().accept(this, mode);
        
        SemType semType = new SemPointer(SemAn.isType.get(ptrType.baseType()));
        
        SemAn.isType.put(ptrType, semType);
        
		return null;
	}
	
	@Override
	public SemType visit(AstArrType arrType, Mode mode) {
        
        arrType.elemType().accept(this, mode);
        arrType.numElems().accept(this, mode);
        
        if(!(SemAn.isType.get(arrType.elemType()) instanceof SemVoid)) {
            if(SemAn.ofType.get(arrType.numElems()) instanceof SemInteger) {
                long numOfEls = Integer.parseInt(((AstAtomExpr)arrType.numElems()).value());
                
                if(numOfEls > 0 && numOfEls <= (Math.pow(2, 63) - 1)) {
                    SemType semType = new SemArray(SemAn.isType.get(arrType.elemType()), numOfEls);
                    
                    SemAn.isType.put(arrType, semType);
                }
                else {
                    throw new Report.Error(arrType.numElems(), "Invalid index value");
                }
            }
            else {
                throw new Report.Error(arrType.numElems(), "Invalid index type");
            }
        }
        else {
            throw new Report.Error(arrType.elemType(), "Invalid array type");
        }
        
        
        return null;
	}
	
	@Override
	public SemType visit(AstRecType recType, Mode mode) {
        
        Vector<SemType> compTypes = new Vector<SemType>();
        
        SymbTable recTable = new SymbTable();
        
        for (AstCompDecl comp : recType.comps()) {
            try {
                comp.accept(this, mode);
                SemType compType = SemAn.isType.get(comp.type());
                
                if(compType instanceof SemVoid) {
                    throw new Report.Error(comp, "Invalid component type.");
                }
                else {
                    recTable.ins(comp.name(), comp);
                    compTypes.add(compType);
                }
            } catch (SymbTable.CannotInsNameException __) {
                throw new Report.Error(comp, "Component name already defined.");
            }
        }
        
        SemRecord semRec = new SemRecord(compTypes);
        
        symbTables.put(semRec, recTable);
        SemAn.isType.put(recType, semRec);
        
        return null;
	}
	
	// EXPRESSIONS
	
	@Override
	public SemType visit(AstAtomExpr atomExpr, Mode mode) {
        
        SemType semType = null;
        
        switch(atomExpr.type()) {
            case VOID: semType = new SemVoid(); break;
            case CHAR: semType = new SemChar(); break;
            case INTEGER: semType = new SemInteger(); break;
            case BOOLEAN: semType = new SemBoolean(); break;
            case POINTER: if(atomExpr.value().equals("nil")) {
                            semType = new SemPointer(new SemVoid());
                        } else {
                            semType = null;
                        } break;
            case STRING: semType = new SemPointer(new SemChar()); break;
            default: throw new Report.Error(atomExpr, "Invalid type.");
        }
        
        SemAn.ofType.put(atomExpr, semType);
        SemAn.isAddr.put(atomExpr, false);
        
        return null;
	}
	
	@Override
	public SemType visit(AstNameExpr nameExpr, Mode mode) {
        
        AstDecl decl = SemAn.declaredAt.get(nameExpr);
        
        if(decl instanceof AstParDecl) {
            SemAn.ofType.put(nameExpr, SemAn.isType.get(((AstParDecl) decl).type()).actualType());  
            SemAn.isAddr.put(nameExpr, true);
        }
        else
        if(decl instanceof AstVarDecl) {
            SemType semType = SemAn.isType.get(((AstVarDecl) decl).type()).actualType();
            SemAn.ofType.put(nameExpr, semType); 
            SemAn.isAddr.put(nameExpr, true);
        }
        else {
            throw new Report.Error(nameExpr, "Something's fishy - not parameter or variable.");
        }
        
        return null;
	}

	@Override
	public SemType visit(AstPfxExpr pfxExpr, Mode mode) {
        
        SemType semType = null;
        
        pfxExpr.expr().accept(this, mode);
        
        switch(pfxExpr.oper()) {
            case ADD: {
                    semType = SemAn.ofType.get(pfxExpr.expr()).actualType();
                    if(semType instanceof SemInteger) {
                        semType = new SemInteger();
                    }
                    else {
                        throw new Report.Error(pfxExpr.expr(), "Invalid expression type.");
                    }
            } break;
            case SUB: {
                    semType = SemAn.ofType.get(pfxExpr.expr()).actualType();
                    if(semType instanceof SemInteger) {
                        semType = new SemInteger();
                    }
                    else {
                        throw new Report.Error(pfxExpr.expr(), "Invalid expression type.");
                    }
            } break;
            case NOT: {
                    semType = SemAn.ofType.get(pfxExpr.expr()).actualType();
                    if(semType instanceof SemBoolean) {
                        semType = new SemBoolean();
                    }
                    else {
                        throw new Report.Error(pfxExpr.expr(), "Invalid expression type.");
                    }
            } break;
            case PTR: {                    
                    semType = new SemPointer(SemAn.ofType.get(pfxExpr.expr()).actualType());
            } break;
            case NEW: {
                    semType = SemAn.ofType.get(pfxExpr.expr()).actualType();
                    
                    if(semType instanceof SemInteger) {
                        semType = new SemPointer(new SemVoid());
                    }
                    else {
                        throw new Report.Error(pfxExpr.expr(), "Invalid expression type.");
                    }
            } break;
            case DEL: {
                    semType = SemAn.ofType.get(pfxExpr.expr()).actualType();
                    
                    if(semType instanceof SemPointer) {
                        semType = new SemVoid();
                    }
                    else {
                        throw new Report.Error(pfxExpr.expr(), "Invalid expression type.");
                    }
            } break;
            default: throw new Report.Error(pfxExpr, "Invalid operator.");
        }
        
        SemAn.ofType.put(pfxExpr, semType);
        SemAn.isAddr.put(pfxExpr, false);
        
        return null;
	}
	
	@Override
	public SemType visit(AstSfxExpr sfxExpr, Mode mode) {
        sfxExpr.expr().accept(this, mode);
	
        SemType semType = SemAn.ofType.get(sfxExpr.expr());
        
        if(semType instanceof SemPointer) {
            semType = ((SemPointer)semType).baseType().actualType();
            SemAn.isAddr.put(sfxExpr, true);
        }
        else {
            SemAn.isAddr.put(sfxExpr, false);
            throw new Report.Error(sfxExpr.expr(), "Expression not a pointer type.");
        }
	
        SemAn.ofType.put(sfxExpr, semType);
	
        return null;
	}
	
	@Override
	public SemType visit(AstBinExpr binExpr, Mode mode) {
       // OR, AND, EQU, NEQ, LTH, GTH, LEQ, GEQ, IN, ADD, SUB, MUL, DIV, MOD
       // IN ?
        SemType semType = null;
        
        binExpr.fstExpr().accept(this, mode);
        binExpr.sndExpr().accept(this, mode);
        
        switch(binExpr.oper()) {
            case OR:
            case AND:{
                    if(SemAn.ofType.get(binExpr.sndExpr()).actualType() instanceof SemBoolean) {
                        semType = SemAn.ofType.get(binExpr.fstExpr()).actualType();
                        if(semType instanceof SemBoolean) {
                            semType = new SemBoolean();
                        }
                        else {
                            throw new Report.Error(binExpr.fstExpr(), "Invalid expression type.");
                        }
                    }
                    else {
                        throw new Report.Error(binExpr.sndExpr(), "Invalid expression type.");
                    }
            } break;
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:{
                    if(SemAn.ofType.get(binExpr.sndExpr()).actualType() instanceof SemInteger) {
                        semType = SemAn.ofType.get(binExpr.fstExpr()).actualType();
                        if(semType instanceof SemInteger) {
                            semType = new SemInteger();
                        }
                        else {
                            throw new Report.Error(binExpr.fstExpr(), "Invalid expression type.");
                        }
                    }
                    else {
                        throw new Report.Error(binExpr.sndExpr(), "Invalid expression type.");
                    }
            } break;
            case EQU:
            case NEQ: {
                    if(SemAn.ofType.get(binExpr.fstExpr()).actualType().getClass().equals(SemAn.ofType.get(binExpr.sndExpr()).actualType().getClass())) {
                        semType = SemAn.ofType.get(binExpr.fstExpr()).actualType();
                        if(semType instanceof SemBoolean |
                            semType instanceof SemChar |
                            semType instanceof SemInteger |
                            semType instanceof SemPointer) {
                            
                            semType = new SemBoolean();
                        }
                        else {
                            throw new Report.Error(binExpr, "Invalid expression types.");
                        }
                    }
                    else {
                        throw new Report.Error(binExpr, "Expression types not matching.");
                    }
            } break;
            case LTH:
            case GTH: 
            case LEQ:
            case GEQ: {
                    if(SemAn.ofType.get(binExpr.fstExpr()).actualType().getClass().equals(SemAn.ofType.get(binExpr.sndExpr()).actualType().getClass())) {
                        semType = SemAn.ofType.get(binExpr.fstExpr()).actualType();
                        if( semType instanceof SemChar |
                            semType instanceof SemInteger |
                            semType instanceof SemPointer) {
                            
                            semType = new SemBoolean();
                        }
                        else {
                            throw new Report.Error(binExpr, "Invalid expression types.");
                        }
                    }
                    else {
                        throw new Report.Error(binExpr, "Expression types not matching.");
                    }                    
            } break;
            default: throw new Report.Error(binExpr, "Invalid operator.");
        }
        
        SemAn.ofType.put(binExpr, semType);
        SemAn.isAddr.put(binExpr, false);
        
        return null;
	}
	
	@Override
    public SemType visit(AstArrExpr arrExpr, Mode mode) {
        
        arrExpr.arr().accept(this, mode);
        arrExpr.idx().accept(this, mode);
        
        if(SemAn.ofType.get(arrExpr.idx()) instanceof SemInteger) {
            SemType semType = SemAn.ofType.get(arrExpr.arr()).actualType();
            
            SemAn.ofType.put(arrExpr, ((SemArray)semType).elemType().actualType());
        }
        else {
            throw new Report.Error(arrExpr.idx(), "Invalid index type.");
        }
        
        SemAn.isAddr.put(arrExpr, SemAn.isAddr.get(arrExpr.arr()));
	
        return null;
	}
	
    @Override
    public SemType visit(AstCallExpr callExpr, Mode mode) {
        
        AstFunDecl funDecl = (AstFunDecl) SemAn.declaredAt.get(callExpr);
        
        if(callExpr.args().size() == funDecl.pars().size()) {
            for(int i = 0; i < callExpr.args().size(); i++) {
                AstExpr arg = callExpr.args().get(i);
                AstParDecl par = funDecl.pars().get(i);
                
                arg.accept(this, mode);
                
                if(!(SemAn.ofType.get(arg).getClass().equals(SemAn.isType.get(par.type()).getClass()))) {
                    throw new Report.Error(arg, "Invalid argument type.");
                }
            }
            
            // Če pridemo do sem, nam je sprejelo vse elemente
            SemAn.ofType.put(callExpr, SemAn.isType.get(funDecl.type()).actualType());
        }
        else {
            throw new Report.Error(callExpr, "Invalid number of arguments.");
        }
        
        /*if(SemAn.isType.get(funDecl.type()) instanceof SemPointer) {
            SemAn.isAddr.put(callExpr, true);
        }
        else {
            SemAn.isAddr.put(callExpr, false);
        }*/
        SemAn.isAddr.put(callExpr, false);
	
        return null;
	}
	
	
	@Override
    public SemType visit(AstWhereExpr whereExpr, Mode mode) {
    
        whereExpr.decls().accept(this, mode);
        whereExpr.expr().accept(this, mode);
        
        SemAn.ofType.put(whereExpr, SemAn.ofType.get(whereExpr.expr()));
        
        SemAn.isAddr.put(whereExpr, false);
	
        return null;
	}
	
	
	@Override
    public SemType visit(AstRecExpr recExpr, Mode mode) {
        
        recExpr.rec().accept(this, mode);
        //recExpr.comp().accept(this, mode);
        
        if (SemAn.ofType.get(recExpr.rec()) instanceof SemRecord) {
            SemType semType = SemAn.ofType.get(recExpr.rec());
            
            if(semType instanceof SemRecord) {
                
                try {
                    AstCompDecl compDecl = (AstCompDecl)symbTables.get(semType).fnd(recExpr.comp().name());
        
                    SemAn.ofType.put(recExpr, SemAn.isType.get(compDecl.type()).actualType());
                    SemAn.declaredAt.put(recExpr.comp(), compDecl);
                } catch (SymbTable.CannotFndNameException __){
                    throw new Report.Error(recExpr, "That's not a valid component.");
                }
            }
            else {
                throw new Report.Error(recExpr, "That's not a record variable.");
            }
        
        }
        else {
            throw new Report.Error(recExpr, "That's not a record");
        }
        
        SemAn.isAddr.put(recExpr, SemAn.isAddr.get(recExpr.rec()));
        
        /*else if (SemAn.ofType.get(recExpr.rec()) instanceof SemRecord) {
            AstDecl decl = SemAn.declaredAt.get(((AstNameExpr)recExpr).rec());
            
            if(decl instanceof AstVarDecl) {
                SemType semType = SemAn.isType.get(((AstVarDecl)decl).type());
                
                if(semType instanceof SemRecord) {
                    AstCompDecl compDecl = (AstCompDecl)symbTables.get(semType).fnd(recExpr.comp().name());
                    
                    if(compDecl != null) {
                        SemAn.ofType.put(recExpr, SemAn.isType.get(compDecl.type()));
                    }
                    else {
                        throw new Report.Error(recExpr, "That's not a valid component");
                    }
                }
                else {
                    throw new Report.Error(recExpr, "That's not a record variable.");
                }
            }
            else {
                throw new Report.Error(recExpr, "That's not a variable.");
            }
        
        }
        else {
            throw new Report.Error(recExpr, "You probably messed something up.");
        }*/
	
        return null;
	}
	
    @Override
    public SemType visit(AstCastExpr castExpr, Mode mode) {
        
        castExpr.expr().accept(this, mode);
        castExpr.type().accept(this, mode);
        
        SemType exprType = SemAn.ofType.get(castExpr.expr()).actualType();
        
        if(exprType instanceof SemInteger |
            exprType instanceof SemChar |
            exprType instanceof SemPointer) {
            
            SemType typeType = SemAn.isType.get(castExpr.type()).actualType();
            
            if(typeType instanceof SemInteger |
                typeType instanceof SemChar |
                typeType instanceof SemPointer) {
                
                SemType semType = SemAn.isType.get(castExpr.type()).actualType();
            
                SemAn.ofType.put(castExpr, semType);
            }
            else {
                throw new Report.Error(castExpr.type(), "Invalid casting type.");
            }
        }
        else {
            throw new Report.Error(castExpr.expr(), "Invalid expression type.");
        }
        
        SemAn.isAddr.put(castExpr, SemAn.isAddr.get(castExpr.expr()));
	
        return null;
	}
	
	@Override
    public SemType visit(AstStmtExpr stmtExpr, Mode mode) {
        
        AstStmt last = null;
        
        for (AstStmt stmt : stmtExpr.stmts()) {
			stmt.accept(this, mode);
			
			last = stmt;
		}
        
        SemAn.ofType.put(stmtExpr, SemAn.ofType.get(last));
        SemAn.isAddr.put(stmtExpr, false);
        
        return null;
	}
	
	
	// STATEMENTS
	
	@Override
	 public SemType visit(AstExprStmt exprStmt, Mode mode) {
        
        exprStmt.expr().accept(this, mode);
        
        SemAn.ofType.put(exprStmt, SemAn.ofType.get(exprStmt.expr()).actualType());
        
        return null;
    } 
    
    @Override
    public SemType visit(AstAssignStmt assStmt, Mode mode) {
        
        assStmt.src().accept(this, mode);
        assStmt.dst().accept(this, mode);
        
        if(SemAn.ofType.get(assStmt.dst()).getClass().equals(SemAn.ofType.get(assStmt.src()).getClass())) {
            SemType semType = SemAn.ofType.get(assStmt.src());
            if(semType instanceof SemBoolean |
                semType instanceof SemChar |
                semType instanceof SemInteger |
                semType instanceof SemPointer) {
                            
                SemAn.ofType.put(assStmt, new SemVoid());
            }
            else {
                throw new Report.Error(assStmt, "Invalid types.");
            }
        }
        else {
            throw new Report.Error(assStmt, "Not matching types.");
        }
        
        return null;
    } 
    
    @Override
    public SemType visit(AstIfStmt ifStmt, Mode mode) {
        
        ifStmt.cond().accept(this, mode);
        ifStmt.thenStmt().accept(this, mode);
        ifStmt.elseStmt().accept(this, mode);

        if(SemAn.ofType.get(ifStmt.cond()) instanceof SemBoolean) {
            SemAn.ofType.put(ifStmt, new SemVoid());
        }
        else {
            throw new Report.Error(ifStmt.cond(), "Invalid condition type.");
        }        
        
        return null;
    } 
    
    @Override
    public SemType visit(AstWhileStmt whileStmt, Mode mode) {
        
        whileStmt.cond().accept(this, mode);
        whileStmt.bodyStmt().accept(this, mode);

        if(SemAn.ofType.get(whileStmt.cond()) instanceof SemBoolean) {
            SemAn.ofType.put(whileStmt, new SemVoid());
        }
        else {
            throw new Report.Error(whileStmt.cond(), "Invalid condition type.");
        }        
        
        return null;
    } 
}
