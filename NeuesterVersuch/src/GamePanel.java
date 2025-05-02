// Import-Anweisungen - lädt benötigte Java-Bibliotheken
import javax.swing.*;          // Für GUI-Komponenten
import java.awt.*;             // Für Grafikfunktionen
import java.awt.event.*;       // Für Event-Handling
import java.util.ArrayList;    // Für dynamische Listen
import java.util.Iterator;     // Zum Durchlaufen von Listen

public class GamePanel extends JPanel implements KeyListener {
    // Konstanten für die Spielfeldgröße
    private static final int PANEL_WIDTH = 800;   // Breite des Spielbereichs
    private static final int PANEL_HEIGHT = 600;  // Höhe des Spielbereichs

    // Spieler-Einstellungen
    private static final int PLAYER_SIZE = 30;    // Größe des Spielers

    // Hindernis-Einstellungen
    private static final int OBSTACLE_HEIGHT = 40;      // Höhe der Hindernisse
    private static final int LOG_LENGTH_MULTIPLIER = 5; // Länge der Baumstämme

    // Bereichseinstellungen
    private static final int ROAD_MARKER_HEIGHT = 50;   // Höhe des Mittelstreifens
    private static final int SAFE_ZONE_HEIGHT = 60;     // Höhe der Startzone
    private static final int TOP_SAFE_ZONE_HEIGHT = 40; // Höhe der Zielzone

    // Spiel-Einstellungen
    private static final int INITIAL_LOG_COUNT = 8;     // Startanzahl Baumstämme
    private static final int GAME_SPEED_MULTIPLIER = 2; // Geschwindigkeitsfaktor
    private static final int TOTAL_LEVELS = 5;          // Maximale Anzahl Level bis Spielende

    // Spielobjekte
    private final Player player;              // Der Spielercharakter
    private final ArrayList<Obstacle> cars;  // Liste der Autos
    private final ArrayList<Obstacle> logs;  // Liste der Baumstämme

    // Spielstatus
    private int score;                       // Aktueller Punktestand
    private int currentLevel;                // Aktuelles Level (1-TOTAL_LEVELS)
    private boolean gameWon;                 // True wenn alle Level geschafft
    private boolean gameOver;                // True bei Spielende

    // Spielsteuerung
    private final Timer gameTimer;           // Steuert die Spielschleife

    // Positionsvariablen
    private final int roadYPosition;         // Y-Position der Straße
    private final int riverHeight;           // Höhe des Flussbereichs

