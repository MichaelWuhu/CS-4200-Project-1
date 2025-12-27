package model;

import java.util.List;

/**
 * Encapsulates the results of an A* search execution.
 * Contains the solution path, performance metrics, and execution time.
 */
public class SearchResult {
    private final List<String> path; // Solution path from start to goal
    private final int nodesGenerated; // Total number of nodes generated (search cost)
    private final double timeMs; // Execution time in milliseconds

    /**
     * Constructs a new SearchResult with the specified parameters.
     * 
     * @param path           Solution path from start to goal
     * @param nodesGenerated Total number of nodes generated
     * @param timeMs         Execution time in milliseconds
     */
    public SearchResult(List<String> path, int nodesGenerated, double timeMs) {
        this.path = path;
        this.nodesGenerated = nodesGenerated;
        this.timeMs = timeMs;
    }

    /**
     * @return The solution path from start to goal
     */
    public List<String> getPath() {
        return path;
    }

    /**
     * @return The total number of nodes generated during search
     */
    public int getNodesGenerated() {
        return nodesGenerated;
    }

    /**
     * @return The execution time in milliseconds
     */
    public double getTimeMs() {
        return timeMs;
    }
}
