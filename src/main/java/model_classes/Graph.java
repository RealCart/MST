package main.java.model_classes;

import java.util.*;

public class Graph {
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