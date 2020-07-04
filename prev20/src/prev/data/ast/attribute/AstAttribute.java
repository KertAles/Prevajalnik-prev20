package prev.data.ast.attribute;

import java.util.*;
import prev.common.report.*;
import prev.data.ast.tree.*;

/**
 * An attribute of the abstract syntax tree node.
 *
 * @param <Node>  Nodes that values are associated with.
 * @param <Value> Values associated with nodes.
 */
public class AstAttribute<Node extends AstTree, Value> {

	/** Mapping of nodes to values. */
	private Vector<Value> mapping;

	/** Whether this attribute can no longer be modified or not. */
	private boolean lock;

	/** Constructs a new attribute. */
	public AstAttribute(int numNodes) {
		mapping = new Vector<Value>();
		lock = false;
	}

	/**
	 * Associates a value with the specified abstract syntax tree node.
	 * 
	 * @param node  The specified abstract syntax tree node.
	 * @param value The value.
	 * @return The value.
	 */
	public Value put(Node node, Value value) {
		if (lock)
			throw new Report.InternalError();
		int id = node.id();
		if (id >= mapping.size())
			mapping.setSize(id + 1000);
		mapping.set(id, value);
		return value;
	}
	
	public Vector<Value> values() {
        return new Vector<Value>(mapping);
	}

	/**
	 * Returns a value associated with the specified abstract syntax tree node.
	 * 
	 * @param node The specified abstract syntax tree node.
	 * @return The value (or {@code null} if the value is not found).
	 */
	public Value get(Node node) {
		int id = node.id();
		if (id >= mapping.size())
			return null;
		return mapping.get(id);
	}

	/**
	 * Prevents further modification of this attribute.
	 */
	public void lock() {
		lock = true;
	}

}
