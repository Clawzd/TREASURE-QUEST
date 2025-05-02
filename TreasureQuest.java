import java.util.Scanner;

public class TreasureQuest {
    static int n, m, k;
    static char[][] map;
    static boolean[][] monitored;
    static boolean[][] visited;

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);


        System.out.println("\n#####################################################");
        System.out.println("# _____ ____  _____    _    ____  _   _ ____  _____ #");
        System.out.println("#|_   _|  _ \\| ____|  / \\  / ___|| | | |  _ \\| ____|#");
        System.out.println("#  | | | |_) |  _|   / _ \\ \\___ \\| | | | |_) |  _|  #");
        System.out.println("#  | | |  _ <| |___ / ___ \\ ___) | |_| |  _ <| |___ #");
        System.out.println("#  |_| |_| \\_\\_____/_/__ \\_\\____/ \\___/|_| \\_\\_____|#");
        System.out.println("# / _ \\| | | | ____/ ___|_   _|                     #");
        System.out.println("#| | | | | | |  _| \\___ \\ | |                       #");
        System.out.println("#| |_| | |_| | |___ ___) || |                       #");
        System.out.println("# \\__\\_\\\\___/|_____|____/ |_|                       #");
        System.out.println("#####################################################\n");

        // For input dimensions
        System.out.println("#################################################");
        System.out.println("   Enter rows, columns, and number of monsters");
        System.out.println("#################################################");
        n = input.nextInt();
        m = input.nextInt();
        k = input.nextInt();
        input.nextLine();

        // Validate initial input
        if (n < 5 || n > 1000 || m < 5 || m > 1000 || k < 0 || k > 10000) {
            System.out.println("Invalid input range for n, m, or k.");
            return;
        }

        // Initialize arrays
        map = new char[n][m];
        monitored = new boolean[n][m];
        visited = new boolean[n][m];

        // Track coordinates of Start (S) and End (E)
        int S_Row = -1;
        int S_Col = -1;
        int E_Row = -1;
        int E_Col = -1;

        // Track how many times S and E appear
        int sCount = 0;
        int eCount = 0;

        // Prompt user to enter map
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

            // Validate line length
            if (line.length() != m) {
                System.out.println("Error: Each line must have exactly " + m + " characters.");
                return;
            }

            for (int j = 0; j < m; j++) {
                char ch = line.charAt(j);

                // Validate allowed characters
                if (ch != '.'&& ch != '\uF09F' && ch != '#' && ch != 'S' && ch != 'E') {
                    System.out.println("Error: Invalid character '" + ch + "' found in the map.");
                    return;
                }

                // Validate that borders are walls
                if ((i == 0 || i == n - 1 || j == 0 || j == m - 1) && ch != '#') {
                    System.out.println("Map border must be a wall (#)");
                    return;
                }

                map[i][j] = ch;

                // Count S and E occurrences
                if (ch == 'S') {
                    S_Row = i;
                    S_Col = j;
                    sCount++;
                }
                if (ch == 'E') {
                    E_Row = i;
                    E_Col = j;
                    eCount++;
                }
            }
        }

        // Reject if more than one 'S' or 'E' is found
        if (sCount != 1 || eCount != 1) {
            System.out.println("Error: Map must contain exactly one 'S' and one 'E'.");
            return;
        }

        // Track monster locations to prevent duplicates
        boolean[][] monsterPlaced = new boolean[n][m];

        // Read monsters and apply their spread effect
        for (int i = 0; i < k; i++) {
            System.out.println((i + 1) + " - Enter monster position and range:");
            int r = input.nextInt() - 1;
            int c = input.nextInt() - 1;
            int d = input.nextInt();

            // Check if the monster is placed on the border
            if (r <= 0 || r >= n - 1 || c <= 0 || c >= m - 1) {
                System.out.println("Error: Monster must be placed inside the map");
                return;
            }
            // Check if the monster is placed on a wall
            if (map[r][c] == '#') {
                System.out.println("Error: Monster cannot be placed on a wall.");
                return;
            }
            // Check the monster's range
            if (d < 0 || d > n * m) {
                System.out.println("Error: Invalid monster range.");
                return;
            }
            // Check if there's already a monster at the same location
            if (monsterPlaced[r][c]) {
                System.out.println("Error: Two monsters cannot be placed at the same location.");
                return;
            }

            monsterPlaced[r][c] = true;

            markDangerZones(r, c, d);
        }

        // If S or E are in danger zones, it's impossible
        if (monitored[S_Row][S_Col] || monitored[E_Row][E_Col]) {
            System.out.println("IMPOSSIBLE");
            return;
        }

        // Run BFS to find shortest path
        int answer = findTreasure(S_Row, S_Col, E_Row, E_Col);

        // Output result
        if (answer == -1) {
            System.out.println("IMPOSSIBLE");
        } else {
            System.out.println(answer);
        }
    }

    // Expand monster's range in 4 straight directions
    public static void markDangerZones(int originalRow, int originalCol, int d) {
        // Create a queue
        Queue<Node> queue = new Queue<>();
        // Enqueue the starting position of the monster with 0 steps
        queue.enqueue(new Node(originalRow, originalCol, 0));
        // Mark the monster's initial position as monitored
        monitored[originalRow][originalCol] = true;

        //process each Node in the queue until all possible monster spread positions are explored
        while (!queue.isEmpty()) {
            Node current = queue.dequeue();

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
    public static int findTreasure(int startRow, int startCol, int endRow, int endCol) {
        Queue<Node> queue = new Queue<>();
        queue.enqueue(new Node(startRow, startCol, 0));
        visited[startRow][startCol] = true;

        // Process each Node in the queue using BFS to explore all reachable paths
        while (!queue.isEmpty()) {
            Node current = queue.dequeue();

            // Goal reached
            if (current.row == endRow && current.col == endCol) {
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
