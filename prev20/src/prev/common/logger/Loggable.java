package prev.common.logger;

/**
 * Implemented by objects that a log should be produced for.
 */
public interface Loggable {

	/**
	 * Produces a log of this object.
	 * 
	 * @param logger The logger a log of this object is dumped to.
	 */
	public void log(Logger logger);

}
