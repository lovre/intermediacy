## Intermediacy of the nodes in a graph

The repository includes Java program for computing the intermediacy of the nodes in a graph for the specified source and target nodes. The intermediacy is defined as the probability that a node is located on a directed path between the source and target nodes whereby each edge of the graph is sampled independently with some probability. 

Particularly, the intermediacy was developed to estimate the importance of scientific publications based on a network of citations between these publications. Given two publications dealing with a specific research topic, an older publication and a more recent one, intermediacy can be used to identify publications that appear to play a major role in the intellectual development from the older to the more recent publication. These are publications that, based on citation links, are important in connecting the older and the more recent publication.

The measure of intermediacy is fully described in a journal paper linked below.

Šubelj, Waltman, Traag & Van Eck, ''[Intermediacy of publications](http://dx.doi.org/10.1098/rsos.190207)'', _Royal Society Open Science_ **7**(1), 190207 (2020).

### Command line usage

The implementation has no dependencies and does not require any compiling. It can be run as:

`java -jar Intermediacy.jar -i <file> -s <source> -t <target> [-p <list>] [-z <samples>] [-h]`

The command line arguments are as follows:

`-i,--input <file>` Pajek or TSV file (example: 'nets/toy.net')

`-s,--source <source>` source node index (example: '1')

`-t,--target <target>` target node index (example: '2')

`-p,--probability <list>` edge probability (default: '0.3,0.5,0.7')

`-z,--samples <samples>` Monte Carlo samples (default: '100000')

`-h,--help` prints help and usage message

### Details of the implementation

The implementation first parses the command line arguments as described above.

The implementation then reads a graph from the specified Pajek or TSV file. If the filename ends with the extension `.net`, the Pajek method is called. Otherwise, the TSV method is called. Next, the graph is reduced to the intermediate graph consisting only of nodes located on a directed path between the source and target nodes, since the intermediacy of the remaining nodes equals zero by definition.

For each of the specified probabilities of the edges in the graph, the intermediacy of the nodes is approximated by Monte Carlo sampling. The implementation approximates the intermediacy using the specified number of Monte Carlo samples $z$ with the standard error $\sqrt{\phi(1-\phi)/z}$, where $\phi$ is the intermediacy of a node.


Finally, the intermediacies of the nodes for each of the specified probabilities are written to a TSV file. Only the nodes in the intermediate graph are included in the TSV file. The file is put in the same folder as the graph file, while the filename is set to the name of the graph followed by the extension `_phi.tsv`.

The implementation runs in time $\mathcal{O}(pzm)$, where $m$ is the number of edges in the intermediate graph, $z$ is the number of Monte Carlo samples and $p$ is the number of the specified probabilities.

The source code of the implementation is located in folder `src`.

### Example input and output

Example Pajek input file included as `nets/toy.net`:

```
*vertices 5
1 "s"
2 "u"
3 "v"
4 "w"
5 "t"
*arcs
1 2
1 3
2 3
2 4
3 5
4 3
4 5
```

Example command line usage of the implementation:

```
java -jar Intermediacy.jar -i nets/toy.net -s 1 -t 5
```

Example command line printout of the execution:

```

            ... | INTERMEDIACY

           Date | 8/10/18 10:31 PM

            ... | NETWORK

        Network | 'toy'
         Source | '1'
         Target | '5'

          Nodes | 5 (5)
          Edges | 7 (7)
         Degree | 2.800 (2.800)

           Time | 0.01 sec
         Memory | 0.00 GB

            ... | MONTE CARLO

    Probability | 0.300
        Samples | 100,000

   Intermediacy | ...
         Source | 0.13440 ± 0.00108
         Target | 0.13440 ± 0.00108
            '3' | 0.11295 ± 0.00196
            '2' | 0.05589 ± 0.00142
            '4' | 0.03245 ± 0.00110

           Time | 0.19 sec
         Memory | 0.05 GB

            ... | MONTE CARLO

    Probability | 0.500
        Samples | 100,000

   Intermediacy | ...
         Source | 0.39649 ± 0.00155
         Target | 0.39649 ± 0.00155
            '3' | 0.32659 ± 0.00291
            '2' | 0.23282 ± 0.00262
            '4' | 0.15583 ± 0.00225

           Time | 0.08 sec
         Memory | 0.06 GB

            ... | MONTE CARLO

    Probability | 0.700
        Samples | 100,000

   Intermediacy | ...
         Source | 0.72411 ± 0.00141
         Target | 0.72411 ± 0.00141
            '3' | 0.61524 ± 0.00302
            '2' | 0.53938 ± 0.00309
            '4' | 0.41563 ± 0.00305

           Time | 0.09 sec
         Memory | 0.01 GB

            ... | INTERMEDIACY

           Time | 0.40 sec
         Memory | 0.01 GB
         
```

Example TSV output file included as `nets/toy_phi.tsv`:

```
id	in_degree	out_degree	phi_0.3	phi_0.5	phi_0.7
1	0	2	0.1344	0.39649	0.72411
2	1	2	0.05589	0.23282	0.53938
3	3	1	0.11295	0.32659	0.61524
4	1	2	0.03245	0.15583	0.41563
5	2	0	0.1344	0.39649	0.72411
```
