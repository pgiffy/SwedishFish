import java.util.*;

public class GreedyWidthExtended {

    public ArrayList<Path> run(Network network, boolean debug) {
        ArrayList<Path> paths = new ArrayList<>();

        network.assignEdgeLetters();
        network.collapseEdges();
        paths.clear();

        while(network.numEdges() > 0) {
            Path toReduce = findFattestPath(network);
            paths.add(toReduce);
            network.reducePath(toReduce);
        }

        return paths;

    }

    private static Path findMaxEdgeRemovePath(Network network, ArrayList<Integer> predictedTruthWeights) {
        ArrayList<Integer> sortedNodes = network.topoSort();
        ArrayList[] data = new ArrayList[network.numNodes()];
        for(int i = 0; i < data.length; i++) {
            data[i] = new ArrayList<HashMap<String, Object>>();
        }

        ArrayList<Edge> itemEdgeList = new ArrayList<>();
        for(int nodeId : sortedNodes) {
            ArrayList<HashMap<String, Object>> thisNodeData = data[nodeId];
            for(Edge e : network.getNode(nodeId).getOutgoingEdges()) {
                int toNodeId = e.getToNode().getId();

                //add new edge flow for each outgoing edge from 0
                if(nodeId == 0) {
                    ArrayList<Edge> newEdgeList = new ArrayList<>();
                    newEdgeList.add(e);
                    HashMap<String, Object> newItem = new HashMap<>();
                    newItem.put("weight", e.getWeight());
                    newItem.put("numEdges", 1);
                    newItem.put("edgeList", newEdgeList);
                    data[toNodeId].add(newItem);
                    continue;
                }

                //copy all flows that can be carried by current edge
                for(HashMap<String, Object> dataItem : thisNodeData) {
                    int itemWeight = (int) dataItem.get("weight");
                    int itemNumEdges = (int) dataItem.get("numEdges");
                    itemEdgeList.clear();
                    if(dataItem.get("edgeList") instanceof ArrayList) {
                        itemEdgeList.addAll((List) dataItem.get("edgeList"));
                    } else {
                        itemEdgeList = new ArrayList<>();
                    }

                    HashMap<String, Object> newItem = new HashMap<>();
                    if(itemWeight <= e.getWeight()) {
                        int itemNumEdges_new = itemNumEdges;
                        if(itemWeight == e.getWeight()) itemNumEdges_new = itemNumEdges + 1;
                        itemEdgeList.add(e);
                        ArrayList<Edge> newItemEdgeList = new ArrayList<>();
                        newItemEdgeList.addAll(itemEdgeList);
                        newItem.put("weight", itemWeight);
                        newItem.put("numEdges", itemNumEdges_new);
                        newItem.put("edgeList", newItemEdgeList);
                    } else {
                        itemEdgeList.add(e);
                        ArrayList<Edge> newItemEdgeList = new ArrayList<>();
                        newItemEdgeList.addAll(itemEdgeList);
                        itemEdgeList.clear();
                        newItem.put("weight", e.getWeight());
                        newItem.put("numEdges", 1);
                        newItem.put("edgeList", newItemEdgeList);
                    }

                    data[toNodeId].add(newItem);
                }

            }

            //System.out.println(nodeId+": "+thisNodeData.toString());
        }


        //find path that will remove largest # edges
        int lastNodeId = network.numNodes()-1;
        int maxNumEdges = -1;
        int maxWeight = -1;
        Path selectedPath = null;
        for(Object item : data[lastNodeId]) {
            HashMap<String, Object> item2 = (HashMap<String, Object>) item;
            int numEdges = (int) item2.get("numEdges");
            int weight = (int) item2.get("weight");
            if(numEdges > maxNumEdges) {
                maxNumEdges = numEdges;
                maxWeight = weight;
                //selectedPath = new Path((ArrayList) item2.get("edgeList"));
            }
        }

        //Break ties by choosing the path with the least amount of uncertainty
        HashMap<String, Integer> duplicates = new HashMap<>();
        for(Object item : data[lastNodeId]) {
            HashMap<String, Object> item2 = (HashMap<String, Object>) item;
            String id = item2.get("numEdges")+"-"+item2.get("weight");
            if((int) item2.get("numEdges") < maxNumEdges) continue;
            if(duplicates.get(id) == null) {
                duplicates.put(id, 1);
            } else {
                int prevVal = duplicates.get(id);
                duplicates.put(id, prevVal+1);
            }
        }

        //System.out.println(duplicates.toString());

        int minDuplicates = -1;
        ArrayList<String> idList = new ArrayList<>();
        for(Map.Entry<String, Integer> item : duplicates.entrySet()) {
            if(item.getValue() <= minDuplicates || minDuplicates < 0) {
                minDuplicates = item.getValue();
                idList.add(item.getKey());
            }
        }

        ArrayList<Path> possiblePaths = new ArrayList<>();
        for(String id : idList) {

            //find the paths with the selected ID
            for(Object item : data[lastNodeId]) {
                HashMap<String, Object> item2 = (HashMap<String, Object>) item;
                String testId = item2.get("numEdges")+"-"+item2.get("weight");
                if(testId.equals(id)) {
                    possiblePaths.add(new Path((ArrayList) item2.get("edgeList")));
                }
            }
        }

        //System.out.println(possiblePaths.toString());
        //System.out.println(predictedTruthWeights.toString());

        //1.5 tie-breaker: take paths that match a predicted truth weight
        ArrayList<Path> possiblePaths1 = new ArrayList<>();
        for(Path p : possiblePaths) {
            if(predictedTruthWeights.contains(p.getWeight())) {
                possiblePaths1.add(p);
            }
        }

        if(possiblePaths1.isEmpty()) {
            possiblePaths1.addAll(possiblePaths);
        }

        //2nd tie-breaker: take the path with the least number of predicted truth weights
        // (that aren't the path weight)

        int minNumTruthEdges = -1;
        ArrayList<Path> possiblePaths2 = new ArrayList<>();
        for(Path p : possiblePaths1) {
            int pathWeight = p.getWeight();
            int numTruthEdges = 0;
            for(Edge e : p.getEdges()) {
                if(e.getWeight() != pathWeight && predictedTruthWeights.contains(e.getWeight())) {
                    numTruthEdges++;
                }
            }

            if(numTruthEdges <= minNumTruthEdges || minNumTruthEdges < 0) {
                if(numTruthEdges < minNumTruthEdges) possiblePaths2.clear();
                minNumTruthEdges = numTruthEdges;
                possiblePaths2.add(p);
            }
        }

        //3rd tie-breaker: take the path that has the least number of distinct edge weights
        /*
        ArrayList<Integer> foundWeights = new ArrayList<>();
        ArrayList<Path> possiblePaths3 = new ArrayList<>();
        int minNumFoundWeights = -1;
        System.out.println("***");
        for(Path p : possiblePaths2) {
            foundWeights.clear();
            for(Edge e : p.getEdges()) {
                if(!foundWeights.contains(e.getWeight())) {
                    foundWeights.add(e.getWeight());
                }
            }

            if(foundWeights.size() <= minNumFoundWeights || minNumFoundWeights < 0) {
                if(foundWeights.size() != minNumFoundWeights) possiblePaths3.clear();
                possiblePaths3.add(p);
                minNumFoundWeights = foundWeights.size();
            }
        }
        */

        //4th tie-breaker: take the path that contains the least number of truth-weights *after subtracting
        //each individual truth weight
        int minCount = -1;
        if(possiblePaths2.size() > 1) {
            for(Path p : possiblePaths2) {
                int count = 0;
                for(Edge e : p.getEdges()) {
                    for (int truthWeight : predictedTruthWeights) {
                        if(truthWeight == p.getWeight()) continue;
                        int testWeight = e.getWeight() - truthWeight;
                        if(predictedTruthWeights.contains(testWeight) && p.getWeight() != testWeight) count++;
                    }
                }

                if(count < minCount || minCount < 0) {
                    selectedPath = p;
                    minCount = count;
                }
            }
        } else {
            selectedPath = possiblePaths2.get(0);
        }

        //System.out.println(selectedPath.getEdges().toString());


        return selectedPath;
    }

