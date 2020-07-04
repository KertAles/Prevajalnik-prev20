package prev.data.imc.code;

import prev.common.logger.*;
import prev.data.imc.visitor.*;

/**
 * Intermediate code instruction.
 */
public abstract class ImcInstr implements Loggable {

	public abstract <Result, Arg> Result accept(ImcVisitor<Result, Arg> visitor, Arg accArg);

}
