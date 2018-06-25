import java.util.*;
public class Path {
    private ArrayList<Node> nodes;
    private int weight;
    private int area;
    public Path(ArrayList<Node> newPath, int pathWeight){
        nodes = newPath;
        weight = pathWeight;
        area = nodes.size()*weight;
    }

    public Path(Node[] newPath, int pathWeight) {
        nodes = new ArrayList<>();
        for(Node node: newPath) nodes.add(node);
        weight = pathWeight;
        area = nodes.size()*weight;
    }

    public int pathLength(){ return nodes.size();}

    public void setWeight(int newWeight) {
        weight = newWeight;
        area = nodes.size() * weight;
    }

    public ArrayList<Node> getNodes(){ return nodes;}

    public int getArea(){ return area; }
    public int getWeight(){ return weight; }

    public String print(){
        System.out.println(nodes.toString());
        String str;
        str = "Weight = " + weight + " Area = " + area;
        str += "\n";
        for(Node node: nodes){
            str += node.getId() + "->";
        }

        return str;
    }
}
