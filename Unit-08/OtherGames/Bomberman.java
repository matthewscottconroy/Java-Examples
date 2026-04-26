import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

//---------------------------------------------------------
// Bomberman: launcher that lets the user choose Local or Network play.
class Bomberman extends JFrame {
    private JRadioButton localButton, networkButton, hostButton, joinButton;
    private JTextField ipField;
    private JButton startButton;

    public Bomberman() {
        super("Bomberman Options");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 250);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        localButton   = new JRadioButton("Local (Two-player on one PC)");
        networkButton = new JRadioButton("Network (LAN play)");
        ButtonGroup modeGroup = new ButtonGroup();
        modeGroup.add(localButton);
        modeGroup.add(networkButton);
        localButton.setSelected(true);
        panel.add(localButton);
        panel.add(networkButton);

        hostButton = new JRadioButton("Host");
        joinButton = new JRadioButton("Join");
        ButtonGroup netGroup = new ButtonGroup();
        netGroup.add(hostButton);
        netGroup.add(joinButton);
        hostButton.setSelected(true);
        JPanel netPanel = new JPanel();
        netPanel.add(hostButton);
        netPanel.add(joinButton);
        panel.add(netPanel);

        JPanel ipPanel = new JPanel();
        ipPanel.add(new JLabel("Host IP:"));
        ipField = new JTextField("localhost", 15);
        ipPanel.add(ipField);
        panel.add(ipPanel);

        startButton = new JButton("Start Game");
        panel.add(startButton);
        add(panel);

        networkButton.addActionListener(e -> {
            hostButton.setEnabled(true);
            joinButton.setEnabled(true);
            ipField.setEnabled(joinButton.isSelected());
        });
        localButton.addActionListener(e -> {
            hostButton.setEnabled(false);
            joinButton.setEnabled(false);
            ipField.setEnabled(false);
        });
        joinButton.addActionListener(e -> ipField.setEnabled(true));
        hostButton.addActionListener(e -> ipField.setEnabled(false));

        startButton.addActionListener(e -> {
            boolean isLocal = localButton.isSelected();
            boolean isHostSelected = hostButton.isSelected();
            String ip = ipField.getText().trim();
            if (!isLocal && !isHostSelected && ip.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Please enter the host's IP address.", "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                JFrame gameFrame = new JFrame("Bomberman Clone");
                BombermanGame gamePanel = new BombermanGame(isLocal, isHostSelected, ip);
                gameFrame.add(gamePanel);
                gameFrame.pack();
                gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                gameFrame.setLocationRelativeTo(null);
                gameFrame.setVisible(true);
            });
            dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Bomberman().setVisible(true));
    }
}

//---------------------------------------------------------
// BombermanGame: main game panel with continuous movement.
class BombermanGame extends JPanel implements ActionListener, KeyListener {
    static final int CELL_SIZE = 32;
    static final int ROWS = 15;
    static final int COLS = 15;

    private Timer timer;
    Player localPlayer, remotePlayer;
    List<Bomb> bombs;
    List<PowerUp> powerUps;
    Block[][] blocks;
    Random rand = new Random();
    NetworkManager networkManager;
    boolean isLocalMode, gameOver = false;

    public BombermanGame(boolean isLocalMode, boolean isHost, String ipAddress) {
        this.isLocalMode = isLocalMode;
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        setFocusable(true);
        addKeyListener(this);
        bombs    = new CopyOnWriteArrayList<>();
        powerUps = new CopyOnWriteArrayList<>();
        initBlocks();

        if (isLocalMode) {
            localPlayer  = new Player(1, 1,        1,        Color.BLUE);
            remotePlayer = new Player(2, COLS - 2,  ROWS - 2, Color.RED);
        } else {
            if (isHost) {
                localPlayer  = new Player(1, 1,        1,        Color.BLUE);
                remotePlayer = new Player(2, COLS - 2,  ROWS - 2, Color.RED);
            } else {
                localPlayer  = new Player(2, COLS - 2,  ROWS - 2, Color.RED);
                remotePlayer = new Player(1, 1,        1,        Color.BLUE);
            }
            networkManager = new NetworkManager(isHost, ipAddress, this);
        }

        timer = new Timer(1000 / 60, this);
        timer.start();
    }

