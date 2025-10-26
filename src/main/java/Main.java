package main.java;

import com.google.gson.Gson;
import main.java.model_classes.Edge;
import main.java.model_classes.Graph;

import java.io.*;
import java.util.*;

public class Main {
    static class GraphInput {
        public int id;
        public List<String> nodes;
        public List<EdgeInput> edges;
    }
    static class EdgeInput {
        public String from; public String to; public int weight;
        public EdgeInput(String f, String t, int w) { this.from=f; this.to=t; this.weight=w; }
    }
    static class InputData {
        public List<GraphInput> graphs;
    }
    static class InputStats {
        public int vertices;
        public int edges;
        public InputStats(int v, int e) { this.vertices = v; this.edges = e; }
    }
    static class OutputResult {
        public int graph_id;
        public InputStats input_stats;
        public Graph.MSTResult prim;
        public Graph.MSTResult kruskal;
        public OutputResult(int graphId, InputStats stats, Graph.MSTResult primRes, Graph.MSTResult kruskalRes) {
            this.graph_id = graphId;
            this.input_stats = stats;
            this.prim = primRes;
            this.kruskal = kruskalRes;
        }
    }

    public static void main(String[] args) throws IOException {
        String inputFile = "input.json";
        if (args.length > 0) {
            inputFile = args[0];
        }
        Gson gson = new Gson();
        try (Reader reader = new FileReader(inputFile)) {
            InputData data = gson.fromJson(reader, InputData.class);
            if (data == null || data.graphs == null) {
                System.err.println("Invalid input format.");
                return;
            }
            List<GraphInput> inputGraphs = data.graphs;
            List<OutputResult> results = new ArrayList<>();

            for (GraphInput g : inputGraphs) {
                Graph graph = new Graph(g.nodes, g.edges);
                Graph.MSTResult primRes = graph.computePrimMST();
                Graph.MSTResult kruskalRes = graph.computeKruskalMST();
                if (primRes.totalCost != kruskalRes.totalCost) {
                    System.err.println("Warning: Prim and Kruskal cost mismatch for graph " + g.id);
                }
                results.add(new OutputResult(
                        g.id,
                        new InputStats(graph.getVertexCount(), graph.getEdgeCount()),
                        primRes, kruskalRes));
            }

            int maxId = inputGraphs.stream().mapToInt(g -> g.id).max().orElse(0);
            Random rand = new Random(12345);
            List<GraphInput> smallGraphs = generateGraphs(rand, /*count=*/3, /*nodes=*/10, /*edges=*/20, maxId + 1);
            List<GraphInput> mediumGraphs = generateGraphs(rand, 1, 100, 300, maxId + 1001);
            List<GraphInput> largeGraphs = generateGraphs(rand, 1, 1000, 5000, maxId + 2001);
            saveGraphsToJson(smallGraphs, "input_small.json");
            saveGraphsToJson(mediumGraphs, "input_medium.json");
            saveGraphsToJson(largeGraphs, "input_large.json");

            List<OutputResult> syntheticResults = new ArrayList<>();
            for (GraphInput g : concatLists(smallGraphs, mediumGraphs, largeGraphs)) {
                Graph graph = new Graph(g.nodes, g.edges);
                Graph.MSTResult primRes = graph.computePrimMST();
                Graph.MSTResult kruskalRes = graph.computeKruskalMST();
                syntheticResults.add(new OutputResult(
                        g.id,
                        new InputStats(graph.getVertexCount(), graph.getEdgeCount()),
                        primRes, kruskalRes));
            }

            try (PrintWriter out = new PrintWriter("output.json")) {
                out.println("{");
                out.println("  \"results\": [");
                for (int i = 0; i < results.size(); i++) {
                    OutputResult r = results.get(i);
                    out.println("    {");
                    out.println("      \"graph_id\": " + r.graph_id + ",");
                    out.println("      \"input_stats\": {");
                    out.println("        \"vertices\": " + r.input_stats.vertices + ",");
                    out.println("        \"edges\": " + r.input_stats.edges);
                    out.println("      },");
                    out.println("      \"prim\": {");
                    out.println("        \"mst_edges\": [");
                    for (int j = 0; j < r.prim.mstEdges.size(); j++) {
                        Edge e = r.prim.mstEdges.get(j);
                        out.print("          {\"from\": \"" + e.from + "\", \"to\": \"" + e.to + "\", \"weight\": " + e.weight + "}");
                        out.println(j < r.prim.mstEdges.size() - 1 ? "," : "");
                    }
                    out.println("        ],");
                    out.println("        \"total_cost\": " + r.prim.totalCost + ",");
                    out.println("        \"operations_count\": " + r.prim.operationsCount + ",");
                    out.printf("        \"execution_time_ms\": %.2f\n", r.prim.executionTimeMs);
                    out.println("      },");
                    out.println("      \"kruskal\": {");
                    out.println("        \"mst_edges\": [");
                    for (int j = 0; j < r.kruskal.mstEdges.size(); j++) {
                        Edge e = r.kruskal.mstEdges.get(j);
                        out.print("          {\"from\": \"" + e.from + "\", \"to\": \"" + e.to + "\", \"weight\": " + e.weight + "}");
                        out.println(j < r.kruskal.mstEdges.size() - 1 ? "," : "");
                    }
                    out.println("        ],");
                    out.println("        \"total_cost\": " + r.kruskal.totalCost + ",");
                    out.println("        \"operations_count\": " + r.kruskal.operationsCount + ",");
                    out.printf("        \"execution_time_ms\": %.2f\n", r.kruskal.executionTimeMs);
                    out.println("      }");
                    out.println("    }" + (i < results.size() - 1 ? "," : ""));
                }
                out.println("  ]");
                out.println("}");
            }

            try (PrintWriter csv = new PrintWriter("summary.csv")) {
                csv.println("graph_id,vertices,edges,prim_time_ms,prim_operations,kruskal_time_ms,kruskal_operations");
                for (OutputResult r : concatLists(results, syntheticResults)) {
                    csv.printf("%d,%d,%d,%.2f,%d,%.2f,%d\n",
                            r.graph_id, r.input_stats.vertices, r.input_stats.edges,
                            r.prim.executionTimeMs, r.prim.operationsCount,
                            r.kruskal.executionTimeMs, r.kruskal.operationsCount);
                }
            }
        }
    }

