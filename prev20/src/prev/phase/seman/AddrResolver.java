package prev.phase.seman;

import prev.data.ast.tree.expr.*;
import prev.data.ast.visitor.*;

/**
 * Address resolver.
 * 
 * The address resolver finds out which expressions denote lvalues and leaves
 * the information in {@link SemAn#isAddr}.
 */
public class AddrResolver extends AstFullVisitor<Object, Object> {

	// TODO

}
