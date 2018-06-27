import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {

        //config variables:
        String[] animals = {"human", "mouse", "salmon", "zebrafish"};   //only used in multiple read mode
        String directory = "/home/dan/dev/instances/rnaseq";            //only used in multiple read mode
        String file = "/home/dan/dev/instances/rnaseq/human/1.graph";   //only used in single read mode
        String truthFile = "/home/dan/dev/instances/rnaseq/human/1.truth"; //only used in single read mode
        //String directory = "/home/peter/Desktop/instances/rnaseq";
        //String file = "/home/peter/Desktop/instances/rnaseq/test/1.graph";         //either single or multiple
        String importMode = "single";                                   //either single or multiple



        ArrayList<Network> networks;
        if(importMode.equals("single")) {
            PrintWriter out = null;
            int numSuccess = 0;
            int numFail = 0;
            try {
                out = new PrintWriter(new File("outputFile.txt"));
                networks = readGraphFile(file);
                ArrayList<Integer> numTruthPaths = readTruthFile(truthFile);
                System.out.println(numTruthPaths.toString());
                int count = 0;
                for(Network network: networks) {
                    out.println("Graph # " + count);
                    ArrayList<Path> paths = new ArrayList<>();
                    //network.collapseEdges();
                    //network.printDetails(out);
                    int numPaths = findPaths(network, paths, out);
                    out.println("# Truth Paths = " + numTruthPaths.get(count) + "\t # Actual Paths = " + numPaths);
                    if(numPaths <= numTruthPaths.get(count)) {
                        out.println("SUCCESS");
                        numSuccess++;
                    } else {
                        out.println("FAIL");
                        numFail++;
                    }
                    out.println();
                    count++;
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file.");
                e.printStackTrace();
            } finally {
                out.close();
            }

            double successPercent = ((double)numSuccess / (numSuccess+numFail) * 100);
            System.out.println("Success: " + numSuccess + " ("+successPercent+"%) \t Fail: " + numFail);
        }

        if(importMode.equals("multiple")) {
            PrintWriter out = null;
            try {
                out = new PrintWriter(new File("outputFile.txt"));
                networks = readGraphFiles(directory, animals);
                for(Network network: networks) {
                    out.println("**********************************");
                    network.printDetails(out);
                }
            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file.");
                e.printStackTrace();
            } finally {
                out.close();
            }
        }
    }

    /**
     * Parses the lines of a .graph file into a Network object
     * @param graphs - each row contains a String representation of a graph
     * @return
     */
    public static ArrayList<Network> parseGraph(ArrayList<String> graphs) {
        ArrayList<Network> networks = new ArrayList<>();
        for(String graph: graphs) {
            Network network = new Network();
            String[] lines = graph.split("\n");
            int numNodes = Integer.parseInt(lines[0]);
            //System.out.println("***NEW GRAPH***");

            for(int i = 0; i < numNodes; i++) {
                network.addNode();
            }
            //System.out.println(numNodes + " nodes added!");

            for(int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(" ");
                Node fromNode = network.getNode(Integer.parseInt(data[0]));
                Node toNode = network.getNode(Integer.parseInt(data[1]));
                int weight = (int) Double.parseDouble(data[2]);
                network.addEdge(fromNode, toNode, weight);
                //System.out.println("Edge: "+fromNode+" -> "+toNode+": "+weight);
            }
            networks.add(network);
        }

        return networks;
    }

    public static ArrayList<Integer> readTruthFile(String file) {
        File inputFile = new File(file);
        ArrayList<Integer> numTruthPaths = new ArrayList<>();
        Scanner scan;

        try {
            scan = new Scanner(inputFile);
            scan.useDelimiter("#[\\s\\S]+?[\\n]"); //splits into graphs by # XXX
            while(scan.hasNext()) {
                String[] lines = scan.next().split("\n");
                numTruthPaths.add(lines.length);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }

        return numTruthPaths;
    }

    /**
     * Reads in all graph files for selected animal subfolders
     */
    public static ArrayList<Network> readGraphFiles(String directory, String[] animals) {
        ArrayList<Network> networks = new ArrayList<>();

        for(String animal: animals) {
            File dir = new File(directory+"/"+animal);
            File[] files = dir.listFiles();

            for (File file : files) {
                int pos = file.getName().lastIndexOf(".");
                String ext = file.getName().substring(pos+1);
                //System.out.println(ext);
                if(!ext.equals("graph")) continue;
                String filePath = file.getPath();
                networks.addAll(readGraphFile(filePath));
            }
        }

        return networks;
    }

    /**
     * Parses a .graph file into individual networks
     * @param file - path to the file
     * @return - list of networks after running on parseGraph()
     */
    public static ArrayList<Network> readGraphFile(String file) {
        File inputFile = new File(file);
        ArrayList<Network> networks;
        ArrayList<String> graphs = new ArrayList<>();
        Scanner scan;

        try {
            scan = new Scanner(inputFile);
            scan.useDelimiter("#[\\s\\S]+?[\\n]"); //splits into graphs by # XXX
            while(scan.hasNext()) {
                graphs.add(scan.next());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }

        //System.out.println(graphs.toString());
        //System.out.print(".");
        networks = parseGraph(graphs);

        return networks;
    }

    public static int findPaths(Network network, ArrayList<Path> paths, PrintWriter out) {

        while(network.numEdges() > 0) {
            int[] area = new int[network.numNodes()];
            Edge[] selectedEdges = new Edge[network.numNodes() - 1];
            int pathLength = 1;

            for (int i = 0; i < area.length; i++) area[i] = -1;
            for (int i = 0; i < selectedEdges.length; i++) selectedEdges[i] = null;
            area[0] = 0;

            for (int nodeId : network.topoSort()) {
                Node n = network.getNode(nodeId);
                for (Edge e : n.getOutgoingEdges()) {
                    int outgoingId = e.getToNode().getId();
                    int weight = e.getWeight();
                    int nodeArea = weight * pathLength;
                    if (nodeArea + area[nodeId] > area[outgoingId]) {
                        area[outgoingId] = nodeArea + area[nodeId];
                        selectedEdges[nodeId] = e;
                    }
                }
                pathLength++;
            }

            ArrayList<Edge> selectedEdges2 = new ArrayList<>();
            for(Edge e: selectedEdges) {
                if(e != null) selectedEdges2.add(e);
            }

            Path path = new Path(selectedEdges2);
            paths.add(path);
            network.reducePath(path);
            //System.out.println(network.toString());
        }

        //System.out.println(Arrays.toString(area));
        //System.out.println(Arrays.toString(selectedEdges));

        return paths.size();
    }
}