    private void initBlocks() {
        blocks = new Block[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (i == 0 || i == ROWS - 1 || j == 0 || j == COLS - 1) {
                    blocks[i][j] = new Block(j, i, false);
                } else if (i % 2 == 0 && j % 2 == 0) {
                    blocks[i][j] = new Block(j, i, false);
                } else if ((i <= 2 && j <= 2) || (i >= ROWS - 3 && j >= COLS - 3)) {
                    blocks[i][j] = null;
                } else if (rand.nextDouble() < 0.7) {
                    blocks[i][j] = new Block(j, i, true);
                } else {
                    blocks[i][j] = null;
                }
            }
        }
    }

    // Called 60 times per second by the Swing Timer.
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // Advance player positions based on currently held keys.
        localPlayer.update(CELL_SIZE, COLS, ROWS, blocks);
        if (isLocalMode) remotePlayer.update(CELL_SIZE, COLS, ROWS, blocks);

        for (Bomb bomb : bombs) bomb.update(this);
        bombs.removeIf(Bomb::isFinished);

        // Collect any power-ups the players are standing on.
        powerUps.removeIf(pu -> {
            if (localPlayer.isAlive() && localPlayer.overlapsBlastCell(pu.cellX, pu.cellY, CELL_SIZE)) {
                applyPowerUp(localPlayer, pu); return true;
            }
            if (isLocalMode && remotePlayer.isAlive() && remotePlayer.overlapsBlastCell(pu.cellX, pu.cellY, CELL_SIZE)) {
                applyPowerUp(remotePlayer, pu); return true;
            }
            return false;
        });

        // Broadcast position every frame in network mode.
        if (!isLocalMode) {
            networkManager.sendMessage("MOVE," + localPlayer.id + ","
                + Math.round(localPlayer.px) + "," + Math.round(localPlayer.py));
        }

        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Floor tiles.
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
                g.setColor(Color.WHITE);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }

        for (int i = 0; i < ROWS; i++)
            for (int j = 0; j < COLS; j++)
                if (blocks[i][j] != null) blocks[i][j].draw(g, CELL_SIZE);

        for (PowerUp pu : powerUps) pu.draw(g, CELL_SIZE);
        for (Bomb bomb : bombs) bomb.draw(g, CELL_SIZE);

        // Draw players on top of everything else.
        localPlayer.draw(g);
        remotePlayer.draw(g);
    }

    private void placeBomb(Player player) {
        if (player.activeBombs >= player.maxBombs) return;
        int cellX = player.getCellX(CELL_SIZE);
        int cellY = player.getCellY(CELL_SIZE);
        bombs.add(new Bomb(cellX, cellY, player));
        player.activeBombs++;
    }

    public void spawnPowerUp(int cellX, int cellY) {
        if (rand.nextDouble() < 0.4) {
            PowerUp.Type[] types = PowerUp.Type.values();
            powerUps.add(new PowerUp(cellX, cellY, types[rand.nextInt(types.length)]));
        }
    }

    private void applyPowerUp(Player player, PowerUp pu) {
        switch (pu.type) {
            case BOMB_UP:  player.maxBombs  = Math.min(player.maxBombs  + 1, 3);   break;
            case FLAME_UP: player.blastRange = Math.min(player.blastRange + 1, 6); break;
            case SPEED_UP: player.speed     = Math.min(player.speed + 0.5f, 4.5f); break;
        }
    }

    // ------- Key handling: only set/clear direction flags here -------

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) return;
        int k = e.getKeyCode();

        // Player 1 – arrow keys + SPACE to drop bomb.
        if      (k == KeyEvent.VK_UP)    localPlayer.up    = true;
        else if (k == KeyEvent.VK_DOWN)  localPlayer.down  = true;
        else if (k == KeyEvent.VK_LEFT)  localPlayer.left  = true;
        else if (k == KeyEvent.VK_RIGHT) localPlayer.right = true;
        else if (k == KeyEvent.VK_SPACE) {
            placeBomb(localPlayer);
            if (!isLocalMode)
                networkManager.sendMessage("BOMB," + localPlayer.id + ","
                    + localPlayer.getCellX(CELL_SIZE) + "," + localPlayer.getCellY(CELL_SIZE));
        }

        // Player 2 – WASD + F to drop bomb (local mode only).
        if (isLocalMode) {
            if      (k == KeyEvent.VK_W) remotePlayer.up    = true;
            else if (k == KeyEvent.VK_S) remotePlayer.down  = true;
            else if (k == KeyEvent.VK_A) remotePlayer.left  = true;
            else if (k == KeyEvent.VK_D) remotePlayer.right = true;
            else if (k == KeyEvent.VK_F) placeBomb(remotePlayer);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();

        if      (k == KeyEvent.VK_UP)    localPlayer.up    = false;
        else if (k == KeyEvent.VK_DOWN)  localPlayer.down  = false;
        else if (k == KeyEvent.VK_LEFT)  localPlayer.left  = false;
        else if (k == KeyEvent.VK_RIGHT) localPlayer.right = false;

        if (isLocalMode) {
            if      (k == KeyEvent.VK_W) remotePlayer.up    = false;
            else if (k == KeyEvent.VK_S) remotePlayer.down  = false;
            else if (k == KeyEvent.VK_A) remotePlayer.left  = false;
            else if (k == KeyEvent.VK_D) remotePlayer.right = false;
        }
    }

    @Override public void keyTyped(KeyEvent e) {}

    // ------- Network -------

    public void processNetworkMessage(String message) {
        String[] parts = message.split(",");
        if (parts[0].equals("DEAD") && parts.length >= 2) {
            int pid = Integer.parseInt(parts[1]);
            if      (pid == remotePlayer.id) JOptionPane.showMessageDialog(this, "You win!");
            else if (pid == localPlayer.id)  JOptionPane.showMessageDialog(this, "You lose!");
            endGame();
            return;
        }
        if (parts.length < 4) return;
        String cmd    = parts[0];
        int playerId  = Integer.parseInt(parts[1]);
        int x         = Integer.parseInt(parts[2]);
        int y         = Integer.parseInt(parts[3]);

        if (cmd.equals("MOVE") && playerId == remotePlayer.id) {
            remotePlayer.px = x;
            remotePlayer.py = y;
        } else if (cmd.equals("BOMB") && playerId == remotePlayer.id) {
            bombs.add(new Bomb(x, y, null));
        }
    }

    // ------- Explosion handling -------

    public void handleExplosion(Bomb bomb, List<Point> blastCells) {
        igniteBombs(blastCells, bomb);
        for (Point p : blastCells) {
            if (localPlayer.isAlive()  && localPlayer.overlapsBlastCell(p.x, p.y, CELL_SIZE))
                playerHit(localPlayer);
            if (remotePlayer.isAlive() && remotePlayer.overlapsBlastCell(p.x, p.y, CELL_SIZE))
                playerHit(remotePlayer);
        }
    }

    public void igniteBombs(List<Point> blastCells, Bomb triggeringBomb) {
        for (Bomb b : bombs) {
            if (!b.exploded && b != triggeringBomb) {
                for (Point p : blastCells) {
                    if (b.x == p.x && b.y == p.y) { b.ignite(); break; }
                }
            }
        }
    }

    public void playerHit(Player player) {
        if (!player.alive) return;
        player.alive = false;
        if (!isLocalMode && player == localPlayer)
            networkManager.sendMessage("DEAD," + localPlayer.id);
        endGame();
    }

    public void endGame() {
        if (gameOver) return;
        gameOver = true;
        timer.stop();
        String msg;
        if (isLocalMode) {
            if  (localPlayer.alive && !remotePlayer.alive) msg = "Player 1 wins!";
            else if (!localPlayer.alive && remotePlayer.alive) msg = "Player 2 wins!";
            else msg = "Draw!";
        } else {
            if  (localPlayer.alive && !remotePlayer.alive) msg = "You win!";
            else if (!localPlayer.alive && remotePlayer.alive) msg = "You lose!";
            else msg = "Draw!";
        }
        JOptionPane.showMessageDialog(this, msg);
    }

    public Block[][] getBlocks() { return blocks; }
}

