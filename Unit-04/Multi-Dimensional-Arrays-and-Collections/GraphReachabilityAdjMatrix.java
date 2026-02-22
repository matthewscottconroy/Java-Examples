import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public class GraphReachabilityAdjMatrix {
    public static void main(String[] args) {
        // adjacency matrix: adj[i][j] == 1 means edge i -> j
        int[][] adj = {
                //0 1 2 3 4
                {0,1,0,0,0}, // 0 -> 1
                {0,0,1,1,0}, // 1 -> 2,3
                {0,0,0,0,1}, // 2 -> 4
                {0,0,0,0,0}, // 3 -> (none)
                {0,0,0,0,0}  // 4 -> (none)
        };

        int start = 0;
        int goal = 4;

        boolean reachable = bfsReachable(adj, start, goal);
        System.out.println("Adj: " + Arrays.deepToString(adj));
        System.out.printf("Path from %d to %d? %s%n", start, goal, reachable ? "YES" : "NO");
    }

    static boolean bfsReachable(int[][] adj, int start, int goal) {
        int n = adj.length;
        boolean[] visited = new boolean[n];
        Queue<Integer> q = new ArrayDeque<>();

        visited[start] = true;
        q.add(start);

        while (!q.isEmpty()) {
            int v = q.remove();
            if (v == goal) return true;

            for (int neighbor = 0; neighbor < n; neighbor++) {
                if (adj[v][neighbor] == 1 && !visited[neighbor]) {
                    visited[neighbor] = true;
                    q.add(neighbor);
                }
            }
        }
        return false;
    }
}
