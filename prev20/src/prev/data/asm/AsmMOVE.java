package prev.data.asm;

import java.util.*;
import prev.common.report.*;
import prev.data.mem.*;

/**
 * An assembly move.
 */
public class AsmMOVE extends AsmOPER {

	public AsmMOVE(String instr, Vector<MemTemp> uses, Vector<MemTemp> defs) {
		super(instr, uses, defs, null);
		if (uses.size() != 1 || defs.size() != 1)
			throw new Report.InternalError();
	}

}
