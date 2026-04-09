package com.schelling.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Grid}.
 *
 * <p>Covers cell access, neighbour enumeration, satisfaction calculation,
 * empty/occupied cell lists, deep copy, and boundary conditions.
 */
@DisplayName("Grid")
class GridTest {

    private Grid grid;

    @BeforeEach
    void setUp() {
        // 3×3 grid for most tests
        grid = new Grid(3, 3);
    }

    // =========================================================================
    // Construction
    // =========================================================================

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("all cells start empty")
        void allCellsStartEmpty() {
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    assertNull(grid.getCell(r, c));
        }

        @Test
        @DisplayName("throws on non-positive dimensions")
        void throwsOnNonPositiveDimensions() {
            assertThrows(IllegalArgumentException.class, () -> new Grid(0, 3));
            assertThrows(IllegalArgumentException.class, () -> new Grid(3, 0));
        }

        @Test
        @DisplayName("dimensions are reported correctly")
        void dimensionsReported() {
            Grid g = new Grid(5, 7);
            assertEquals(5,  g.getRows());
            assertEquals(7,  g.getCols());
            assertEquals(35, g.getTotalCells());
        }
    }

    // =========================================================================
    // Cell access
    // =========================================================================

    @Nested
    @DisplayName("Cell access")
    class CellAccess {

        @Test
        @DisplayName("set and get round-trip")
        void setAndGet() {
            grid.setCell(1, 2, AgentType.TYPE_A);
            assertEquals(AgentType.TYPE_A, grid.getCell(1, 2));
        }

        @Test
        @DisplayName("setting null empties a cell")
        void setNullEmpties() {
            grid.setCell(0, 0, AgentType.TYPE_B);
            grid.setCell(0, 0, null);
            assertTrue(grid.isEmpty(0, 0));
        }

        @Test
        @DisplayName("out-of-bounds access throws")
        void outOfBoundsThrows() {
            assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(-1, 0));
            assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(3,  0));
            assertThrows(IndexOutOfBoundsException.class, () -> grid.getCell(0,  3));
        }
    }

    // =========================================================================
    // Neighbour enumeration
    // =========================================================================

    @Nested
    @DisplayName("Neighbour enumeration")
    class Neighbours {

        @Test
        @DisplayName("centre cell sees all 8 occupied neighbours")
        void centreSeesEight() {
            // Fill all cells with TYPE_A, then ask for neighbours of (1,1)
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    grid.setCell(r, c, AgentType.TYPE_A);

            List<AgentType> neighbours = grid.getNeighbours(1, 1);
            assertEquals(8, neighbours.size());
        }

        @Test
        @DisplayName("corner cell sees at most 3 neighbours")
        void cornerSeesThree() {
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    grid.setCell(r, c, AgentType.TYPE_A);

            List<AgentType> neighbours = grid.getNeighbours(0, 0);
            assertEquals(3, neighbours.size());
        }

        @Test
        @DisplayName("empty neighbours are excluded from the list")
        void emptyNeighboursExcluded() {
            grid.setCell(0, 0, AgentType.TYPE_A);
            // All other cells are null
            List<AgentType> neighbours = grid.getNeighbours(1, 1);
            assertEquals(1, neighbours.size());
            assertEquals(AgentType.TYPE_A, neighbours.get(0));
        }
    }

    // =========================================================================
    // Satisfaction
    // =========================================================================

    @Nested
    @DisplayName("Satisfaction")
    class Satisfaction {

        @Test
        @DisplayName("agent surrounded only by same type has ratio 1.0")
        void allSameTypeIsFullySatisfied() {
            for (int r = 0; r < 3; r++)
                for (int c = 0; c < 3; c++)
                    grid.setCell(r, c, AgentType.TYPE_A);

            assertEquals(1.0, grid.getSatisfactionRatio(1, 1), 1e-9);
        }

        @Test
        @DisplayName("agent surrounded only by other type has ratio 0.0")
        void allOppositeTypeIsFullyUnsatisfied() {
            grid.setCell(1, 1, AgentType.TYPE_A);
            // Surround with TYPE_B
            grid.setCell(0, 0, AgentType.TYPE_B);
            grid.setCell(0, 1, AgentType.TYPE_B);
            grid.setCell(0, 2, AgentType.TYPE_B);
            grid.setCell(1, 0, AgentType.TYPE_B);
            grid.setCell(1, 2, AgentType.TYPE_B);
            grid.setCell(2, 0, AgentType.TYPE_B);
            grid.setCell(2, 1, AgentType.TYPE_B);
            grid.setCell(2, 2, AgentType.TYPE_B);

            assertEquals(0.0, grid.getSatisfactionRatio(1, 1), 1e-9);
        }

        @Test
        @DisplayName("empty cell is vacuously satisfied")
        void emptyCellVacuouslySatisfied() {
            assertEquals(1.0, grid.getSatisfactionRatio(0, 0), 1e-9);
            assertTrue(grid.isSatisfied(0, 0, 0.5));
        }

        @Test
        @DisplayName("agent with no neighbours is satisfied")
        void noNeighboursIsSatisfied() {
            grid.setCell(0, 0, AgentType.TYPE_A);
            assertEquals(1.0, grid.getSatisfactionRatio(0, 0), 1e-9);
            assertTrue(grid.isSatisfied(0, 0, 1.0));
        }

        @Test
        @DisplayName("satisfaction threshold check is correct at boundary")
        void thresholdBoundary() {
            // 4 same, 4 different → ratio = 0.5
            grid.setCell(1, 1, AgentType.TYPE_A);
            grid.setCell(0, 0, AgentType.TYPE_A);
            grid.setCell(0, 1, AgentType.TYPE_A);
            grid.setCell(0, 2, AgentType.TYPE_A);
            grid.setCell(1, 0, AgentType.TYPE_A);
            grid.setCell(1, 2, AgentType.TYPE_B);
            grid.setCell(2, 0, AgentType.TYPE_B);
            grid.setCell(2, 1, AgentType.TYPE_B);
            grid.setCell(2, 2, AgentType.TYPE_B);

            assertEquals(0.5, grid.getSatisfactionRatio(1, 1), 1e-9);
            assertTrue(grid.isSatisfied(1, 1, 0.5));   // exactly at threshold
            assertFalse(grid.isSatisfied(1, 1, 0.51)); // just above threshold
        }
    }

    // =========================================================================
    // Cell enumeration
    // =========================================================================

    @Nested
    @DisplayName("Cell enumeration")
    class CellEnumeration {

        @Test
        @DisplayName("empty grid reports 9 empty cells and 0 occupied")
        void emptyGrid() {
            assertEquals(9, grid.getEmptyCells().size());
            assertEquals(0, grid.getOccupiedCells().size());
        }

        @Test
        @DisplayName("placing agents updates both lists correctly")
        void placingAgentsUpdatesLists() {
            grid.setCell(0, 0, AgentType.TYPE_A);
            grid.setCell(2, 2, AgentType.TYPE_B);

            assertEquals(7, grid.getEmptyCells().size());
            assertEquals(2, grid.getOccupiedCells().size());
        }

        @Test
        @DisplayName("occupied cells list contains correct coordinates")
        void occupiedCellsCoordinates() {
            grid.setCell(1, 2, AgentType.TYPE_A);
            List<Point> occupied = grid.getOccupiedCells();
            assertEquals(1, occupied.size());
            Point p = occupied.get(0);
            assertEquals(1, p.x);
            assertEquals(2, p.y);
        }
    }

    // =========================================================================
    // Deep copy
    // =========================================================================

    @Nested
    @DisplayName("Deep copy")
    class DeepCopy {

        @Test
        @DisplayName("copy has same contents as original")
        void copyHasSameContents() {
            grid.setCell(0, 0, AgentType.TYPE_A);
            grid.setCell(2, 2, AgentType.TYPE_B);

            Grid copy = grid.copy();
            assertEquals(AgentType.TYPE_A, copy.getCell(0, 0));
            assertEquals(AgentType.TYPE_B, copy.getCell(2, 2));
            assertNull(copy.getCell(1, 1));
        }

        @Test
        @DisplayName("mutations to copy do not affect original")
        void mutationDoesNotAffectOriginal() {
            grid.setCell(1, 1, AgentType.TYPE_A);
            Grid copy = grid.copy();

            copy.setCell(1, 1, AgentType.TYPE_B);
            assertEquals(AgentType.TYPE_A, grid.getCell(1, 1),
                "Original must be unchanged after mutating copy");
        }
    }
}
