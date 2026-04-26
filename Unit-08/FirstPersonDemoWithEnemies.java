import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class FirstPersonDemoWithEnemies extends Canvas implements Runnable, KeyListener {
    private Thread thread;
    private boolean running = false;
    final int WIDTH = 800, HEIGHT = 600;
    
    // Player position and direction
    double posX = 5.0, posY = 5.0;      // Starting position (inside the map)
    double dirX = -1, dirY = 0;          // Initial direction (facing west)
    double planeX = 0, planeY = 0.66;    // Camera plane (determines field-of-view)
    
    // Simple 10x10 map: 1's are walls, 0's are open space.
    final int mapWidth = 10, mapHeight = 10;
    int[][] map = {
        {1,1,1,1,1,1,1,1,1,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,0,0,0,0,0,0,0,0,1},
        {1,1,1,1,1,1,1,1,1,1}
    };
    
    // Movement flags
    boolean moveForward = false, moveBackward = false, turnLeft = false, turnRight = false;
    boolean shoot = false;
    
    // Lists for enemies and bullets
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<Bullet> bullets = new ArrayList<>();
    
    public FirstPersonDemoWithEnemies() {
        JFrame frame = new JFrame("First Person Demo with Enemies & Shooting");
        frame.setSize(WIDTH, HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.addKeyListener(this);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Place two enemies in the world
        enemies.add(new Enemy(3.5, 3.5));
        enemies.add(new Enemy(7.5, 7.5));
        
        start();
    }
    
    public synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public synchronized void stop() {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        long lastTime = System.nanoTime();
        double nsPerTick = 1000000000.0 / 60.0; // 60 updates per second
        double delta = 0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / nsPerTick;
            lastTime = now;
            while (delta >= 1) {
                tick();
                delta--;
            }
            render();
        }
        stop();
    }
    
    // Update game logic: player movement, bullet updates, and collision detection
    private void tick() {
        double moveSpeed = 0.05; // Movement speed
        double rotSpeed = 0.03;  // Rotation speed
        
        // Move forward/backward (with collision checking)
        if(moveForward) {
            if(map[(int)(posX + dirX * moveSpeed)][(int)posY] == 0)
                posX += dirX * moveSpeed;
            if(map[(int)posX][(int)(posY + dirY * moveSpeed)] == 0)
                posY += dirY * moveSpeed;
        }
        if(moveBackward) {
            if(map[(int)(posX - dirX * moveSpeed)][(int)posY] == 0)
                posX -= dirX * moveSpeed;
            if(map[(int)posX][(int)(posY - dirY * moveSpeed)] == 0)
                posY -= dirY * moveSpeed;
        }
        // Rotate left/right
        if(turnLeft) {
            double oldDirX = dirX;
            dirX = dirX * Math.cos(rotSpeed) - dirY * Math.sin(rotSpeed);
            dirY = oldDirX * Math.sin(rotSpeed) + dirY * Math.cos(rotSpeed);
            double oldPlaneX = planeX;
            planeX = planeX * Math.cos(rotSpeed) - planeY * Math.sin(rotSpeed);
            planeY = oldPlaneX * Math.sin(rotSpeed) + planeY * Math.cos(rotSpeed);
        }
        if(turnRight) {
            double oldDirX = dirX;
            dirX = dirX * Math.cos(-rotSpeed) - dirY * Math.sin(-rotSpeed);
            dirY = oldDirX * Math.sin(-rotSpeed) + dirY * Math.cos(-rotSpeed);
            double oldPlaneX = planeX;
            planeX = planeX * Math.cos(-rotSpeed) - planeY * Math.sin(-rotSpeed);
            planeY = oldPlaneX * Math.sin(-rotSpeed) + planeY * Math.cos(-rotSpeed);
        }
        
        // Spawn a bullet if shoot flag is set (space bar pressed)
        if(shoot) {
            bullets.add(new Bullet(posX, posY, dirX, dirY));
            shoot = false;
        }
        
        // Advance every bullet one step.
        for (Bullet b : bullets) b.update();

        // Remove bullets that are out-of-bounds or have hit a wall.
        bullets.removeIf(b -> {
            int bx = (int) b.x, by = (int) b.y;
            return bx < 0 || bx >= mapWidth || by < 0 || by >= mapHeight || map[bx][by] > 0;
        });

        // Check bullet–enemy collisions using iterators so both lists can be
        // mutated safely without index arithmetic.
        Iterator<Bullet> bit = bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            Iterator<Enemy> eit = enemies.iterator();
            while (eit.hasNext()) {
                Enemy enemy = eit.next();
                double dx = enemy.x - b.x;
                double dy = enemy.y - b.y;
                if (Math.sqrt(dx * dx + dy * dy) < 0.5) {
                    eit.remove();
                    bit.remove();
                    break;
                }
            }
        }
    }
    
    // Render the scene: walls, enemies, and bullets
    private void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if(bs == null) {
            createBufferStrategy(3);
            return;
        }
        Graphics g = bs.getDrawGraphics();
        
        // Clear the screen
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // --- Raycast Walls ---
        for(int x = 0; x < WIDTH; x++) {
            double cameraX = 2 * x / (double)WIDTH - 1;
            double rayDirX = dirX + planeX * cameraX;
            double rayDirY = dirY + planeY * cameraX;
            
            int mapX = (int) posX;
            int mapY = (int) posY;
            
            double sideDistX;
            double sideDistY;
            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);
            double perpWallDist;
            
            int stepX;
            int stepY;
            int hit = 0;
            int side = 0;
            
            if(rayDirX < 0) {
                stepX = -1;
                sideDistX = (posX - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - posX) * deltaDistX;
            }
            if(rayDirY < 0) {
                stepY = -1;
                sideDistY = (posY - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - posY) * deltaDistY;
            }
            
            while(hit == 0) {
                if(sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                if(map[mapX][mapY] > 0) hit = 1;
            }
            
            if(side == 0)
                perpWallDist = (mapX - posX + (1 - stepX) / 2) / rayDirX;
            else
                perpWallDist = (mapY - posY + (1 - stepY) / 2) / rayDirY;
            
            int lineHeight = (int)(HEIGHT / perpWallDist);
            int drawStart = -lineHeight / 2 + HEIGHT / 2;
            if(drawStart < 0) drawStart = 0;
            int drawEnd = lineHeight / 2 + HEIGHT / 2;
            if(drawEnd >= HEIGHT) drawEnd = HEIGHT - 1;
            
            Color wallColor = Color.RED;
            if(side == 1) wallColor = wallColor.darker();
            
            g.setColor(wallColor);
            g.drawLine(x, drawStart, x, drawEnd);
        }
        
        // --- Render Enemies ---
        // Sort enemies by distance (farthest first) for proper overlap
        Collections.sort(enemies, new Comparator<Enemy>() {
            public int compare(Enemy a, Enemy b) {
                double distA = (a.x - posX) * (a.x - posX) + (a.y - posY) * (a.y - posY);
                double distB = (b.x - posX) * (b.x - posX) + (b.y - posY) * (b.y - posY);
                return Double.compare(distB, distA);
            }
        });
        for (Enemy enemy : enemies) {
            double spriteX = enemy.x - posX;
            double spriteY = enemy.y - posY;
            
            double invDet = 1.0 / (planeX * dirY - dirX * planeY);
            double transformX = invDet * (dirY * spriteX - dirX * spriteY);
            double transformY = invDet * (-planeY * spriteX + planeX * spriteY);
            
            if(transformY > 0) { // Only render if in front of player
                int spriteScreenX = (int)((WIDTH / 2) * (1 + transformX / transformY));
                int spriteHeight = Math.abs((int)(HEIGHT / transformY));
                int spriteWidth = spriteHeight;
                int drawStartY = -spriteHeight / 2 + HEIGHT / 2;
                int drawStartX = -spriteWidth / 2 + spriteScreenX;
                
                g.setColor(Color.BLUE);
                g.fillRect(drawStartX, drawStartY, spriteWidth, spriteHeight);
            }
        }
        
        // --- Render Bullets as Spheres with Perspective Scaling ---
        for (Bullet b : bullets) {
            // Transform bullet position into camera space
            double spriteX = b.x - posX;
            double spriteY = b.y - posY;
            double invDet = 1.0 / (planeX * dirY - dirX * planeY);
            double transformX = invDet * (dirY * spriteX - dirX * spriteY);
            double transformY = invDet * (-planeY * spriteX + planeX * spriteY);
            
            if(transformY > 0) {
                // Compute screen projection
                int bulletScreenX = (int)((WIDTH / 2) * (1 + transformX / transformY));
                int bulletScreenY = (int)(HEIGHT / 2 - HEIGHT / transformY);
                
                // Compute size based on depth (clamping between a min and max)
                // When the bullet is very near (small transformY), it appears larger.
                int bulletSize = (int)(50 / transformY);
                bulletSize = Math.max(4, Math.min(50, bulletSize));
                
                g.setColor(Color.YELLOW);
                g.fillOval(bulletScreenX - bulletSize/2, bulletScreenY - bulletSize/2, bulletSize, bulletSize);
            }
        }
        
        g.dispose();
        bs.show();
    }
    
    public static void main(String[] args) {
        new FirstPersonDemoWithEnemies();
    }
    
    // --- KeyListener implementations ---
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_W) moveForward = true;
        if(key == KeyEvent.VK_S) moveBackward = true;
        if(key == KeyEvent.VK_A) turnLeft = true;
        if(key == KeyEvent.VK_D) turnRight = true;
        if(key == KeyEvent.VK_SPACE) shoot = true;
    }
    
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if(key == KeyEvent.VK_W) moveForward = false;
        if(key == KeyEvent.VK_S) moveBackward = false;
        if(key == KeyEvent.VK_A) turnLeft = false;
        if(key == KeyEvent.VK_D) turnRight = false;
    }
    
    public void keyTyped(KeyEvent e) {}
    
    // --- Inner class for Enemy ---
    class Enemy {
        double x, y;
        public Enemy(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // --- Inner class for Bullet ---
    class Bullet {
        double x, y;
        double vx, vy;
        double speed = 0.2;
        
        public Bullet(double x, double y, double dirX, double dirY) {
            this.x = x;
            this.y = y;
            this.vx = dirX * speed;
            this.vy = dirY * speed;
        }
        
        public void update() {
            x += vx;
            y += vy;
        }
    }
}

