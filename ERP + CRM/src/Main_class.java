import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class Main_class extends JFrame implements ActionListener {
    // Constants for better maintainability
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 700;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color ACCENT_COLOR = new Color(231, 76, 60);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    
    // UI Components
    private JLabel timeLabel, dateLabel, welcomeLabel;
    private JButton addEmployeeBtn, viewEmployeeBtn, removeEmployeeBtn, 
                   updateEmployeeBtn, reportsBtn, settingsBtn, logoutBtn;
    private Timer clockTimer;
    
    public Main_class() {
        initializeUI();
        startClock();
        loadBackgroundAsync();
    }
    
    private void initializeUI() {
        // Configure main window
        setTitle("Employee Management System - Dashboard");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
        
        // Create main layout
        createMainLayout();
        
        setVisible(true);
    }
    
    private void createMainLayout() {
        // Main container with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Create gradient background
                GradientPaint gradient = new GradientPaint(0, 0, PRIMARY_COLOR, 
                                                         getWidth(), getHeight(), BACKGROUND_COLOR);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create center content panel
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Create footer panel
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Left side - Welcome message
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        
        welcomeLabel = new JLabel("Welcome to Employee Management System");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(Color.WHITE);
        leftPanel.add(welcomeLabel);
        
        // Center - Date and Time
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setOpaque(false);
        
        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        dateLabel = new JLabel();
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        centerPanel.add(timeLabel);
        centerPanel.add(dateLabel);
        
        // Right side - Settings button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);
        
        settingsBtn = createHeaderSettingsButton();
        rightPanel.add(settingsBtn);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(centerPanel, BorderLayout.CENTER);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.setBorder(new EmptyBorder(40, 50, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        
        // Create feature cards in a grid layout with enhanced icons
        // Row 1
        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(createFeatureCard("Add Employee", "Add new employees to the system", 
                                        SUCCESS_COLOR, "user-plus"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 0;
        centerPanel.add(createFeatureCard("View Employees", "Browse all employee records", 
                                        PRIMARY_COLOR, "users"), gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        centerPanel.add(createFeatureCard("Update Employee", "Modify employee information", 
                                        WARNING_COLOR, "edit"), gbc);
        
        // Row 2
        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(createFeatureCard("Remove Employee", "Delete employee records", 
                                        ACCENT_COLOR, "user-minus"), gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        centerPanel.add(createFeatureCard("Reports", "Generate employee reports", 
                                        SECONDARY_COLOR, "chart-bar"), gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        centerPanel.add(createFeatureCard("Task Assignment", "AI-powered task allocation and management", 
                                        new Color(155, 89, 182), "ai-brain"), gbc);
        
        return centerPanel;
    }
    
    // Enhanced icon creation method
    private JLabel createModernIcon(String iconType, Color color) {
        JLabel iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                
                int size = Math.min(getWidth(), getHeight());
                int iconSize = (int)(size * 0.7);
                int x = (getWidth() - iconSize) / 2;
                int y = (getHeight() - iconSize) / 2;
                
                g2d.setColor(color);
                g2d.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                switch (iconType) {
                    case "user-plus":
                        drawUserPlusIcon(g2d, x, y, iconSize);
                        break;
                    case "users":
                        drawUsersIcon(g2d, x, y, iconSize);
                        break;
                    case "edit":
                        drawEditIcon(g2d, x, y, iconSize);
                        break;
                    case "user-minus":
                        drawUserMinusIcon(g2d, x, y, iconSize);
                        break;
                    case "chart-bar":
                        drawChartBarIcon(g2d, x, y, iconSize);
                        break;
                    case "ai-brain":
                        drawAIBrainIcon(g2d, x, y, iconSize);
                        break;
                }
            }
        };
        
        iconLabel.setPreferredSize(new Dimension(48, 48));
        return iconLabel;
    }
    
    private void drawUserPlusIcon(Graphics2D g2d, int x, int y, int size) {
        int centerX = x + size/2;
        int centerY = y + size/2;
        int radius = size/6;
        
        // Draw head
        g2d.drawOval(centerX - radius/2, y + size/8, radius, radius);
        
        // Draw body
        g2d.drawArc(centerX - radius, y + size/3, radius*2, radius*2, 0, 180);
        
        // Draw plus sign
        int plusSize = size/4;
        int plusX = x + size - plusSize;
        int plusY = y + size/6;
        
        g2d.drawLine(plusX + plusSize/2, plusY, plusX + plusSize/2, plusY + plusSize);
        g2d.drawLine(plusX, plusY + plusSize/2, plusX + plusSize, plusY + plusSize/2);
    }
    
    private void drawUsersIcon(Graphics2D g2d, int x, int y, int size) {
        int radius = size/8;
        
        // First user
        g2d.drawOval(x + size/6, y + size/8, radius, radius);
        g2d.drawArc(x + size/8, y + size/3, radius + radius/2, radius + radius/2, 0, 180);
        
        // Second user (overlapping)
        g2d.drawOval(x + size/2, y + size/8, radius, radius);
        g2d.drawArc(x + size/2 - radius/4, y + size/3, radius + radius/2, radius + radius/2, 0, 180);
    }
    
    private void drawEditIcon(Graphics2D g2d, int x, int y, int size) {
        // Pencil body
        int[] xPoints = {x + size/4, x + size*3/4, x + size*3/4 + size/8, x + size/4 + size/8};
        int[] yPoints = {y + size/4, y + size*3/4, y + size*3/4 + size/8, y + size/4 + size/8};
        g2d.drawPolygon(xPoints, yPoints, 4);
        
        // Pencil tip
        g2d.drawLine(x + size/4, y + size/4, x + size/8, y + size/8);
        
        // Edit lines
        g2d.drawLine(x + size/8, y + size*3/4, x + size/3, y + size*3/4);
        g2d.drawLine(x + size/8, y + size*7/8, x + size/2, y + size*7/8);
    }
    
    private void drawUserMinusIcon(Graphics2D g2d, int x, int y, int size) {
        int centerX = x + size/2;
        int centerY = y + size/2;
        int radius = size/6;
        
        // Draw head
        g2d.drawOval(centerX - radius/2, y + size/8, radius, radius);
        
        // Draw body
        g2d.drawArc(centerX - radius, y + size/3, radius*2, radius*2, 0, 180);
        
        // Draw minus sign
        int minusSize = size/4;
        int minusX = x + size - minusSize;
        int minusY = y + size/6 + minusSize/2;
        
        g2d.drawLine(minusX, minusY, minusX + minusSize, minusY);
    }
    
    private void drawChartBarIcon(Graphics2D g2d, int x, int y, int size) {
        int barWidth = size/6;
        int spacing = size/8;
        
        // Draw bars of different heights
        g2d.fillRect(x + spacing, y + size*3/4, barWidth, size/4);
        g2d.fillRect(x + spacing*2 + barWidth, y + size/2, barWidth, size/2);
        g2d.fillRect(x + spacing*3 + barWidth*2, y + size/4, barWidth, size*3/4);
        g2d.fillRect(x + spacing*4 + barWidth*3, y + size*5/8, barWidth, size*3/8);
        
        // Draw axes
        g2d.drawLine(x, y + size, x + size, y + size); // X-axis
        g2d.drawLine(x, y, x, y + size); // Y-axis
    }
    
    private void drawAIBrainIcon(Graphics2D g2d, int x, int y, int size) {
        int centerX = x + size/2;
        int centerY = y + size/2;
        
        // Draw brain outline (left hemisphere)
        g2d.drawArc(x + size/8, y + size/6, size/3, size/2, 90, 180);
        
        // Draw brain outline (right hemisphere)  
        g2d.drawArc(x + size/2, y + size/6, size/3, size/2, 270, 180);
        
        // Draw brain top connection
        g2d.drawArc(x + size/4, y + size/8, size/2, size/3, 0, 180);
        
        // Draw neural network nodes
        int nodeSize = size/12;
        g2d.fillOval(centerX - nodeSize, centerY - size/6, nodeSize*2, nodeSize*2);
        g2d.fillOval(centerX - size/4, centerY, nodeSize*2, nodeSize*2);
        g2d.fillOval(centerX + size/6, centerY, nodeSize*2, nodeSize*2);
        g2d.fillOval(centerX, centerY + size/6, nodeSize*2, nodeSize*2);
        
        // Draw connections between nodes
        g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(centerX, centerY - size/6, centerX - size/4, centerY);
        g2d.drawLine(centerX, centerY - size/6, centerX + size/6, centerY);
        g2d.drawLine(centerX - size/4, centerY, centerX, centerY + size/6);
        g2d.drawLine(centerX + size/6, centerY, centerX, centerY + size/6);
    }
    
    private JPanel createFeatureCard(String title, String description, Color color, String iconType) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw rounded rectangle with shadow effect
                g2d.setColor(new Color(0, 0, 0, 30));
                g2d.fillRoundRect(5, 5, getWidth()-5, getHeight()-5, 20, 20);
                
                g2d.setColor(CARD_COLOR);
                g2d.fillRoundRect(0, 0, getWidth()-5, getHeight()-5, 20, 20);
                
                // Draw colored top border
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth()-5, 8, 20, 20);
            }
        };
        
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(280, 160));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        card.setOpaque(false);
        
        // Icon and title panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);
        
        JLabel iconLabel = createModernIcon(iconType, color);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        topPanel.add(iconLabel);
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(titleLabel);
        
        // Description
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(new Color(127, 140, 141));
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        
        // Add hover effect
        addHoverEffect(card, color);
        
        // Add click listener based on title
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleCardClick(title);
            }
        });
        
        return card;
    }
    
    private void addHoverEffect(JPanel card, Color color) {
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                card.repaint();
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                card.repaint();
            }
        });
    }
    
    private void handleCardClick(String cardTitle) {
        // Add smooth transition effect
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Small delay for visual feedback
                Thread.sleep(100);
                return null;
            }
            
            @Override
            protected void done() {
                switch (cardTitle) {
                    case "Add Employee":
                        setVisible(false);
                        dispose();
                        new AddEmployee();
                        /*JOptionPane.showMessageDialog(Main_class.this, 
                            "Add Employee feature will be implemented.\nPlease create the AddEmployee class.", 
                            "Feature Status", JOptionPane.INFORMATION_MESSAGE);*/
                        break;
                    case "View Employees":
                        setVisible(false);
                        dispose();
                        new View_Employee();
                        /*JOptionPane.showMessageDialog(Main_class.this, 
                            "View Employee feature will be implemented.\nPlease create the View_Employee class.", 
                            "Feature Status", JOptionPane.INFORMATION_MESSAGE);*/
                        break;
                    case "Update Employee":
                        setVisible(false);
                        dispose();
                        new View_Employee();
                        /*JOptionPane.showMessageDialog(Main_class.this, 
                            "Update Employee feature will be implemented.\nPlease create the appropriate class.", 
                            "Feature Status", JOptionPane.INFORMATION_MESSAGE);*/
                        break;
                    case "Remove Employee":
                        setVisible(false);
                        dispose();
                        new RemoveEmployee();
                        /*JOptionPane.showMessageDialog(Main_class.this, 
                            "Remove Employee feature will be implemented.\nPlease create the RemoveEmployee class.", 
                            "Feature Status", JOptionPane.INFORMATION_MESSAGE);*/
                        break;
                    case "Reports":
                    	setVisible(false);
                        dispose();
                        new Reports();
                        /*JOptionPane.showMessageDialog(Main_class.this, 
                            "Reports feature will be implemented", 
                            "Feature Status", JOptionPane.INFORMATION_MESSAGE);*/
                        break;
                    case "Task Assignment":
                    	setVisible(false);
                        dispose();
                        new Task_ass();
                        /*JOptionPane.showMessageDialog(Main_class.this, 
                            "AI Task Assignment feature will be implemented", 
                            "Feature Status", JOptionPane.INFORMATION_MESSAGE);*/
                        break;
                }
            }
        };
        worker.execute();
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 30, 20, 30));
        
        // Left side - System info
        JLabel systemInfo = new JLabel("EMS v2.0 | Developed with ❤️");
        systemInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        systemInfo.setForeground(Color.WHITE);
        
        // Right side - Logout button
        logoutBtn = createModernButton("Logout", ACCENT_COLOR);
        logoutBtn.setPreferredSize(new Dimension(100, 35));
        logoutBtn.addActionListener(this);
        
        footerPanel.add(systemInfo, BorderLayout.WEST);
        footerPanel.add(logoutBtn, BorderLayout.EAST);
        
        return footerPanel;
    }
    
    // Create header settings button
    private JButton createHeaderSettingsButton() {
        JButton button = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Button background
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(255, 255, 255, 60));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 255, 255, 40));
                } else {
                    g2d.setColor(new Color(255, 255, 255, 20));
                }
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                // Draw settings icon
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int outerRadius = Math.min(getWidth(), getHeight()) / 4;
                int innerRadius = outerRadius / 2;
                
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                
                // Draw gear teeth
                for (int i = 0; i < 8; i++) {
                    double angle = i * Math.PI / 4;
                    int x1 = centerX + (int)(outerRadius * Math.cos(angle));
                    int y1 = centerY + (int)(outerRadius * Math.sin(angle));
                    int x2 = centerX + (int)((outerRadius + 4) * Math.cos(angle));
                    int y2 = centerY + (int)((outerRadius + 4) * Math.sin(angle));
                    g2d.drawLine(x1, y1, x2, y2);
                }
                
                // Draw outer circle
                g2d.drawOval(centerX - outerRadius, centerY - outerRadius, outerRadius*2, outerRadius*2);
                
                // Draw inner circle
                g2d.drawOval(centerX - innerRadius, centerY - innerRadius, innerRadius*2, innerRadius*2);
            }
        };
        
        button.setPreferredSize(new Dimension(50, 50));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setToolTipText("Settings");
        
        // Add click handler for settings
        button.addActionListener(e -> {
            // Settings functionality
            JOptionPane.showMessageDialog(this, "Settings panel will be implemented", 
                                        "Settings", JOptionPane.INFORMATION_MESSAGE);
        });
        
        return button;
    }
    
    // Fixed: Complete createModernButton method
    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void startClock() {
        clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();
        updateDateTime(); // Initial update
    }
    
    private void updateDateTime() {
        Date now = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
        
        timeLabel.setText(timeFormat.format(now));
        dateLabel.setText(dateFormat.format(now));
    }
    
    private void loadBackgroundAsync() {
        SwingWorker<ImageIcon, Void> imageLoader = new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                try {
                    // Try to load background image
                    ImageIcon originalIcon = new ImageIcon(ClassLoader.getSystemResource("icons/home.jpg"));
                    if (originalIcon.getIconWidth() > 0) {
                        Image scaledImage = originalIcon.getImage().getScaledInstance(
                            WINDOW_WIDTH, WINDOW_HEIGHT, Image.SCALE_SMOOTH);
                        return new ImageIcon(scaledImage);
                    }
                } catch (Exception e) {
                    System.err.println("Background image not found, using default gradient: " + e.getMessage());
                }
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    ImageIcon bgImage = get();
                    if (bgImage != null) {
                        // If background image is loaded successfully, you can add it here
                        // For now, we're using the modern gradient design
                        System.out.println("Background image loaded successfully");
                    }
                } catch (Exception e) {
                    // Continue with gradient background
                    System.out.println("Using gradient background");
                }
            }
        };
        imageLoader.execute();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == logoutBtn) {
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
                
            if (option == JOptionPane.YES_OPTION) {
                if (clockTimer != null) {
                    clockTimer.stop();
                }
                dispose();
                // new Login(); // Uncomment when Login class is available
                System.exit(0);
            }
        }
    }
    
    @Override
    public void dispose() {
        if (clockTimer != null) {
            clockTimer.stop();
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set system look and feel for better appearance
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | 
                     IllegalAccessException | UnsupportedLookAndFeelException e) {
                // If system L&F fails, continue with default
                System.err.println("Could not set system look and feel: " + e.getMessage());
            }
            new Main_class();
        });
    }
}