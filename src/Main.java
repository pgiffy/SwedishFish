
import java.util.*;
import java.io.*;

public class Main {

    public static void main(String args[]) {

            String directory = "/home/dan/dev/instances/rnaseq/";
            String animal = "test";
            boolean debug = true;

            ArrayList<Network> networks;
            PrintWriter out = null;
            int[] resultBinsGW = new int[101];
            int[] resultBinsSF = new int[101];
            int[] resultBinsGER = new int[101];
            int[] totals = new int[101];
            ArrayList<Path> paths = new ArrayList<>();

            try {

                out = new PrintWriter(new File("outputFile.txt"));
                File dir = new File(directory + "/" + animal);
                File[] files = dir.listFiles();
                if(files == null) throw new NullPointerException();

                for (int i = 0; i < 101; i++) {
                    resultBinsGW[i] = 0;
                    resultBinsSF[i] = 0;
                    resultBinsGER[i] = 0;
                    totals[i] = 0;
                }

                for (File curFile : files) {
                    int pos = curFile.getName().lastIndexOf(".");
                    String ext = curFile.getName().substring(pos + 1);
                    String filenameNoExt = curFile.getName().substring(0, pos);
                    String filename = curFile.getName();
                    if (ext.equals("graph")) {
                        networks = readGraphFile(directory + "/" + animal + "/" + filename);
                        ArrayList<Integer> numTruthPaths = readTruthFile(directory + "/" + animal + "/" + filenameNoExt + ".truth");
                        for (int num : numTruthPaths) totals[num - 1]++;
                        System.out.print(".");
                        if(debug) out.printf("File: %s\n", filename);

                        GreedyWidth greedyWidth = new GreedyWidth();
                        SwedishFish swedishFish = new SwedishFish();
                        GreedyEdgeRemove greedyEdgeRemove = new GreedyEdgeRemove();

                        int count = 0;
                        for (Network network : networks) {

                            Network network1 = new Network(network);
                            Network network2 = new Network(network);
                            Network network3 = new Network(network);
                            ArrayList<Path> greedyWidthPaths = greedyWidth.run(network1);
                            ArrayList<Path> swedishFishPaths = swedishFish.run(network2, debug);
                            ArrayList<Path> greedyEdgeRemovePaths = greedyEdgeRemove.run(network3, debug);

                            int truthPaths = numTruthPaths.get(count);
                            if(debug) out.printf("%d \t # Truth Paths = %d \t # GreedyWidth Paths = %d \t # SwedishFish Paths = %d \t # GER Paths = %d \n",
                                    count, truthPaths, greedyWidthPaths.size(), swedishFishPaths.size(), greedyEdgeRemovePaths.size());

                            if (greedyWidthPaths.size() <= truthPaths) resultBinsGW[truthPaths - 1]++;
                            if (swedishFishPaths.size() <= truthPaths) resultBinsSF[truthPaths - 1]++;
                            if (greedyEdgeRemovePaths.size() <= truthPaths) resultBinsGER[truthPaths - 1]++;
                            count++;
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                System.out.println("Could not open output file.");
                e.printStackTrace();
            } catch (NullPointerException e) {
                System.out.println("Could not find any files in chosen directory.");
                e.printStackTrace();
            } finally {
                if(out != null) out.close();
            }


        //print paths to graphviz files
        //recommended to only run on 1 graph at a time
        if(debug) {
            ArrayList<Path> expandedPaths = new ArrayList<>();
            for (Path p : paths) {
                ArrayList<Edge> expandedEdgeList = new ArrayList<>();
                for (Edge e : p.getEdges()) {
                    expandedEdgeList.addAll(e.getRemovedEdges());
                }
                Path expandedPath = new Path(expandedEdgeList);
                expandedPaths.add(expandedPath);
            }

            for (Path p : expandedPaths) {
                System.out.println(p.toString());
            }
        }
        // print final results to console
        System.out.printf("Results for %s\n\n", animal);
        System.out.printf("\n # Paths \t Success Rate GW \t Success Rate SF \t Success Rate GER \n");

        for (int i = 0; i < 10; i++) {
            double successRateGW = ((double) resultBinsGW[i] / totals[i]) * 100;
            double successRateSF = ((double) resultBinsSF[i] / totals[i]) * 100;
            double successRateGER = ((double) resultBinsGER[i] / totals[i]) * 100;
            System.out.printf("%7d %19.2f %19.2f %19.2f\n", i + 1, successRateGW, successRateSF, successRateGER);
        }
    }

    public static ArrayList<Network> parseGraph(ArrayList<String> graphs) {
        ArrayList<Network> networks = new ArrayList<>();
        for (String graph : graphs) {
            Network network = new Network();
            String[] lines = graph.split("\n");
            int numNodes = Integer.parseInt(lines[0]);
            for (int i = 0; i < numNodes; i++) network.addNode();
            for (int i = 1; i < lines.length; i++) {
                String[] data = lines[i].split(" ");
                Node fromNode = network.getNode(Integer.parseInt(data[0]));
                Node toNode = network.getNode(Integer.parseInt(data[1]));
                int weight = (int) Double.parseDouble(data[2]);
                network.addEdge(fromNode, toNode, weight);
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
            while (scan.hasNext()) {
                String[] lines = scan.next().split("\n");
                numTruthPaths.add(lines.length);
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }
        return numTruthPaths;
    }

    public static ArrayList<Network> readGraphFile(String file) {
        File inputFile = new File(file);
        ArrayList<Network> networks;
        ArrayList<String> graphs = new ArrayList<>();
        Scanner scan;
        try {
            scan = new Scanner(inputFile);
            scan.useDelimiter("#[\\s\\S]+?[\\n]"); //splits into graphs by # XXX
            while (scan.hasNext()) {
                graphs.add(scan.next());
            }
        } catch (FileNotFoundException e) {
            System.out.println("Could not open file: " + inputFile.toString());
            e.printStackTrace();
        }
        networks = parseGraph(graphs);
        return networks;
    }
}