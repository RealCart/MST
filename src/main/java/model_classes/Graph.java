package main.java.model_classes;

import java.util.*;

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

    public static class MSTResult {
        public List<Edge> mstEdges;
        public int totalCost;
        public long operationsCount;
        public double executionTimeMs;
    }

    public MSTResult computePrimMST() {
        MSTResult result = new MSTResult();
        result.mstEdges = new ArrayList<>();
        result.totalCost = 0;
        long ops = 0;
        long compOps = 0;

        Comparator<Edge> comp = new Comparator<>() {
            public int compare(Edge e1, Edge e2) {
                compOps++;
                return Integer.compare(e1.weight, e2.weight);
            }
        };
        PriorityQueue<Edge> pq = new PriorityQueue<>(comp);

        boolean[] visited = new boolean[nodes.size()];
        if (nodes.isEmpty()) {
            result.operationsCount = 0;
            result.executionTimeMs = 0.0;
            return result;
        }
        String startLabel = nodes.get(0);
        visited[nodeIndex.get(startLabel)] = true;
        int visitedCount = 1;

        long startTime = System.nanoTime();
        for (Edge e : adjacency.get(startLabel)) {
            ops++;
            String other = e.from.equals(startLabel) ? e.to : e.from;
            if (!visited[nodeIndex.get(other)]) {
                pq.add(e);
                ops++;
            }
        }

        while (!pq.isEmpty() && visitedCount < nodes.size()) {
            Edge edge = pq.poll();
            ops++;
            int uIndex = nodeIndex.get(edge.from);
            int vIndex = nodeIndex.get(edge.to);
            if (visited[uIndex] && visited[vIndex]) {
                ops++;
                continue;
            }
            result.mstEdges.add(edge);
            result.totalCost += edge.weight;
            ops++;
            int newIndex = visited[uIndex] ? vIndex : uIndex;
            visited[newIndex] = true;
            visitedCount++;

            String newNodeLabel = nodes.get(newIndex);
            for (Edge e : adjacency.get(newNodeLabel)) {
                ops++;
                String other = e.from.equals(newNodeLabel) ? e.to : e.from;
                int otherIndex = nodeIndex.get(other);
                if (!visited[otherIndex]) {
                    pq.add(e);
                    ops++;
                }
            }
        }
        long endTime = System.nanoTime();

        Collections.sort(result.mstEdges);
        result.operationsCount = ops + compOps;
        result.executionTimeMs = Math.round((endTime - startTime) / 1e4) / 100.0;
        return result;
    }
}