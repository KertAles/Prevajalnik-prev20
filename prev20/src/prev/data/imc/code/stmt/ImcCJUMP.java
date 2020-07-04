package prev.data.imc.code.stmt;

import prev.common.logger.*;
import prev.data.mem.*;
import prev.data.imc.code.expr.*;
import prev.data.imc.visitor.*;

/**
 * Conditional jump.
 * 
 * Evaluates the condition, jumps to the positive label if the condition is
 * nonzero or to the negative label if the condition is zero.
 */
public class ImcCJUMP extends ImcStmt {

	/** The condition. */
	public ImcExpr cond;

	/** The positive label. */
	public MemLabel posLabel;

	/** The negative label. */
	public MemLabel negLabel;

	/**
	 * Constructs a conditional jump.
	 * 
	 * @param cond     The condition.
	 * @param posLabel The positive label.
	 * @param negLabel The negative label.
	 */
	public ImcCJUMP(ImcExpr cond, MemLabel posLabel, MemLabel negLabel) {
		this.cond = cond;
		this.posLabel = posLabel;
		this.negLabel = negLabel;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("instruction",
				"CJUMP(" + posLabel.name + "," + negLabel.name + ")");
		cond.log(logger);
		logger.endElement();
	}

	@Override
	public String toString() {
		return "CJUMP(" + cond.toString() + "," + posLabel.name + "," + negLabel.name + ")";
	}

}
