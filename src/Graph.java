/**
 * 
 */
// package si.lj.uni.fri.lna.test.bib;

import java.util.List;

/**
 * Static implementation of directed multigraph with loops using arrays and primitive types.
 * <p>
 * The graph is implemented with an array of predecessors and an array of successors for each node.
 * The nodes are represented with indices between {@literal 0} and {@literal n-1}, where {@literal n} is the number of nodes in the graph. 
 * Each node also stores an additional integer label that can be used as a reference.
 * The labels of the nodes and the indices of their successors are specified during the construction. 
 * <p>
 * All public methods except the constructors and {@link #getNode(int)} run in constant time {@literal O(1)}.
 * 
 * @author Lovro Šubelj
 *
 * @version 1.0.0
 */
public class Graph {

	/**
	 * The name of the graph.
	 */
	protected String name;

	/**
	 * The labels of the nodes.
	 */
	protected int[] labels;

	/**
	 * The indices of the successors of the nodes.
	 */
	protected int[][] successors;

	/**
	 * The indices of the predecessors of the nodes.
	 */
	private int[][] predecessors;

	/**
	 * The number of edges in the graph.
	 */
	private int m;

	/**
	 * Constructs a graph with the specified labels of the nodes and the indices of their successors.
	 * <p>
	 * The length of the arrays of labels and successors must equal the number of nodes in the graph {@literal n}. 
	 * The indices of the successors must be between {@literal 0} and {@literal n-1}, whereas the labels of the nodes can be arbitrary integers. 
	 * <p>
	 * The constructor runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param name the name of the graph
	 * @param labels the labels of the nodes
	 * @param successors the indices of the successors
	 */
	public Graph(String name, int[] labels, int[][] successors) {
		super();
		this.name = name;
		this.labels = labels;
		this.successors = successors;
		
		for (int i = 0; i < successors.length; i++)
			if (successors[i] == null)
				successors[i] = new int[0];

		setPredecessors();
		setM();
	}
	
	/**
	 * @deprecated Replaced by {@link #Graph(String, int[], int[][])}
	 */
	public Graph(String name, List<Integer> labels, List<List<Integer>> successors) {
		super();
		this.name = name;
		this.labels = new int[labels.size()];
		this.successors = new int[successors.size()][];
		
		int i = 0;
		for (int label: labels)
			this.labels[i++] = label;
		
		i = 0;
		for (List<Integer> list: successors)
			if (list == null)
				this.successors[i++] = new int[0];
			else {
				this.successors[i++] = new int[list.size()];
				
				int j = 0;
				for (int successor: list)
					this.successors[i - 1][j++] = successor;
			}

		setPredecessors();
		setM();
	}
	
	/**
	 * Computes the number of edges in the graph from the successors of the nodes. 
	 * <p>
	 * The method runs in linear time {@literal O(n)}, where {@literal n} is the number of nodes in the graph. 
	 */
	private void setM() {
		m = 0;
		for (int i = 0; i < getN(); i++)
			m += successors[i].length;
	}
	
	/**
	 * Sets the indices of the predecessors of the nodes using the indices of the successors. 
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 */
	private void setPredecessors() {
		int[] degrees = new int[successors.length];
		for (int i = 0; i < successors.length; i++)
			for (int j = 0; j < successors[i].length; j++)
				degrees[successors[i][j]]++;
		
		predecessors = new int[successors.length][];
		for (int i = 0; i < successors.length; i++)
			predecessors[i] = new int[degrees[i]];
		
		for (int i = 0; i < successors.length; i++)
			for (int j = 0; j < successors[i].length; j++)
				predecessors[successors[i][j]][--degrees[successors[i][j]]] = i;
	}
	
	/**
	 * Returns the labels of the nodes in the graph. 
	 * 
	 * @return the labels of the nodes
	 */
	public int[] getLabels() {
		return labels;
	}

	/**
	 * Returns the indices of the predecessors of the specified node.
	 * 
	 * @param node the index of the node
	 * 
	 * @return the indices of the predecessors 
	 */
	public int[] getPredecessors(int node) {
		return predecessors[node];
	}

	/**
	 * Returns the indices of the successors of the specified node.
	 * 
	 * @param node the index of the node
	 * 
	 * @return the indices of the successors 
	 */
	public int[] getSuccessors(int node) {
		return successors[node];
	}

	/**
	 * Returns the label of the node with the specified index. 
	 * 
	 * @param node the index of the node
	 * 
	 * @return the label of the node
	 */
	public int getLabel(int node) {
		return labels[node];
	}
	
	/**
	 * Returns the index of the node with the specified label.
	 * <p>
	 * If multiple nodes share the specified label, the lowest index is returned. 
	 * <p>
	 * The method runs in linear time {@literal O(n)}, where {@literal n} is the number of nodes in the graph. 
	 * 
	 * @param label the label of the node
	 * 
	 * @return the index of the node
	 * 
	 * @throws IllegalArgumentException if no node has the specified label
	 */
	public int getNode(int label) throws IllegalArgumentException {
		for (int i = 0; i < labels.length; i++)
			if (labels[i] == label)
				return i;
		
		throw new IllegalArgumentException();
	}

	/**
	 * Returns the number of nodes in the graph.
	 * 
	 * @return the number of nodes
	 */
	public int getN() {
		return labels.length;
	}

	/**
	 * Returns the number of edges in the graph.
	 * 
	 * @return the number of edges
	 */
	public int getM() {
		return m;
	}

	/**
	 * Returns the current name of the graph.
	 * 
	 * @return the current graph name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Changes the current name of the graph.
	 * 
	 * @param name the new graph name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
