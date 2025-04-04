import javax.swing.*;

public class SimpleCrossyRoad {
    private static GameWindow gameWindow;  // Declared as class field
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            gameWindow = new GameWindow();
            gameWindow.setVisible(true);
        });
    }
}