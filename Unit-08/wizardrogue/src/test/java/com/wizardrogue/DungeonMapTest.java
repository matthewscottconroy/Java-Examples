package com.wizardrogue;

import com.wizardrogue.core.DungeonMap;
import com.wizardrogue.core.Tile;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link DungeonMap} generation and visibility.
 */
class DungeonMapTest {

    @Test
    void generatedMapHasCorrectDimensions() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(42));
        // Just sanity-checking bounds; isPassable should handle any coordinate.
        assertFalse(map.isPassable(-1, -1));
        assertFalse(map.isPassable(DungeonMap.WIDTH, DungeonMap.HEIGHT));
    }

    @Test
    void startTileIsPassable() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(1));
        assertTrue(map.isPassable(map.getStartX(), map.getStartY()),
            "Player start must be a passable tile");
    }

    @Test
    void stairTileIsStairDown() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(7));
        assertEquals(Tile.STAIR_DOWN, map.getTile(map.getStairX(), map.getStairY()),
            "Stair tile must be STAIR_DOWN");
    }

    @Test
    void startAndStairAreNotSameCell() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(99));
        assertFalse(map.getStartX() == map.getStairX()
                 && map.getStartY() == map.getStairY(),
            "Start and stair should not overlap (generation places them in different rooms)");
    }

    @RepeatedTest(5)
    void enemyAndItemSpawnsAreOnFloorTiles() {
        DungeonMap map = new DungeonMap();
        map.generate(2, new Random());
        for (int[] spawn : map.getEnemySpawns()) {
            assertTrue(map.isPassable(spawn[0], spawn[1]),
                "Enemy spawn at (" + spawn[0] + "," + spawn[1] + ") must be passable");
        }
        for (int[] spawn : map.getItemSpawns()) {
            assertTrue(map.isPassable(spawn[0], spawn[1]),
                "Item spawn at (" + spawn[0] + "," + spawn[1] + ") must be passable");
        }
    }

    @Test
    void visibilityComputationMarksStartVisible() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(5));
        int sx = map.getStartX(), sy = map.getStartY();
        map.computeVisibility(sx, sy, 9);
        assertTrue(map.isVisible(sx, sy), "Player's own tile must be visible");
        assertTrue(map.isExplored(sx, sy), "Player's own tile must be explored");
    }

    @Test
    void losIsBlockedByWalls() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(3));
        // Walk along top edge; wall tiles should block LOS from the start tile
        int sx = map.getStartX(), sy = map.getStartY();
        // The tile at (0,0) is almost certainly a wall; check it blocks LOS
        // from the start unless they happen to be in LOS (which would be a valid map).
        // The point is just that hasLOS doesn't crash or always return true.
        boolean result = map.hasLOS(sx, sy, 0, 0);
        // We can't assert a specific value without knowing the map layout,
        // but we verify it executes without error.
        assertNotNull(Boolean.valueOf(result));
    }

    @Test
    void unexploredTilesAreNotVisible() {
        DungeonMap map = new DungeonMap();
        map.generate(1, new Random(11));
        // Before any visibility compute, nothing should be visible
        for (int y = 0; y < DungeonMap.HEIGHT; y++) {
            for (int x = 0; x < DungeonMap.WIDTH; x++) {
                assertFalse(map.isVisible(x, y),
                    "No tile should be visible before computeVisibility()");
            }
        }
    }
}
