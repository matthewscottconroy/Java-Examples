import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class HexagonGameClient extends JFrame {
    private GamePanel gamePanel;
    private NetworkClient networkClient;
    private int localPlayer;    // This client's assigned player number (1 or 2)
    private int currentTurn;    // Whose turn it is (updated from server)

    public HexagonGameClient(String serverAddress) {
        setTitle("Hexagon Game - Networked");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        // Start network connection.
        networkClient = new NetworkClient(serverAddress, 55555);
        new Thread(networkClient).start();
    }

    public static void main(String[] args) {
        String tempServerAddress = "localhost";
        if (args.length >= 1) {
            tempServerAddress = args[0];
        }

				final String serverAddress = tempServerAddress;
        SwingUtilities.invokeLater(() -> {
            new HexagonGameClient(serverAddress).setVisible(true);
        });
    }

    // The GamePanel draws the board and handles user clicks.
    class GamePanel extends JPanel {
        private List<Cell> cells = new ArrayList<>();
        private Cell selectedCell = null;
        private final int HEX_SIZE = 30;
        private final int BOARD_RADIUS = 3;
        private final int OFFSET_X = 300;
        private final int OFFSET_Y = 300;

        public GamePanel() {
            setPreferredSize(new Dimension(600, 600));
            setBackground(Color.LIGHT_GRAY);
            createBoard();
            setInitialPieces();
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Allow moves only if it is our turn.
                    if (currentTurn != localPlayer) return;
                    Cell clicked = getCellAt(e.getX(), e.getY());
                    if (clicked == null) {
                        selectedCell = null;
                        repaint();
                        return;
                    }
                    if (selectedCell == null) {
                        if (clicked.state == localPlayer) {
                            selectedCell = clicked;
                        }
                    } else {
                        if (clicked.state == 0 && isValidMove(selectedCell, clicked)) {
                            // Instead of applying the move locally, send it to the server.
                            // Message format: MOVE srcQ srcR destQ destR
                            String message = "MOVE " + selectedCell.q + " " + selectedCell.r + " " +
                                             clicked.q + " " + clicked.r;
                            networkClient.sendMessage(message);
                            // Clear selection (the board will update when the move is received back).
                            selectedCell = null;
                        } else {
                            if (clicked.state == localPlayer) {
                                selectedCell = clicked;
                            } else {
                                selectedCell = null;
                            }
                        }
                    }
                    repaint();
                }
            });
        }

        private void createBoard() {
            cells.clear();
            for (int q = -BOARD_RADIUS; q <= BOARD_RADIUS; q++) {
                int r1 = Math.max(-BOARD_RADIUS, -q - BOARD_RADIUS);
                int r2 = Math.min(BOARD_RADIUS, -q + BOARD_RADIUS);
                for (int r = r1; r <= r2; r++) {
                    Cell cell = new Cell(q, r, HEX_SIZE, OFFSET_X, OFFSET_Y);
                    cells.add(cell);
                }
            }
        }

        private void setInitialPieces() {
            // For a board of radius 3, assign starting positions.
            // Player 1: (-BOARD_RADIUS, 0) and (0, -BOARD_RADIUS)
            // Player 2: (BOARD_RADIUS, 0) and (0, BOARD_RADIUS)
            for (Cell cell : cells) {
                if ((cell.q == -BOARD_RADIUS && cell.r == 0) ||
                    (cell.q == 0 && cell.r == -BOARD_RADIUS)) {
                    cell.state = 1;
                } else if ((cell.q == BOARD_RADIUS && cell.r == 0) ||
                           (cell.q == 0 && cell.r == BOARD_RADIUS)) {
                    cell.state = 2;
                }
            }
        }

        private Cell getCellAt(int x, int y) {
            for (Cell cell : cells) {
                if (cell.hexPath.contains(x, y)) {
                    return cell;
                }
            }
            return null;
        }

        private boolean isValidMove(Cell source, Cell dest) {
            int d = hexDistance(source, dest);
            return d == 1 || d == 2;
        }

        // Compute distance using axial coordinates.
        private int hexDistance(Cell a, Cell b) {
            int dq = Math.abs(a.q - b.q);
            int dr = Math.abs(a.r - b.r);
            int ds = Math.abs((-a.q - a.r) - (-b.q - b.r));
            return (dq + dr + ds) / 2;
        }

        // Convert any enemy pieces adjacent to the destination.
        private void convertAdjacent(Cell center, int player) {
            for (Cell cell : cells) {
                if (cell.state != 0 && cell.state != player) {
                    if (hexDistance(center, cell) == 1) {
                        cell.state = player;
                    }
                }
            }
        }

        // This method is called when a MOVE message is received.
        // Format: MOVE <player> srcQ srcR destQ destR
        public void applyMove(String moveMessage) {
            String[] tokens = moveMessage.split(" ");
            if (tokens.length != 6) return;
            try {
                int player = Integer.parseInt(tokens[1]);
                int srcQ = Integer.parseInt(tokens[2]);
                int srcR = Integer.parseInt(tokens[3]);
                int destQ = Integer.parseInt(tokens[4]);
                int destR = Integer.parseInt(tokens[5]);
                Cell source = null, dest = null;
                for (Cell cell : cells) {
                    if (cell.q == srcQ && cell.r == srcR) source = cell;
                    if (cell.q == destQ && cell.r == destR) dest = cell;
                }
                if (source == null || dest == null) return;
                int dist = hexDistance(source, dest);
                if (dist == 1) {
                    dest.state = player;
                } else if (dist == 2) {
                    dest.state = player;
                    source.state = 0;
                }
                convertAdjacent(dest, player);
                repaint();
            } catch (NumberFormatException e) {
                // Ignore invalid message.
            }
        }

        // Called when the TURN message is received.
        public void updateTurnDisplay() {
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (Cell cell : cells) {
                if (cell.state == 0) {
                    g2.setColor(Color.WHITE);
                } else if (cell.state == 1) {
                    g2.setColor(new Color(100, 149, 237));
                } else if (cell.state == 2) {
                    g2.setColor(new Color(240, 128, 128));
                }
                g2.fill(cell.hexPath);
                if (cell == selectedCell) {
                    g2.setColor(Color.GREEN.darker());
                    g2.setStroke(new BasicStroke(3));
                } else {
                    g2.setColor(Color.BLACK);
                    g2.setStroke(new BasicStroke(1));
                }
                g2.draw(cell.hexPath);
            }
            // Display status.
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("Local Player: " + localPlayer, 10, 20);
            g2.drawString("Current Turn: " + currentTurn, 10, 40);
        }
    }

    // Cell is defined in Cell.java — compile all files together: javac *.java

    // The NetworkClient handles communication with the server.
    class NetworkClient implements Runnable {
        private String serverAddress;
        private int port;
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public NetworkClient(String serverAddress, int port) {
            this.serverAddress = serverAddress;
            this.port = port;
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverAddress, port);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Received: " + line);
                    if (line.startsWith("START")) {
                        // Message format: START <player>
                        String[] tokens = line.split(" ");
                        if (tokens.length >= 2) {
                            localPlayer = Integer.parseInt(tokens[1]);
                        }
                    } else if (line.startsWith("TURN")) {
                        // Message format: TURN <player>
                        String[] tokens = line.split(" ");
                        if (tokens.length >= 2) {
                            currentTurn = Integer.parseInt(tokens[1]);
                            SwingUtilities.invokeLater(() -> gamePanel.updateTurnDisplay());
                        }
                    } else if (line.startsWith("MOVE")) {
                        // Message format: MOVE srcQ srcR destQ destR
												final String constantLine = line;
                        SwingUtilities.invokeLater(() -> gamePanel.applyMove(constantLine));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

