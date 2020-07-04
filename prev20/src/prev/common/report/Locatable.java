package prev.common.report;

/**
 * Implemented by classes containing information relating to a part of the
 * source file.
 */
public interface Locatable {

	/**
	 * Returns the location of the part of the source file.
	 * 
	 * @return The location of the part of the source file.
	 */
	public Location location();

	/**
	 * Modifies the location.
	 * 
	 * @param location The new location.
	 */
	public default void relocate(Location location) {
		throw new Report.InternalError();
	}

}
