package prev.data.imc.code.stmt;

import prev.common.logger.*;
import prev.data.mem.*;
import prev.data.imc.visitor.*;

/**
 * Unconditional jump.
 * 
 * Jumps to the label provided.
 */
public class ImcJUMP extends ImcStmt {

	/** The label. */
	public MemLabel label;

	/**
	 * Constructs an uncoditional jump.
	 * 
	 * @param label The label.
	 */
	public ImcJUMP(MemLabel label) {
		this.label = label;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("instruction", "JUMP(" + label.name + ")");
		logger.endElement();
	}

	@Override
	public String toString() {
		return "JUMP(" + label.name + ")";
	}

}
