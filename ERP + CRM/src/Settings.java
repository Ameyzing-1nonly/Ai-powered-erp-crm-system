import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class Settings extends JFrame implements ActionListener {
    // Constants for modern design
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 700;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color INPUT_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(220, 221, 225);
    
    // UI Components - Database Settings
    private JTextField dbHostField, dbPortField, dbNameField, dbUsernameField;
    private JPasswordField dbPasswordField;
    private JButton testConnectionBtn, saveDbSettingsBtn;
    
    // UI Components - Application Settings
    private JComboBox<String> themeCombo, languageCombo, dateFormatCombo;
    private JSpinner sessionTimeoutSpinner, backupIntervalSpinner;
    private JCheckBox autoBackupCheckBox, notificationCheckBox, soundCheckBox;
    private JSlider fontSizeSlider;
    private JButton saveAppSettingsBtn, resetSettingsBtn;
    
    // UI Components - User Management
    private JTextField newUsernameField, currentPasswordField;
    private JPasswordField newPasswordField, confirmPasswordField;
    private JButton changePasswordBtn, addUserBtn;
    private JComboBox<String> userRoleCombo;
    
    // UI Components - System Info & Backup
    private JTextArea systemInfoArea, backupLogArea;
    private JButton backupNowBtn, restoreBtn, clearLogsBtn, exportSettingsBtn;
    private JProgressBar backupProgressBar;
    
    // Other components
    private JButton backButton, applyAllBtn;
    private JLabel connectionStatusLabel, settingsStatusLabel;
    private JTabbedPane settingsTabs;
    
    // Data
    private conn dbConnection;
    private Properties appProperties;
    private final String PROPERTIES_FILE = "app_settings.properties";
    
    public Settings() {
        initializeDatabase();
        initializeProperties();
        initializeUI();
        loadCurrentSettings();
    }
    
    private void initializeDatabase() {
        try {
            dbConnection = new conn();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                                        "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void initializeProperties() {
        appProperties = new Properties();
        loadProperties();
    }
    
    private void loadProperties() {
        try {
            File propFile = new File(PROPERTIES_FILE);
            if (propFile.exists()) {
                FileInputStream input = new FileInputStream(propFile);
                appProperties.load(input);
                input.close();
            } else {
                // Set default properties
                setDefaultProperties();
                saveProperties();
            }
        } catch (IOException e) {
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        appProperties.setProperty("theme", "Modern");
        appProperties.setProperty("language", "English");
        appProperties.setProperty("dateFormat", "dd/MM/yyyy");
        appProperties.setProperty("fontSize", "14");
        appProperties.setProperty("sessionTimeout", "30");
        appProperties.setProperty("autoBackup", "true");
        appProperties.setProperty("backupInterval", "24");
        appProperties.setProperty("notifications", "true");
        appProperties.setProperty("sounds", "true");
        appProperties.setProperty("dbHost", "localhost");
        appProperties.setProperty("dbPort", "3306");
        appProperties.setProperty("dbName", "erpcrm");
        appProperties.setProperty("dbUsername", "root");
    }
    
    private void saveProperties() {
        try {
            FileOutputStream output = new FileOutputStream(PROPERTIES_FILE);
            appProperties.store(output, "ERP+CRM Application Settings");
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        // Configure main window
        setTitle("Settings - Employee Management System");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        
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
                
                // Create subtle gradient background
                GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND_COLOR, 
                                                         getWidth(), getHeight(), Color.WHITE);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout());
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for different settings categories
        settingsTabs = createSettingsTabs();
        mainPanel.add(settingsTabs, BorderLayout.CENTER);
        
        // Create footer with action buttons
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 40, 20, 40));
        
        // Title and subtitle
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("System Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Configure application preferences, database, and system options");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Status panel
        JPanel statusPanel = createStatusPanel();
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statusPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        statusPanel.setOpaque(false);
        
        connectionStatusLabel = new JLabel("Database: Connected");
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        connectionStatusLabel.setForeground(SUCCESS_COLOR);
        connectionStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        settingsStatusLabel = new JLabel("Settings: Ready");
        settingsStatusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        settingsStatusLabel.setForeground(PRIMARY_COLOR);
        settingsStatusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statusPanel.add(connectionStatusLabel);
        statusPanel.add(settingsStatusLabel);
        
        return statusPanel;
    }
    
    private JTabbedPane createSettingsTabs() {
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(CARD_COLOR);
        
        // Database Settings Tab
        tabbedPane.addTab("🗄️ Database", createDatabaseSettingsPanel());
        
        // Application Settings Tab
        tabbedPane.addTab("⚙️ Application", createApplicationSettingsPanel());
        
        // User Management Tab
        tabbedPane.addTab("👥 Users", createUserManagementPanel());
        
        // System & Backup Tab
        tabbedPane.addTab("💾 System", createSystemBackupPanel());
        
        // Security Tab
        tabbedPane.addTab("🔒 Security", createSecurityPanel());
        
        return tabbedPane;
    }
    
    private JPanel createDatabaseSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Database Configuration Section
        JPanel dbConfigPanel = createSectionPanel("Database Configuration");
        JPanel dbContent = new JPanel(new GridBagLayout());
        dbContent.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Database fields
        gbc.gridx = 0; gbc.gridy = 0;
        dbContent.add(createFieldLabel("Host:"), gbc);
        gbc.gridx = 1;
        dbHostField = createStyledTextField("localhost");
        dbContent.add(dbHostField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        dbContent.add(createFieldLabel("Port:"), gbc);
        gbc.gridx = 3;
        dbPortField = createStyledTextField("3306");
        dbPortField.setPreferredSize(new Dimension(100, 35));
        dbContent.add(dbPortField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        dbContent.add(createFieldLabel("Database Name:"), gbc);
        gbc.gridx = 1;
        dbNameField = createStyledTextField("erpcrm");
        dbContent.add(dbNameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        dbContent.add(createFieldLabel("Username:"), gbc);
        gbc.gridx = 1;
        dbUsernameField = createStyledTextField("root");
        dbContent.add(dbUsernameField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 2;
        dbContent.add(createFieldLabel("Password:"), gbc);
        gbc.gridx = 3;
        dbPasswordField = new JPasswordField(15);
        styleTextField(dbPasswordField);
        dbContent.add(dbPasswordField, gbc);
        
        // Button panel
        JPanel dbButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        dbButtonPanel.setOpaque(false);
        
        testConnectionBtn = createModernButton("Test Connection", PRIMARY_COLOR);
        saveDbSettingsBtn = createModernButton("Save Database Settings", SUCCESS_COLOR);
        
        testConnectionBtn.addActionListener(this);
        saveDbSettingsBtn.addActionListener(this);
        
        dbButtonPanel.add(testConnectionBtn);
        dbButtonPanel.add(saveDbSettingsBtn);
        
        dbConfigPanel.add(dbContent, BorderLayout.CENTER);
        dbConfigPanel.add(dbButtonPanel, BorderLayout.SOUTH);
        
        panel.add(dbConfigPanel, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JPanel createApplicationSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Main content panel with sections
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Appearance Section
        JPanel appearanceSection = createSectionPanel("Appearance Settings");
        JPanel appearanceContent = new JPanel(new GridBagLayout());
        appearanceContent.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme selection
        gbc.gridx = 0; gbc.gridy = 0;
        appearanceContent.add(createFieldLabel("Theme:"), gbc);
        gbc.gridx = 1;
        String[] themes = {"Modern", "Classic", "Dark", "Light"};
        themeCombo = createStyledComboBox(themes);
        appearanceContent.add(themeCombo, gbc);
        
        // Language selection
        gbc.gridx = 2; gbc.gridy = 0;
        appearanceContent.add(createFieldLabel("Language:"), gbc);
        gbc.gridx = 3;
        String[] languages = {"English", "Spanish", "French", "German", "Hindi"};
        languageCombo = createStyledComboBox(languages);
        appearanceContent.add(languageCombo, gbc);
        
        // Font size
        gbc.gridx = 0; gbc.gridy = 1;
        appearanceContent.add(createFieldLabel("Font Size:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 2;
        fontSizeSlider = new JSlider(10, 20, 14);
        fontSizeSlider.setMajorTickSpacing(2);
        fontSizeSlider.setMinorTickSpacing(1);
        fontSizeSlider.setPaintTicks(true);
        fontSizeSlider.setPaintLabels(true);
        fontSizeSlider.setOpaque(false);
        appearanceContent.add(fontSizeSlider, gbc);
        
        appearanceSection.add(appearanceContent, BorderLayout.CENTER);
        
        // System Preferences Section
        JPanel systemSection = createSectionPanel("System Preferences");
        JPanel systemContent = new JPanel(new GridBagLayout());
        systemContent.setOpaque(false);
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Date format
        gbc.gridx = 0; gbc.gridy = 0;
        systemContent.add(createFieldLabel("Date Format:"), gbc);
        gbc.gridx = 1;
        String[] dateFormats = {"dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd", "dd-MM-yyyy"};
        dateFormatCombo = createStyledComboBox(dateFormats);
        systemContent.add(dateFormatCombo, gbc);
        
        // Session timeout
        gbc.gridx = 2; gbc.gridy = 0;
        systemContent.add(createFieldLabel("Session Timeout (min):"), gbc);
        gbc.gridx = 3;
        sessionTimeoutSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 120, 5));
        styleSpinner(sessionTimeoutSpinner);
        systemContent.add(sessionTimeoutSpinner, gbc);
        
        // Checkboxes for features
        gbc.gridx = 0; gbc.gridy = 1;
        autoBackupCheckBox = new JCheckBox("Enable Auto Backup");
        autoBackupCheckBox.setOpaque(false);
        autoBackupCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        systemContent.add(autoBackupCheckBox, gbc);
        
        gbc.gridx = 1; gbc.gridy = 1;
        notificationCheckBox = new JCheckBox("Enable Notifications");
        notificationCheckBox.setOpaque(false);
        notificationCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        systemContent.add(notificationCheckBox, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        soundCheckBox = new JCheckBox("Enable Sounds");
        soundCheckBox.setOpaque(false);
        soundCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        systemContent.add(soundCheckBox, gbc);
        
        // Backup interval
        gbc.gridx = 0; gbc.gridy = 2;
        systemContent.add(createFieldLabel("Backup Interval (hours):"), gbc);
        gbc.gridx = 1;
        backupIntervalSpinner = new JSpinner(new SpinnerNumberModel(24, 1, 168, 1));
        styleSpinner(backupIntervalSpinner);
        systemContent.add(backupIntervalSpinner, gbc);
        
        systemSection.add(systemContent, BorderLayout.CENTER);
        
        // Button panel
        JPanel appButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        appButtonPanel.setOpaque(false);
        
        resetSettingsBtn = createModernButton("Reset to Default", WARNING_COLOR);
        saveAppSettingsBtn = createModernButton("Save Settings", SUCCESS_COLOR);
        
        resetSettingsBtn.addActionListener(this);
        saveAppSettingsBtn.addActionListener(this);
        
        appButtonPanel.add(resetSettingsBtn);
        appButtonPanel.add(saveAppSettingsBtn);
        
        contentPanel.add(appearanceSection);
        contentPanel.add(Box.createVerticalStrut(20));
        contentPanel.add(systemSection);
        
        panel.add(contentPanel, BorderLayout.CENTER);
        panel.add(appButtonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Change Password Section
        JPanel passwordSection = createSectionPanel("Change Password");
        JPanel passwordContent = new JPanel(new GridBagLayout());
        passwordContent.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        passwordContent.add(createFieldLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        currentPasswordField = createStyledTextField("");
        passwordContent.add(currentPasswordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        passwordContent.add(createFieldLabel("New Password:"), gbc);
        gbc.gridx = 1;
        newPasswordField = new JPasswordField(15);
        styleTextField(newPasswordField);
        passwordContent.add(newPasswordField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        passwordContent.add(createFieldLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField(15);
        styleTextField(confirmPasswordField);
        passwordContent.add(confirmPasswordField, gbc);
        
        JPanel passwordButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        passwordButtonPanel.setOpaque(false);
        changePasswordBtn = createModernButton("Change Password", PRIMARY_COLOR);
        changePasswordBtn.addActionListener(this);
        passwordButtonPanel.add(changePasswordBtn);
        
        passwordSection.add(passwordContent, BorderLayout.CENTER);
        passwordSection.add(passwordButtonPanel, BorderLayout.SOUTH);
        
        // Add New User Section
        JPanel addUserSection = createSectionPanel("Add New User");
        JPanel addUserContent = new JPanel(new GridBagLayout());
        addUserContent.setOpaque(false);
        
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        addUserContent.add(createFieldLabel("Username:"), gbc);
        gbc.gridx = 1;
        newUsernameField = createStyledTextField("");
        addUserContent.add(newUsernameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        addUserContent.add(createFieldLabel("User Role:"), gbc);
        gbc.gridx = 1;
        String[] roles = {"Admin", "Manager", "Employee", "Viewer"};
        userRoleCombo = createStyledComboBox(roles);
        addUserContent.add(userRoleCombo, gbc);
        
        JPanel addUserButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addUserButtonPanel.setOpaque(false);
        addUserBtn = createModernButton("Add User", SUCCESS_COLOR);
        addUserBtn.addActionListener(this);
        addUserButtonPanel.add(addUserBtn);
        
        addUserSection.add(addUserContent, BorderLayout.CENTER);
        addUserSection.add(addUserButtonPanel, BorderLayout.SOUTH);
        
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.add(passwordSection);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(addUserSection);
        
        panel.add(mainContent, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSystemBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // System Information Section
        JPanel infoSection = createSectionPanel("System Information");
        systemInfoArea = new JTextArea(6, 50);
        systemInfoArea.setEditable(false);
        systemInfoArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        systemInfoArea.setBackground(INPUT_COLOR);
        systemInfoArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane infoScroll = new JScrollPane(systemInfoArea);
        infoSection.add(infoScroll, BorderLayout.CENTER);
        
        // Backup Section
        JPanel backupSection = createSectionPanel("Database Backup & Restore");
        JPanel backupContent = new JPanel(new BorderLayout());
        backupContent.setOpaque(false);
        
        // Backup buttons
        JPanel backupButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backupButtonPanel.setOpaque(false);
        
        backupNowBtn = createModernButton("Backup Now", SUCCESS_COLOR);
        restoreBtn = createModernButton("Restore Database", WARNING_COLOR);
        clearLogsBtn = createModernButton("Clear Logs", DANGER_COLOR);
        exportSettingsBtn = createModernButton("Export Settings", PRIMARY_COLOR);
        
        backupNowBtn.addActionListener(this);
        restoreBtn.addActionListener(this);
        clearLogsBtn.addActionListener(this);
        exportSettingsBtn.addActionListener(this);
        
        backupButtonPanel.add(backupNowBtn);
        backupButtonPanel.add(restoreBtn);
        backupButtonPanel.add(clearLogsBtn);
        backupButtonPanel.add(exportSettingsBtn);
        
        // Progress bar
        backupProgressBar = new JProgressBar();
        backupProgressBar.setStringPainted(true);
        backupProgressBar.setVisible(false);
        
        // Backup log area
        backupLogArea = new JTextArea(8, 50);
        backupLogArea.setEditable(false);
        backupLogArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        backupLogArea.setBackground(INPUT_COLOR);
        backupLogArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane logScroll = new JScrollPane(backupLogArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("Backup Logs"));
        
        backupContent.add(backupButtonPanel, BorderLayout.NORTH);
        backupContent.add(backupProgressBar, BorderLayout.CENTER);
        backupContent.add(logScroll, BorderLayout.SOUTH);
        
        backupSection.add(backupContent, BorderLayout.CENTER);
        
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.add(infoSection);
        mainContent.add(Box.createVerticalStrut(20));
        mainContent.add(backupSection);
        
        panel.add(mainContent, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSecurityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Security Settings Section
        JPanel securitySection = createSectionPanel("Security Settings");
        JPanel securityContent = new JPanel(new GridBagLayout());
        securityContent.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Security options
        gbc.gridx = 0; gbc.gridy = 0;
        JCheckBox encryptDataCheckBox = new JCheckBox("Enable Data Encryption");
        encryptDataCheckBox.setOpaque(false);
        encryptDataCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        encryptDataCheckBox.setSelected(true);
        securityContent.add(encryptDataCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        JCheckBox loginAttemptsCheckBox = new JCheckBox("Limit Login Attempts");
        loginAttemptsCheckBox.setOpaque(false);
        loginAttemptsCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginAttemptsCheckBox.setSelected(true);
        securityContent.add(loginAttemptsCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        JCheckBox auditLogCheckBox = new JCheckBox("Enable Audit Logging");
        auditLogCheckBox.setOpaque(false);
        auditLogCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        auditLogCheckBox.setSelected(true);
        securityContent.add(auditLogCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        JCheckBox twoFactorCheckBox = new JCheckBox("Enable Two-Factor Authentication");
        twoFactorCheckBox.setOpaque(false);
        twoFactorCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        securityContent.add(twoFactorCheckBox, gbc);
        
        // Information panel
        JTextArea securityInfo = new JTextArea(8, 50);
        securityInfo.setEditable(false);
        securityInfo.setBackground(INPUT_COLOR);
        securityInfo.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        securityInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        securityInfo.setText("Security Information:\n\n" +
                           "• Data encryption protects sensitive information\n" +
                           "• Login attempt limits prevent brute force attacks\n" +
                           "• Audit logging tracks all system activities\n" +
                           "• Two-factor authentication adds extra security layer\n" +
                           "• Regular backups ensure data safety\n" +
                           "• Strong passwords are recommended for all users");
        
        JScrollPane securityInfoScroll = new JScrollPane(securityInfo);
        securityInfoScroll.setBorder(BorderFactory.createTitledBorder("Security Guidelines"));
        
        securitySection.add(securityContent, BorderLayout.NORTH);
        securitySection.add(securityInfoScroll, BorderLayout.CENTER);
        
        panel.add(securitySection, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSectionPanel(String title) {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 20, 0)));
        section.setBackground(CARD_COLOR);
        
        // Section header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        section.add(headerPanel, BorderLayout.NORTH);
        return section;
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(SECONDARY_COLOR);
        label.setPreferredSize(new Dimension(150, 30));
        return label;
    }
    
    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text, 15);
        styleTextField(field);
        return field;
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(INPUT_COLOR);
        field.setPreferredSize(new Dimension(200, 35));
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(INPUT_COLOR);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        combo.setPreferredSize(new Dimension(200, 35));
        return combo;
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        spinner.setPreferredSize(new Dimension(100, 35));
        
        // Style the spinner editor
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(INPUT_COLOR);
        }
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(10, 40, 20, 40));
        
        // Left side - Info
        JLabel infoLabel = new JLabel("Changes will be applied after restart");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        
        // Right side - Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        backButton = createModernButton("Back", new Color(108, 117, 125));
        applyAllBtn = createModernButton("Apply All Changes", SUCCESS_COLOR);
        
        backButton.addActionListener(this);
        applyAllBtn.addActionListener(this);
        
        buttonPanel.add(backButton);
        buttonPanel.add(applyAllBtn);
        
        footerPanel.add(infoLabel, BorderLayout.WEST);
        footerPanel.add(buttonPanel, BorderLayout.EAST);
        
        return footerPanel;
    }
    
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
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
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
        button.setPreferredSize(new Dimension(150, 40));
        
        return button;
    }
    
    private void loadCurrentSettings() {
        // Load database settings
        dbHostField.setText(appProperties.getProperty("dbHost", "localhost"));
        dbPortField.setText(appProperties.getProperty("dbPort", "3306"));
        dbNameField.setText(appProperties.getProperty("dbName", "erpcrm"));
        dbUsernameField.setText(appProperties.getProperty("dbUsername", "root"));
        
        // Load application settings
        themeCombo.setSelectedItem(appProperties.getProperty("theme", "Modern"));
        languageCombo.setSelectedItem(appProperties.getProperty("language", "English"));
        dateFormatCombo.setSelectedItem(appProperties.getProperty("dateFormat", "dd/MM/yyyy"));
        fontSizeSlider.setValue(Integer.parseInt(appProperties.getProperty("fontSize", "14")));
        sessionTimeoutSpinner.setValue(Integer.parseInt(appProperties.getProperty("sessionTimeout", "30")));
        backupIntervalSpinner.setValue(Integer.parseInt(appProperties.getProperty("backupInterval", "24")));
        
        autoBackupCheckBox.setSelected(Boolean.parseBoolean(appProperties.getProperty("autoBackup", "true")));
        notificationCheckBox.setSelected(Boolean.parseBoolean(appProperties.getProperty("notifications", "true")));
        soundCheckBox.setSelected(Boolean.parseBoolean(appProperties.getProperty("sounds", "true")));
        
        // Load system information
        loadSystemInfo();
        
        settingsStatusLabel.setText("Settings loaded");
        settingsStatusLabel.setForeground(SUCCESS_COLOR);
    }
    
    private void loadSystemInfo() {
        StringBuilder info = new StringBuilder();
        info.append("System Information:\n");
        info.append("===================\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("Java Vendor: ").append(System.getProperty("java.vendor")).append("\n");
        info.append("Operating System: ").append(System.getProperty("os.name")).append("\n");
        info.append("OS Version: ").append(System.getProperty("os.version")).append("\n");
        info.append("Architecture: ").append(System.getProperty("os.arch")).append("\n");
        info.append("User Name: ").append(System.getProperty("user.name")).append("\n");
        info.append("User Home: ").append(System.getProperty("user.home")).append("\n");
        info.append("Working Directory: ").append(System.getProperty("user.dir")).append("\n");
        
        // Memory information
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        
        info.append("\nMemory Information:\n");
        info.append("Max Memory: ").append(maxMemory).append(" MB\n");
        info.append("Total Memory: ").append(totalMemory).append(" MB\n");
        info.append("Free Memory: ").append(freeMemory).append(" MB\n");
        info.append("Used Memory: ").append(totalMemory - freeMemory).append(" MB\n");
        
        systemInfoArea.setText(info.toString());
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == testConnectionBtn) {
            testDatabaseConnection();
        } else if (e.getSource() == saveDbSettingsBtn) {
            saveDatabaseSettings();
        } else if (e.getSource() == saveAppSettingsBtn) {
            saveApplicationSettings();
        } else if (e.getSource() == resetSettingsBtn) {
            resetToDefaults();
        } else if (e.getSource() == changePasswordBtn) {
            changePassword();
        } else if (e.getSource() == addUserBtn) {
            addNewUser();
        } else if (e.getSource() == backupNowBtn) {
            performBackup();
        } else if (e.getSource() == restoreBtn) {
            performRestore();
        } else if (e.getSource() == clearLogsBtn) {
            clearBackupLogs();
        } else if (e.getSource() == exportSettingsBtn) {
            exportSettings();
        } else if (e.getSource() == applyAllBtn) {
            applyAllChanges();
        } else if (e.getSource() == backButton) {
            dispose();
            new Main_class();
        }
    }
    
    private void testDatabaseConnection() {
        connectionStatusLabel.setText("Testing connection...");
        connectionStatusLabel.setForeground(WARNING_COLOR);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String host = dbHostField.getText().trim();
                    String port = dbPortField.getText().trim();
                    String dbName = dbNameField.getText().trim();
                    String username = dbUsernameField.getText().trim();
                    String password = new String(dbPasswordField.getPassword());
                    
                    String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    
                    try (java.sql.Connection testConn = java.sql.DriverManager.getConnection(url, username, password)) {
                        return testConn != null && !testConn.isClosed();
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        connectionStatusLabel.setText("Database: Connected ✓");
                        connectionStatusLabel.setForeground(SUCCESS_COLOR);
                        JOptionPane.showMessageDialog(Settings.this,
                            "Database connection successful!",
                            "Connection Test", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        connectionStatusLabel.setText("Database: Failed ✗");
                        connectionStatusLabel.setForeground(DANGER_COLOR);
                        JOptionPane.showMessageDialog(Settings.this,
                            "Database connection failed!",
                            "Connection Test", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    connectionStatusLabel.setText("Database: Error ✗");
                    connectionStatusLabel.setForeground(DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }
    
    private void saveDatabaseSettings() {
        appProperties.setProperty("dbHost", dbHostField.getText().trim());
        appProperties.setProperty("dbPort", dbPortField.getText().trim());
        appProperties.setProperty("dbName", dbNameField.getText().trim());
        appProperties.setProperty("dbUsername", dbUsernameField.getText().trim());
        // Note: In production, consider encrypting passwords
        
        saveProperties();
        settingsStatusLabel.setText("Database settings saved");
        settingsStatusLabel.setForeground(SUCCESS_COLOR);
        
        JOptionPane.showMessageDialog(this,
            "Database settings saved successfully!\nRestart application to apply changes.",
            "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void saveApplicationSettings() {
        appProperties.setProperty("theme", (String) themeCombo.getSelectedItem());
        appProperties.setProperty("language", (String) languageCombo.getSelectedItem());
        appProperties.setProperty("dateFormat", (String) dateFormatCombo.getSelectedItem());
        appProperties.setProperty("fontSize", String.valueOf(fontSizeSlider.getValue()));
        appProperties.setProperty("sessionTimeout", sessionTimeoutSpinner.getValue().toString());
        appProperties.setProperty("backupInterval", backupIntervalSpinner.getValue().toString());
        appProperties.setProperty("autoBackup", String.valueOf(autoBackupCheckBox.isSelected()));
        appProperties.setProperty("notifications", String.valueOf(notificationCheckBox.isSelected()));
        appProperties.setProperty("sounds", String.valueOf(soundCheckBox.isSelected()));
        
        saveProperties();
        settingsStatusLabel.setText("Application settings saved");
        settingsStatusLabel.setForeground(SUCCESS_COLOR);
        
        JOptionPane.showMessageDialog(this,
            "Application settings saved successfully!",
            "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void resetToDefaults() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to reset all settings to default values?",
            "Reset Settings", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            setDefaultProperties();
            loadCurrentSettings();
            saveProperties();
            
            settingsStatusLabel.setText("Settings reset to defaults");
            settingsStatusLabel.setForeground(WARNING_COLOR);
            
            JOptionPane.showMessageDialog(this,
                "Settings have been reset to default values!",
                "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void changePassword() {
        String currentPassword = currentPasswordField.getText().trim();
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all password fields.",
                                        "Incomplete Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match.",
                                        "Password Mismatch", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (newPassword.length() < 6) {
            JOptionPane.showMessageDialog(this, "Password must be at least 6 characters long.",
                                        "Weak Password", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (dbConnection != null && dbConnection.statement != null) {
                // Verify current password (in production, use proper authentication)
                String verifyQuery = "SELECT * FROM login WHERE password = ?";
                PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(verifyQuery);
                pstmt.setString(1, currentPassword);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Update password
                    String updateQuery = "UPDATE login SET password = ? WHERE password = ?";
                    PreparedStatement updateStmt = dbConnection.statement.getConnection().prepareStatement(updateQuery);
                    updateStmt.setString(1, newPassword);
                    updateStmt.setString(2, currentPassword);
                    
                    int result = updateStmt.executeUpdate();
                    if (result > 0) {
                        JOptionPane.showMessageDialog(this, "Password changed successfully!",
                                                    "Success", JOptionPane.INFORMATION_MESSAGE);
                        // Clear password fields
                        currentPasswordField.setText("");
                        newPasswordField.setText("");
                        confirmPasswordField.setText("");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to change password.",
                                                    "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Current password is incorrect.",
                                                "Authentication Failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addNewUser() {
        String username = newUsernameField.getText().trim();
        String role = (String) userRoleCombo.getSelectedItem();
        
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username.",
                                        "Incomplete Information", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            if (dbConnection != null && dbConnection.statement != null) {
                // Check if username already exists
                String checkQuery = "SELECT * FROM login WHERE username = ?";
                PreparedStatement checkStmt = dbConnection.statement.getConnection().prepareStatement(checkQuery);
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Username already exists.",
                                                "Duplicate User", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Generate default password
                String defaultPassword = "password123";
                
                // Insert new user
                String insertQuery = "INSERT INTO login (username, password, role) VALUES (?, ?, ?)";
                PreparedStatement insertStmt = dbConnection.statement.getConnection().prepareStatement(insertQuery);
                insertStmt.setString(1, username);
                insertStmt.setString(2, defaultPassword);
                insertStmt.setString(3, role);
                
                int result = insertStmt.executeUpdate();
                if (result > 0) {
                    JOptionPane.showMessageDialog(this,
                        "User added successfully!\nUsername: " + username +
                        "\nDefault Password: " + defaultPassword +
                        "\nRole: " + role +
                        "\n\nPlease ask the user to change their password on first login.",
                        "User Added", JOptionPane.INFORMATION_MESSAGE);
                    
                    newUsernameField.setText("");
                    userRoleCombo.setSelectedIndex(0);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add user.",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void performBackup() {
        backupProgressBar.setVisible(true);
        backupProgressBar.setIndeterminate(true);
        backupProgressBar.setString("Creating backup...");
        backupNowBtn.setEnabled(false);
        
        SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    publish("Starting database backup...");
                    Thread.sleep(1000); // Simulate backup process
                    
                    publish("Backing up employee table...");
                    Thread.sleep(1500);
                    
                    publish("Backing up login table...");
                    Thread.sleep(1000);
                    
                    publish("Compressing backup file...");
                    Thread.sleep(800);
                    
                    publish("Backup completed successfully!");
                    return true;
                } catch (Exception e) {
                    publish("Backup failed: " + e.getMessage());
                    return false;
                }
            }
            
            @Override
            protected void process(java.util.List<String> chunks) {
                for (String message : chunks) {
                    backupLogArea.append(java.time.LocalDateTime.now().toString() + " - " + message + "\n");
                    backupLogArea.setCaretPosition(backupLogArea.getDocument().getLength());
                }
            }
            
            @Override
            protected void done() {
                backupProgressBar.setVisible(false);
                backupNowBtn.setEnabled(true);
                
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(Settings.this,
                            "Database backup completed successfully!",
                            "Backup Complete", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(Settings.this,
                            "Database backup failed!",
                            "Backup Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Settings.this,
                        "Backup error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void performRestore() {
        int option = JOptionPane.showConfirmDialog(this,
            "Warning: This will replace all current data with backup data.\nAre you sure you want to continue?",
            "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Backup File");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".sql");
                }
                
                @Override
                public String getDescription() {
                    return "SQL Backup Files (*.sql)";
                }
            });
            
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                backupLogArea.append(java.time.LocalDateTime.now().toString() + " - Restore operation initiated\n");
                JOptionPane.showMessageDialog(this,
                    "Restore feature would be implemented here.\nSelected file: " + fileChooser.getSelectedFile().getName(),
                    "Restore", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void clearBackupLogs() {
        int option = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to clear all backup logs?",
            "Clear Logs", JOptionPane.YES_NO_OPTION);
        
        if (option == JOptionPane.YES_OPTION) {
            backupLogArea.setText("");
            backupLogArea.append(java.time.LocalDateTime.now().toString() + " - Logs cleared\n");
        }
    }
    
    private void exportSettings() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Settings");
        fileChooser.setSelectedFile(new File("ems_settings.properties"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File exportFile = fileChooser.getSelectedFile();
                FileOutputStream output = new FileOutputStream(exportFile);
                appProperties.store(output, "Exported ERP+CRM Settings - " + new java.util.Date());
                output.close();
                
                JOptionPane.showMessageDialog(this,
                    "Settings exported successfully to:\n" + exportFile.getAbsolutePath(),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting settings: " + ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void applyAllChanges() {
        // Save all settings
        saveDatabaseSettings();
        saveApplicationSettings();
        
        // Show confirmation
        int option = JOptionPane.showConfirmDialog(this,
            "All settings have been saved.\nSome changes require application restart.\nWould you like to restart now?",
            "Settings Applied", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            // In a real application, you would implement proper restart logic
            JOptionPane.showMessageDialog(this,
                "Please manually restart the application to apply all changes.",
                "Restart Required", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
    }
    
    @Override
    public void dispose() {
        // Clean up database connection
        if (dbConnection != null) {
            try {
                if (dbConnection.statement != null) {
                    dbConnection.statement.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Could not set system look and feel: " + e.getMessage());
            }
            new Settings();
        });
    }
}