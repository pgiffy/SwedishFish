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

    public int pathLength(){ return nodes.size();}

    public void setWeight(int newWeight) {
        weight = newWeight;
        area = nodes.size() * weight;
    }

    public ArrayList<Node> getNodes(){ return nodes;}

    public int getArea(){ return area; }
    public int getWeight(){ return weight; }

    public void print(){
        System.out.println("Weight = " + weight + " Area = " + area);
        for(int i = 0; i < nodes.size(); i++){
            System.out.print(nodes.get(i).getId() + " ");
        }
    }
}
