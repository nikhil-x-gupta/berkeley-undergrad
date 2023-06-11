package byow.Core;

public class Creature {

    int xPos;
    int yPos;

    public Creature(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    public void moveToPos(int givenX, int givenY) {
        this.xPos = givenX;
        this.yPos = givenY;
    }
}
