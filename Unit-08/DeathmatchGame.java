import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.UUID;
import javax.swing.Timer;

/**
 * A simple LAN peer-to-peer top-down deathmatch game.
 * - Movement: WASD keys
 * - Shooting: Arrow keys (each arrow key shoots a bullet in that direction)
 * - Each player has a health meter; when hit by a bullet, health decreases.
 * - Game state is exchanged over LAN using multicast sockets.
 */
public class DeathmatchGame extends JFrame {

    // Unique ID for this player; ask the user at startup.
    private int localPlayerId;
    // Used to filter echoed multicast packets sent by this instance.
    private final String instanceId = UUID.randomUUID().toString();
    // The color for each player (for demonstration, only players 1 and 2 are defined)
    private static final Map<Integer, Color> PLAYER_COLORS = Map.of(
            1, Color.BLUE,
            2, Color.RED
    );
    
    // The main game panel.
    private GamePanel gamePanel;
    // The networking peer for p2p communications.
    private NetworkPeer networkPeer;
    
    public DeathmatchGame(int localPlayerId) {
        this.localPlayerId = localPlayerId;
        setTitle("Deathmatch Game (Player " + localPlayerId + ")");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        
        // Start the network peer on a background thread.
        networkPeer = new NetworkPeer();
        new Thread(networkPeer).start();
    }
    
    public static void main(String[] args) {
        // Ask user for a player id.
        int tempPlayerId = 1;
        String input = JOptionPane.showInputDialog("Enter your player ID (e.g., 1 or 2):");
        try {
            tempPlayerId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            tempPlayerId = 1;
        }
				final int playerId = tempPlayerId;
        SwingUtilities.invokeLater(() -> new DeathmatchGame(playerId).setVisible(true));
    }
    
    /**
     * The GamePanel is responsible for rendering the game world and handling user input.
     */
    class GamePanel extends JPanel implements KeyListener {
        // Dimensions for the play area.
        private final int PANEL_WIDTH = 800;
        private final int PANEL_HEIGHT = 600;
        
        // Game state: players and bullets.
        private Map<Integer, Player> players = new HashMap<>();
        private java.util.List<Bullet> bullets = new ArrayList<>();
        
        // For local player movement.
        private boolean upPressed, downPressed, leftPressed, rightPressed;
        // For shooting cooldown (milliseconds)
        private long lastShotTime = 0;
        private final long SHOOT_COOLDOWN = 300;
        // Bullet speed.
        private final double BULLET_SPEED = 6;
        
        // Game loop timer.
        private Timer timer;
        
        public GamePanel() {
            setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
            setBackground(Color.DARK_GRAY);
            setFocusable(true);
            addKeyListener(this);
            
            // Initialize players.
            // For simplicity, we assume two players. The local player's starting position
            // is centered near one side; the remote player is placed at another position.
            players.put(1, new Player(1, 100, PANEL_HEIGHT / 2, 100));
            players.put(2, new Player(2, PANEL_WIDTH - 100, PANEL_HEIGHT / 2, 100));
            
            // Start game loop at ~50 FPS.
            timer = new Timer(20, e -> gameLoop());
            timer.start();
        }
        
