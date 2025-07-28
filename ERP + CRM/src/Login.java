import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.*;
import javax.swing.*;

public class Login extends JFrame implements ActionListener, KeyListener {
    // Constants for better maintainability
    private static final int WINDOW_WIDTH = 600;
    private static final int WINDOW_HEIGHT = 300;
    private static final Color BUTTON_COLOR = Color.BLACK;
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    
    // UI Components
    private JTextField tusername;
    private JPasswordField tpassword; // Changed to JPasswordField for security
    private JButton loginButton, backButton;
    private JRadioButton erpRadioButton, crmRadioButton;
    private ButtonGroup loginGroup;
    
    // Database connection - reuse connection
    private conn dbConnection;
    
    public Login() {
        initializeDatabase();
        initializeUI();
        setupEventListeners();
    }
    
    private void initializeDatabase() {
        try {
            dbConnection = new conn();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(), 
                                        "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initializeUI() {
        // Configure window
        setTitle("Login");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocation(450, 200);
        setLayout(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
        // Create components
        createLoginComponents();
        loadBackgroundImages();
        
        setVisible(true);
    }
    
    private void createLoginComponents() {
        // Username field
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(40, 20, 100, 30);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(usernameLabel);
        
        tusername = new JTextField();
        tusername.setBounds(150, 20, 150, 30);
        tusername.setFont(new Font("Arial", Font.PLAIN, 12));
        add(tusername);
        
        // Password field
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(40, 70, 100, 30);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(passwordLabel);
        
        tpassword = new JPasswordField(); // More secure than JTextField
        tpassword.setBounds(150, 70, 150, 30);
        tpassword.setFont(new Font("Arial", Font.PLAIN, 12));
        add(tpassword);
        
        // Login type selection
        JLabel loginTypeLabel = new JLabel("Login for:");
        loginTypeLabel.setBounds(40, 100, 100, 30);
        loginTypeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(loginTypeLabel);
        
        erpRadioButton = new JRadioButton("ERP");
        erpRadioButton.setBounds(150, 100, 60, 30);
        erpRadioButton.setSelected(true);
        add(erpRadioButton);
        
        crmRadioButton = new JRadioButton("CRM");
        crmRadioButton.setBounds(220, 100, 60, 30);
        add(crmRadioButton);
        
        loginGroup = new ButtonGroup();
        loginGroup.add(erpRadioButton);
        loginGroup.add(crmRadioButton);
        
        // Buttons
        loginButton = createStyledButton("Login", 200, 140);
        backButton = createStyledButton("Back", 200, 180);
        
        add(loginButton);
        add(backButton);
    }
    
    private JButton createStyledButton(String text, int x, int y) {
        JButton button = new JButton(text);
        button.setBounds(x, y, 150, 30);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        return button;
    }
    
    private void loadBackgroundImages() {
        // Load images in background thread to improve startup time
        SwingWorker<Void, Void> imageLoader = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Load second image
                    ImageIcon secondIcon = new ImageIcon(ClassLoader.getSystemResource("icons/second.jpg"));
                    if (secondIcon.getIconWidth() > 0) {
                        Image secondImage = secondIcon.getImage().getScaledInstance(600, 400, Image.SCALE_SMOOTH);
                        ImageIcon scaledSecondIcon = new ImageIcon(secondImage);
                        
                        SwingUtilities.invokeLater(() -> {
                            JLabel secondImageLabel = new JLabel(scaledSecondIcon);
                            secondImageLabel.setBounds(350, 10, 600, 400);
                            add(secondImageLabel);
                            repaint();
                        });
                    }
                    
                    // Load background image
                    ImageIcon bgIcon = new ImageIcon(ClassLoader.getSystemResource("icons/LoginB.jpg"));
                    if (bgIcon.getIconWidth() > 0) {
                        Image bgImage = bgIcon.getImage().getScaledInstance(600, 300, Image.SCALE_SMOOTH);
                        ImageIcon scaledBgIcon = new ImageIcon(bgImage);
                        
                        SwingUtilities.invokeLater(() -> {
                            JLabel bgImageLabel = new JLabel(scaledBgIcon);
                            bgImageLabel.setBounds(0, 0, 600, 300);
                            add(bgImageLabel);
                            repaint();
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error loading background images: " + e.getMessage());
                }
                return null;
            }
        };
        imageLoader.execute();
    }
    
    private void setupEventListeners() {
        loginButton.addActionListener(this);
        backButton.addActionListener(this);
        
        // Add Enter key support for login
        tusername.addKeyListener(this);
        tpassword.addKeyListener(this);
        
        // Remove unnecessary radio button listeners since they just show messages
        // This improves performance by reducing unnecessary event handling
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            performLogin();
        } else if (e.getSource() == backButton) {
            handleBack();
        }
    }
    
    private void performLogin() {
        String username = tusername.getText().trim();
        String password = new String(tpassword.getPassword()).trim();
        
        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password", 
                                        "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Disable login button to prevent multiple clicks
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");
        
        // Perform login in background thread
        SwingWorker<Boolean, Void> loginWorker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authenticateUser(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    boolean loginSuccess = get();
                    if (loginSuccess) {
                        String loginType = erpRadioButton.isSelected() ? "ERP" : "CRM";
                        JOptionPane.showMessageDialog(Login.this, 
                            "Welcome! Logged in to " + loginType + " system.", 
                            "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                        
                        setVisible(false);
                        dispose();
                        new Main_class();
                    } else {
                        JOptionPane.showMessageDialog(Login.this, 
                            "Invalid username or password", 
                            "Login Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Login.this, 
                        "Login failed: " + ex.getMessage(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    // Re-enable login button
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                }
            }
        };
        
        loginWorker.execute();
    }
    
    private boolean authenticateUser(String username, String password) throws SQLException {
        if (dbConnection == null || dbConnection.statement == null) {
            throw new SQLException("Database connection not available");
        }
        
        // Use PreparedStatement to prevent SQL injection
        String query = "SELECT * FROM login WHERE username = ? AND password = ?";
        try (PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password); // In production, use hashed passwords
            
            try (ResultSet resultSet = pstmt.executeQuery()) {
                return resultSet.next();
            }
        }
    }
    
    private void handleBack() {
        int option = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to exit?", 
            "Confirm Exit", 
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            dispose();
            System.exit(0);
        }
    }
    
    // KeyListener implementation for Enter key support
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            performLogin();
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
    
    @Override
    public void keyReleased(KeyEvent e) {}
    
    @Override
    public void dispose() {
        // Clean up database connection
        if (dbConnection != null) {
            try {
                if (dbConnection.statement != null) {
                    dbConnection.statement.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login());
    }
}