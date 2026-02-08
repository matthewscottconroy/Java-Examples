public class GridWorld {
    static class Cell {
        int energy;

        Cell(int energy) {
            this.energy = energy;
        }
    }

    static Cell[][] makeWorld(int rows, int cols) {
        Cell[][] world = new Cell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                world[r][c] = new Cell((r + 1) * (c + 1));
            }
        }
        return world;
    }

    static void printEnergies(Cell[][] world) {
        for (int r = 0; r < world.length; r++) {
            for (int c = 0; c < world[r].length; c++) {
                System.out.print(world[r][c].energy);
                if (c < world[r].length - 1) System.out.print("\t");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Cell[][] world = makeWorld(4, 5);
        printEnergies(world);
    }
}

