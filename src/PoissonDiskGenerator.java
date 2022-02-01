import java.util.*;
import java.awt.geom.Point2D;
import java.util.Random;

public class PoissonDiskGenerator {
    double minDistance;
    Point2D.Double[] grid;
    List<Point2D.Double> activeGrid;
    double cellSize;
    int amount; // number of rows/columns
    Random random;

    final double sqrt2 = 1.41421356237d; // the square root of 2
    final int k = 30; // number of samples to chose before rejection (normally 30)



    public PoissonDiskGenerator(double minDistance, int size, int seed){
        // STEP 0
        this.minDistance = minDistance;
        cellSize = minDistance / sqrt2; // w
        amount = (int)Math.floor(size / cellSize);
        activeGrid = new ArrayList<>();

        grid = new Point2D.Double[amount * amount];

        // STEP 1
        random = new Random(seed);
        int x = random.nextInt(size);
        int y = random.nextInt(size);


        int i = (int)Math.floor(x / cellSize);
        int j = (int)Math.floor(y / cellSize);
        grid[j * amount + i] = new Point2D.Double(x, y);
        activeGrid.add(new Point2D.Double(x, y));

        // STEP 2
        while (activeGrid.size() > 0){
            int index = random.nextInt(activeGrid.size());
            Point2D.Double pos = activeGrid.get(index);
            boolean found = false;

            for (int n = 0; n < k; n++) {
                double angle = random.nextDouble(2 * Math.PI);

                double offsetX = Math.cos(angle);
                double offsetY = Math.sin(angle);

                double magnitude =  (Math.sqrt(random.nextDouble(0,3) + 1) + 1) * minDistance; // random.nextDouble(minDistance, 2 * minDistance); // m.nextDouble(1) * (Math.pow(2*minDistance, 2) - Math.pow(minDistance, 2)) + Math.pow(minDistance, 2) );

                offsetX *= magnitude;
                offsetY *= magnitude;

                Point2D.Double sample = new Point2D.Double(pos.x + offsetX, pos.y + offsetY);

                int col = (int) Math.floor(sample.x / cellSize);
                int row = (int) Math.floor(sample.y / cellSize);

                if (col > -1 && row > -1 && col < amount && row < amount && grid[row * amount + col] == null) {
                    boolean ok = true;
                    for (int a = -2; a <= 2; a++) {
                        for (int b = -2; b <= 2; b++) {
                            Point2D.Double neighbor;
                            try {
                                neighbor = grid[(row + a) * amount + (col + b)];
                            } catch (IndexOutOfBoundsException e){ // this is because it can check an index out of range like -2 or 2 higher then the length
                                neighbor = null;
                            }

                            if (neighbor != null) {
                                double distanceSquared = Point2D.distanceSq(sample.x, sample.y, neighbor.x, neighbor.y);

                                if (distanceSquared < minDistance * minDistance) {
                                    ok = false;
                                }
                            }
                        }
                    }

                    if (ok) {
                        found = true;
                        grid[row * amount + col] = sample;
                        activeGrid.add(sample);
                    }
                }
            }

            if (!found){
                activeGrid.remove(index);
            }
        }

    }
}