//---------------------------------------------------------
// Player: continuous pixel-space movement with AABB grid collision.
class Player {
    int id;
    float px, py;   // pixel coords of the top-left corner of the bounding box
    Color color;
    boolean alive = true;

    // Direction flags set/cleared by key events; movement happens each game tick.
    boolean up, down, left, right;

    // Player bounding box is slightly smaller than a cell so tight corridors work.
    static final int SIZE = 26;
    float speed      = 2.5f;
    int maxBombs     = 1;
    int activeBombs  = 0;
    int blastRange   = 3;

    public Player(int id, int startCol, int startRow, Color color) {
        this.id    = id;
        this.color = color;
        int cs = BombermanGame.CELL_SIZE;
        // Centre the player inside the starting cell.
        this.px = startCol * cs + (cs - SIZE) / 2f;
        this.py = startRow * cs + (cs - SIZE) / 2f;
    }

    // Advance position by one game tick using currently held direction flags.
    public void update(int cs, int cols, int rows, Block[][] blocks) {
        float dx = 0, dy = 0;
        if (up)    dy -= 1;
        if (down)  dy += 1;
        if (left)  dx -= 1;
        if (right) dx += 1;

        if (dx == 0 && dy == 0) return;

        // Normalise so diagonal speed equals cardinal speed.
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        dx = dx / len * speed;
        dy = dy / len * speed;

        // Resolve each axis independently so the player slides along walls.
        float newPx = px + dx;
        if (!collides(newPx, py, cs, cols, rows, blocks)) px = newPx;

        float newPy = py + dy;
        if (!collides(px, newPy, cs, cols, rows, blocks)) py = newPy;
    }

