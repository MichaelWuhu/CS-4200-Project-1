package model;

/**
 * Represents a node in the A* search tree.
 * Each node contains a puzzle state and cost information for pathfinding.
 */
public class Node {
    private final String state;  // Puzzle state as a string
    private final int g;         // Cost from start to this node (depth)
    private final int f;         // Estimated total cost: f = g + h
    private final long id;       // Unique ID for consistent tie-breaking in priority queue

    /**
     * Constructs a new Node with the specified parameters.
     * 
     * @param state The puzzle state as a string
     * @param g The cost from start to this node
     * @param f The estimated total cost (g + h)
     * @param id Unique identifier for tie-breaking
     */
    public Node(String state, int g, int f, long id) {
        this.state = state;
        this.g = g;
        this.f = f;
        this.id = id;
    }

    /**
     * @return The puzzle state
     */
    public String getState() {
        return state;
    }

    /**
     * @return The cost from start to this node
     */
    public int getG() {
        return g;
    }

    /**
     * @return The estimated total cost
     */
    public int getF() {
        return f;
    }

    /**
     * @return The unique identifier
     */
    public long getId() {
        return id;
    }
}
