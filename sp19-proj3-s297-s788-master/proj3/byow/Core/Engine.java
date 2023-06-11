package byow.Core;

import byow.InputDemo.StringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.In;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

public class Engine {
    /* Feel free to change the width and height. */
    private static final int WIDTH = 80;
    private static final int HEIGHT = 30;
    private static final int MIN_SIZE = 4;
    private static final int MAX_SIZE = 10;
    private static final int AREA_THRESHOLD = (int) (0.75 * WIDTH * HEIGHT);
    private static final int MAX_ATTEMPTS = 10;

    private static final String SEEDS = "0123456789s";
    private static final String VALID_MOVES = "wasd:q";

    TERenderer ter = new TERenderer();
    private long seed = 0;
    private Random random;
    private int areaCovered = 0;
    private StringInputDevice sid;
    private String gameString = "";
    private Player player;
    private TETile playerOn;

        /*
        Possible extra thing: have energy that is used up per move,
        and in world generation, randomly generate "food" in some floor tiles
        that replenish movement energy
         */

        /*
        Gold Points (3 mechanics plus win/lose)
            -collect something to win
            -moving enemies counts for 1 mechanic
         */

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {

        this.sid = new StringInputDevice(input.toLowerCase());
        char firstChar = sid.getNextKey();
        if (firstChar == 'l') {
            String oldInput = loadGame();
            input = oldInput + input.substring(input.indexOf('l') + 1);
            this.sid = new StringInputDevice(input.toLowerCase());
            firstChar = sid.getNextKey();
        }
        if (firstChar == 'n') {
            this.getSeed(sid);
        }
        this.random = new Random(this.seed);
        TETile[][] finalWorldFrame = new TETile[WIDTH][HEIGHT];
        buildWorld(finalWorldFrame);
        while (sid.possibleNextInput()) {
            char curr = sid.getNextKey();
            if (curr == ':' && sid.getNextKey() == 'q') {
                saveGame(input);
            } else {
                doAction(curr, finalWorldFrame);
            }
        }
//        ter.renderFrame(finalWorldFrame);
        return finalWorldFrame;
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        this.setUpCanvas();
        TETile[][] world = new TETile[WIDTH][HEIGHT];
        this.showMenu();
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                switch (c) {
                    case 'n':
                        this.gameString += c;
                        this.gameString += this.seedPrompt();
                        this.sid = new StringInputDevice(this.gameString);
                        this.getSeed(sid);
                        this.random = new Random(this.seed);
                        buildWorld(world);
                        break;
                    case 'l':
                        this.gameString = this.loadGame();
                        world = this.interactWithInputString(gameString);
                        break;
                    case 'r':
                        this.gameString = this.loadGame();
                        this.sid = new StringInputDevice(gameString);
                        this.getSeed(sid);
                        this.random = new Random(this.seed);
                        world = new TETile[WIDTH][HEIGHT];
                        buildWorld(world);
                        while (sid.possibleNextInput()) {
                            this.hud(world);
                            char curr = sid.getNextKey();
                            doAction(curr, world);
                            ter.renderFrame(world);
                            try {
                                Thread.sleep(175);
                            } catch (InterruptedException ie) {
                                ie.printStackTrace();
                            }
                        }
                        break;
                    case 'q':
                        System.exit(0);
                        break;
                    default:
                        continue;
                }
                break;
            }
        }
        boolean tryingtoQuit = false;
        while (true) {
            this.hud(world);
            ter.renderFrame(world);
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (VALID_MOVES.contains(Character.toString(c))) {
                    this.gameString += c;
                    if (c == ':') {
                        tryingtoQuit = true;
                    } else if (c == 'q' && tryingtoQuit) {
                        this.saveGame(this.gameString);
                        System.exit(0);
                    } else {
                        tryingtoQuit = false;
                        this.doAction(c, world);
                    }
                }
                ter.renderFrame(world);
            }
        }
    }

    /** Clear canvas and set up basic StdDraw parameters */
    private void setUpCanvas() {
        StdDraw.setCanvasSize(WIDTH * 16, (HEIGHT + 3) * 16);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT + 3);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();
        Font font = new Font("Comic Sans", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
    }

    /** Display initial menu */
    private void showMenu() {
        StdDraw.text(0.5 * WIDTH, 0.8 * HEIGHT, "CS61B: The Game");
        StdDraw.text(0.5 * WIDTH, 0.6 * HEIGHT, "[N]ew Game");
        StdDraw.text(0.5 * WIDTH, 0.5 * HEIGHT, "[L]oad Game");
        StdDraw.text(0.5 * WIDTH, 0.4 * HEIGHT, "[R]eplay Last Save");
        StdDraw.text(0.5 * WIDTH, 0.3 * HEIGHT, "[Q]uit");
        StdDraw.show();
    }

    /** Display prompt to enter seed and return entered string */
    private String seedPrompt() {
        String output = "";
        char nextChar = 'z';
        StdDraw.clear(StdDraw.BLACK);
        StdDraw.text(0.5 * WIDTH, 0.55 * HEIGHT, "Enter seed (press S to confirm):");
        StdDraw.show();
        while (nextChar != 's') {
            if (StdDraw.hasNextKeyTyped()) {
                nextChar = Character.toLowerCase(StdDraw.nextKeyTyped());
                if (SEEDS.contains(Character.toString(nextChar))) {
                    StdDraw.clear(StdDraw.BLACK);
                    StdDraw.text(0.5 * WIDTH, 0.55 * HEIGHT, "Enter seed (press S to confirm):");
                    output += nextChar;
                    StdDraw.text(0.5 * WIDTH, 0.45 * HEIGHT, output);
                    StdDraw.show();
                }
            }
        }
        return output;
    }

    /** Displays HUD on top of screen */
    private void hud(TETile[][] world) {
        double textY = HEIGHT + 1.75;
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.filledRectangle(0.5 * WIDTH, textY, 0.5 * WIDTH, 1.25);
        int tileX = (int) (StdDraw.mouseX());
        int tileY = (int) (StdDraw.mouseY());
        String mouseTile = "";
        if (tileY < HEIGHT) {
            mouseTile = world[tileX][tileY].description();
        }
        Font font = new Font("Comic Sans", Font.PLAIN, 15);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.line(0, HEIGHT + 0.5, WIDTH, HEIGHT + 0.5);
//        StdDraw.text(0.5 * WIDTH, textY, "Health: ");
        StdDraw.textLeft(0.05 * WIDTH, textY, mouseTile);
        LocalDateTime ldt = LocalDateTime.now();
        String dateTime = ldt.getDayOfWeek().toString() + ", " + ldt.getMonth().toString() + " "
                + ldt.getDayOfMonth() + "  " + ldt.getHour() + ":" + ldt.getMinute();
        StdDraw.textRight(0.95 * WIDTH, textY, dateTime);
        StdDraw.show();
    }

    /** Get seed from an SID and set this.seed
     * @param inputDevice a StringInputDevice starting with n<seed>s... or <seed>s...*/
    private void getSeed(StringInputDevice inputDevice) {
        char nextChar = inputDevice.getNextKey();
        while (nextChar != 's') {
            if (nextChar != 'n') {
                this.seed = this.seed * 10 + (long) Character.getNumericValue(nextChar);
            }
            nextChar = inputDevice.getNextKey();
        }
    }

    /**
     * Saves input string to savefile.txt
     *
     * @source SaveDemo/Main.java, StackOverflow
     */
    private void saveGame(String input) {
        String saveString = input.substring(0, input.indexOf(":q"));
        try (PrintWriter p = new PrintWriter(new FileOutputStream("./savefile.txt", false))) {
            p.println(saveString);
        } catch (FileNotFoundException e1) {
            System.out.println("Savefile not found");
        }
    }

    /**
     * Loads game using input string from savefile.txt
     */
    private String loadGame() {
        In in = new In("./savefile.txt");
        return in.readString();
    }

    /** Does action specified by MOVE
     * MOVE must be in VALID_MOVES
     * */
    private void doAction(Character move, TETile[][] world) {
        int lastX = player.xPos, lastY = player.yPos;
        int nextX = lastX, nextY = lastY;
        switch (move) {
            case 'w':
                nextY = lastY + 1;
                break;
            case 'a':
                nextX = lastX - 1;
                break;
            case 's':
                nextY = lastY - 1;
                break;
            case 'd':
                nextX = lastX + 1;
                break;
            default:
                break;
        }
        if (canMove(world, nextX, nextY)) {
            player.moveToPos(nextX, nextY);
            world[lastX][lastY] = new TETile(playerOn);
            playerOn = new TETile(world[player.xPos][player.yPos]);
            world[player.xPos][player.yPos] = Tileset.AVATAR;
        }
    }

    /** Returns whether xPos, yPos is a valid tile for player to move on to
     * Checks if tile is in bounds of the world
     * Checks if the tile to move on is not Tileset.WALL */
    private boolean canMove(TETile[][] world, int xPos, int yPos) {
        boolean inBounds = (xPos >= 0 && xPos < WIDTH && yPos >= 0 && yPos < HEIGHT);
        if (inBounds) {
            return !world[xPos][yPos].equals(Tileset.WALL);
        }
        return false;
    }

    /**
     * Generates rooms and hallways in WORLD
     */
    private void buildWorld(TETile[][] world) {
//        ter.initialize(WIDTH, HEIGHT + 3); // 3 tiles of space on top
        int xPos = RandomUtils.uniform(this.random, MAX_SIZE, WIDTH - MAX_SIZE);
        int yPos = RandomUtils.uniform(this.random, MAX_SIZE, HEIGHT - MAX_SIZE);
        this.initializeEmpty(world);
        Position start = new Position(xPos, yPos);
        Opening initOP = new Opening(start, Direction.LEFT);
        Opening[] initOpenings = addRoomFromOpening(world, initOP, 1);
        LinkedList<Opening> roomOpenings = new LinkedList<>(Arrays.asList(initOpenings));
        LinkedList<Opening> hallOpenings = new LinkedList<>();
        LinkedList<Opening> sideOpenings = new LinkedList<>();
        while (this.areaCovered < AREA_THRESHOLD && !roomOpenings.isEmpty()) {

            while (!roomOpenings.isEmpty()) {
                Opening hallStart = roomOpenings.pop();
                LinkedList<Opening> allOpenings = generateHallway(world, hallStart, 1);
                if (allOpenings != null && !allOpenings.isEmpty()) {
                    hallOpenings.add(allOpenings.pop());
                    sideOpenings.addAll(allOpenings);
                }
            }
            while (!sideOpenings.isEmpty()) {
                Opening sideOp = sideOpenings.pop();
                LinkedList<Opening> allOpenings = generateHallway(world, sideOp, 1);
                if (allOpenings != null && !allOpenings.isEmpty()) {
                    hallOpenings.add(allOpenings.pop());
                    sideOpenings.addAll(allOpenings);
                }
            }
            while (!hallOpenings.isEmpty()) {
                Opening op = hallOpenings.pop();
                if (op != null) {
                    Opening[] newOpenings = addRoomFromOpening(world, op, 1);
                    if (newOpenings != null) {
                        roomOpenings.addAll(Arrays.asList(newOpenings));
                    }
                }
            }
        }
        if (world[xPos][yPos].equals(Tileset.WALL)) {
            world[xPos][yPos] = Tileset.FLOOR;
        }
        playerOn = new TETile(world[xPos][yPos]);
        world[xPos][yPos] = Tileset.AVATAR;
        player = new Player(xPos, yPos);
//        ter.renderFrame(world);
    }

    /**
     * Fills world with Tileset.NOTHING tiles
     */
    private void initializeEmpty(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    /**
     * Adds a room in WORLD given an opening OP
     *
     * @return Array of openings in the room
     */
    private Opening[] addRoomFromOpening(TETile[][] world, Opening op, int attempt) {
        Position start = op.p.copy();
        int width = RandomUtils.uniform(this.random, MIN_SIZE, MAX_SIZE);
        int height = RandomUtils.uniform(this.random, MIN_SIZE, MAX_SIZE);
        Opening[] openings;
        int shift;
        switch (op.dir) {
            case LEFT:
                shift = RandomUtils.uniform(random, 1, height - 1);
                start.x -= width;
                start.y -= shift;
                openings = generateRoom(world, start, width, height);
                if (openings != null) {
                    world[op.p.x - 1][op.p.y] = Tileset.FLOOR;
                } else if (attempt < MAX_ATTEMPTS) {
                    openings = addRoomFromOpening(world, op, attempt + 1);
                } else {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                return openings;
            case UP:
                shift = RandomUtils.uniform(random, 1, width - 1);
                start.x -= shift;
                start.y += 1;
                openings = generateRoom(world, start, width, height);
                if (openings != null) {
                    world[op.p.x][op.p.y + 1] = Tileset.FLOOR;
                } else if (attempt < MAX_ATTEMPTS) {
                    openings = addRoomFromOpening(world, op, attempt + 1);
                } else {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                return openings;
            case RIGHT:
                shift = RandomUtils.uniform(random, 1, height - 1);
                start.x += 1;
                start.y -= shift;
                openings = generateRoom(world, start, width, height);
                if (openings != null) {
                    world[op.p.x + 1][op.p.y] = Tileset.FLOOR;
                } else if (attempt < MAX_ATTEMPTS) {
                    openings = addRoomFromOpening(world, op, attempt + 1);
                } else {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                return openings;
            case DOWN:
                shift = RandomUtils.uniform(random, 1, width - 1);
                start.x -= shift;
                start.y -= height;
                openings = generateRoom(world, start, width, height);
                if (openings != null) {
                    world[op.p.x][op.p.y - 1] = Tileset.FLOOR;
                } else if (attempt < MAX_ATTEMPTS) {
                    openings = addRoomFromOpening(world, op, attempt + 1);
                } else {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                return openings;
            default:
                return null;
        }
    }

    /**
     * Returns whether all tiles for a room from position P
     * with width W and height H are Tileset.NOTHING
     * Position P is the bottom-left corner of the room
     */
    private boolean canRoomExist(TETile[][] world, Position p, int w, int h) {
        if (p.x + w - 1 >= WIDTH || p.y + h - 1 >= HEIGHT || p.x < 0 || p.y < 0) {
            return false;
        }
        for (int i = p.x; i < p.x + w; i += 1) {
            for (int j = p.y; j < p.y + h; j += 1) {
                if (!world[i][j].equals(Tileset.NOTHING)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Generates a wall from START to END
     * START and END must be in a line
     * START must be to the left of or below END
     */
    private void generateWall(TETile[][] world, Position start, Position end) {
        Position s = start.copy();
        Position e = end.copy();
        if (s.x == e.x) {
            while (s.y <= e.y) {
                world[s.x][s.y] = Tileset.WALL;
                s.y += 1;
            }
        } else if (s.y == e.y) {
            while (s.x <= e.x) {
                world[s.x][s.y] = Tileset.WALL;
                s.x += 1;
            }
        }
    }

    /**
     * Fills a room between BL and TR with Tileset.FLOOR
     * BL is the bottom-left corner and TR the top-right corner
     * of the room's floorspace (not including walls)
     */
    private void fillFloor(TETile[][] world, Position bl, Position tr) {
        Position curr = bl.copy();
        while (curr.y <= tr.y) {
            while (curr.x <= tr.x) {
                world[curr.x][curr.y] = Tileset.FLOOR;
                curr.x += 1;
            }
            curr.x = bl.x;
            curr.y += 1;
        }
    }

    /**
     * Generate numOpenings openings in room with the given corners
     * @return Array of openings in the room
     */
    private Opening[] generateRoomOpenings(TETile[][] world, int numOpenings,
                                           Position bl, Position tl, Position tr, Position br) {
        int[] order = {1, 2, 3, 4};
        RandomUtils.shuffle(random, order);
        Opening[] openings = new Opening[numOpenings];
        for (int i = 0; i < numOpenings; i += 1) {
            int wall = order[i];
            switch (wall) {
                case 1: //Left wall
                    openings[i] = new Opening(makeWallOpening(world, bl, tl), Direction.LEFT);
                    break;
                case 2: //Top wall
                    openings[i] = new Opening(makeWallOpening(world, tl, tr), Direction.UP);
                    break;
                case 3: //Right wall
                    openings[i] = new Opening(makeWallOpening(world, br, tr), Direction.RIGHT);
                    break;
                case 4: //Bottom wall
                    openings[i] = new Opening(makeWallOpening(world, bl, br), Direction.DOWN);
                    break;
                default:
                    break;
            }
        }
        return openings;
    }

    /**
     * Generates a random opening on a wall between START and END
     * START should be to the left of or below END
     *
     * @return Position of opening in the wall
     */
    private Position makeWallOpening(TETile[][] world, Position start, Position end) {
        Position opening;
        if (start.x == end.x) {
            int openY = RandomUtils.uniform(random, start.y + 1, end.y);
            opening = new Position(start.x, openY);
        } else {
            int openX = RandomUtils.uniform(random, start.x + 1, end.x);
            opening = new Position(openX, start.y);
        }
        world[opening.x][opening.y] = Tileset.FLOOR;
        return opening;
    }

    /**
     * Blocks opening if it doesn't lead to another room or hallway
     */
    private void blockOpening(TETile[][] world, Opening op) {
        switch (op.dir) {
            case LEFT:
                if (op.p.x == 0 || world[op.p.x - 1][op.p.y].equals(Tileset.NOTHING)) {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                break;
            case UP:
                if (op.p.y + 1 == HEIGHT || world[op.p.x][op.p.y + 1].equals(Tileset.NOTHING)) {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                break;
            case RIGHT:
                if (op.p.x + 1 == WIDTH || world[op.p.x + 1][op.p.y].equals(Tileset.NOTHING)) {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                break;
            case DOWN:
                if (op.p.y == 0 || world[op.p.x][op.p.y - 1].equals(Tileset.NOTHING)) {
                    world[op.p.x][op.p.y] = Tileset.WALL;
                }
                break;
            default:
                break;
        }
    }

    /**
     * Genrates a room from position P with given WIDTH and HEIGHT
     * P is the bottom-left corner of the room
     * WIDTH and HEIGHT are random integers between MIN_SIZE and MAX_SIZE
     *
     * @return Array of openings in the room
     */
    private Opening[] generateRoom(TETile[][] world, Position p, int width, int height) {

        if (!canRoomExist(world, p, width, height)) {
            return null;
        }

        Position bl = p.copy();
        Position tl = new Position(bl.x, bl.y + height - 1);
        Position tr = new Position(tl.x + width - 1, tl.y);
        Position br = new Position(tr.x, bl.y);

        generateWall(world, bl, tl);
        generateWall(world, tl, tr);
        generateWall(world, bl, br);
        generateWall(world, br, tr);

        Position floorBL = new Position(bl.x + 1, bl.y + 1);
        Position floorTR = new Position(tr.x - 1, tr.y - 1);
        fillFloor(world, floorBL, floorTR);

        this.areaCovered += width * height;
        int numOpenings = RandomUtils.uniform(this.random, 2, 5);
        return generateRoomOpenings(world, numOpenings, bl, tl, tr, br);
    }

    /**
     * Randomly open a wall 20% of the time
     * Adds the opening, if any, to the OPENINGS LinkedList
     */
    private void openWall(TETile[][] world, Position start, Position end,
                          Direction dir, LinkedList<Opening> openings) {
        double openProb = RandomUtils.uniform(random);
        double openThresh = 0.8;
        if (openProb > openThresh) {
            Opening op = new Opening(makeWallOpening(world, start, end), dir);
            openings.add(op);
        }
    }

    /**
     * Finds info of hallway based on Opening direction
     *
     * @return Map<String, Object> of info
     *          "start" -> start Position for generating hallway
     *          "end" -> end Position for generating hallway
     *          "hori" -> horizontal dimension of hallway
     *          "vert" -> vertical dimension of hallway
     *          "length" -> length of hallway
     *
     * */
    private Map<String, Object> findHallInfo(Opening op) {
        HashMap<String, Object> hallInfo = new HashMap<>();
        int length = RandomUtils.uniform(this.random, MIN_SIZE, MAX_SIZE);
        Position start = op.p.copy();
        Position end = op.p.copy();
        int hori = 3;
        int vert = length;
        switch (op.dir) {
            case LEFT:
                start.x -= length;
                start.y -= 1;
                end.x -= 1;
                end.y -= 1;
                hori = length;
                vert = 3;
                break;
            case UP:
                start.x -= 1;
                start.y += 1;
                end.x -= 1;
                end.y += length;
                break;
            case RIGHT:
                start.x += 1;
                start.y -= 1;
                end.x += length;
                end.y -= 1;
                hori = length;
                vert = 3;
                break;
            case DOWN:
                start.x -= 1;
                start.y -= length;
                end.x -= 1;
                end.y -= 1;
                break;
            default:
                break;
        }
        hallInfo.put("start", start);
        hallInfo.put("end", end);
        hallInfo.put("hori", hori);
        hallInfo.put("vert", vert);
        hallInfo.put("length", length);
        return hallInfo;
    }

    /**
     * Generate a hallway from Opening
     *
     * @return List of openings in the hallway
     *      First opening is the other end of the hallway (for another room)
     *      Following openings are any on the sides to generate intersecting hallways
     */
    private LinkedList<Opening> generateHallway(TETile[][] world, Opening op, int attempt) {
        LinkedList<Opening> openings = new LinkedList<>();
        Opening endOpening = null;
        HashMap<String, Object> hallInfo = (HashMap<String, Object>) findHallInfo(op);
        int length = (int) hallInfo.get("length");
        Position start = (Position) hallInfo.get("start");
        Position end = (Position) hallInfo.get("end");
        int hori = (int) hallInfo.get("hori");
        int vert = (int) hallInfo.get("vert");

        if (!canRoomExist(world, start, hori, vert) && attempt < MAX_ATTEMPTS) {
            return generateHallway(world, op, attempt + 1);
        } else if (!canRoomExist(world, start, hori, vert)) {
            blockOpening(world, op);
            return null;
        }
        switch (op.dir) {
            case LEFT:
                generateWall(world, start, end);
                openWall(world, start, end, Direction.DOWN, openings);
                start.y += 1;
                end.y += 1;
                fillFloor(world, start, end);
                endOpening = new Opening(start.copy(), Direction.LEFT);
                start.y += 1;
                end.y += 1;
                generateWall(world, start, end);
                openWall(world, start, end, Direction.UP, openings);
                break;
            case UP:
                generateWall(world, start, end);
                openWall(world, start, end, Direction.LEFT, openings);
                start.x += 1;
                end.x += 1;
                fillFloor(world, start, end);
                endOpening = new Opening(end.copy(), Direction.UP);
                start.x += 1;
                end.x += 1;
                generateWall(world, start, end);
                openWall(world, start, end, Direction.RIGHT, openings);
                break;
            case RIGHT:
                generateWall(world, start, end);
                openWall(world, start, end, Direction.DOWN, openings);
                start.y += 1;
                end.y += 1;
                fillFloor(world, start, end);
                endOpening = new Opening(end.copy(), Direction.RIGHT);
                start.y += 1;
                end.y += 1;
                generateWall(world, start, end);
                openWall(world, start, end, Direction.UP, openings);
                break;
            case DOWN:
                generateWall(world, start, end);
                openWall(world, start, end, Direction.UP, openings);
                start.x += 1;
                end.x += 1;
                fillFloor(world, start, end);
                endOpening = new Opening(start.copy(), Direction.DOWN);
                start.x += 1;
                end.x += 1;
                generateWall(world, start, end);
                openWall(world, start, end, Direction.RIGHT, openings);
                break;
            default:
                return null;
        }
        openings.addFirst(endOpening);
        areaCovered += 3 * length;
        return openings;
    }

    enum Direction {
        /** For direction of openings */
        LEFT, UP, RIGHT, DOWN
    }

    private static class Position {
        int x;
        int y;

        private Position(int xGiven, int yGiven) {
            x = xGiven;
            y = yGiven;
        }

        private Position copy() {
            return new Position(this.x, this.y);
        }
    }

    private static class Opening {
        Position p;
        Direction dir;

        private Opening(Position pGiven, Direction dirGiven) {
            p = pGiven;
            dir = dirGiven;
        }
    }
}
