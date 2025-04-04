import java.awt.*;

public class Obstacle {
    private int x, y;
    private int width, height;
    private int speed;
    
    public Obstacle(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }
    
    public void move() {
        x += speed;
    }
    
    public void draw(Graphics g) {
        g.fillRect(x, y, width, height);
    }
    
    // Getter-Methoden
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSpeed() { return speed; }
}