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
            if(e.getWeight() < minWeight || minWeight < 0) minWeight = e.getWeight();
        }
        weight = minWeight;
        flow = minWeight;
        area = weight * edges.size();
        weightFreqCalc();
    }

    public Path(ArrayList<Edge> newEdges, int k) {
        edges = new ArrayList<>();
        flow = k;
        int minWeight = -1;
        for(Edge e: newEdges) {
            edges.add(e);
            if(e.getWeight() < minWeight || minWeight < 0) minWeight = e.getWeight();
        }
        weight = minWeight;
        area = weight * edges.size();
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

    public ArrayList<Edge> getEdges() { return edges; }

    public int getWeight() { return weight; }

    public String toString() {
        String str = "[PATH weight="+weight+" area="+area+" ";
        for(Edge e: edges) str += e.toString()+" ";
        str += "]";
        return str;
    }

}
