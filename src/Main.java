import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {
        //config variables:
        String[] animals = {"human", "mouse", "salmon", "zebrafish"};   //only used in multiple read mode
        String directory = "/home/dan/dev/instances/rnaseq";            //only used in multiple read mode
        String file = "/home/dan/dev/instances/rnaseq/human/1.graph";   //only used in single read mode
        String importMode = "multiple";                                 //either single or multiple

        if(importMode.equals("single")) {
            readGraphFile(file);
        }

        if(importMode.equals("multiple")) {
            readGraphFiles(directory, animals);
        }
    }

    /**
     * Parses the lines of a .graph file into a Network object
     * @param graphs - each row contains a String representation of a graph
     * @return
     */
    public static Network parseGraph(ArrayList<String> graphs) {
        Network network = new Network();

        for(String graph: graphs) {
            String[] lines = graph.split("\n");
            int numNodes = Integer.parseInt(lines[0]);
            System.out.println("***NEW GRAPH***");

            for(int i = 0; i < numNodes; i++) {
                //network.addNode();
            }
            System.out.println(numNodes + " nodes added!");

            for(int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(" ");
                int fromNode = Integer.parseInt(data[0]);
                int toNode = Integer.parseInt(data[1]);
                int weight = (int) Double.parseDouble(data[2]);
                //network.addEdge(fromNode, toNode, weight);
                System.out.println("Edge: "+fromNode+" -> "+toNode+": "+weight);
            }
        }

        return network;
    }

    /**
     * Reads in all graph files for selected animal subfolders
     */
    public static ArrayList<Network> readGraphFiles(String directory, String[] animals) {
        ArrayList<Network> graphs = new ArrayList<>();

        for(String animal: animals) {
            File dir = new File(directory+"/"+animal);
            File[] files = dir.listFiles();

            for (File file : files) {
                int pos = file.getName().lastIndexOf(".");
                String ext = file.getName().substring(pos+1);
                System.out.println(ext);
                if(!ext.equals("graph")) continue;
                String filePath = file.getPath();
                readGraphFile(filePath);
            }
        }

        return graphs;
    }

    /**
     * Parses a .graph file into individual graphs
     * @param file - path to the file
     * @return - ArrayList of Strings representing the nodes of a graph
     */
    public static Network readGraphFile(String file) {
        File inputFile = new File(file);
        Network network = new Network();
        ArrayList<String> graphs = new ArrayList<>();
        Scanner scan;

        try {
            scan = new Scanner(inputFile);
            scan.useDelimiter("#[\\s\\S]+?[\\n]");
            while(scan.hasNext()) {
                graphs.add(scan.next());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }

        //System.out.println(graphs.toString());
        parseGraph(graphs);

        return network;
    }

}
