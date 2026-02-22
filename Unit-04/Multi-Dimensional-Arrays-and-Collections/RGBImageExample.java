public class RGBImageExample {

    public static void main(String[] args) {

        int height = 2;
        int width = 2;

        int[][][] image = {
                { {255, 0, 0}, {0, 255, 0} },
                { {0, 0, 255}, {255, 255, 0} }
        };

        convertToGrayscale(image);

        printImage(image);
    }

    static void convertToGrayscale(int[][][] img) {
        for (int r = 0; r < img.length; r++) {
            for (int c = 0; c < img[r].length; c++) {

                int red   = img[r][c][0];
                int green = img[r][c][1];
                int blue  = img[r][c][2];

                int gray = (red + green + blue) / 3;

                img[r][c][0] = gray;
                img[r][c][1] = gray;
                img[r][c][2] = gray;
            }
        }
    }

    static void printImage(int[][][] img) {
        for (int r = 0; r < img.length; r++) {
            for (int c = 0; c < img[r].length; c++) {
                System.out.print("[" +
                        img[r][c][0] + "," +
                        img[r][c][1] + "," +
                        img[r][c][2] + "] ");
            }
            System.out.println();
        }
    }
}
