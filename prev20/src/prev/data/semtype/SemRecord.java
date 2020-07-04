package prev.data.semtype;

import java.util.*;

import prev.common.logger.*;

/**
 * Record type.
 */
public class SemRecord extends SemType {

	/** Component types. */
	private final SemType[] compTypes;

	public SemRecord(Collection<SemType> compTypes) {
		this.compTypes = new SemType[compTypes.size()];
		int index = 0;
		for (SemType compType: compTypes) {
			this.compTypes[index++] = compType;
		}
	}
	
	@Override
	public long size() {
		long size = 0;
		for (int index = 0; index < compTypes.length; index++) {
			size += compTypes[index].size();
		}
		return size;
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("semtype");
		logger.addAttribute("type", "RECORD");
		for (SemType compType: compTypes)
			compType.log(logger);
		logger.endElement();
	}

}
