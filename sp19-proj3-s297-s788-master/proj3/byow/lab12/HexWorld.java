package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 * @source Josh Hug, RandomWorldDemo.java
 */
public class HexWorld {

    private static final int WIDTH = 120;
    private static final int HEIGHT = 80;
    private static final long SEED = 2813123;
    private static final Random RANDOM = new Random(SEED);

    private static class Position {
        int x;
        int y;

        private Position(int xGiven, int yGiven) {
            x = xGiven;
            y = yGiven;
        }
    }

    /** Fills world with Tileset.NOTHING tiles */
    public static void initializeEmpty(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Draws a hexagon of size S, made of tile T at position P in the WORLD */
    public static void addHexagon(TETile[][] world, Position p, int s, TETile t) {

        int biggestLine = s + 2 * (s - 1);
        int lineWidth = s;
        int currX = p.x;
        int currY = p.y;

        drawBottom(world, lineWidth, biggestLine, currX, currY, t);
        lineWidth = biggestLine;
        currX = currX - (s - 1);
        currY = currY + s;
        drawTop(world, lineWidth, s, currX, currY, t);
    }

    /** Draw bottom half of a hexagon */
    private static void drawBottom(TETile[][] world, int lineWidth, int biggestLine,
                                   int currX, int currY, TETile t) {
        while (lineWidth <= biggestLine) {
            for (int i = 0; i < lineWidth; i += 1) {
                world[currX + i][currY] = t;
            }
            currX -= 1;
            currY += 1;
            lineWidth += 2;
        }
    }

    /** Draw top half of a hexagon */
    private static void drawTop(TETile[][] world, int lineWidth, int s,
                                int currX, int currY, TETile t) {
        while (lineWidth >= s) {
            for (int i = 0; i < lineWidth; i += 1) {
                world[currX + i][currY] = t;
            }
            currX += 1;
            currY += 1;
            lineWidth -= 2;
        }
    }

    /** Draw a column of N hexagons of size S in the WORLD starting at position P*/
    public static void drawHexColumn(TETile[][] world, Position pGiven, int N, int s) {
        Position p = new Position(pGiven.x, pGiven.y);
        for (int i = 0; i < N; i += 1) {
            TETile t = randomTile();
            addHexagon(world, p, s, t);
            p.y += 2 * s;
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being grass.
     * @source Josh Hug, RandomWorldDemo.java
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(3);
        switch (tileNum) {
            case 0: return Tileset.WALL;
            case 1: return Tileset.FLOWER;
            case 2: return Tileset.GRASS;
            default: return Tileset.NOTHING;
        }
    }

    /** Gets bottom-left position of bottom right neighbor of hexagon at position P of size S */
    private static Position getBotRightNeighbor(Position p, int s) {
        int newX = p.x + 2 * s - 1;
        int newY = p.y - s;
        return new Position(newX, newY);
    }

    /** Gets top-left position of bottom right neighbor of hexagon at position P of size S */
    private static Position getTopRightNeighbor(Position p, int s) {
        int newX = p.x + 2 * s - 1;
        int newY = p.y + s;
        return new Position(newX, newY);
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] hexWorld = new TETile[WIDTH][HEIGHT];
        initializeEmpty(hexWorld);
        ter.renderFrame(hexWorld);
        Position p = new Position(30, 30);
        int size = 3;
        drawHexColumn(hexWorld, p, 3, size);
        p = getBotRightNeighbor(p, size);
        drawHexColumn(hexWorld, p, 4, size);
        p = getBotRightNeighbor(p, size);
        drawHexColumn(hexWorld, p, 5, size);
        p = getTopRightNeighbor(p, size);
        drawHexColumn(hexWorld, p, 4, size);
        p = getTopRightNeighbor(p, size);
        drawHexColumn(hexWorld, p, 3, size);
        ter.renderFrame(hexWorld);
    }

}