        /**
         * The main game loop: update positions, check collisions, repaint.
         */
        private void gameLoop() {
            // Update local player position if controlled.
            Player localPlayer = players.get(localPlayerId);
            if (localPlayer != null) {
                double speed = 3;
                double dx = 0, dy = 0;
                if (upPressed)    dy -= speed;
                if (downPressed)  dy += speed;
                if (leftPressed)  dx -= speed;
                if (rightPressed) dx += speed;
                if (dx != 0 || dy != 0) {
                    localPlayer.x += dx;
                    localPlayer.y += dy;
                    // Constrain to panel bounds.
                    localPlayer.x = Math.max(0, Math.min(PANEL_WIDTH, localPlayer.x));
                    localPlayer.y = Math.max(0, Math.min(PANEL_HEIGHT, localPlayer.y));
                    // Broadcast new position.
                    networkPeer.sendMessage("PLAYER " + localPlayerId + " " + localPlayer.x + " " + localPlayer.y + " " + localPlayer.health);
                }
            }
            
            // Update bullet positions.
            Iterator<Bullet> iter = bullets.iterator();
            while (iter.hasNext()) {
                Bullet b = iter.next();
                b.x += b.vx;
                b.y += b.vy;
                // Remove bullet if offscreen.
                if (b.x < 0 || b.x > PANEL_WIDTH || b.y < 0 || b.y > PANEL_HEIGHT) {
                    iter.remove();
                    continue;
                }
                // Check collision with players (ignore shooter).
                for (Player p : players.values()) {
                    if (p.id != b.shooterId && p.health > 0) {
                        double dist = Math.hypot(b.x - p.x, b.y - p.y);
                        if (dist < p.radius) {
                            p.health -= 10;
                            iter.remove();
                            // Broadcast updated health.
                            networkPeer.sendMessage("PLAYER " + p.id + " " + p.x + " " + p.y + " " + p.health);
                            break;
                        }
                    }
                }
            }
            
            repaint();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            // Enable anti-aliasing.
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Draw players.
            for (Player p : players.values()) {
                // Use the predefined color or a default.
                Color col = PLAYER_COLORS.getOrDefault(p.id, Color.WHITE);
                g2.setColor(col);
                Ellipse2D.Double circle = new Ellipse2D.Double(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
                g2.fill(circle);
                // Draw health meter above the player.
                g2.setColor(Color.GREEN);
                int barWidth = (int)p.radius * 2;
                int barHeight = 6;
                int healthBarWidth = (int) (barWidth * (p.health / 100.0));
                g2.fillRect((int)(p.x - p.radius), (int)(p.y - p.radius - 10), healthBarWidth, barHeight);
                g2.setColor(Color.WHITE);
                g2.drawRect((int)(p.x - p.radius), (int)(p.y - p.radius - 10), barWidth, barHeight);
            }
            
            // Draw bullets.
            g2.setColor(Color.YELLOW);
            for (Bullet b : bullets) {
                Ellipse2D.Double bulletShape = new Ellipse2D.Double(b.x - b.radius, b.y - b.radius, b.radius * 2, b.radius * 2);
                g2.fill(bulletShape);
            }
        }
        
        // KeyListener methods for movement and shooting.
        @Override
        public void keyPressed(KeyEvent e) {
            int code = e.getKeyCode();
            // Movement keys.
            if (code == KeyEvent.VK_W) upPressed = true;
            if (code == KeyEvent.VK_S) downPressed = true;
            if (code == KeyEvent.VK_A) leftPressed = true;
            if (code == KeyEvent.VK_D) rightPressed = true;
            // Shooting with arrow keys.
            long now = System.currentTimeMillis();
            if (now - lastShotTime < SHOOT_COOLDOWN) return; // enforce cooldown
            double shootV = BULLET_SPEED;
            double vx = 0, vy = 0;
            if (code == KeyEvent.VK_UP)    { vy = -shootV; }
            if (code == KeyEvent.VK_DOWN)  { vy = shootV; }
            if (code == KeyEvent.VK_LEFT)  { vx = -shootV; }
            if (code == KeyEvent.VK_RIGHT) { vx = shootV; }
            if (vx != 0 || vy != 0) {
                Player p = players.get(localPlayerId);
                if (p != null) {
                    // Create a new bullet starting at the player's position.
                    Bullet b = new Bullet(localPlayerId, p.x, p.y, vx, vy);
                    bullets.add(b);
                    // Broadcast bullet creation.
                    networkPeer.sendMessage("BULLET " + localPlayerId + " " + p.x + " " + p.y + " " + vx + " " + vy);
                    lastShotTime = now;
                }
            }
        }
        
        @Override public void keyReleased(KeyEvent e) {
            int code = e.getKeyCode();
            if (code == KeyEvent.VK_W) upPressed = false;
            if (code == KeyEvent.VK_S) downPressed = false;
            if (code == KeyEvent.VK_A) leftPressed = false;
            if (code == KeyEvent.VK_D) rightPressed = false;
        }
        @Override public void keyTyped(KeyEvent e) { }
        
        /**
         * Called by the network thread when a PLAYER update is received.
         * Format: "PLAYER id x y health"
         */
        public void updatePlayer(String[] tokens) {
            if (tokens.length < 5) return;
            try {
                int id = Integer.parseInt(tokens[1]);
                double x = Double.parseDouble(tokens[2]);
                double y = Double.parseDouble(tokens[3]);
                double health = Double.parseDouble(tokens[4]);
                // If the player already exists, update its state.
                Player p = players.get(id);
                if (p == null) {
                    p = new Player(id, x, y, health);
                    players.put(id, p);
                } else {
                    p.x = x;
                    p.y = y;
                    p.health = health;
                }
            } catch (NumberFormatException ex) {
                // Ignore malformed update.
            }
        }
        
        /**
         * Called by the network thread when a BULLET message is received.
         * Format: "BULLET shooterId x y vx vy"
         */
        public void addBullet(String[] tokens) {
            if (tokens.length < 6) return;
            try {
                int shooterId = Integer.parseInt(tokens[1]);
                double x = Double.parseDouble(tokens[2]);
                double y = Double.parseDouble(tokens[3]);
                double vx = Double.parseDouble(tokens[4]);
                double vy = Double.parseDouble(tokens[5]);
                Bullet b = new Bullet(shooterId, x, y, vx, vy);
                bullets.add(b);
            } catch (NumberFormatException ex) {
                // Ignore malformed message.
            }
        }
    }
    
