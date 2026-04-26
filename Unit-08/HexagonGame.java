import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

public class HexagonGame extends JFrame {
    public HexagonGame() {
        setTitle("Hexagon Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        GamePanel panel = new GamePanel();
        add(panel);
        pack();
        setLocationRelativeTo(null);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HexagonGame().setVisible(true);
        });
    }
}

/**
 * The GamePanel is responsible for drawing the board and handling mouse clicks.
 */
class GamePanel extends JPanel {
    private List<Cell> cells = new ArrayList<>();
    private Cell selectedCell = null;
    private int currentPlayer = 1; // 1 = player1, 2 = player2
    private final int HEX_SIZE = 30;  // radius of hexagon (distance from center to vertex)
    private final int BOARD_RADIUS = 3; // hexagon board of radius 3 (37 cells)
    private final int OFFSET_X = 300; // pixel offset for centering the board
    private final int OFFSET_Y = 300;
    
    public GamePanel() {
        // Set preferred size for the panel.
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.LIGHT_GRAY);
        createBoard();
        setInitialPieces();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Cell clicked = getCellAt(e.getX(), e.getY());
                if (clicked == null) {
                    // Clicked outside any cell: clear selection.
                    selectedCell = null;
                    repaint();
                    return;
                }
                
                // If nothing is selected, try to select a piece owned by the current player.
                if (selectedCell == null) {
                    if (clicked.state == currentPlayer) {
                        selectedCell = clicked;
                    }
                } else {
                    // If something is selected, check if the clicked cell is a valid destination.
                    if (clicked.state == 0 && isValidMove(selectedCell, clicked)) {
                        int dist = hexDistance(selectedCell, clicked);
                        // If one step, reproduce (the original remains).
                        if (dist == 1) {
                            clicked.state = currentPlayer;
                        }
                        // If two steps, move (the original cell becomes empty).
                        else if (dist == 2) {
                            clicked.state = currentPlayer;
                            selectedCell.state = 0;
                        }
                        // In either case, convert any enemy pieces adjacent to the destination.
                        convertAdjacent(clicked);
                        // Clear selection and switch turn.
                        selectedCell = null;
                        currentPlayer = (currentPlayer == 1) ? 2 : 1;
                    } else {
                        // Either clicking on your own piece to change selection or invalid destination.
                        if (clicked.state == currentPlayer) {
                            selectedCell = clicked;
                        } else {
                            // Invalid move; clear selection.
                            selectedCell = null;
                        }
                    }
                }
                repaint();
            }
        });
    }
    
    /**
     * Create the hexagon board using axial coordinates.
     * A hexagon board of radius R contains all cells (q, r) with:
     *   -R ≤ q ≤ R  and  max(-R, -q-R) ≤ r ≤ min(R, -q+R)
     */
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
    
    /**
     * Set initial pieces for the two players.
     * In this example we place player 1 pieces in two opposite corners
     * and player 2 pieces in the two opposite corners on the other side.
     */
    private void setInitialPieces() {
        // For a board of radius 3 the six corners (axial) are:
        // ( 3, 0), (0, 3), (-3, 3), (-3, 0), (0, -3), (3, -3)
        // We'll use ( -3, 0 ) and (0, -3 ) for player 1,
        // and ( 3, 0 ) and (0, 3 ) for player 2.
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
    
    /**
     * Return the cell (if any) that contains the given pixel coordinates.
     */
    private Cell getCellAt(int x, int y) {
        for (Cell cell : cells) {
            if (cell.hexPath.contains(x, y)) {
                return cell;
            }
        }
        return null;
    }
    
    /**
     * Check if moving from source to destination is valid (must be distance 1 or 2).
     */
    private boolean isValidMove(Cell source, Cell dest) {
        int d = hexDistance(source, dest);
        return d == 1 || d == 2;
    }
    
    /**
     * Compute the hex distance between two cells using axial coordinates.
     */
    private int hexDistance(Cell a, Cell b) {
        int dq = Math.abs(a.q - b.q);
        int dr = Math.abs(a.r - b.r);
        int ds = Math.abs((-a.q - a.r) - (-b.q - b.r));
        return (dq + dr + ds) / 2;
    }
    
    /**
     * For each neighbor (distance 1) of the given cell, convert any enemy pieces.
     */
    private void convertAdjacent(Cell center) {
        for (Cell cell : cells) {
            if (cell.state != 0 && cell.state != currentPlayer) {
                if (hexDistance(center, cell) == 1) {
                    cell.state = currentPlayer;
                }
            }
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Enable anti-aliasing for smoother drawing.
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw each cell.
        for (Cell cell : cells) {
            // Fill with color depending on state.
            if (cell.state == 0) {
                g2.setColor(Color.WHITE);
            } else if (cell.state == 1) {
                g2.setColor(new Color(100, 149, 237)); // player 1: cornflower blue
            } else if (cell.state == 2) {
                g2.setColor(new Color(240, 128, 128)); // player 2: light coral
            }
            g2.fill(cell.hexPath);
            // Draw border.
            if (cell == selectedCell) {
                g2.setColor(Color.GREEN.darker());
                g2.setStroke(new BasicStroke(3));
            } else {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
            }
            g2.draw(cell.hexPath);
        }
        
        // Draw current player's turn indicator.
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.drawString("Current Turn: Player " + currentPlayer, 10, 20);
    }
}

// Cell is defined in Cell.java and shared with HexagonGameClient and HexagonGameP2P.

