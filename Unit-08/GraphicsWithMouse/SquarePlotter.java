import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class SquarePlotter extends JFrame {
    public SquarePlotter() {
        setTitle("Square Plotter");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new SquarePanel());
        setLocationRelativeTo(null); // Center the window on the screen
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SquarePlotter frame = new SquarePlotter();
            frame.setVisible(true);
        });
    }
}

class Square {
    int x, y; // Top-left coordinates of the square
    int size;

    public Square(int x, int y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
    }

    // Check if a point is inside this square
    public boolean contains(Point p) {
        return p.x >= x && p.x <= x + size && p.y >= y && p.y <= y + size;
    }

    // Draw the square
    public void draw(Graphics g) {
        g.drawRect(x, y, size, size);
    }
}

class SquarePanel extends JPanel implements MouseListener, MouseMotionListener {
    private List<Square> squares;
    private Square selectedSquare = null;
    private int offsetX, offsetY;
    private final int SQUARE_SIZE = 40; // Fixed square size

    public SquarePanel() {
        squares = new ArrayList<>();
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // Draw all squares
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Square square : squares) {
            square.draw(g);
        }
    }

    // MouseListener methods

    // When the mouse is pressed, check if it is over a square to select it for dragging
    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        // Iterate in reverse order to pick the top-most square if squares overlap
        for (int i = squares.size() - 1; i >= 0; i--) {
            Square s = squares.get(i);
            if (s.contains(p)) {
                selectedSquare = s;
                offsetX = p.x - s.x;
                offsetY = p.y - s.y;
                break;
            }
        }
    }

    // When the mouse is released, clear the selection
    @Override
    public void mouseReleased(MouseEvent e) {
        selectedSquare = null;
    }

    // On mouse click, add a new square if the click is not inside an existing square
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        boolean insideExisting = false;
        for (Square s : squares) {
            if (s.contains(p)) {
                insideExisting = true;
                break;
            }
        }
        if (!insideExisting) {
            // Add a new square with its top-left corner at the click location
            squares.add(new Square(p.x, p.y, SQUARE_SIZE));
            repaint();
        }
    }

    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    // MouseMotionListener method

    // While dragging, update the position of the selected square to follow the mouse (keeping the initial offset)
    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedSquare != null) {
            selectedSquare.x = e.getX() - offsetX;
            selectedSquare.y = e.getY() - offsetY;
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) { }
}

