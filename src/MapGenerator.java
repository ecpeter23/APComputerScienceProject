import java.awt.*;
import java.util.Random;

public class MapGenerator {
    double[][] heightMap;
    double[][] moistureMap;
    double[][] falloffMap;
    Color[][] mapColors;
    int mapSize;
    int seed;
    double scale;
    double scaleMultiplier;
    int octaves;
    double persistence;
    double lacunarity;

    final boolean useFalloffMap = true; // if true the entire map is surrounded by water
    final boolean realTrees = true; // if false trees are just a black marker

    PoissonDiskGenerator treeGenerator;
    Random random;

    // COLORS:

    // BIOMES
    Color deepOcean = new Color(52, 123, 128);
    Color ocean = new Color(65, 155, 161);
    Color beach = new Color(164,148,116);
    Color desert = new Color(212,188,140);
    Color grasslands = new Color(112, 164, 79);
    Color tropicalSeasonedForest = new Color(96,146,94);
    Color tropicalRainForest = new Color(52,116,84);
    Color darkDesert = new Color(172, 113, 74);
    Color darkGrasslands = new Color(92, 135, 65);
    Color dryLightRocks = new Color(146, 74, 54);
    Color lightRocks = new Color(136,136,136);
    Color dryDarkRocks = new Color(58, 22, 14);
    Color darkRocks = new Color(85,85,85);
    Color snow = new Color(217, 215, 199);

    // TREES
    Color palmTree = new Color(111, 153, 64);
    Color pineTree = new Color(42, 47, 35);
    Color normalTree = new Color(34, 138, 11);
    Color treeTrunk = new Color(92, 67, 34);

    public MapGenerator(int seed, int mapSize){
        this.seed = seed;
        this.mapSize = mapSize;

        heightMap = new double[this.mapSize][this.mapSize];
        moistureMap = new double[this.mapSize][this.mapSize];
        mapColors = new Color[this.mapSize][this.mapSize];
        falloffMap = FalloffMapGenerator.generateFalloffMap(mapSize);

        treeGenerator = new PoissonDiskGenerator(5, mapSize - 50, seed);

        scale = 500d;
        scaleMultiplier = 10d;
        octaves = 18;
        persistence = 0.2d; // 0 < x < 1
        lacunarity = 5d; // 1 < x

        random = new Random(seed); // might wanna change seed by 1 or somthing
    }


    public void generateMap(int offsetX, int offsetY){
        heightMap = generateNoiseMap(offsetX, offsetY);
        moistureMap = generateMoisetureMap(offsetX, offsetY);

        mapColors = generateMapColors();
    }

    private double[][] generateNoiseMap(int offsetX, int offsetY){
        double[][] hMap = new double[mapSize][mapSize];

        Random oRandom = new Random(seed);
        Point[] octaveOffsets = new Point[octaves];

        double maxPossibleHeight = 0;
        double amplitude = 1;
        double frequency;
        double minValue = Integer.MAX_VALUE;
        double maxValue = Integer.MIN_VALUE;

        for (int i = 0; i < octaves; i++) {
            int oOffsetX = oRandom.nextInt(-100000, 100000) + offsetX;
            int oOffsetY = oRandom.nextInt(-100000, 100000) + offsetY;
            octaveOffsets[i] = new Point(oOffsetX, oOffsetY);

            maxPossibleHeight += amplitude;
            amplitude *= persistence;
        }

        double halfSize = mapSize / 2d;

        for(int y = 0; y < mapSize; y++){
            for (int x = 0; x < mapSize; x++){

                amplitude = 1;
                frequency = 1;
                float noiseHeight = 0;
                for (int i = 0; i < octaves; i++){
                    double sampleX = (x - halfSize + octaveOffsets[i].x) / scale * frequency;
                    double sampleY = (y - halfSize + octaveOffsets[i].y) / scale * frequency;

                    double noiseValue = OpenSimplex2.noise2(seed, sampleX, sampleY) * 2d - 1;
                    noiseHeight += (noiseValue * amplitude);

                    amplitude *= persistence;
                    frequency *= lacunarity;
                }

                if (noiseHeight > maxValue){
                    maxValue = noiseHeight;
                }

                if (noiseHeight < minValue){
                    minValue = noiseHeight;
                }

                hMap[x][y] = noiseHeight;
            }
        }

        for (int y = 0; y < mapSize; y++){
            for (int x = 0; x < mapSize; x++){
                hMap[x][y] = inverseLerp(minValue, maxValue, hMap[x][y]);
            }
        }

        return hMap;
    }

    private double[][] generateMoisetureMap(int offsetX, int offsetY){

        seed += 1;
        double[][] mMap = generateNoiseMap(offsetX, offsetY);
        seed -= 1;

        return mMap;
    }

