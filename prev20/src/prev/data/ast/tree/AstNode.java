package prev.data.ast.tree;

import prev.common.report.*;

/**
 * An abstract node of an abstract syntax tree.
 */
public abstract class AstNode implements AstTree {

	/** The number of nodes constructed so far. */
	private static int numNodes = 0;

	/** The unique id of this node. */
	private int id;

	/** The location of this node. */
	private Location location;

	/** Indicator whether location can be modified or not. */
	private static boolean locationLocked = false;

	/** Prevents modifying locations for good. */
	public static void lock() {
		locationLocked = true;
	}

	/**
	 * Constructs an abstract node of an abstract syntax tree.
	 * 
	 * @param location The location.
	 */
	public AstNode(Location location) {
		id = numNodes++;
		this.location = location;
	}

	@Override
	public final int id() {
		return id;
	}

	@Override
	public AstNode clone() {
		try {
			AstNode ast = (AstNode) super.clone();
			ast.id = numNodes++;
			ast.location = location == null ? null : new Location(location);
			return ast;
		} catch (CloneNotSupportedException __) {
			throw new Report.InternalError();
		}
	}

	@Override
	public final void relocate(Location location) {
		if (locationLocked)
			throw new Report.InternalError();
		this.location = location;
	}

	@Override
	public final Location location() {
		return location;
	}

}
