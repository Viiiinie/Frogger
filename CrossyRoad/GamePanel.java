import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;

public class GamePanel extends JPanel implements KeyListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int PLAYER_SIZE = 50;
    private static final int OBSTACLE_HEIGHT = 40;
    private static final int ROAD_MARKER_HEIGHT = 50;
    
    private final Player player;
    private final ArrayList<Obstacle> cars;
    private final ArrayList<Obstacle> logs;
    private int score;
    private boolean gameOver;
    private final Timer gameTimer;
    private final int roadYPosition;
    
    public GamePanel() {
        // Set up panel
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);
        
        // Initialize game elements
        roadYPosition = PANEL_HEIGHT / 2;
        player = new Player(PANEL_WIDTH / 2 - PLAYER_SIZE / 2, PANEL_HEIGHT - PLAYER_SIZE);
        cars = new ArrayList<>();
        logs = new ArrayList<>();
        
        // Initialize game state
        resetGame();
        
        // Set up game timer
        gameTimer = new Timer(50, e -> {
            if (!gameOver) {
                updateGame();
                repaint();
            }
        });
        gameTimer.start();
    }
    
    private void resetGame() {
        player.reset(PANEL_WIDTH / 2 - PLAYER_SIZE / 2, PANEL_HEIGHT - PLAYER_SIZE);
        score = 0;
        gameOver = false;
        cars.clear();
        logs.clear();
        
        // Create initial obstacles
        for (int i = 0; i < 5; i++) {
            addCar();
            addLog();
        }
    }
    
    private void addCar() {
        int y = (int) (Math.random() * (roadYPosition - PLAYER_SIZE)) + PLAYER_SIZE;
        int width = (int) (Math.random() * 3 + 2) * PLAYER_SIZE;
        int speed = (int) (Math.random() * 3 + 1);
        
        if (Math.random() < 0.5) {
            cars.add(new Obstacle(-width, y, width, OBSTACLE_HEIGHT, speed));
        } else {
            cars.add(new Obstacle(PANEL_WIDTH, y, width, OBSTACLE_HEIGHT, -speed));
        }
    }
    
    private void addLog() {
        int y = (int) (Math.random() * (roadYPosition - PLAYER_SIZE));
        int width = (int) (Math.random() * 2 + 3) * PLAYER_SIZE;
        int speed = (int) (Math.random() * 2 + 1);
        
        if (Math.random() < 0.5) {
            logs.add(new Obstacle(-width, y, width, OBSTACLE_HEIGHT, speed));
        } else {
            logs.add(new Obstacle(PANEL_WIDTH, y, width, OBSTACLE_HEIGHT, -speed));
        }
    }
    
    private void updateGame() {
        moveObstacles(cars);
        moveObstacles(logs);
        checkCollisions();
        checkWinCondition();
    }
    
    private void moveObstacles(ArrayList<Obstacle> obstacles) {
        Iterator<Obstacle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Obstacle obs = iterator.next();
            obs.move();
            
            if ((obs.getSpeed() > 0 && obs.getX() > PANEL_WIDTH) || 
                (obs.getSpeed() < 0 && obs.getX() + obs.getWidth() < 0)) {
                iterator.remove();
                if (obs.getY() < roadYPosition) {
                    addLog();
                } else {
                    addCar();
                }
            }
        }
    }
    
    private void checkCollisions() {
        // Check car collisions (road area)
        if (player.getY() > roadYPosition) {
            for (Obstacle car : cars) {
                if (player.intersects(car)) {
                    gameOver = true;
                    return;
                }
            }
        }
        
        // Check water collisions (river area)
        if (player.getY() < roadYPosition) {
            boolean onLog = false;
            for (Obstacle log : logs) {
                if (player.intersects(log)) {
                    onLog = true;
                    player.moveWithObstacle(log.getSpeed());
                    break;
                }
            }
            
            if (!onLog) {
                gameOver = true;
            }
        }
    }
    
    private void checkWinCondition() {
        if (player.getY() < 0) {
            score++;
            player.reset(PANEL_WIDTH / 2 - PLAYER_SIZE / 2, PANEL_HEIGHT - PLAYER_SIZE);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw background
        drawBackground(g);
        
        // Draw obstacles
        drawObstacles(g, cars, Color.RED);
        drawObstacles(g, logs, new Color(139, 69, 19));
        
        // Draw player
        player.draw(g);
        
        // Draw UI
        drawUI(g);
    }
    
    private void drawBackground(Graphics g) {
        // Water (top half)
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, PANEL_WIDTH, roadYPosition);
        
        // Road
        g.setColor(Color.GRAY);
        g.fillRect(0, roadYPosition - ROAD_MARKER_HEIGHT, PANEL_WIDTH, ROAD_MARKER_HEIGHT);
        
        // Grass (bottom half)
        g.setColor(Color.GREEN);
        g.fillRect(0, roadYPosition, PANEL_WIDTH, PANEL_HEIGHT - roadYPosition);
    }
    
    private void drawObstacles(Graphics g, ArrayList<Obstacle> obstacles, Color color) {
        g.setColor(color);
        for (Obstacle obs : obstacles) {
            obs.draw(g);
        }
    }
    
    private void drawUI(Graphics g) {
        // Score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        
        // Game over message
        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", PANEL_WIDTH / 2 - 150, PANEL_HEIGHT / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Press SPACE to restart", PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 + 50);
        }
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_SPACE) {
            resetGame();
        } else if (!gameOver) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    player.move(0, -PLAYER_SIZE);
                    break;
                case KeyEvent.VK_DOWN:
                    if (player.getY() < PANEL_HEIGHT - PLAYER_SIZE) {
                        player.move(0, PLAYER_SIZE);
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    if (player.getX() > 0) {
                        player.move(-PLAYER_SIZE, 0);
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (player.getX() < PANEL_WIDTH - PLAYER_SIZE) {
                        player.move(PLAYER_SIZE, 0);
                    }
                    break;
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void keyTyped(KeyEvent e) {}
}