    // Returns true if the bounding box at (npx, npy) overlaps any solid block or the world edge.
    private boolean collides(float npx, float npy, int cs, int cols, int rows, Block[][] blocks) {
        if (npx < 0 || npy < 0 || npx + SIZE > cols * cs || npy + SIZE > rows * cs)
            return true;

        // Determine which grid cells the bounding box overlaps.
        int minCol = (int) (npx / cs);
        int maxCol = (int) ((npx + SIZE - 1) / cs);
        int minRow = (int) (npy / cs);
        int maxRow = (int) ((npy + SIZE - 1) / cs);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                if (blocks[r][c] != null && !blocks[r][c].isDestroyed())
                    return true;
            }
        }
        return false;
    }

    // Grid cell that contains the player's centre.
    public int getCellX(int cs) { return (int) ((px + SIZE / 2f) / cs); }
    public int getCellY(int cs) { return (int) ((py + SIZE / 2f) / cs); }

    // True if the player's bounding box overlaps the given blast cell.
    public boolean overlapsBlastCell(int cellX, int cellY, int cs) {
        float bx = cellX * cs, by = cellY * cs;
        return px < bx + cs && px + SIZE > bx
            && py < by + cs && py + SIZE > by;
    }

    public boolean isAlive() { return alive; }

    public void draw(Graphics g) {
        if (!alive) return;
        g.setColor(color);
        g.fillOval((int) px, (int) py, SIZE, SIZE);
    }
}

//---------------------------------------------------------
// Bomb: placed on the grid, explodes after a countdown.
class Bomb {
    int x, y;
    private int countdown  = 210; // ~3.5 s at 60 FPS
    private int explosionTimer = 0;
    private final int blastRange;
    private final Player owner;
    private static final int EXPLOSION_DURATION = 54; // total frames the explosion shows
    private static final int GROWTH_FRAMES = 6;       // frames between each outward step
    boolean exploded  = false;
    private boolean finished  = false;
    private int visibleRange  = 0; // grows from 0 → blastRange during the animation
    private final List<Point> blastCells;

    public Bomb(int x, int y, Player owner) {
        this.x = x;
        this.y = y;
        this.owner = owner;
        this.blastRange = (owner != null) ? owner.blastRange : 3;
        blastCells = new CopyOnWriteArrayList<>();
    }

    public void ignite() {
        if (!exploded) countdown = 0;
    }

    public void update(BombermanGame game) {
        if (!exploded) {
            if (countdown > 0) {
                countdown--;
            } else {
                explode(game);
            }
        } else {
            if (explosionTimer > 0) {
                explosionTimer--;
                int elapsed = EXPLOSION_DURATION - explosionTimer;
                visibleRange = Math.min(elapsed / GROWTH_FRAMES, blastRange);
            } else {
                if (owner != null) owner.activeBombs--;
                finished = true;
            }
        }
    }

    private void explode(BombermanGame game) {
        exploded = true;
        explosionTimer = EXPLOSION_DURATION;
        visibleRange = 0;
        blastCells.add(new Point(x, y));
        int[][] dirs = { {0, -1}, {0, 1}, {-1, 0}, {1, 0} };
        Block[][] blocks = game.getBlocks();

        for (int[] dir : dirs) {
            for (int i = 1; i <= blastRange; i++) {
                int nx = x + dir[0] * i;
                int ny = y + dir[1] * i;
                if (nx < 0 || nx >= BombermanGame.COLS || ny < 0 || ny >= BombermanGame.ROWS) break;
                if (blocks[ny][nx] != null && !blocks[ny][nx].isDestroyed()) {
                    if (blocks[ny][nx].isDestructible()) {
                        blocks[ny][nx].destroy();
                        blastCells.add(new Point(nx, ny));
                        game.spawnPowerUp(nx, ny);
                    }
                    break;
                }
                blastCells.add(new Point(nx, ny));
            }
        }
        game.handleExplosion(this, blastCells);
    }

