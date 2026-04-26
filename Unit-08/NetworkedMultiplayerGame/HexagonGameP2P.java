import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HexagonGameP2P extends JFrame {
    private GamePanel gamePanel;
    private PeerNetwork peerNetwork;
    private int localPlayer;    // 1 or 2 (chosen at startup)
    private int currentTurn = 1;  // Initially, player 1 goes first
    // Generate a unique instance ID for this client.
    private final String instanceId = UUID.randomUUID().toString();

    public HexagonGameP2P(int localPlayer) {
        this.localPlayer = localPlayer;
        setTitle("Hexagon Game - Peer-to-Peer (Player " + localPlayer + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        // Start the peer-to-peer network thread.
        peerNetwork = new PeerNetwork();
        new Thread(peerNetwork).start();
    }

    public static void main(String[] args) {
        // Ask the user to enter their player number.
        int player = 1;
        String input = JOptionPane.showInputDialog("Enter your player number (1 or 2):");
        if (input == null) {  // User cancelled the dialog.
            System.exit(0);
        }
        try {
            player = Integer.parseInt(input);
            if (player != 1 && player != 2) {
                player = 1;
            }
        } catch (NumberFormatException e) {
            player = 1;
        }
        final int finalPlayer = player;
        SwingUtilities.invokeLater(() -> new HexagonGameP2P(finalPlayer).setVisible(true));
    }

    // GamePanel: draws the hexagon board, pieces, and handles mouse clicks.
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
                    // Only allow moves when it is our turn.
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
                            // Form a MOVE message using axial coordinates.
                            String moveMessage = "MOVE " + selectedCell.q + " " + selectedCell.r + " " + 
                                                 clicked.q + " " + clicked.r;
                            // Apply the move locally.
                            applyMove(moveMessage, localPlayer);
                            // Broadcast the move message.
                            peerNetwork.sendMessage(moveMessage);
                            // Change turn and broadcast it.
                            currentTurn = (localPlayer == 1) ? 2 : 1;
                            peerNetwork.sendMessage("TURN " + currentTurn);
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
                    cells.add(new Cell(q, r, HEX_SIZE, OFFSET_X, OFFSET_Y));
                }
            }
        }

        private void setInitialPieces() {
            // For board of radius 3, we assign:
            // Player 1 pieces at (-3,0) and (0,-3)
            // Player 2 pieces at (3,0) and (0,3)
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

        // Compute hexagonal distance using axial coordinates.
        private int hexDistance(Cell a, Cell b) {
            int dq = Math.abs(a.q - b.q);
            int dr = Math.abs(a.r - b.r);
            int ds = Math.abs((a.q + a.r) - (b.q + b.r));
            return (dq + dr + ds) / 2;
        }

        // Convert adjacent enemy pieces around the destination cell.
        private void convertAdjacent(Cell center, int player) {
            for (Cell cell : cells) {
                if (cell.state != 0 && cell.state != player) {
                    if (hexDistance(center, cell) == 1) {
                        cell.state = player;
                    }
                }
            }
        }

        // Apply a MOVE message locally.
        public void applyMove(String moveMessage, int player) {
            // Expected format: "MOVE srcQ srcR destQ destR"
            String[] tokens = moveMessage.split(" ");
            if (tokens.length != 5) return;
            try {
                int srcQ = Integer.parseInt(tokens[1]);
                int srcR = Integer.parseInt(tokens[2]);
                int destQ = Integer.parseInt(tokens[3]);
                int destR = Integer.parseInt(tokens[4]);
                Cell source = null, dest = null;
                for (Cell cell : cells) {
                    if (cell.q == srcQ && cell.r == srcR) source = cell;
                    if (cell.q == destQ && cell.r == destR) dest = cell;
                }
                if (source == null || dest == null) return;
                int dist = hexDistance(source, dest);
                if (dist == 1) {
                    dest.state = player;  // Reproduce: add new piece.
                } else if (dist == 2) {
                    dest.state = player;  // Move: destination gets player's piece...
                    source.state = 0;     // ...and source is cleared.
                }
                convertAdjacent(dest, player);
                repaint();
            } catch (NumberFormatException e) {
                // Ignore malformed message.
            }
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
                    g2.setColor(new Color(100, 149, 237)); // player 1: blue
                } else if (cell.state == 2) {
                    g2.setColor(new Color(240, 128, 128)); // player 2: red
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
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("Local Player: " + localPlayer, 10, 20);
            g2.drawString("Current Turn: " + currentTurn, 10, 40);
        }
    }

    // Cell is defined in Cell.java — compile all files together: javac *.java

    // PeerNetwork uses a MulticastSocket so that both players send and receive messages.
    class PeerNetwork implements Runnable {
        private final String GROUP_ADDRESS = "230.0.0.1";
        private final int PORT = 4446;
        private MulticastSocket socket;
        private InetAddress group;

        public PeerNetwork() {
            try {
                socket = new MulticastSocket(PORT);
                group = InetAddress.getByName(GROUP_ADDRESS);
                // Find the first non-loopback, up, multicast-capable interface.
                // getLocalHost() usually resolves to loopback (127.x), which keeps
                // multicast traffic local and prevents cross-machine communication.
                NetworkInterface ni = null;
                try {
                    java.util.Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                    while (ifaces != null && ifaces.hasMoreElements()) {
                        NetworkInterface iface = ifaces.nextElement();
                        if (!iface.isLoopback() && iface.isUp() && iface.supportsMulticast()) {
                            ni = iface;
                            break;
                        }
                    }
                } catch (java.net.SocketException ignored) {}
                // ni == null tells the OS to pick the default multicast interface.
                socket.joinGroup(new InetSocketAddress(group, PORT), ni);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send a message (as bytes) to the multicast group.
        // The message is prefixed with the instance ID.
        public void sendMessage(String message) {
            String fullMessage = instanceId + " " + message;
            byte[] buf = fullMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buf = new byte[256];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("Received: " + received);
                    // Expect the message in the format: "senderId MESSAGE_CONTENT"
                    String[] tokens = received.split(" ", 2);
                    if (tokens.length < 2) continue;
                    String senderId = tokens[0];
                    String content = tokens[1];
                    // Ignore messages sent by this instance.
                    if (senderId.equals(instanceId)) continue;
                    
                    if (content.startsWith("MOVE")) {
                        // Process remote moves.
                        SwingUtilities.invokeLater(() -> gamePanel.applyMove(content, 
                                (localPlayer == 1) ? 2 : 1));
                    } else if (content.startsWith("TURN")) {
                        String[] parts = content.split(" ");
                        if (parts.length == 2) {
                            try {
                                currentTurn = Integer.parseInt(parts[1]);
                                SwingUtilities.invokeLater(() -> gamePanel.repaint());
                            } catch (NumberFormatException e) {
                                // Ignore malformed TURN message.
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

