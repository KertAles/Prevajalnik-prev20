package prev.phase.imclin;

import java.util.*;

import prev.data.ast.tree.decl.*;
import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;
import prev.data.mem.*;
import prev.data.imc.code.expr.*;
import prev.data.imc.code.stmt.*;
import prev.data.lin.*;
import prev.phase.imcgen.ImcGen;
import prev.phase.memory.*;

public class ChunkGenerator extends AstFullVisitor<Object, Object> {

	@Override
	public Object visit(AstAtomExpr atomExpr, Object arg) {
		switch (atomExpr.type()) {
		case STRING:
			MemAbsAccess absAccess = Memory.strings.get(atomExpr);
			ImcLin.addDataChunk(new LinDataChunk(absAccess));
			break;
		default:
			break;
		}
		return null;
	}

	@Override
	public Object visit(AstFunDecl funDecl, Object arg) {
		funDecl.expr().accept(this, arg);

		MemFrame frame = Memory.frames.get(funDecl);
		MemLabel entryLabel = new MemLabel();
		MemLabel exitLabel = new MemLabel();
		
		Vector<ImcStmt> canonStmts = new Vector<ImcStmt>();
		canonStmts.add(new ImcLABEL(entryLabel));
		ImcExpr bodyExpr = ImcGen.exprImc.get(funDecl.expr());
		ImcStmt bodyStmt = new ImcMOVE(new ImcTEMP(frame.RV), bodyExpr);
		canonStmts.addAll(bodyStmt.accept(new StmtCanonizer(), null));
		canonStmts.add(new ImcJUMP(exitLabel));
		
		Vector<ImcStmt> linearStmts = linearize (canonStmts);
		ImcLin.addCodeChunk(new LinCodeChunk(frame, linearStmts, entryLabel, exitLabel));
		
		return null;
	}

	@Override
	public Object visit(AstVarDecl varDecl, Object arg) {
		MemAccess access = Memory.accesses.get(varDecl);
		if (access instanceof MemAbsAccess) {
			MemAbsAccess absAccess = (MemAbsAccess) access;
			ImcLin.addDataChunk(new LinDataChunk(absAccess));
		}
		return null;
	}
	
	private Vector<ImcStmt> linearize(Vector<ImcStmt> stmts) {
		Vector<ImcStmt> result = new Vector<ImcStmt>();
		Vector<ImcStmt> posLabels = new Vector<ImcStmt>();
		
		/*for(int i = 0; i < stmts.size(); i++) {
            ImcStmt currStmt = stmts.get(i);
            
            
            if(currStmt instanceof ImcCJUMP) {
                ImcCJUMP cJump = (ImcCJUMP) currStmt;
                
                
                result.add(currStmt);
                
                for(i++; i < stmts.size(); i++) {
                    currStmt = stmts.get(i);
                    
                    if(currStmt instanceof ImcLABEL) { // prisli smo do labele
                        if(((ImcLABEL)currStmt).label.name.equals(cJump.negLabel.name)) { //nasli smo konec pozitivnega bloka
                            i--;
                            break;
                        }
                    }
                    
                    posLabels.add(currStmt);
                }
            }
            else {
                result.add(currStmt);
            }
		}
		
		result.addAll(posLabels);*/
		
		result.addAll(stmts);
		
		return result;
	}

}
