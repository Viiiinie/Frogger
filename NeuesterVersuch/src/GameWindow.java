import javax.swing.*;

public class GameWindow extends JFrame {
    private final GamePanel gamePanel;

    public GameWindow() {
        setTitle("Simple Crossy Road");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);
        pack();

        setLocationRelativeTo(null); // Fenster zentrieren
    }
}
