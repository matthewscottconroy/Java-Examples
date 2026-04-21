package com.wizardrogue.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * A procedurally generated dungeon floor with fog-of-war visibility.
 *
 * <h2>Generation algorithm</h2>
 * <ol>
 *   <li>Fill the map with {@link Tile#WALL}.</li>
 *   <li>Attempt to place up to {@value #TARGET_ROOMS} rectangular rooms
 *       (5–14 × 4–8 tiles) with at least 1-tile padding between them.</li>
 *   <li>Sort rooms by centre X and connect each consecutive pair with an
 *       L-shaped corridor — this guarantees the dungeon is fully connected.</li>
 *   <li>Place the player start in the first room and {@link Tile#STAIR_DOWN}
 *       in the last room.</li>
 *   <li>Scatter enemy and item spawns across the non-starting rooms.</li>
 * </ol>
 *
 * <h2>Visibility and fog of war</h2>
 * <p>{@link #computeVisibility(int, int, int)} runs a Bresenham line-of-sight
 * check from the player to every tile within a fixed radius.  Visible tiles
 * are marked {@code explored}.  The renderer dims explored-but-not-visible
 * tiles and hides unexplored tiles entirely.
 */
public final class DungeonMap {

    public static final int WIDTH  = 72;
    public static final int HEIGHT = 26;

    private static final int TARGET_ROOMS  = 12;
    private static final int MAX_ATTEMPTS  = 300;

    private final Tile[][]    tiles;
    private final boolean[][] explored;
    private final boolean[][] visible;

    private int startX, startY;
    private int stairX, stairY;

    private final List<int[]> enemySpawns = new ArrayList<>();
    private final List<int[]> itemSpawns  = new ArrayList<>();

    // ------------------------------------------------------------------ inner record

    private record Room(int x, int y, int w, int h) {
        int cx() { return x + w / 2; }
        int cy() { return y + h / 2; }

        boolean intersects(Room o, int pad) {
            return x - pad < o.x + o.w + pad
                && x + w + pad > o.x - pad
                && y - pad < o.y + o.h + pad
                && y + h + pad > o.y - pad;
        }
    }

    // ------------------------------------------------------------------ constructor

    public DungeonMap() {
        tiles    = new Tile[HEIGHT][WIDTH];
        explored = new boolean[HEIGHT][WIDTH];
        visible  = new boolean[HEIGHT][WIDTH];
    }

    // ------------------------------------------------------------------ generation

    /** Generates a new dungeon floor appropriate for {@code floorNumber}. */
    public void generate(int floorNumber, Random rng) {
        // Fill with walls
        for (int y = 0; y < HEIGHT; y++)
            for (int x = 0; x < WIDTH; x++)
                tiles[y][x] = Tile.WALL;

        for (int y = 0; y < HEIGHT; y++) {
            java.util.Arrays.fill(explored[y], false);
            java.util.Arrays.fill(visible[y],  false);
        }

        enemySpawns.clear();
        itemSpawns.clear();

        // Place rooms
        List<Room> rooms = new ArrayList<>();
        int attempts = MAX_ATTEMPTS;
        while (rooms.size() < TARGET_ROOMS && attempts-- > 0) {
            int rw = 5 + rng.nextInt(10);
            int rh = 4 + rng.nextInt(5);
            int rx = 1 + rng.nextInt(WIDTH  - rw - 2);
            int ry = 1 + rng.nextInt(HEIGHT - rh - 2);
            Room candidate = new Room(rx, ry, rw, rh);
            if (rooms.stream().noneMatch(r -> r.intersects(candidate, 1))) {
                rooms.add(candidate);
                carveRoom(candidate);
            }
        }

        if (rooms.isEmpty()) {
            // Fallback: a single large room
            Room fallback = new Room(2, 2, WIDTH - 4, HEIGHT - 4);
            rooms.add(fallback);
            carveRoom(fallback);
        }

        // Sort by centre X for ordered corridor connection
        rooms.sort(Comparator.comparingInt(Room::cx));

        // Connect consecutive rooms with L-shaped corridors
        for (int i = 1; i < rooms.size(); i++) {
            connectRooms(rooms.get(i - 1), rooms.get(i), rng);
        }

        // Player start: first room
        startX = rooms.get(0).cx();
        startY = rooms.get(0).cy();

        // Stair down: last room
        Room last = rooms.get(rooms.size() - 1);
        stairX = last.cx();
        stairY = last.cy();
        tiles[stairY][stairX] = Tile.STAIR_DOWN;

        // Enemy and item spawns in non-start rooms
        int maxEnemiesPerRoom = 1 + floorNumber / 2;
        for (int i = 1; i < rooms.size(); i++) {
            Room room = rooms.get(i);
            int numEnemies = 1 + rng.nextInt(maxEnemiesPerRoom);
            for (int e = 0; e < numEnemies; e++) {
                int ex = room.x + 1 + rng.nextInt(room.w - 2);
                int ey = room.y + 1 + rng.nextInt(room.h - 2);
                if (!(ex == stairX && ey == stairY)) {
                    enemySpawns.add(new int[]{ex, ey});
                }
            }
            if (rng.nextInt(3) != 0) {  // ~67% chance of item
                int ix = room.x + 1 + rng.nextInt(room.w - 2);
                int iy = room.y + 1 + rng.nextInt(room.h - 2);
                itemSpawns.add(new int[]{ix, iy});
            }
        }
    }

    private void carveRoom(Room r) {
        for (int y = r.y; y < r.y + r.h; y++)
            for (int x = r.x; x < r.x + r.w; x++)
                tiles[y][x] = Tile.FLOOR;
    }

    private void connectRooms(Room a, Room b, Random rng) {
        int x1 = a.cx(), y1 = a.cy();
        int x2 = b.cx(), y2 = b.cy();
        if (rng.nextBoolean()) {
            carveHLine(y1, x1, x2);
            carveVLine(x2, y1, y2);
        } else {
            carveVLine(x1, y1, y2);
            carveHLine(y2, x1, x2);
        }
    }

    private void carveHLine(int y, int x1, int x2) {
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
            if (inBounds(x, y)) tiles[y][x] = Tile.FLOOR;
    }

    private void carveVLine(int x, int y1, int y2) {
        for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
            if (inBounds(x, y)) tiles[y][x] = Tile.FLOOR;
    }

    // ------------------------------------------------------------------ visibility

    /**
     * Recomputes the visible set from ({@code px},{@code py}) within
     * {@code radius} tiles using Bresenham line-of-sight.
     * All visible tiles are also marked explored.
     */
    public void computeVisibility(int px, int py, int radius) {
        for (boolean[] row : visible) java.util.Arrays.fill(row, false);
        for (int dy = -radius; dy <= radius; dy++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dy * dy > radius * radius) continue;
                int tx = px + dx, ty = py + dy;
                if (!inBounds(tx, ty)) continue;
                if (hasLOS(px, py, tx, ty)) {
                    visible[ty][tx]  = true;
                    explored[ty][tx] = true;
                }
            }
        }
    }

    /**
     * Bresenham line-of-sight: returns {@code false} if any wall tile lies
     * between (x1,y1) and (x2,y2), exclusive of both endpoints.
     */
    public boolean hasLOS(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1,  sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        int cx = x1, cy = y1;
        while (true) {
            if (cx == x2 && cy == y2) return true;
            if (inBounds(cx, cy) && tiles[cy][cx].blocksLight
                    && !(cx == x1 && cy == y1)) return false;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 <  dx) { err += dx; cy += sy; }
        }
    }

    // ------------------------------------------------------------------ queries

    public boolean isPassable(int x, int y) {
        return inBounds(x, y) && !tiles[y][x].blocksMove;
    }

    public boolean isVisible(int x, int y)  { return inBounds(x, y) && visible[y][x]; }
    public boolean isExplored(int x, int y) { return inBounds(x, y) && explored[y][x]; }

    public Tile getTile(int x, int y) {
        return inBounds(x, y) ? tiles[y][x] : Tile.WALL;
    }

    public void setTile(int x, int y, Tile t) {
        if (inBounds(x, y)) tiles[y][x] = t;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    // ------------------------------------------------------------------ accessors

    public int      getStartX()       { return startX; }
    public int      getStartY()       { return startY; }
    public int      getStairX()       { return stairX; }
    public int      getStairY()       { return stairY; }
    public List<int[]> getEnemySpawns() { return enemySpawns; }
    public List<int[]> getItemSpawns()  { return itemSpawns; }
}
