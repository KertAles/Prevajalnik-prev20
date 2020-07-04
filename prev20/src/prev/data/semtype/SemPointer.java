package prev.data.semtype;

import prev.common.logger.*;

/**
 * Pointer type.
 */
public class SemPointer extends SemType {

	/** Base type. */
	private final SemType baseType;

	/**
	 * Constructs a new pointer type.
	 * 
	 * @param baseType The base type.
	 */
	public SemPointer(SemType baseType) {
		this.baseType = baseType;
	}

	/**
	 * Returns the base type.
	 * 
	 * @return The base type.
	 */
	public SemType baseType() {
		return baseType;
	}

	@Override
	public long size() {
		return 8;
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("semtype");
		logger.addAttribute("type", "PTR");
		baseType.log(logger);
		logger.endElement();
	}

}
