package main.java.model_classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private final List<String> nodes;
    private final Map<String, List<Edge>> adjacency;
    private final List<Edge> edges;
    private final Map<String, Integer> nodeIndex;  // maps node label to index for internal use

    public Graph(List<String> nodes) {
        this.nodes = new ArrayList<>(nodes);
        this.adjacency = new HashMap<>();
        this.edges = new ArrayList<>();
        this.nodeIndex = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            String label = nodes.get(i);
            nodeIndex.put(label, i);
            adjacency.put(label, new ArrayList<>());
        }
    }

    public Graph(List<String> nodes, List<GraphInput.EdgeInput> edgeList) {
        this(nodes);
        for (GraphInput.EdgeInput e : edgeList) {
            addEdge(e.from, e.to, e.weight);
        }
    }

    public void addEdge(String from, String to, int weight) {
        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);
        adjacency.get(to).add(edge);
    }

    public int getVertexCount() {
        return nodes.size();
    }

    public int getEdgeCount() {
        return edges.size();
    }

}