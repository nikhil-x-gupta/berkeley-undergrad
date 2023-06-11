package byow.lab13;

import edu.princeton.cs.introcs.StdDraw;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

public class MemoryGame {
    private int width;
    private int height;
    private int round;
    private Random rand;
    private boolean gameOver;
    private boolean playerTurn;
    private static final char[] CHARACTERS = "abcdefghijklmnopqrstuvwxyz".toCharArray();
    private static final String[] ENCOURAGEMENT = {"You can do this!", "I believe in you!",
                                                   "You got this!", "You're a star!", "Go Bears!",
                                                   "Too easy for you!", "Wow, so impressive!"};

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please enter a seed");
            return;
        }

        int seed = Integer.parseInt(args[0]);
        MemoryGame game = new MemoryGame(40, 40, seed);
        game.startGame();
    }

    public MemoryGame(int width, int height, int seed) {
        /* Sets up StdDraw so that it has a width by height grid of 16 by 16 squares as its canvas
         * Also sets up the scale so the top left is (0,0) and the bottom right is (width, height)
         */
        this.width = width;
        this.height = height;
        this.rand = new Random(seed);
        this.round = 0;
        this.gameOver = false;
        this.playerTurn = false;
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

        //TODO: Initialize random number generator
    }

    public String generateRandomString(int n) {
        //TODO: Generate random string of letters of length n
        String output = "";
        for (int i = 0; i < n; i += 1) {
            int index = rand.nextInt(26);
            output += CHARACTERS[index];
        }
        return output;
    }

    public void drawFrame(String s) {
        //TODO: Take the string and display it in the center of the screen
        //TODO: If game is not over, display relevant game information at the top of the screen
        //Clear canvas
        StdDraw.clear(StdDraw.BLACK);
        this.topBar();
        //Set font to be large and bold (size 30)
        Font font = new Font("Comic Sans", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setPenColor(StdDraw.WHITE);
        StdDraw.text(0.5 * this.width, 0.5 * this.height, s);
        StdDraw.show();
    }

    public void flashSequence(String letters) {
        //TODO: Display each character in letters, making sure to blank the screen between letters
        char[] charArray = letters.toCharArray();
        for (char c : charArray) {
            this.drawFrame(Character.toString(c));
            StdDraw.pause(1000);
            this.drawFrame("");
            StdDraw.pause(500);
        }
    }

    public String solicitNCharsInput(int n) {
        //TODO: Read n letters of player input
        String output = "";
        int numTyped = 0;
        while (numTyped < n) {
            if (StdDraw.hasNextKeyTyped()) {
                output += StdDraw.nextKeyTyped();
                this.drawFrame(output);
                numTyped += 1;
            }
        }
        return output;
    }

    public void topBar() {
        Font font = new Font("Comic Sans", Font.BOLD, 15);
        StdDraw.setFont(font);
        StdDraw.line(0, 0.95 * this.height, this.width, 0.95 * this.height);
        StdDraw.textLeft(0, 0.975 * this.height, "Round: " + this.round);
        int encIndex = this.rand.nextInt(ENCOURAGEMENT.length);
        StdDraw.textRight(this.width, 0.975 * this.height, ENCOURAGEMENT[encIndex]);
        if (playerTurn) {
            StdDraw.text(0.5 * this.width, 0.975 * this.height, "Type!");
        } else {
            StdDraw.text(0.5 * width, 0.975 * this.height, "Watch!");
        }
    }

    public void startGame() {
        //TODO: Set any relevant variables before the game starts
        //TODO: Establish Engine loop
        while (!this.gameOver) {
            this.playerTurn = false;
            this.round += 1;
            this.drawFrame("Round: " + this.round);
            StdDraw.pause(1000);
            String target = generateRandomString(this.round);
            this.flashSequence(target);
            this.playerTurn = true;
            this.drawFrame("");
            String player = solicitNCharsInput(this.round);
            this.gameOver = !player.equals(target);
            StdDraw.pause(500);
        }
        this.drawFrame("Game Over! You made it to round: " + this.round);
    }

}
