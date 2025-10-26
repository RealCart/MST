package test.java;


import main.java.Main;
import main.java.model_classes.Edge;
import main.java.model_classes.Graph;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;

public class MSTTest {
    private boolean isValidTree(List<Edge> edges, List<String> nodes) {
        int n = nodes.size();
        if (n == 0) {
            return edges.isEmpty();
        }
        if (edges.size() != n - 1) {
            return false;
        }
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indexMap.put(nodes.get(i), i);
        }
        int[] parent = new int[n];
        for (int i = 0; i < n; i++) parent[i] = i;

        int components = n;
        for (Edge e : edges) {
            int u = indexMap.get(e.from);
            int v = indexMap.get(e.to);
            while (parent[u] != u) u = parent[u];
            while (parent[v] != v) v = parent[v];
            if (u == v) {
                return false;
            }
            parent[u] = v;
            components--;
        }
        return components == 1;
    }

    @Test
    public void testMSTCorrectnessExample() {
        List<String> nodes = Arrays.asList("A", "B", "C", "D", "E");
        List<Main.EdgeInput> edges = Arrays.asList(
                new Main.EdgeInput("A", "B", 4),
                new Main.EdgeInput("A", "C", 3),
                new Main.EdgeInput("B", "C", 2),
                new Main.EdgeInput("B", "D", 5),
                new Main.EdgeInput("C", "D", 7),
                new Main.EdgeInput("C", "E", 8),
                new Main.EdgeInput("D", "E", 6)
        );
        Graph graph = new Graph(nodes, edges);
        Graph.MSTResult primRes = graph.computePrimMST();
        Graph.MSTResult kruskalRes = graph.computeKruskalMST();
        assertEquals(primRes.totalCost, kruskalRes.totalCost);
        assertEquals(16, primRes.totalCost);
        assertEquals(nodes.size() - 1, primRes.mstEdges.size());
        assertTrue(isValidTree(primRes.mstEdges, nodes));
        assertTrue(isValidTree(kruskalRes.mstEdges, nodes));
    }

    @Test
    public void testAlgorithmsOnTrivialGraphs() {
        Graph g1 = new Graph(Arrays.asList("X"));
        Graph.MSTResult prim1 = g1.computePrimMST();
        Graph.MSTResult kruskal1 = g1.computeKruskalMST();
        assertEquals(0, prim1.totalCost);
        assertEquals(0, prim1.mstEdges.size());
        assertEquals(0, kruskal1.totalCost);
        assertTrue(prim1.mstEdges.isEmpty() && kruskal1.mstEdges.isEmpty());

        Graph g2 = new Graph(Arrays.asList("A", "B"));
        g2.addEdge("A", "B", 5);
        Graph.MSTResult prim2 = g2.computePrimMST();
        Graph.MSTResult kruskal2 = g2.computeKruskalMST();
        assertEquals(5, prim2.totalCost);
        assertEquals(1, prim2.mstEdges.size());
        Edge e = prim2.mstEdges.get(0);
        assertTrue((e.from.equals("A") && e.to.equals("B")) || (e.from.equals("B") && e.to.equals("A")));
        assertEquals(prim2.totalCost, kruskal2.totalCost);
        assertEquals(prim2.mstEdges.size(), kruskal2.mstEdges.size());
        assertTrue(isValidTree(prim2.mstEdges, Arrays.asList("A", "B")));
    }

    @Test
    public void testPrimAndKruskalConsistency() {
        List<String> nodes = Arrays.asList("A", "B", "C", "D");
        Graph g = new Graph(nodes);
        g.addEdge("A", "B", 1);
        g.addEdge("B", "C", 2);
        g.addEdge("C", "D", 3);
        g.addEdge("A", "D", 4);
        Graph.MSTResult primRes = g.computePrimMST();
        Graph.MSTResult kruskalRes = g.computeKruskalMST();
        assertEquals(primRes.totalCost, kruskalRes.totalCost);
        assertEquals(primRes.mstEdges.size(), kruskalRes.mstEdges.size());
        assertEquals(nodes.size() - 1, primRes.mstEdges.size());
        assertTrue(isValidTree(primRes.mstEdges, nodes));
        Graph.MSTResult primRes2 = g.computePrimMST();
        Graph.MSTResult kruskalRes2 = g.computeKruskalMST();
        assertEquals(primRes.operationsCount, primRes2.operationsCount);
        assertEquals(kruskalRes.operationsCount, kruskalRes2.operationsCount);
        assertEquals(primRes.totalCost, primRes2.totalCost);
        assertEquals(kruskalRes.totalCost, kruskalRes2.totalCost);
    }
}
