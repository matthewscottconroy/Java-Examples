import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class SquarePlotterAdvanced extends JFrame {
    public SquarePlotterAdvanced() {
        setTitle("Square Plotter Advanced");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(new SquarePanel());
        setLocationRelativeTo(null); // center the window
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SquarePlotterAdvanced().setVisible(true));
    }
}

// Class representing a square with center, size, and rotation.
class Square {
    double centerX, centerY;
    double size;      // side length of the square
    double angle;     // rotation in radians
    public static final double MIN_SIZE = 20; // minimum allowed size

    public Square(double centerX, double centerY, double size, double angle) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.size = size;
        this.angle = angle;
    }

    // Determines if a given point (in panel coordinates) is inside the rotated square.
    public boolean contains(Point p) {
        double dx = p.x - centerX;
        double dy = p.y - centerY;
        // Rotate the point by -angle to align with the unrotated square
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double localX = dx * cos + dy * sin;
        double localY = -dx * sin + dy * cos;
        return Math.abs(localX) <= size / 2 && Math.abs(localY) <= size / 2;
    }

    // Returns the position of the bottom-right resize handle.
    public Point getResizeHandle() {
        // In the square's local (unrotated) coordinates, the bottom-right corner is at (size/2, size/2).
        double localX = size / 2;
        double localY = size / 2;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int hx = (int) Math.round(centerX + localX * cos - localY * sin);
        int hy = (int) Math.round(centerY + localX * sin + localY * cos);
        return new Point(hx, hy);
    }

    // Returns the position of the rotation handle.
    public Point getRotationHandle() {
        // Use the top-center of the square in local coordinates (0, -size/2)
        // then offset upward by 20 pixels (in local coordinates)
        double localX = 0;
        double localY = -size / 2 - 20;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        int hx = (int) Math.round(centerX + localX * cos - localY * sin);
        int hy = (int) Math.round(centerY + localX * sin + localY * cos);
        return new Point(hx, hy);
    }

    // Draws the square and its handles.
    public void draw(Graphics2D g2d) {
        // Save the current transform.
        AffineTransform old = g2d.getTransform();
        // Translate to the square's center and apply rotation.
        g2d.translate(centerX, centerY);
        g2d.rotate(angle);
        int half = (int) (size / 2);
        // Draw the square (centered at the origin).
        g2d.setColor(Color.BLACK);
        g2d.drawRect(-half, -half, (int) size, (int) size);
        // Restore the original transform.
        g2d.setTransform(old);

        // Draw the resize handle (blue square).
        Point resizeHandle = getResizeHandle();
        int handleSize = 8;
        g2d.setColor(Color.BLUE);
        g2d.fillRect(resizeHandle.x - handleSize / 2, resizeHandle.y - handleSize / 2, handleSize, handleSize);

        // Draw the rotation handle (red circle).
        Point rotateHandle = getRotationHandle();
        g2d.setColor(Color.RED);
        g2d.fillOval(rotateHandle.x - handleSize / 2, rotateHandle.y - handleSize / 2, handleSize, handleSize);

        // Reset color.
        g2d.setColor(Color.BLACK);
    }
}

class SquarePanel extends JPanel implements MouseListener, MouseMotionListener {
    private List<Square> squares;
    private Square selectedSquare = null;
    private enum Mode { NONE, DRAG, RESIZE, ROTATE }
    private Mode currentMode = Mode.NONE;
    
    // Variables for dragging.
    private double dragOffsetX, dragOffsetY;
    // Variables for rotation.
    private double initialSquareAngle;
    private double initialMouseAngle;
    // Handle size for hit detection.
    private final int HANDLE_SIZE = 8;

    public SquarePanel() {
        squares = new ArrayList<>();
        addMouseListener(this);
        addMouseMotionListener(this);
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Use anti-aliasing for smoother rendering.
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (Square square : squares) {
            square.draw(g2d);
        }
    }

    // Utility: check if a point is inside a square "handle" area.
    private boolean pointInHandle(Point p, Point handleCenter) {
        Rectangle rect = new Rectangle(handleCenter.x - HANDLE_SIZE / 2, handleCenter.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        return rect.contains(p);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Point p = e.getPoint();
        // Right click: delete a square if the click is on or within its handles.
        if (SwingUtilities.isRightMouseButton(e)) {
            for (int i = squares.size() - 1; i >= 0; i--) {
                Square sq = squares.get(i);
                if (sq.contains(p) || pointInHandle(p, sq.getResizeHandle()) || pointInHandle(p, sq.getRotationHandle())) {
                    squares.remove(i);
                    repaint();
                    return;
                }
            }
        }
        // Left click: first check if the click is on a handle.
        else if (SwingUtilities.isLeftMouseButton(e)) {
            for (int i = squares.size() - 1; i >= 0; i--) {
                Square sq = squares.get(i);
                // Check the rotation handle.
                if (pointInHandle(p, sq.getRotationHandle())) {
                    selectedSquare = sq;
                    currentMode = Mode.ROTATE;
                    initialMouseAngle = Math.atan2(p.y - sq.centerY, p.x - sq.centerX);
                    initialSquareAngle = sq.angle;
                    return;
                }
                // Check the resize handle.
                if (pointInHandle(p, sq.getResizeHandle())) {
                    selectedSquare = sq;
                    currentMode = Mode.RESIZE;
                    return;
                }
            }
            // If no handle is clicked, check if the click is inside a square (for dragging).
            for (int i = squares.size() - 1; i >= 0; i--) {
                Square sq = squares.get(i);
                if (sq.contains(p)) {
                    selectedSquare = sq;
                    currentMode = Mode.DRAG;
                    dragOffsetX = p.x - sq.centerX;
                    dragOffsetY = p.y - sq.centerY;
                    return;
                }
            }
            // If the click isn't on any existing square, create a new one.
            squares.add(new Square(p.x, p.y, 80, 0)); // default size 80, angle 0
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedSquare == null) {
            return;
        }
        Point p = e.getPoint();
        switch (currentMode) {
            case DRAG:
                // Update the square's center based on the drag offset.
                selectedSquare.centerX = p.x - dragOffsetX;
                selectedSquare.centerY = p.y - dragOffsetY;
                break;
            case RESIZE:
                // For resizing, transform the mouse position into the square's local coordinates.
                double dx = p.x - selectedSquare.centerX;
                double dy = p.y - selectedSquare.centerY;
                double cos = Math.cos(selectedSquare.angle);
                double sin = Math.sin(selectedSquare.angle);
                double localX = dx * cos + dy * sin;
                double localY = -dx * sin + dy * cos;
                // Use the larger absolute value of localX or localY as the new half-size.
                double newHalfSize = Math.max(Math.abs(localX), Math.abs(localY));
                double newSize = newHalfSize * 2;
                selectedSquare.size = Math.max(newSize, Square.MIN_SIZE);
                break;
            case ROTATE:
                // Calculate the angle from the square's center to the current mouse position.
                double currentMouseAngle = Math.atan2(p.y - selectedSquare.centerY, p.x - selectedSquare.centerX);
                double angleDelta = currentMouseAngle - initialMouseAngle;
                selectedSquare.angle = initialSquareAngle + angleDelta;
                break;
            default:
                break;
        }
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        currentMode = Mode.NONE;
        selectedSquare = null;
    }

    // Unused mouse events.
    @Override public void mouseClicked(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
    @Override public void mouseMoved(MouseEvent e) { }
}

