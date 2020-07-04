package prev.data.ast.tree;

import java.util.*;
import java.util.function.*;

import prev.common.report.*;
import prev.data.ast.visitor.AstVisitor;

/**
 * A sequence of abstract syntax trees.
 * 
 * @param <Tree> The type of abstract syntax trees stored in this sequence.
 */
public class AstTrees<Tree extends AstTree> extends AstNode implements AstTree, Iterable<Tree> {

	/** The abstract syntax trees stored in this sequence. */
	private Tree[] trees;

	/**
	 * Constructs a sequence of abstract syntax trees.
	 */
	public AstTrees() {
		this(new Location(0, 0), new Vector<Tree>());
	}

	/**
	 * Constructs a sequence of abstract syntax trees.
	 * 
	 * @param trees The abstract syntax trees stored in this sequence.
	 */
	@SuppressWarnings("unchecked")
	public AstTrees(Collection<Tree> trees) {
		super(null);
		this.trees = (Tree[]) (new AstTree[trees.size()]);
		int index = 0;
		for (Tree t : trees)
			this.trees[index++] = t;
		if (this.trees.length == 0)
			relocate(new Location(0, 0));
		else
			relocate(new Location(this.trees[0], this.trees[this.trees.length - 1]));
	}

	/**
	 * Constructs a sequence of abstract syntax trees.
	 * 
	 * @param location The location.
	 * @param trees    The abstract syntax trees stored in this sequence.
	 */
	@SuppressWarnings("unchecked")
	public AstTrees(Location location, Collection<Tree> trees) {
		super(location);
		this.trees = (Tree[]) (new AstTree[trees.size()]);
		int index = 0;
		for (Tree t : trees)
			this.trees[index++] = t;
	}

	/**
	 * Returns the abstract syntax tree at the specified position in this sequence.
	 * 
	 * @param index The index of the abstract syntax tree to return.
	 * @return The abstract syntax tree at the specified index.
	 */
	public final Tree get(int index) {
		return trees[index];
	}

	/**
	 * Returns the number of abstract syntax trees in this sequence.
	 * 
	 * @return The number of abstract syntax trees in this sequence.
	 */
	public final int size() {
		return trees.length;
	}

	// Iterable<Tree>

	@Override
	public void forEach(Consumer<? super Tree> action) throws NullPointerException {
		for (Tree t : this)
			action.accept(t);
	}

	@Override
	public Iterator<Tree> iterator() {
		return new TreesIterator();
	}

	// Iterator.

	/**
	 * Iterator over abstract syntax trees with the removal operation blocked.
	 * 
	 * It is assumed that the underlying array of abstract syntax trees is
	 * immutable.
	 */
	private final class TreesIterator implements Iterator<Tree> {

		private int index = 0;

		@Override
		public boolean hasNext() {
			return index < trees.length;
		}

		@Override
		public Tree next() throws NoSuchElementException {
			if (index < trees.length)
				return trees[index++];
			else
				throw new NoSuchElementException("");
		}

		@Override
		public void remove() {
			throw new Report.InternalError();
		}

		@Override
		public void forEachRemaining(Consumer<? super Tree> action) {
			while (hasNext())
				action.accept(next());
		}

	}

	@Override
	@SuppressWarnings("unchecked")
	public AstTrees<Tree> clone() {
		AstTrees<Tree> ast = (AstTrees<Tree>) super.clone();
		ast.trees = (Tree[]) (new AstTree[trees.length]);
		int index = 0;
		for (Tree t : trees)
			this.trees[index++] = t == null ? null : (Tree) t.clone();
		return ast;
	}

	@Override
	public <Result, Arg> Result accept(AstVisitor<Result, Arg> visitor, Arg arg) {
		return visitor.visit(this, arg);
	}

}
