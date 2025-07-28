import java.awt.*;
import javax.swing.*;

public class Splash extends JFrame {
    // Constants for better maintainability
    private static final int WINDOW_WIDTH = 1170;
    private static final int WINDOW_HEIGHT = 650;
    private static final int DISPLAY_TIME = 5000; // 5 seconds
    
    public Splash() {
        initializeUI();
        displaySplash();
    }
    
    private void initializeUI() {
        // Configure window properties
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocation(200, 100);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Load and configure image
        setupSplashImage();
        
        setVisible(true);
    }
    
    private void setupSplashImage() {
        try {
            ImageIcon originalIcon = new ImageIcon(ClassLoader.getSystemResource("icons/front.gif"));
            
            // Check if we need to scale the GIF
            if (originalIcon.getIconWidth() != WINDOW_WIDTH || originalIcon.getIconHeight() != WINDOW_HEIGHT) {
                // For GIFs, we need to preserve animation, so we'll scale the container instead
                JLabel imageLabel = new JLabel(originalIcon);
                imageLabel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
                
                // Scale the label content to fit the window while preserving aspect ratio
                double scaleX = (double) WINDOW_WIDTH / originalIcon.getIconWidth();
                double scaleY = (double) WINDOW_HEIGHT / originalIcon.getIconHeight();
                double scale = Math.min(scaleX, scaleY);
                
                int scaledWidth = (int) (originalIcon.getIconWidth() * scale);
                int scaledHeight = (int) (originalIcon.getIconHeight() * scale);
                int x = (WINDOW_WIDTH - scaledWidth) / 2;
                int y = (WINDOW_HEIGHT - scaledHeight) / 2;
                
                imageLabel.setBounds(x, y, scaledWidth, scaledHeight);
                add(imageLabel);
            } else {
                // If GIF is already the right size, use it directly
                JLabel imageLabel = new JLabel(originalIcon);
                imageLabel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
                add(imageLabel);
            }
        } catch (Exception e) {
            System.err.println("Error loading splash image: " + e.getMessage());
            // Create a simple colored background as fallback
            JLabel fallbackLabel = new JLabel("Loading...", SwingConstants.CENTER);
            fallbackLabel.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);
            fallbackLabel.setBackground(Color.DARK_GRAY);
            fallbackLabel.setOpaque(true);
            fallbackLabel.setForeground(Color.WHITE);
            fallbackLabel.setFont(new Font("Arial", Font.BOLD, 24));
            add(fallbackLabel);
        }
    }
    
    private void displaySplash() {
        // Use SwingWorker for better thread management
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(DISPLAY_TIME);
                return null;
            }
            
            @Override
            protected void done() {
                setVisible(false);
                dispose(); // Properly dispose of resources
                SwingUtilities.invokeLater(() -> new Login());
            }
        };
        
        worker.execute();
    }
    
    public static void main(String[] args) {
        // Ensure GUI creation happens on EDT
        SwingUtilities.invokeLater(() -> new Splash());
    }
}