/**
 * 
 */
// package si.lj.uni.fri.lna.test.bib;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * A program for computing the intermediacy of the nodes in a graph for the specified source and target nodes. 
 * The intermediacy is the probability that a node is located on a directed path between the source and target nodes
 * whereby each edge of the graph is sampled independently with some probability. 
 * <p>
 * The implementation first parses the command line arguments as follows.
 * <pre>{@code
 * Usage: java -jar Intermediacy.jar -i <file> -s <source> -t <target> [-p <list>] [-z <samples>] [-h]
 * Computes intermediacy of nodes for selected source and target nodes
 * 	-i,--input <file>         Pajek or TSV file (example: ../nets/toy.net)
 * 	-s,--source <source>      source node index (example: 1)
 * 	-t,--target <target>      target node index (example: 2)
 * 	-p,--probability <list>   edge probability (default: 0.3,0.5,0.7)
 * 	-z,--samples <samples>    Monte Carlo samples (default: 100000)
 * 	-h,--help                 prints help and usage message
 * }</pre>
 * <p>
 * The implementation then reads a graph from the specified Pajek or TSV file. If the filename ends with the extension {@code .net}, 
 * the method {@link Graphology#pajek(String)} is called. Otherwise, the method {@link Graphology#TSV(String)} is called.
 * Next, the graph is reduced to the intermediate graph consisting only of nodes located on a directed path between the source and target nodes, 
 * since the intermediacy of the remaining nodes equals zero by definition.
 * <p>
 * For each of the specified probabilities of the edges in the graph, the intermediacy of the nodes is approximated by 
 * Monte Carlo sampling using the method {@link Graphology#intermediacy(Graph, int, int, double, int)}. 
 * The implementation approximates the intermediacy using the specified number of Monte Carlo samples {@literal z}
 * with the standard error {@literal sqrt(Φ(1-Φ)/z)}, where {@literal Φ} is the intermediacy of a node.
 * <p>
 * Finally, the intermediacies of the nodes for each of the specified probabilities are written to a TSV file.
 * Only the nodes in the intermediate graph are included in the TSV file. The file is put in the same folder as the graph file,
 * while the filename is set to the name of the graph followed by the extension {@code _phi.tsv}.
 * <p>
 * The program runs in time {@literal O(pzm)}, where {@literal m} is the number of edges in the intermediate graph,
 * {@literal z} is the number of Monte Carlo samples and {@literal p} is the number of the specified probabilities.
 * 
 * @author Lovro Šubelj
 *
 * @version 1.0.0
 */
public class Intermediacy {
	
	/**
	 * The usage of the program in the command line.
	 */
	private static final String USAGE = "java -jar Intermediacy.jar";

	/**
	 * The description of the program in the command line.
	 */
	private static final String DESCRIPTION = "Computes intermediacy of nodes for selected source and target nodes";
	
	/**
	 * The Pajek or TSV file containing the graph.
	 */
	private static String filename = "../nets/toy.net";
	
	/**
	 * The probabilities of the edges in the graph.
	 */
	private static double[] probabilities = new double[] { 0.3, 0.5, 0.7 };
	
	/**
	 * The index of the source node in the graph.
	 */
	private static int source = 1;
	
	/**
	 * The index of the target node in the graph.
	 */
	private static int target = 2;
	
	/**
	 * The number of Monte Carlo samples of the graph.
	 */
	private static int samples = 100000;
	
	public static void main(String[] args) throws IOException {
//		args = "-i /Users/lovre/Documents/office/coding/repositories/intermediacy/nets/toy.net -s 1 -t 5 -z 1000000 -p 0.714286".split(" ");
//		args = "-i /Users/lovre/Documents/office/research/networks/raw/peere/CiteAcy.net -s 43718 -t 6256 -z 1000000 -p 0.1,0.3,0.5,0.7,0.9".split(" ");
//		args = "-i /Users/lovre/Documents/office/coding/remotes/cwts/cit.txt -s 6687267 -t 1 -z 1000000 -p 0.1,0.3,0.5,0.7,0.9".split(" ");
		
		// parses the command line arguments
		
		parse(args);
		
		Complexity.init("INTERMEDIACY");
		
		// reads a graph from the specified file
		
		Graph graph = filename.endsWith(".net")? Graphology.pajek(filename): Graphology.TSV(filename);
		
		// constructs the intermediate graph for the specified source and target nodes
		
		Graph intermediate = Graphology.induced(graph, Graphology.intermediate(graph, graph.getNode(source), graph.getNode(target)));
		
		print("NETWORK");
		print(graph, intermediate, source, target);
		Complexity.poc();
		
		graph = null;
		
		if (intermediate.getN() > 2) {
			List<Integer> nodes = new ArrayList<Integer>();
			for (int i = 0; i < intermediate.getN(); i++)
				if (intermediate.getLabel(i) != source && intermediate.getLabel(i) != target)
					nodes.add(i);
			
			// approximates the intermediacies of the nodes for the specified probabilities of the edges
			
			double[][] intermediacies = new double[probabilities.length][];
			for (int i = 0; i < intermediacies.length; i++) {
				print("MONTE CARLO");
				print(probabilities[i], samples);

				intermediacies[i] = Graphology.intermediacy(intermediate, intermediate.getNode(source), intermediate.getNode(target), probabilities[i], samples);

				print(intermediate, nodes, intermediacies[i], source, target, samples);
				Complexity.poc();
			}
			
			// writes the intermediacies of the nodes in the intermediate graph to a TSV file
			
			write(intermediate, probabilities, intermediacies, new File(filename).getParent());
		}
		
		Complexity.exit("INTERMEDIACY");
	}
	
	private static void parse(String[] args) {
		Options options = new Options();
		
		Option option = new Option("i", "input", true, "Pajek or TSV file (example: " + filename + ")");
		option.setRequired(true); option.setArgName("file");
		options.addOption(option);
		
		option = new Option("s", "source", true, "source node index (example: " + source + ")");
		option.setRequired(true); option.setArgName("source");
		options.addOption(option);
		
		option = new Option("t", "target", true, "target node index (example: " + target + ")");
		option.setRequired(true); option.setArgName("target");
		options.addOption(option);
		
		String p = ""; 
		for (int i = 0; i < probabilities.length; i++) 
			p += (i > 0? ",": "") + probabilities[i];
		option = new Option("p", "probability", true, "edge probability (default: " + p + ")");
		option.setRequired(false); option.setArgName("list");
		options.addOption(option);
		
		option = new Option("z", "samples", true, "Monte Carlo samples (default: " + samples + ")");
		option.setRequired(false); option.setArgName("samples");
		options.addOption(option);
		
		options.addOption(new Option("h", "help", false, "print help and usage message"));
		
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator<Option>() {
			@Override
			public int compare(Option first, Option second) {
				if (first.getOpt().equals("h") || second.getOpt().equals("h"))
					return first.getOpt().equals("h")? 1: -1;
				else if (first.isRequired() == second.isRequired())
					return first.getOpt().compareTo(second.getOpt());
				return first.isRequired()? -1: 1;
			}});
        
        CommandLine command;
        try {
        	command = new DefaultParser().parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp(USAGE, DESCRIPTION, options, "", true);

            System.exit(1);
            return;
        }
        
        if (command.hasOption("h")) {
        	formatter.printHelp(USAGE, DESCRIPTION, options, "", true);
        	
        	System.exit(0);
        }
        
        filename = command.getOptionValue("i");
        source = Integer.parseInt(command.getOptionValue("s"));
        target = Integer.parseInt(command.getOptionValue("t"));
        
        if (command.hasOption("z"))
        	samples = Integer.parseInt(command.getOptionValue("z"));
        if (command.hasOption("p")) {
        	String[] array = command.getOptionValue("p").split(",");
        	probabilities = new double[array.length];
        	for (int i = 0; i < array.length; i++)
				probabilities[i] = Double.parseDouble(array[i]);
        }
	}
	
	private static void print(String title) {
		System.out.println(String.format("\n%15s | %s\n", "...", title));
	}
	
	private static void print(double probability, int samples) {
		System.out.println(String.format("%15s | %.3f", "Probability", probability));
		System.out.println(String.format("%15s | %,d", "Samples", samples));
	}
	
	private static void print(Graph graph, Graph intermediate, int source, int target) {
		System.out.println(String.format("%15s | '%s'", "Network", graph.getName()));
		System.out.println(String.format("%15s | '%d'", "Source", source));   
		System.out.println(String.format("%15s | '%d'\n", "Target", target));
		
		System.out.println(String.format("%15s | %d (%d)", "Nodes", intermediate.getN(), graph.getN()));   
		System.out.println(String.format("%15s | %d (%d)", "Edges", intermediate.getM(), graph.getM()));
		System.out.println(String.format("%15s | %.3f (%.3f)", "Degree", 2.0 * intermediate.getM() / intermediate.getN(), 2.0 * graph.getM() / graph.getN()));
	}
	
	private static void print(Graph graph, List<Integer> nodes, final double[] intermediacy, int source, int target, int samples) {
		Collections.sort(nodes, new Comparator<Integer>() {
			@Override
			public int compare(Integer first, Integer second) {
				if (intermediacy[first] == intermediacy[second])
					return new Integer(first).compareTo(second);

				return -new Double(intermediacy[first]).compareTo(intermediacy[second]);
			}
		});
		
		source = graph.getNode(source);
		target = graph.getNode(target);
		
		System.out.println(String.format("\n%15s | %s", "Intermediacy", "..."));
		System.out.println(String.format("%15s | %.5f ± %.5f", "Source", intermediacy[source], Math.sqrt(intermediacy[source] * (1.0 - intermediacy[source]) / samples)));
		System.out.println(String.format("%15s | %.5f ± %.5f", "Target", intermediacy[target], Math.sqrt(intermediacy[target] * (1.0 - intermediacy[target]) / samples)));
		
		for (int i = 0; i < Math.min(10, nodes.size()); i++)
			System.out.println(String.format("%15s | %.5f ± %.5f", "'" + graph.getLabel(nodes.get(i)) + "'", intermediacy[nodes.get(i)], 1.96 * Math.sqrt(intermediacy[nodes.get(i)] * (1.0 - intermediacy[nodes.get(i)]) / samples)));
	}
	
	private static void write(Graph graph, double[] probabilities, double[][] intermediacies, String folder) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(folder + "/" + graph.getName() + "_phi.tsv"));
		
		writer.write("id\tin_degree\tout_degree");
		for (double probability: probabilities)
			writer.write("\tphi_" + probability);
		writer.write("\n");
		
		for (int i = 0; i < graph.getN(); i++) {
			writer.write("" + graph.getLabel(i));
			writer.write("\t" + graph.getPredecessors(i).length);
			writer.write("\t" + graph.getSuccessors(i).length);
			for (int j = 0; j < intermediacies.length; j++)
				writer.write("\t" + intermediacies[j][i]);
			writer.write("\n");
		}
		
		writer.flush();
		writer.close();
	}
	
}

