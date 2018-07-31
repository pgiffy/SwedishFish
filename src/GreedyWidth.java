import java.util.ArrayList;

public class GreedyWidth {

    public ArrayList<Path> run(Network network) {
        ArrayList<Path> paths = new ArrayList<>();

        while(network.numEdges() > 0) {
            Path selectedPath = findFattestPath(network);
            network.reducePath(selectedPath);
            paths.add(selectedPath);
        }

        return paths;
    }

    private static Path findFattestPath(Network network) {
        //System.out.println(network.toString());
        ArrayList<Integer> sortedNodes = network.topoSort();
        int flow[] = new int[network.numNodes()];
        Edge edges[] = new Edge[network.numNodes()];
        for(int i = 0; i < flow.length; i++) {
            flow[i] = -1;
            edges[i] = null;
        }

        for(int u: sortedNodes) {
            for(Edge e: network.getNode(u).getOutgoingEdges()) {
                int v = e.getToNode().getId();
                int weight = e.getWeight();
                if((weight < flow[u] || flow[u] < 0) && weight >= flow[v]) {
                    flow[v] = weight;
                    edges[v] = e;
                } else if(flow[u] >= flow[v]) {
                    flow[v] = flow[u];
                    edges[v] = e;
                }
            }
        }

        ArrayList<Edge> pathEdges = new ArrayList<>();
        Edge e = edges[edges.length-1];
        while(e != null) {
            pathEdges.add(e);
            e = edges[e.getFromNode().getId()];
        }

        return new Path(pathEdges);
    }
}
