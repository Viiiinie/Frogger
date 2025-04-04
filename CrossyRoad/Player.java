
import java.awt.*;

public class Player {
    private int x, y;
    private final int width = 50;
    private final int height = 50;
    
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }
    
    public void moveWithObstacle(int speed) {
        x += speed;
    }
    
    public boolean intersects(Obstacle other) {
        return x < other.getX() + other.getWidth() &&
               x + width > other.getX() &&
               y < other.getY() + other.getHeight() &&
               y + height > other.getY();
    }
    
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, width, height);
    }
    
    public void reset(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    // Getter-Methoden
    public int getX() { return x; }
    public int getY() { return y; }
}