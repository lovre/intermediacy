/**
 * 
 */
// package si.lj.uni.fri.lna.test.bib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * {@link Graph} functions and I/O methods implemented using arrays and primitive types.
 * <p>
 * All public methods except {@link #intermediacy(Graph, int, int, double, int)} run in linear time {@literal O(m)}, 
 * where {@literal m} is the number of edges in the graph.
 * 
 * @author Lovro Šubelj
 *
 * @version 1.0.0
 */
public class Graphology {
	
	/**
	 * Finds the in/out-component consisting of nodes located on a directed path to/from the specified root node. 
	 * <p>
	 * The implementation makes a depth-first search traversal of the graph starting at the root node.
	 * If the parameter {@code reverse} is set to {@code true}/{@code false},
	 * the edges are followed in the opposite/proper direction and the implementation finds the in/out-component of the root node.
	 * The parameter {@code nodes} specifies the indices of the nodes to be considered and can be set to {@code null} to consider all nodes.
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param graph the graph to be traversed
	 * @param nodes the indices of the nodes
	 * @param root the index of the root node
	 * @param reverse the direction of the edges
	 * 
	 * @return the indices of the in/out-component nodes
	 */
	public static boolean[] component(Graph graph, boolean[] nodes, int root, boolean reverse) {
		boolean[] component = new boolean[graph.getN()];
		
		if (nodes == null || nodes[root]) {
			component[root] = true;

			Deque<Integer> stack = new ArrayDeque<Integer>(); 
			stack.push(root);

			while (stack.size() > 0)
				for (int neighbor: reverse? graph.getPredecessors(stack.pop()): graph.getSuccessors(stack.pop()))
					if ((nodes == null || nodes[neighbor]) && !component[neighbor]) {
						component[neighbor] = true;
						stack.push(neighbor);
					}
		}

		return component;
	}
	
	/**
	 * Finds the intermediate nodes located on a directed path between the specified source and target nodes. 
	 * <p>
	 * The implementation makes two depth-first search traversals of the graph starting at the source and target nodes.
	 * The implementation first finds the out-component of the source node in the original graph and then
	 * returns the in-component of the target node in the subgraph induced by the nodes of the out-component.
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param graph the graph to be traversed
	 * @param source the index of the source node
	 * @param target the index of the target node
	 * 
	 * @return the indices of the intermediate nodes
	 */
	public static boolean[] intermediate(Graph graph, int source, int target) {
		return component(graph, component(graph, null, source, false), target, true);
	}
	
	/**
	 * Finds the intermediate nodes located on a directed path between the specified source and target nodes 
	 * whereby each edge of the graph is sampled independently with the specified probability. 
	 * <p>
	 * The implementation makes two depth-first search traversals of the graph starting at the source and target nodes.
	 * The implementation first finds the out-component of the source node by independently sampling each edge of the original graph and then
	 * returns the in-component of the target node in the subgraph induced by both the sampled edges and the nodes of the out-component.
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param graph the graph to be traversed
	 * @param source the index of the source node
	 * @param target the index of the target node
	 * @param probability the probability of an edge
	 * 
	 * @return the indices of the intermediate nodes
	 */
	public static boolean[] intermediate(Graph graph, int source, int target, double probability) {
		int[][] successors = new int[graph.getN()][];
		
		boolean[] component = new boolean[graph.getN()];
		component[source] = true;

		Deque<Integer> stack = new ArrayDeque<Integer>(); 
		stack.push(source);

		while (stack.size() > 0) {
			int node = stack.pop();

			List<Integer> nodes = new ArrayList<Integer>();
			for (int successor: graph.getSuccessors(node)) 
				if (Math.random() < probability){
					nodes.add(successor);

					if (!component[successor]) {
						component[successor] = true;
						stack.push(successor);
					}
				}

			successors[node] = new int[nodes.size()];
			for (int i = 0; i < successors[node].length; i++)
				successors[node][i] = nodes.get(i);
		}

		return component(new Graph(graph.getName(), graph.getLabels(), successors), component, target, true);
	}
	
	/**
	 * Computes the intermediacy of the nodes in a graph for the specified source and target nodes. 
	 * The intermediacy is the probability that a node is located on a directed path between the source and target nodes
	 * whereby each edge of the graph is sampled independently with the specified probability. 
	 * <p>
	 * The implementation approximates the intermediacy of the nodes by Monte Carlo sampling of the edges in the graph.
	 * A Monte Carlo sample of the graph is realized by calling the method {@link #intermediate(Graph, int, int, double)},
	 * while the parameter {@code samples} specifies the total number of Monte Carlo samples.
	 * <p>
	 * The method runs in time {@literal O(zm)}, where {@literal m} is the number of edges in the graph and
	 * {@literal z} is the number of Monte Carlo samples.
	 * 
	 * @param graph the graph to be traversed
	 * @param source the index of the source node
	 * @param target the index of the target node
	 * @param probability the probability of an edge
	 * @param samples the number of Monte Carlo samples
	 * 
	 * @return the intermediacy of the nodes
	 */
	public static double[] intermediacy(Graph graph, int source, int target, double probability, int samples) {
		double[] intermediacy = new double[graph.getN()];
		
		int s = 0;
		while (s++ < samples) {
			boolean[] intermediate = Graphology.intermediate(graph, source, target, probability);

			for (int i = 0; i < intermediacy.length; i++)
				if (intermediate[i])
					intermediacy[i]++;
		}

		for (int i = 0; i < intermediacy.length; i++)
			intermediacy[i] /= samples;

		return intermediacy;
	}
	
	/**
	 * Constructs a subgraph of the graph induced by the specified nodes.
	 * <p>
	 * The parameter {@code nodes} specifies the indices of the nodes in the original graph to be included in the subgraph.
	 * The implementation retains the labels of the nodes from the original graph, but not the indices of the nodes.
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param graph the graph to be induced
	 * @param nodes the indices of the nodes
	 * 
	 * @return the subgraph induced by the specified nodes
	 */
	public static Graph induced(Graph graph, boolean[] nodes) {
		int[] mapping = new int[graph.getN()];
		
		int n = 0;
		
		for (int i = 0; i < graph.getN(); i++) 
			if (nodes[i]) {
				mapping[i] = n;
				n++;
			}
		
		int[] labels = new int[n];
		int[][] successors = new int[n][];
		
		for (int i = 0; i < graph.getN(); i++)
			if (nodes[i]) {
				labels[mapping[i]] = graph.getLabel(i);
				
				int degree = 0;
				for (int successor: graph.getSuccessors(i))
					if (nodes[successor])
						degree++;

				successors[mapping[i]] = new int[degree];
				for (int successor: graph.getSuccessors(i))
					if (nodes[successor])
						successors[mapping[i]][--degree] = mapping[successor];
			}
		
		return new Graph(graph.getName(), labels, successors);
	}

	/**
	 * Reads a graph from the specified TSV file.
	 * <p>
	 * The file must contain a directed multigraph in the edge list format,
	 * where each line consists of a pair of tab-separated node indices representing a directed edge.    
	 * The indices of the nodes must be between {@literal 1} and {@literal n}, where {@literal n} is the number of nodes in the graph.
	 * Any line not starting with a digit is ignored.
	 * <p>
	 * The implementation retains the indices of the nodes in the file as the labels of the nodes in the constructed graph.
	 * The graph can contain multiple parallel edges between the nodes, whereas any loops are ignored.
	 * The name of the graph is set to the name of the file without extension.
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param filename the name of the TSV file
	 * 
	 * @return the graph read from the TSV file
	 * 
	 * @throws IOException if I/O exception occurs
	 * 
	 * @see Graph
	 */
	public static Graph TSV(String filename) throws IOException {
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file)); 
		
		List<Integer> degrees = new ArrayList<Integer>();
		
		String line;
		while ((line = reader.readLine()) != null) {
			String[] nodes = line.trim().split("\t");

			if (nodes.length >= 2 && !nodes[0].equals(nodes[1]) && Character.isDigit(nodes[0].charAt(0))) {
				int source = Integer.parseInt(nodes[0]) - 1;
				int target = Integer.parseInt(nodes[1]) - 1;
				
				while (degrees.size() < source + 1 || degrees.size() < target + 1)
					degrees.add(0);
				
				degrees.set(source, degrees.get(source) + 1);
			}
		}

		reader.close();
		
		int n = degrees.size();
		
		int[] labels = new int[n];
		int[][] successors = new int[n][];
		
		for (int i = 0; i < n; i++) {
			labels[i] = i + 1;
			successors[i] = new int[degrees.get(i)];
		}
		
		reader = new BufferedReader(new FileReader(file));
		
		while ((line = reader.readLine()) != null) {
			String[] nodes = line.trim().split("\t");

			if (nodes.length >= 2 && !nodes[0].equals(nodes[1]) && Character.isDigit(nodes[0].charAt(0))) {
				int source = Integer.parseInt(nodes[0]) - 1;
				
				degrees.set(source, degrees.get(source) - 1);
				successors[source][degrees.get(source)] = Integer.parseInt(nodes[1]) - 1;
			}
		}
		
		reader.close();

		int index = file.getName().lastIndexOf(".");

		return new Graph(index != -1? file.getName().substring(0, index): file.getName(), labels, successors);
	}
	
	/**
	 * Reads a graph from the specified Pajek file.
	 * <p>
	 * The file must contain a directed multigraph in the standard Pajek format consisting of {@code *vertices} and {@code *arcs} sections.    
	 * The indices of the nodes must be between {@literal 1} and {@literal n}, where {@literal n} is the number of nodes in the graph.
	 * <p>
	 * The implementation retains the indices of the nodes in the file as the labels of the nodes in the constructed graph.
	 * The graph can contain multiple parallel edges between the nodes, whereas any loops are ignored.
	 * The name of the graph is set to the name of the file without extension.
	 * <p>
	 * The method runs in linear time {@literal O(m)}, where {@literal m} is the number of edges in the graph. 
	 * 
	 * @param filename the name of the Pajek file
	 * 
	 * @return the graph read from the Pajek file
	 * 
	 * @throws IOException if I/O exception occurs
	 * 
	 * @see Graph
	 */
	public static Graph pajek(String filename) throws IOException {
		File file = new File(filename);
		BufferedReader reader = new BufferedReader(new FileReader(file));
		
		int n = -1;
		
		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim().toLowerCase();
			
			if (line.startsWith("*vertices"))
				n = Integer.parseInt(line.split(" ")[1]);
			else if (line.startsWith("*arcs"))
				break;
		}
		
		int[] degrees = new int[n];
		
		while ((line = reader.readLine()) != null) {
			String[] nodes = line.trim().split(" ");

			if (nodes.length >= 2 && !nodes[0].equals(nodes[1]))
				degrees[Integer.parseInt(nodes[0]) - 1]++;
		}

		reader.close();
		
		int[] labels = new int[n];
		int[][] successors = new int[n][];
		
		for (int i = 0; i < n; i++) {
			labels[i] = i + 1;
			successors[i] = new int[degrees[i]];
		}
		
		reader = new BufferedReader(new FileReader(file));
		
		while ((line = reader.readLine()) != null) 
			if (line.trim().toLowerCase().startsWith("*arcs"))
				break;
		
		while ((line = reader.readLine()) != null) {
			String[] nodes = line.trim().split(" ");

			if (nodes.length >= 2 && !nodes[0].equals(nodes[1])) {
				int source = Integer.parseInt(nodes[0]) - 1;
				
				successors[source][--degrees[source]] = Integer.parseInt(nodes[1]) - 1;
			}
		}
		
		reader.close();
		
		int index = file.getName().lastIndexOf(".");

		return new Graph(index != -1? file.getName().substring(0, index): file.getName(), labels, successors);
	}
	
}