    private static <T> List<T> concatLists(List<T>... lists) {
        List<T> all = new ArrayList<>();
        for (List<T> lst : lists) all.addAll(lst);
        return all;
    }

    private static List<GraphInput> generateGraphs(Random rand, int count, int n, int e, int startId) {
        List<GraphInput> graphs = new ArrayList<>();
        for (int k = 0; k < count; k++) {
            GraphInput g = new GraphInput();
            g.id = startId + k;
            g.nodes = new ArrayList<>();
            for (int i = 1; i <= n; i++) {
                g.nodes.add("N" + i);
            }
            List<EdgeInput> edges = new ArrayList<>();
            for (int i = 2; i <= n; i++) {
                int j = rand.nextInt(i - 1) + 1;
                int w = 1 + rand.nextInt(20);
                edges.add(new EdgeInput("N" + j, "N" + i, w));
            }
            // Add remaining edges randomly (avoid duplicate or self-loop)
            Set<String> edgeSet = new HashSet<>();
            for (EdgeInput ei : edges) {
                edgeSet.add(ei.from + "-" + ei.to);
                edgeSet.add(ei.to + "-" + ei.from);
            }
            int extra = e - (n - 1);
            int attempts = 0;
            while (extra > 0 && attempts < e * 2) {
                int u = rand.nextInt(n) + 1;
                int v = rand.nextInt(n) + 1;
                if (u == v) {
                    attempts++;
                    continue;
                }
                String key = "N" + u + "-N" + v;
                if (edgeSet.contains(key)) {
                    attempts++;
                    continue;
                }
                int w = 1 + rand.nextInt(20);
                edges.add(new EdgeInput("N" + u, "N" + v, w));
                edgeSet.add("N" + u + "-N" + v);
                edgeSet.add("N" + v + "-N" + u);
                extra--;
                attempts++;
            }
            g.edges = edges;
            graphs.add(g);
        }
        return graphs;
    }

    private static void saveGraphsToJson(List<GraphInput> graphs, String filename) throws IOException {
        try (PrintWriter out = new PrintWriter(filename)) {
            out.println("{");
            out.println("  \"graphs\": [");
            for (int i = 0; i < graphs.size(); i++) {
                GraphInput g = graphs.get(i);
                out.println("    {");
                out.println("      \"id\": " + g.id + ",");
                out.print("      \"nodes\": [");
                for (int j = 0; j < g.nodes.size(); j++) {
                    out.print("\"" + g.nodes.get(j) + "\"");
                    if (j < g.nodes.size() - 1) out.print(", ");
                }
                out.println("],");
                out.println("      \"edges\": [");
                for (int j = 0; j < g.edges.size(); j++) {
                    EdgeInput e = g.edges.get(j);
                    out.print("        {\"from\": \"" + e.from + "\", \"to\": \"" + e.to + "\", \"weight\": " + e.weight + "}");
                    out.println(j < g.edges.size() - 1 ? "," : "");
                }
                out.println("      ]");
                out.println("    }" + (i < graphs.size() - 1 ? "," : ""));
            }
            out.println("  ]");
            out.println("}");
        }
    }
}