/**
 * Methods for measuring the execution time and memory complexity of a program.
 * <p>
 * The method {@link #init(String)} starts measuring the time and memory complexity, and must be called at the beginning of a program.
 * The method {@link #exit(String)} prints out the overall complexity and must be called at the end of a program.
 * The method {@link #poc()} prints out the complexity from the last milestone and should be called at each milestone.
 * The method {@link #pic()} can be used to start a new milestone.
 * <p>
 * All public methods run in constant time {@literal O(1)}.
 * 
 * @author Lovro Šubelj
 *
 * @version 1.0.0
 */
class Complexity {
	
	public static void init(String label) {
		Complexity.pic(label); 
		
		System.out.println(String.format("\n%15s | %s\n", "...", label));   
		System.out.println(String.format("%15s | %s", "Date", new SimpleDateFormat().format(Calendar.getInstance().getTime())));
		
		Complexity.pic();
	}
	
	public static void exit(String label) {
		System.out.println(String.format("\n%15s | %s", "...", label)); 
		
		Complexity.poc(label);
		
		System.exit(0);
	}
	
	public static void pic() {
		pic("");
	}
	
	public static void poc() {
		poc("");
	}
	
	private static Map<String, Long> tics = new HashMap<String, Long>();
	
	private static Map<String, Long> mics = new HashMap<String, Long>();

	private static void pic(String label) {
		tic(label);
		mic(label);
	}

	private static void poc(String label) {
		System.out.println(String.format("\n%15s | " + format(toc(label)), "Time"));
		System.out.println(String.format("%15s | %.2f GB", "Memory", moc(label) / Math.pow(2.0, 30.0)));
		
		tic(label);
	}

	private static void tic(String label) {
		tics.put(label, System.currentTimeMillis());
	}
	
	private static long toc(String label) {
		if (!tics.containsKey(label))
			throw new IllegalArgumentException();
		
		return System.currentTimeMillis() - tics.get(label);
	}
	
	private static void mic(String label) {
		mics.put(label, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
	}
	
	private static long moc(String label) {
		if (!mics.containsKey(label))
			throw new IllegalArgumentException();
		
		return Math.max(0, Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - mics.get(label));
	}
	
	private static String format(long millis) {
		if (millis >= 60000)
			return String.format("%." + (millis >= 600000? 1: 2) + "f min", millis / 60000.0);
		
		return String.format("%." + (millis >= 10000? 1: 2) + "f sec", millis / 1000.0);
	}
	
}