    /**
     * Represents a player in the game.
     */
    class Player {
        int id;
        double x, y;
        double health;
        double radius = 20;
        
        public Player(int id, double x, double y, double health) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.health = health;
        }
    }
    
    /**
     * Represents a bullet shot by a player.
     */
    class Bullet {
        int shooterId;
        double x, y;
        double vx, vy;
        double radius = 5;
        
        public Bullet(int shooterId, double x, double y, double vx, double vy) {
            this.shooterId = shooterId;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }
    }
    
    /**
     * The NetworkPeer uses multicast to send and receive game messages.
     * Message types:
     *   PLAYER <id> <x> <y> <health>
     *   BULLET <shooterId> <x> <y> <vx> <vy>
     */
    class NetworkPeer implements Runnable {
        private final String GROUP_ADDRESS = "230.0.0.1";
        private final int PORT = 4446;
        private MulticastSocket socket;
        private InetAddress group;
        
        public NetworkPeer() {
            try {
                socket = new MulticastSocket(PORT);
                group = InetAddress.getByName(GROUP_ADDRESS);
                // Find the first non-loopback, up, multicast-capable interface.
                // getByIndex(0) is invalid on most systems and can return null or
                // throw, leaving the socket unjoined and unable to receive packets.
                NetworkInterface ni = null;
                try {
                    Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
                    while (ifaces != null && ifaces.hasMoreElements()) {
                        NetworkInterface iface = ifaces.nextElement();
                        if (!iface.isLoopback() && iface.isUp() && iface.supportsMulticast()) {
                            ni = iface;
                            break;
                        }
                    }
                } catch (SocketException ignored) {}
                // ni == null lets the OS choose the default multicast interface.
                socket.joinGroup(new InetSocketAddress(group, PORT), ni);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        /**
         * Send a message to the multicast group, prefixed with this instance's ID
         * so the receive loop can discard the loopback echo.
         */
        public void sendMessage(String message) {
            byte[] buf = (instanceId + " " + message).getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, group, PORT);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        public void run() {
            byte[] buf = new byte[512];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
                    // Strip the sender prefix and ignore our own echoed packets.
                    String[] parts = received.split(" ", 2);
                    if (parts.length < 2) continue;
                    if (parts[0].equals(instanceId)) continue;
                    String content = parts[1];
                    SwingUtilities.invokeLater(() -> processMessage(content));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        /**
         * Parse and process a received message.
         */
        private void processMessage(String message) {
            String[] tokens = message.split(" ");
            if (tokens.length == 0) return;
            String type = tokens[0];
            if ("PLAYER".equals(type)) {
                gamePanel.updatePlayer(tokens);
            } else if ("BULLET".equals(type)) {
                gamePanel.addBullet(tokens);
            }
        }
    }
}

