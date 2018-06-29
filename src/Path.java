import java.util.*;
public class Path {
    private ArrayList<Edge> edges;
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
        area = weight * edges.size();
    }

    public Path(Edge[] newEdges, int k) {
        edges = new ArrayList<>();
        flow = k;
        int minWeight = -1;
        for(Edge e: newEdges) {
            edges.add(e);
            if(e.getWeight() < minWeight || minWeight < 0) {
                minWeight = e.getWeight();
            }
        }

        weight = minWeight;
        area = weight * edges.size();
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public int getFlow() {return flow;}

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