    // Konstruktor - initialisiert das Spiel
    public GamePanel() {
        // Setzt die bevorzugte Größe des Panels
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));

        // Aktiviert Tastaturfokus
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(this);

        // Berechnet Bereichspositionen
        riverHeight = PANEL_HEIGHT / 3;  // Fluss nimmt 1/3 der Höhe ein
        roadYPosition = riverHeight + ROAD_MARKER_HEIGHT;  // Straßenposition

        // Initialisiert den Spieler (mittig unten)
        player = new Player(
                PANEL_WIDTH / 2 - PLAYER_SIZE / 2,
                PANEL_HEIGHT - PLAYER_SIZE - SAFE_ZONE_HEIGHT
        );

        // Erstellt leere Listen für Hindernisse
        cars = new ArrayList<>();
        logs = new ArrayList<>();

        // Setzt das Spiel zurück (initialisiert es)
        resetGame();

        // Erstellt den Spieltimer (aktualisiert alle 50ms = 20 FPS)
        gameTimer = new Timer(50, e -> {
            if (!gameOver && !gameWon) {   // Nur wenn Spiel läuft
                updateGame();  // Spiel logik aktualisieren
                repaint();    // Grafik neu zeichnen
            }
        });
        gameTimer.start();  // Startet den Timer
    }

    // Fügt ein neues Auto zur Liste hinzu
    private void addCar() {
        // Zufällige Y-Position auf der Straße
        int y = (int) (Math.random() *
                (PANEL_HEIGHT - SAFE_ZONE_HEIGHT - roadYPosition - OBSTACLE_HEIGHT))
                + roadYPosition;

        // Zufällige Breite (2-5x Spielergröße)
        int width = (int) (Math.random() * 3 + 2) * PLAYER_SIZE;

        // Zufällige Geschwindigkeit (Level-basiert schneller)
        int speed = (int) (Math.random() * 3 + currentLevel) * GAME_SPEED_MULTIPLIER;

        // 50% Chance von links oder rechts
        if (Math.random() < 0.5) {
            cars.add(new Obstacle(-width, y, width, OBSTACLE_HEIGHT, speed));
        } else {
            cars.add(new Obstacle(PANEL_WIDTH, y, width, OBSTACLE_HEIGHT, -speed));
        }
    }

    // Fügt einen neuen Baumstamm hinzu
    private void addLog() {
        // Zufällige Y-Position im Fluss
        int y = (int) (Math.random() * (riverHeight - OBSTACLE_HEIGHT));

        // Zufällige Breite (5-7x Spielergröße)
        int width = (int) (Math.random() * 2 + LOG_LENGTH_MULTIPLIER) * PLAYER_SIZE;

        // Zufällige Geschwindigkeit (Level-basiert)
        int speed = (int) (Math.random() * 2 + currentLevel) * GAME_SPEED_MULTIPLIER;

        if (Math.random() < 0.5) {
            logs.add(new Obstacle(-width, y, width, OBSTACLE_HEIGHT, speed));
        } else {
            logs.add(new Obstacle(PANEL_WIDTH, y, width, OBSTACLE_HEIGHT, -speed));
        }
    }

    // Setzt das Spiel zurück (Neustart)
    private void resetGame() {
        // Spieler zur Startposition
        player.reset(
                PANEL_WIDTH / 2 - PLAYER_SIZE / 2,
                PANEL_HEIGHT - PLAYER_SIZE - SAFE_ZONE_HEIGHT
        );

        score = 0;           // Punktestand zurücksetzen
        currentLevel = 1;    // Startlevel
        gameWon = false;    // Spielgewinn zurücksetzen
        gameOver = false;   // Spielstatus zurücksetzen

        // Hindernislisten leeren
        cars.clear();
        logs.clear();

        // Initiale Hindernisse erstellen
        for (int i = 0; i < INITIAL_LOG_COUNT; i++) {
            addCar();
            addLog();
        }
    }

    // Aktualisiert den Spielzustand
    private void updateGame() {
        moveObstacles(cars);  // Bewegt alle Autos
        moveObstacles(logs);  // Bewegt alle Baumstämme
        checkCollisions();    // Prüft Kollisionen
        checkWinCondition();  // Prüft Levelabschluss
    }

    // Bewegt alle Hindernisse in einer Liste
    private void moveObstacles(ArrayList<Obstacle> obstacles) {
        Iterator<Obstacle> iterator = obstacles.iterator();

        while (iterator.hasNext()) {
            Obstacle obs = iterator.next();
            obs.move();  // Bewegt das Hindernis

            if ((obs.getSpeed() > 0 && obs.getX() > PANEL_WIDTH) ||
                    (obs.getSpeed() < 0 && obs.getX() + obs.getWidth() < 0)) {

                iterator.remove();

                if (obs.getY() < riverHeight) {
                    addLog();
                } else {
                    addCar();
                }
            }
        }
    }

    // Prüft Kollisionen
    private void checkCollisions() {
        // Kollision mit Autos
        if (player.getY() >= roadYPosition &&
                player.getY() < PANEL_HEIGHT - SAFE_ZONE_HEIGHT) {

            for (Obstacle car : cars) {
                if (player.intersects(car)) {
                    gameOver = true;
                    return;
                }
            }
        }

        // Im Wasserbereich
        if (player.getY() >= TOP_SAFE_ZONE_HEIGHT &&
                player.getY() < riverHeight) {

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

    // Prüft ob Level abgeschlossen
    private void checkWinCondition() {
        if (player.getY() < TOP_SAFE_ZONE_HEIGHT) {
            score += currentLevel * 10; // Mehr Punkte für höhere Level

            if (currentLevel >= TOTAL_LEVELS) {
                gameWon = true; // Alle Level geschafft
            } else {
                currentLevel++; // Nächstes Level
                // Spieler zurücksetzen
                player.reset(
                        PANEL_WIDTH / 2 - PLAYER_SIZE / 2,
                        PANEL_HEIGHT - PLAYER_SIZE - SAFE_ZONE_HEIGHT
                );
                // Schwierigkeit erhöhen
                increaseDifficulty();
            }
        }
    }

    // Erhöht die Schwierigkeit
    private void increaseDifficulty() {
        // Mehr Hindernisse pro Level
        for (int i = 0; i < currentLevel; i++) {
            addCar();
            addLog();
        }
    }

    // Zeichenmethode
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBackground(g);
        drawObstacles(g, cars, Color.RED);
        drawObstacles(g, logs, new Color(139, 69, 19));
        player.draw(g);
        drawUI(g);
    }

    // Zeichnet den Hintergrund
    private void drawBackground(Graphics g) {
        // Obere Zielzone
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, PANEL_WIDTH, TOP_SAFE_ZONE_HEIGHT);

        // Flussbereich
        g.setColor(Color.BLUE);
        g.fillRect(0, TOP_SAFE_ZONE_HEIGHT, PANEL_WIDTH, riverHeight - TOP_SAFE_ZONE_HEIGHT);

        // Mittelstreifen
        g.setColor(Color.GREEN);
        g.fillRect(0, riverHeight, PANEL_WIDTH, ROAD_MARKER_HEIGHT);

        // Straßenbereich
        g.setColor(Color.GRAY);
        g.fillRect(0, roadYPosition, PANEL_WIDTH, PANEL_HEIGHT - roadYPosition - SAFE_ZONE_HEIGHT);

        // Startzone
        g.setColor(Color.GREEN);
        g.fillRect(0, PANEL_HEIGHT - SAFE_ZONE_HEIGHT, PANEL_WIDTH, SAFE_ZONE_HEIGHT);
    }

    // Zeichnet Hindernisse
    private void drawObstacles(Graphics g, ArrayList<Obstacle> obstacles, Color color) {
        g.setColor(color);
        for (Obstacle obs : obstacles) {
            obs.draw(g);
        }
    }

    // Zeichnet UI-Elemente
    private void drawUI(Graphics g) {
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Punkte: " + score, 20, 30);
        g.drawString("Level: " + currentLevel + "/" + TOTAL_LEVELS, 20, 60);

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GAME OVER", PANEL_WIDTH / 2 - 150, PANEL_HEIGHT / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Leertaste zum Neustart", PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 + 50);
        }

        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 50));
            g.drawString("GEWONNEN!", PANEL_WIDTH / 2 - 150, PANEL_HEIGHT / 2);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Punktestand: " + score, PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 + 50);
            g.drawString("Leertaste zum Neustart", PANEL_WIDTH / 2 - 100, PANEL_HEIGHT / 2 + 80);
        }
    }

    // Tastatursteuerung
    @Override
    public void keyPressed(KeyEvent e) {
        if ((gameOver || gameWon) && e.getKeyCode() == KeyEvent.VK_SPACE) {
            resetGame();
        }
        else if (!gameOver && !gameWon) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    player.move(0, -PLAYER_SIZE);
                    break;
                case KeyEvent.VK_DOWN:
                    if (player.getY() < PANEL_HEIGHT - PLAYER_SIZE - SAFE_ZONE_HEIGHT) {
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