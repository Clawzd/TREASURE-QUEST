import java.util.Scanner;

public class TreasureQuest {
    static int n, m, k;
    static char[][] map;
    static boolean[][] monitored;
    static boolean[][] visited;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);


        System.out.println("#####################################################");
        System.out.println("# _____ ____  _____    _    ____  _   _ ____  _____ #");
        System.out.println("#|_   _|  _ \\| ____|  / \\  / ___|| | | |  _ \\| ____|#");
        System.out.println("#  | | | |_) |  _|   / _ \\ \\___ \\| | | | |_) |  _|  #");
        System.out.println("#  | | |  _ <| |___ / ___ \\ ___) | |_| |  _ <| |___ #");
        System.out.println("#  |_| |_| \\_\\_____/_/__ \\_\\____/ \\___/|_| \\_\\_____|#");
        System.out.println("# / _ \\| | | | ____/ ___|_   _|                     #");
        System.out.println("#| | | | | | |  _| \\___ \\ | |                       #");
        System.out.println("#| |_| | |_| | |___ ___) || |                       #");
        System.out.println("# \\__\\_\\\\___/|_____|____/ |_|                       #");
        System.out.println("#####################################################");


        System.out.println("#################################################");
        System.out.println("   Enter rows, columns, and number of monsters");
        System.out.println("#################################################");
        try {
            //Input dimensions
            n = input.nextInt();
            m = input.nextInt();
            k = input.nextInt();
            input.nextLine();
        } catch (Exception e) {
            // Validate initial input
            System.out.println("Invalid input!: enter 3 integer numbers");
            return;
        }

        // Validate initial input
        if (n <= 0 || m <= 0 || k < 0) {
            System.out.println("Invalid input: inputs must be positive");
            return;
        }

        // Initialize arrays
        map = new char[n][m];
        monitored = new boolean[n][m];
        visited = new boolean[n][m];


        // Initialize coordinates of Start (S) and End (E) to -1 as a marker for not found
        int S_Row = -1;
        int S_Col = -1;
        int E_Row = -1;
        int E_Col = -1;


        System.out.println("\n#################################################");
        System.out.println("               Enter the map");
        System.out.println("#################################################");
        System.out.println("- Use '.' for empty");
        System.out.println("- '#' for wall");
        System.out.println("- 'S' for start, 'E' for end");
        System.out.println("- Map size: " + n + " rows × " + m + " columns");


        // Read map lines and locate S and E
        for (int i = 0; i < n; i++) {
            String line = input.nextLine();
            if (line.length() != m) {
                System.out.println("Error: Each line must have exactly " + m + " characters.");
                return;
            }
            for (int j = 0; j < m; j++) {
                map[i][j] = line.charAt(j);
                if (map[i][j] == 'S') {
                    S_Row = i;
                    S_Col = j;
                }
                if (map[i][j] == 'E') {
                    E_Row = i;
                    E_Col = j;
                }
            }
        }
        // Ensure S and E were found
        if (S_Row == -1 || S_Col == -1 || E_Row == -1 || E_Col == -1) {
            System.out.println("Error: Start 'S' or End 'E' not found in map.");
            return;
        }
        // Read monsters info
        for (int i = 0; i < k; i++) {
            System.out.println((i + 1) + " - Enter monster position and range:");
            int r = input.nextInt() - 1;
            int c = input.nextInt() - 1;
            int d = input.nextInt();

            // Validate monster input
            if (r < 0 || r >= n || c < 0 || c >= m) {
                System.out.println("Error: Monster position out of map.");
                return;
            }
            // Validate monster input
            if (map[r][c] == '#') {
                System.out.println("Error: Monster cannot be placed on a wall.");
                return;
            }
            // Run spreadMonster to danger zones
            markDangerZones(r, c, d);
        }
        // If S or E are in danger zones, it's impossible
        if (monitored[S_Row][S_Col] || monitored[E_Row][E_Col]) {
            System.out.println("IMPOSSIBLE");
            return;
        }
        // Run findTreasur to find shortest path
        int answer = findTreasure(S_Row, S_Col, E_Row, E_Col);

        // Output result
        if (answer == -1) {
            System.out.println("IMPOSSIBLE");
        } else {
            System.out.println("##########");
            System.out.println("Success");
            System.out.println("##########");
            System.out.println("##  " + answer + "  ##");
            System.out.println("##########");
        }
    }
    // Expand monster's range in 4 straight directions
    public static void markDangerZones(int originalRow, int originalCol, int d) {
        // Create a queue
        Queue<Node> queue = new Queue<>();
        // Enqueue the starting position of the monster with 0 steps
        queue.enqueue(new Node( originalRow, originalCol, 0));
        // Mark the monster's initial position as monitored
        monitored[originalRow][originalCol] = true;

        //process each Node in the queue until all possible monster spread positions are explored
        while (!queue.isEmpty()) {
            Node current = queue.dequeue();

            // Continue spreading only if the monster hasn't reached its maximum range
            if (current.steps < d) {
                // Move Up
                if ((current.col == originalCol && originalRow - (current.row - 1) <= d) && current.row - 1 >= 0 && map[current.row - 1][current.col] != '#' && !monitored[current.row - 1][current.col]) {
                    // Mark as monitored
                    monitored[current.row - 1][current.col] = true;
                    // Add the new Node to the queue
                    queue.enqueue(new Node(current.row - 1, current.col, current.steps + 1));
                }
                // Move Down
                if ((current.col == originalCol && (current.row + 1 - originalRow) <= d) && current.row + 1 < n && map[current.row + 1][current.col] != '#' && !monitored[current.row + 1][current.col]) {
                    // Mark as monitored
                    monitored[current.row + 1][current.col] = true;
                    // Add the new Node to the queue
                    queue.enqueue(new Node(current.row + 1, current.col, current.steps + 1));
                }
                // Move Left
                if ((current.row == originalRow && originalCol - (current.col - 1) <= d) && current.col - 1 >= 0 && map[current.row][current.col - 1] != '#' && !monitored[current.row][current.col - 1]) {
                    // Mark as monitored
                    monitored[current.row][current.col - 1] = true;
                    // Add the new Node to the queue
                    queue.enqueue(new Node(current.row, current.col - 1, current.steps + 1));
                }
                // Move Right
                if ((current.row == originalRow && (current.col + 1 - originalCol) <= d) && current.col + 1 < m && map[current.row][current.col + 1] != '#' && !monitored[current.row][current.col + 1]) {
                    // Mark as monitored
                    monitored[current.row][current.col + 1] = true;
                    // Add the new Node to the queue
                    queue.enqueue(new Node(current.row, current.col + 1, current.steps + 1));
                }
            }
        }
    }
    // Find shortest path from start to end
    public static int findTreasure(int S_Row, int S_Col, int E_Row, int E_Col) {
        Queue<Node> queue = new Queue<>();
        queue.enqueue(new Node(S_Row, S_Col, 0));
        visited[S_Row][S_Col] = true;

        // Process each Node in the queue using BFS to explore all reachable paths
        while (!queue.isEmpty()) {
            Node current = queue.dequeue();

            // Goal reached
            if (current.row == E_Row && current.col == E_Col) {
                return current.steps;
            }

            // Move Up
            if (current.row - 1 >= 0 && map[current.row - 1][current.col] != '#' && !monitored[current.row - 1][current.col] && !visited[current.row - 1][current.col]) {
                // Mark as visited
                visited[current.row - 1][current.col] = true;
                // Add the new Node to the queue
                queue.enqueue(new Node(current.row - 1, current.col, current.steps + 1));
            }
            // Move Down
            if (current.row + 1 < n && map[current.row + 1][current.col] != '#' && !monitored[current.row + 1][current.col] && !visited[current.row + 1][current.col]) {
                // Mark as visited
                visited[current.row + 1][current.col] = true;
                // Add the new Node to the queue
                queue.enqueue(new Node(current.row + 1, current.col, current.steps + 1));
            }
            // Move Left
            if (current.col - 1 >= 0 && map[current.row][current.col - 1] != '#' && !monitored[current.row][current.col - 1] && !visited[current.row][current.col - 1]) {
                // Mark as visited
                visited[current.row][current.col - 1] = true;
                // Add the new Node to the queue
                queue.enqueue(new Node(current.row, current.col - 1, current.steps + 1));
            }
            // Move Right
            if (current.col + 1 < m && map[current.row][current.col + 1] != '#' && !monitored[current.row][current.col + 1] && !visited[current.row][current.col + 1]) {
                // Mark as visited
                visited[current.row][current.col + 1] = true;
                // Add the new Node to the queue
                queue.enqueue(new Node(current.row, current.col + 1, current.steps + 1));
            }
        }
        // No path found
        return -1;
    }
}