    private static Path findFattestPath(Network network) {
        //System.out.println(network.toString());
        ArrayList<Integer> sortedNodes = network.topoSort();
        int flow[] = new int[network.numNodes()];
        Edge edges[] = new Edge[network.numNodes()];

        for (int i = 0; i < flow.length; i++) {
            flow[i] = -1;
            edges[i] = null;
        }

        for (int u : sortedNodes) {
            for (Edge e : network.getNode(u).getOutgoingEdges()) {
                int v = e.getToNode().getId();
                int weight = e.getWeight();
                if (weight < flow[u] || flow[u] < 0) {
                    if (weight >= flow[v]) {
                        flow[v] = weight;
                        edges[v] = e;
                    }
                } else {
                    if (flow[u] >= flow[v]) {
                        flow[v] = flow[u];
                        edges[v] = e;
                    }
                }
            }
        }

        ArrayList<Edge> pathEdges = new ArrayList<>();
        Edge e = edges[edges.length - 1];
        while (e != null) {
            pathEdges.add(e);
            e = edges[e.getFromNode().getId()];
        }
        return new Path(pathEdges);

    }

    private static ArrayList<Integer> getPredictedTruthWeights(Network network){
        ArrayList[] stackHolder = new ArrayList[network.numNodes()];
        for(int i = 0; i < stackHolder.length; i++){
            stackHolder[i] = new ArrayList<Integer>();
        }

        ArrayList<Edge> edgeList = network.getEdges();
        for(Edge e: edgeList){
            int start = e.getFromNode().getId();
            int end = e.getToNode().getId();
            for(int i = start; i < end; i++) stackHolder[i].add(e.getWeight());

        }
        int largestSize = 0;
        for(int i = 0; i < stackHolder.length; i++){
            if(stackHolder[i].size()>largestSize){
                largestSize = stackHolder[i].size();
            }
        }

        //holds all the values held by biggest stacks
        ArrayList<ArrayList<Integer>> allBiggest = new ArrayList<>();
        for(int i = 0; i < stackHolder.length; i++){
            if(stackHolder[i].size() == largestSize){
                allBiggest.add(stackHolder[i]);
            }
        }

        // allBiggest now holds all of the greatest size paths.
        if(allBiggest.size() == 1) return allBiggest.get(0);

        //this makes it a little better by picking the list with the lowest highest number
        int smallestLargest = 100000000;
        ArrayList<Integer> bestList = new ArrayList<>();
        for(ArrayList<Integer> list: allBiggest){
            Collections.sort(list);
            Collections.reverse(list);
            if(list.get(0) < smallestLargest){
                smallestLargest = list.get(0);
                bestList = list;
            }
        }

        return bestList;

    }


}
