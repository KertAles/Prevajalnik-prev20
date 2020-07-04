package prev.data.imc.code.expr;

import prev.common.logger.*;
import prev.data.imc.visitor.*;

/**
 * New.
 * 
 * Allocates a new pointer.
 */
public class ImcNEW extends ImcExpr {

	/** The size. */
	public final ImcExpr size;

	/**
	 * Constructs a new pointer.
	 * 
	 * @param size The size.
	 */
	public ImcNEW(ImcExpr size) {
		this.size = size;
	}

	@Override
	public <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg) {
		return visitor.visit(this, accArg);
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("instruction", toString());
		logger.endElement();
	}

	@Override
	public String toString() {
		return "NEW(" + size + ")";
	}

}
