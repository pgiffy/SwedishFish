import java.util.*;
public class Path {
    private ArrayList<Edge> edges;
    private HashMap<Integer, Integer> weightFreq;
    private int weight;
    private int area;
    private int flow;

    public Path(ArrayList<Edge> newEdges) {
        edges = new ArrayList<>();
        int minWeight = -1;
        for(Edge e: newEdges) {
            edges.add(e);
            if(e.getWeight() < minWeight || minWeight < 0) {
                minWeight = e.getWeight();
            }
        }

        weight = minWeight;
        flow = minWeight;
        area = weight * edges.size();
        weightFreqCalc();
    }

    private void weightFreqCalc() {
        weightFreq = new HashMap<>();
        for(Edge e: edges) {
            int weight = e.getWeight();
            if(weightFreq.get(weight) == null) {
                weightFreq.put(weight, 1);
            } else {
                int oldFreq = weightFreq.get(weight);
                weightFreq.put(weight, oldFreq + 1);
            }
        }
    }

    public HashMap<Integer, Integer> getWeightFreq() {
        return weightFreq;
    }

    public int largestFreqWeight() {
        int maxFreq = -1;
        int maxWeight = -1;
        for(Map.Entry<Integer,Integer> entry: weightFreq.entrySet()) {
            if(entry.getValue() > maxFreq) {
                maxFreq = entry.getValue();
                maxWeight = entry.getKey();
            }
        }

        return maxWeight;
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public void replaceEdge(Edge e, ArrayList<Edge> newEdges) {
        edges.remove(e);
        System.out.println("remove " + e.toString());
        for(Edge newEdge : newEdges) {
            System.out.println(edges.toString());
            if(!listContainsEdge(edges, newEdge)) {
                System.out.println("add " + newEdge.toString());
                edges.add(newEdge);
            }
            System.out.println();
        }
    }

    private boolean listContainsEdge(ArrayList<Edge> edgeList, Edge edge) {
        int weight = edge.getWeight();
        Node toNode = edge.getToNode();
        Node fromNode = edge.getFromNode();
        String label = edge.getLabel();

        for(Edge e : edgeList) {
            if(e.getLabel().equals(label)) {
                System.out.println("edge found: " + e.toString());
                return true;
            }
        }

        return false;
    }

    public int getFlow() {return flow;}

    public int getArea() {return area;}

    public int getWeight() {
        return weight;
    }

    public String toString() {
        String str = "[PATH weight="+weight+" area="+area+" ";
        for(Edge e: edges) {
            str += e.toString()+" ";
        }
        str += "]";
        return str;
    }

}