    public void draw(Graphics g, int cs) {
        if (!exploded) {
            // Throb: pulse speed and red tint both increase as the fuse burns down.
            float fuseRatio = (float) countdown / 210;        // 1.0 → 0.0
            float frequency = 0.15f + (1 - fuseRatio) * 0.35f; // speeds up near detonation
            float pulse = (float) Math.sin(countdown * frequency);
            int margin = 4 + (int)(pulse * 3);                // oscillates between 1–7 px
            int redness = (int)((1 - fuseRatio) * 200);       // 0 (black) → 200 (red)
            g.setColor(new Color(redness, 0, 0));
            g.fillOval(x * cs + margin, y * cs + margin, cs - margin * 2, cs - margin * 2);
        } else {
            // Fade out over the explosion lifetime (1.0 → 0.0).
            float fade    = (float) explosionTimer / EXPLOSION_DURATION;
            int   elapsed = EXPLOSION_DURATION - explosionTimer;
            for (Point p : blastCells) {
                int dist = Math.abs(p.x - x) + Math.abs(p.y - y);
                if (dist > visibleRange) continue; // not yet revealed

                // Each cell grows from a point to full-cell size over GROWTH_FRAMES.
                int   cellAge   = elapsed - dist * GROWTH_FRAMES;
                float cellScale = Math.min(cellAge / (float) GROWTH_FRAMES, 1.0f);
                int   inset     = (int) ((1 - cellScale) * cs / 2);

                // Colour: white core → yellow → orange towards the tips, then fade.
                int r    = 255;
                int gVal = dist == 0 ? 255 : (dist == 1 ? 200 : 120);
                int b    = dist == 0 ? 180 : 0;
                r    = (int)(r    * fade);
                gVal = (int)(gVal * fade);
                b    = (int)(b    * fade);
                g.setColor(new Color(r, gVal, b));
                g.fillRect(p.x * cs + inset, p.y * cs + inset, cs - inset * 2, cs - inset * 2);
            }
        }
    }

    public boolean isFinished() { return finished; }
}

//---------------------------------------------------------
// Block: an indestructible wall or a destructible crate.
class Block {
    private int x, y;
    private boolean destructible;
    private boolean destroyed = false;

    public Block(int x, int y, boolean destructible) {
        this.x = x;
        this.y = y;
        this.destructible = destructible;
    }

    public boolean isDestructible() { return destructible; }
    public boolean isDestroyed()    { return destroyed; }

    public void destroy() {
        if (destructible) destroyed = true;
    }

    public void draw(Graphics g, int cs) {
        if (destroyed) return;
        g.setColor(destructible ? new Color(139, 69, 19) : Color.DARK_GRAY);
        g.fillRect(x * cs, y * cs, cs, cs);
    }
}

//---------------------------------------------------------
// NetworkManager: simple peer-to-peer socket connection.
class NetworkManager {
    private Socket socket;
    private ServerSocket serverSocket;
    private BufferedReader in;
    private PrintWriter out;
    private BombermanGame game;

    public NetworkManager(boolean isHost, String ipAddress, BombermanGame game) {
        this.game = game;
        try {
            if (isHost) {
                serverSocket = new ServerSocket(5000);
                serverSocket.setSoTimeout(10_000);
                System.out.println("Waiting for a client connection...");
                try {
                    socket = serverSocket.accept();
                } catch (java.net.SocketTimeoutException ex) {
                    System.err.println("No client connected within 10 seconds. Exiting.");
                    System.exit(0);
                    return;
                }
                System.out.println("Client connected.");
            } else {
                socket = new Socket(ipAddress, 5000);
                System.out.println("Connected to host.");
            }
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            new Thread(this::listen).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String msg) { out.println(msg); }

    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) game.processNetworkMessage(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

//---------------------------------------------------------
// PowerUp: dropped by destroyed crates, collected by walking over them.
class PowerUp {
    enum Type { BOMB_UP, FLAME_UP, SPEED_UP }

    final int cellX, cellY;
    final Type type;

    public PowerUp(int cellX, int cellY, Type type) {
        this.cellX = cellX;
        this.cellY = cellY;
        this.type  = type;
    }

    public void draw(Graphics g, int cs) {
        // Colored rounded tile.
        switch (type) {
            case BOMB_UP:  g.setColor(new Color(80, 80, 220));  break;
            case FLAME_UP: g.setColor(new Color(220, 60, 0));   break;
            case SPEED_UP: g.setColor(new Color(200, 180, 0));  break;
        }
        int m = 5;
        g.fillRoundRect(cellX * cs + m, cellY * cs + m, cs - m * 2, cs - m * 2, 8, 8);

        // Bold letter label centered on the tile.
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 13));
        String label;
        switch (type) {
            case BOMB_UP:  label = "B"; break;
            case FLAME_UP: label = "F"; break;
            default:       label = "S"; break;
        }
        FontMetrics fm = g.getFontMetrics();
        int tx = cellX * cs + (cs - fm.stringWidth(label)) / 2;
        int ty = cellY * cs + (cs + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(label, tx, ty);
    }
}
