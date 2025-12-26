import heuristic.ManhattanDistanceHeuristic;
import heuristic.MisplacedTilesHeuristic;
import model.SearchResult;
import puzzle.PuzzleState;
import solver.AStarSolver;
import util.PuzzleGenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Automated test runner for evaluating A* search performance.
 * Generates 100 random solvable puzzles and solves each with both H1 and H2 heuristics.
 * Outputs detailed results to a CSV file for analysis.
 */
public class TestRunner {
    
    private static final int NUM_TRIALS = 100;
    private static final String OUTPUT_FILE = "test_results.csv";
    
    public static void main(String[] args) {
        System.out.println("Running " + NUM_TRIALS + " test trials...");
        System.out.println("Output will be written to: " + OUTPUT_FILE);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_FILE))) {
            // Write CSV header
            writer.println("Trial,Steps,H1_Search_Cost,H2_Search_Cost,H1_Time_ms,H2_Time_ms,H1_Faster");
            writer.println("# Steps = solution depth (number of moves from start to goal)");
            writer.println("# Search Cost = total nodes generated during search");
            writer.println("# Time = execution time in milliseconds");
            writer.println();
            
            // Initialize solvers
            AStarSolver solverH1 = new AStarSolver(new MisplacedTilesHeuristic());
            AStarSolver solverH2 = new AStarSolver(new ManhattanDistanceHeuristic());
            
            // Track statistics for summary
            int totalSteps = 0;
            long totalH1Nodes = 0;
            long totalH2Nodes = 0;
            double totalH1Time = 0.0;
            double totalH2Time = 0.0;
            int h1FasterCount = 0;
            int h2FasterCount = 0;
            
            // Run trials
            for (int trial = 1; trial <= NUM_TRIALS; trial++) {
                // Generate random solvable puzzle
                String startState = PuzzleGenerator.generateRandom();
                
                // Solve with both heuristics
                SearchResult resultH1 = solverH1.solve(startState);
                SearchResult resultH2 = solverH2.solve(startState);
                
                // Verify both searches succeeded
                if (resultH1 == null || resultH2 == null) {
                    System.err.println("Trial " + trial + " failed: no solution found");
                    continue;
                }
                
                // Extract metrics
                int steps = resultH1.getPath().size() - 1; // Subtract 1 for initial state
                int h1Nodes = resultH1.getNodesGenerated();
                int h2Nodes = resultH2.getNodesGenerated();
                double h1Time = resultH1.getTimeMs();
                double h2Time = resultH2.getTimeMs();
                boolean h1Faster = h1Time < h2Time;
                
                // Write trial results
                writer.printf("%d,%d,%d,%d,%.3f,%.3f,%s%n",
                        trial, steps, h1Nodes, h2Nodes, h1Time, h2Time, h1Faster);
                
                // Update statistics
                totalSteps += steps;
                totalH1Nodes += h1Nodes;
                totalH2Nodes += h2Nodes;
                totalH1Time += h1Time;
                totalH2Time += h2Time;
                if (h1Faster) {
                    h1FasterCount++;
                } else {
                    h2FasterCount++;
                }
                
                // Progress indicator
                if (trial % 10 == 0) {
                    System.out.println("Completed " + trial + " / " + NUM_TRIALS + " trials");
                }
            }
            
            // Write summary statistics
            writer.println();
            writer.println("# SUMMARY STATISTICS");
            writer.printf("# Average Steps: %.2f%n", (double) totalSteps / NUM_TRIALS);
            writer.printf("# Average H1 Search Cost: %.2f%n", (double) totalH1Nodes / NUM_TRIALS);
            writer.printf("# Average H2 Search Cost: %.2f%n", (double) totalH2Nodes / NUM_TRIALS);
            writer.printf("# Average H1 Time: %.3f ms%n", totalH1Time / NUM_TRIALS);
            writer.printf("# Average H2 Time: %.3f ms%n", totalH2Time / NUM_TRIALS);
            writer.printf("# H1 Faster: %d trials (%.1f%%)%n", h1FasterCount, 
                    100.0 * h1FasterCount / NUM_TRIALS);
            writer.printf("# H2 Faster: %d trials (%.1f%%)%n", h2FasterCount, 
                    100.0 * h2FasterCount / NUM_TRIALS);
            writer.printf("# H2 Efficiency: %.2fx fewer nodes generated%n", 
                    (double) totalH1Nodes / totalH2Nodes);
            
            System.out.println("\nTest complete! Results written to " + OUTPUT_FILE);
            System.out.println("\nSummary:");
            System.out.printf("  Average Steps: %.2f%n", (double) totalSteps / NUM_TRIALS);
            System.out.printf("  Average H1 Search Cost: %.2f%n", (double) totalH1Nodes / NUM_TRIALS);
            System.out.printf("  Average H2 Search Cost: %.2f%n", (double) totalH2Nodes / NUM_TRIALS);
            System.out.printf("  Average H1 Time: %.3f ms%n", totalH1Time / NUM_TRIALS);
            System.out.printf("  Average H2 Time: %.3f ms%n", totalH2Time / NUM_TRIALS);
            System.out.printf("  H2 Efficiency: %.2fx fewer nodes%n", 
                    (double) totalH1Nodes / totalH2Nodes);
            
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}