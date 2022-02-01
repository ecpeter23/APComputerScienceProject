public class FalloffMapGenerator {

    public static double[][] generateFalloffMap(int size){
        double[][] map = new double[size][size];

        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                double y = i / (double)size * 2 - 1;
                double x = j / (double)size * 2 - 1;

                double value = Math.max(Math.abs(x), Math.abs(y));
                map[j][i] = evaluate(value);
            }
        }

        return map;
    }

    private static double evaluate(double value){
        double a = 2;
        double b = 2.2d;

        return (Math.pow(value, a) / (Math.pow(value, a) + Math.pow(b - b * value, a)));
    }
}