    private Color[][] generateMapColors(){
        Color[][] cMap = new Color[mapSize][mapSize];

        for(int y = 0; y < mapSize; y++){
            for (int x = 0; x < mapSize; x++){
                if (useFalloffMap){
                    heightMap[x][y] = clamp01(heightMap[x][y] - falloffMap[x][y]);
                }

                if (heightMap[x][y] == 0){ // super deed ocean

                    cMap[x][y] = new Color(52, 123, 128); // new Color(68,68,123);

                } else if (heightMap[x][y] <= 0.1){ // deep ocean

                    cMap[x][y] = deepOcean;

                } else  if (heightMap[x][y] <= 0.3){ // ocean

                    cMap[x][y] = ocean;

                } else if (heightMap[x][y] <= 0.35){ // beach

                    cMap[x][y] = beach; // new Color(254, 196, 119);

                } else if (heightMap[x][y] <= 0.55){

                    if (moistureMap[x][y] <= 0.2){ // desert

                        cMap[x][y] = desert;

                    } else if (moistureMap[x][y] <= 0.4){

                        cMap[x][y] = grasslands; // grasslands

                    } else if (moistureMap[x][y] <= 0.65){

                        cMap[x][y] = tropicalSeasonedForest; // Tropical seasoned forest

                    } else{

                        cMap[x][y] = tropicalRainForest; // tropical rain forest

                    }
                } else if (heightMap[x][y] <= 0.7){ // dark grasslands / dark desert
                    if (moistureMap[x][y] <= 0.5){
                        cMap[x][y] = darkDesert;
                    } else {
                        cMap[x][y] = darkGrasslands;
                    }
                } else if (heightMap[x][y] <= 0.8){ // rocky

                    if (moistureMap[x][y] <= 0.4){
                        cMap[x][y] = dryLightRocks;
                    } else{
                        cMap[x][y] = lightRocks;
                    }

                } else if (heightMap[x][y] <= 0.9){ // Dark rocks
                    if (moistureMap[x][y] <= 0.4){
                        cMap[x][y] = dryDarkRocks;
                    } else {
                        cMap[x][y] = darkRocks;
                    }

                } else { // snow
                    cMap[x][y] = snow;
                }
            }
        }
        cMap = addTrees(cMap);

        return cMap;
    }

    private Color[][] addTrees(Color[][] colorMap){
        for (int i = 0; i < treeGenerator.grid.length; i++){
            if (treeGenerator.grid[i] != null){
                try {
                    boolean addTree = false;
                    Color colorMapColor = colorMap[(int) Math.floor(treeGenerator.grid[i].x)][(int) Math.floor(treeGenerator.grid[i].y)];
                    double r;
                    Color treeColor = normalTree;

                    if (colorMapColor.equals(beach)){
                        r = 0.3d;

                        if (random.nextDouble() <= r){
                            addTree = true; // IDEA have diffrent tree colors for diffrent biomes (palm trees for example)
                            treeColor = palmTree;
                        }
                    } else if (colorMapColor.equals(grasslands)){
                        r = 0.3d;

                        if (random.nextDouble() <= r){
                            addTree = true;
                            treeColor = normalTree;
                        }
                    } else if (colorMapColor.equals(tropicalSeasonedForest)){
                        r = 0.6d;

                        if (random.nextDouble() <= r){
                            addTree = true;
                            treeColor = normalTree;
                        }
                    } else if (colorMapColor.equals(tropicalRainForest)){
                        r = 0.6d;

                        if (random.nextDouble() <= r){
                            addTree = true;
                            treeColor = normalTree;
                        }
                    } else if (colorMapColor.equals(darkGrasslands)){
                        r = 0.4d;
                        if (random.nextDouble() <= r){
                            addTree = true;
                            treeColor = normalTree;
                        }
                    } else if (colorMapColor.equals(lightRocks)){
                        r = 0.2d;
                        if (random.nextDouble() <= r){
                            addTree = true;
                            treeColor = pineTree;
                        }
                    }

                    if (addTree) {
                        if (realTrees){
                            int tempX = (int) Math.floor(treeGenerator.grid[i].x);
                            int tempY = (int) Math.floor(treeGenerator.grid[i].y);

                            colorMap[tempX][tempY] = treeColor.darker();
                            colorMap[tempX + 1][tempY] = treeColor;
                            colorMap[tempX - 1][tempY] = treeColor;
                            colorMap[tempX][tempY + 1] = treeTrunk;
                            colorMap[tempX][tempY - 1] = treeColor;

                        } else {
                            colorMap[(int) Math.floor(treeGenerator.grid[i].x)][(int) Math.floor(treeGenerator.grid[i].y)] = new Color(0, 0, 0);
                        }
                    }
                } catch (IndexOutOfBoundsException ignored){

                }
            }
        }

        return  colorMap;
    }

    public Color[][] getColorMap(){
        return mapColors;
    }

    private double inverseLerp(double min, double max, double value){
        return (value - min) / (max - min);
    }

    private double clamp01(double value){
        if (value < 0){
            return 0;
        } else if (value > 1){
            return 1;
        } else{
            return value;
        }
    }
}
