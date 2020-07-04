package prev.data.semtype;

import prev.common.logger.*;

/**
 * Array type.
 */
public class SemArray extends SemType {

	/** Element type. */
	private final SemType elemType;

	/** Number of elements. */
	private final long numElems;

	/**
	 * Constructs a new array type.
	 * 
	 * @param elemType The element type.
	 * @param numElems The number of elements.
	 */
	public SemArray(SemType elemType, long numElems) {
		this.elemType = elemType;
		this.numElems = numElems;
	}

	/**
	 * Returns the element type.
	 * 
	 * @return The element type.
	 */
	public SemType elemType() {
		return elemType;
	}

	/**
	 * Returns the number of elements.
	 * 
	 * @return The number of elements.
	 */
	public long numElems() {
		return numElems;
	}

	@Override
	public long size() {
		return numElems * elemType.size();
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("semtype");
		logger.addAttribute("type", "ARRAY[" + numElems + "]");
		elemType.log(logger);
		logger.endElement();
	}

}
