import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AddEmployee extends JFrame implements ActionListener {
    // Constants for modern design
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 750;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color INPUT_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(220, 221, 225);
    
    // UI Components
    private JButton addButton, backButton, clearButton, previewButton;
    private JTextField nameField, salaryField, phoneField, emailField, positionField;
    private JTextArea addressArea;
    private JComboBox<String> educationCombo, departmentCombo, genderCombo;
    private JLabel idLabel, validationLabel;
    private JSpinner ageSpinner;
    private JFormattedTextField joiningDateField;
    private JProgressBar progressBar;
    
    // Data
    private final Random random = new Random();
    private final int employeeId = 100000 + random.nextInt(899999); // 6-digit ID
    private conn dbConnection;
    
    public AddEmployee() {
        initializeDatabase();
        initializeUI();
        setupValidation();
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
        // Configure main window
        setTitle("Add New Employee - EMS");
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
        
        // Create form panel
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Create footer with buttons
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
        
        JLabel titleLabel = new JLabel("Add New Employee");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Fill in the details to add a new employee to the system");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Employee ID display
        JPanel idPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        idPanel.setOpaque(false);
        
        JLabel idTitleLabel = new JLabel("Employee ID: ");
        idTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        idTitleLabel.setForeground(SECONDARY_COLOR);
        
        idLabel = new JLabel(String.valueOf(employeeId));
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        idLabel.setForeground(PRIMARY_COLOR);
        
        idPanel.add(idTitleLabel);
        idPanel.add(idLabel);
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(idPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setOpaque(false);
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));
        
        // Create form sections
        formPanel.add(createPersonalInfoSection());
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createContactInfoSection());
        formPanel.add(Box.createVerticalStrut(20));
        formPanel.add(createProfessionalInfoSection());
        formPanel.add(Box.createVerticalStrut(20));
        
        // Validation message label
        validationLabel = new JLabel();
        validationLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        validationLabel.setHorizontalAlignment(SwingConstants.CENTER);
        formPanel.add(validationLabel);
        
        return formPanel;
    }
    
    private JPanel createPersonalInfoSection() {
        JPanel section = createSectionPanel("Personal Information");
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Name field
        gbc.gridx = 0; gbc.gridy = 0;
        content.add(createFieldLabel("Full Name *"), gbc);
        gbc.gridx = 1;
        nameField = createStyledTextField("Enter full name");
        content.add(nameField, gbc);
        
        // Age field
        gbc.gridx = 2; gbc.gridy = 0;
        content.add(createFieldLabel("Age *"), gbc);
        gbc.gridx = 3;
        ageSpinner = new JSpinner(new SpinnerNumberModel(25, 18, 65, 1));
        styleSpinner(ageSpinner);
        content.add(ageSpinner, gbc);
        
        // Gender field
        gbc.gridx = 0; gbc.gridy = 1;
        content.add(createFieldLabel("Gender *"), gbc);
        gbc.gridx = 1;
        String[] genders = {"Male", "Female", "Other"};
        genderCombo = createStyledComboBox(genders);
        content.add(genderCombo, gbc);
        
        // Joining Date
        gbc.gridx = 2; gbc.gridy = 1;
        content.add(createFieldLabel("Joining Date *"), gbc);
        gbc.gridx = 3;
        joiningDateField = new JFormattedTextField(new SimpleDateFormat("dd/MM/yyyy"));
        joiningDateField.setValue(new Date());
        styleTextField(joiningDateField);
        content.add(joiningDateField, gbc);
        
        section.add(content, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createContactInfoSection() {
        JPanel section = createSectionPanel("Contact Information");
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Phone field
        gbc.gridx = 0; gbc.gridy = 0;
        content.add(createFieldLabel("Phone Number *"), gbc);
        gbc.gridx = 1;
        phoneField = createStyledTextField("10-digit phone number");
        content.add(phoneField, gbc);
        
        // Email field
        gbc.gridx = 2; gbc.gridy = 0;
        content.add(createFieldLabel("Email Address *"), gbc);
        gbc.gridx = 3;
        emailField = createStyledTextField("employee@company.com");
        content.add(emailField, gbc);
        
        // Address field
        gbc.gridx = 0; gbc.gridy = 1;
        content.add(createFieldLabel("Address *"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        addressArea = new JTextArea(3, 30);
        addressArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        addressArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        addressArea.setBackground(INPUT_COLOR);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setPreferredSize(new Dimension(400, 80));
        content.add(addressScroll, gbc);
        
        section.add(content, BorderLayout.CENTER);
        return section;
    }
    
    private JPanel createProfessionalInfoSection() {
        JPanel section = createSectionPanel("Professional Information");
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Position field
        gbc.gridx = 0; gbc.gridy = 0;
        content.add(createFieldLabel("Position *"), gbc);
        gbc.gridx = 1;
        positionField = createStyledTextField("Job title/position");
        content.add(positionField, gbc);
        
        // Department field
        gbc.gridx = 2; gbc.gridy = 0;
        content.add(createFieldLabel("Department *"), gbc);
        gbc.gridx = 3;
        String[] departments = {"IT", "HR", "Finance", "Marketing", "Operations", "Sales", "Other"};
        departmentCombo = createStyledComboBox(departments);
        content.add(departmentCombo, gbc);
        
        // Salary field
        gbc.gridx = 0; gbc.gridy = 1;
        content.add(createFieldLabel("Salary *"), gbc);
        gbc.gridx = 1;
        salaryField = createStyledTextField("Annual salary");
        content.add(salaryField, gbc);
        
        // Education field
        gbc.gridx = 2; gbc.gridy = 1;
        content.add(createFieldLabel("Education *"), gbc);
        gbc.gridx = 3;
        String[] education = {"B.Tech", "BE", "BSC IT", "MBA", "MS", "PhD", "Diploma", "Other"};
        educationCombo = createStyledComboBox(education);
        content.add(educationCombo, gbc);
        
        section.add(content, BorderLayout.CENTER);
        return section;
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
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        
        section.add(headerPanel, BorderLayout.NORTH);
        return section;
    }
    
    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(SECONDARY_COLOR);
        label.setPreferredSize(new Dimension(120, 30));
        return label;
    }
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField(15);
        styleTextField(field);
        
        // Add placeholder functionality
        field.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(SECONDARY_COLOR);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setForeground(Color.GRAY);
                    field.setText(placeholder);
                }
            }
        });
        
        field.setText(placeholder);
        field.setForeground(Color.GRAY);
        
        return field;
    }
    
    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(INPUT_COLOR);
        field.setPreferredSize(new Dimension(200, 40));
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBackground(INPUT_COLOR);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        combo.setPreferredSize(new Dimension(200, 40));
        return combo;
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        spinner.setPreferredSize(new Dimension(100, 40));
        
        // Style the spinner editor
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(INPUT_COLOR);
        }
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Adding employee...");
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        backButton = createModernButton("Back", new Color(108, 117, 125));
        clearButton = createModernButton("Clear All", new Color(255, 193, 7));
        previewButton = createModernButton("Preview", PRIMARY_COLOR);
        addButton = createModernButton("Add Employee", SUCCESS_COLOR);
        
        backButton.addActionListener(this);
        clearButton.addActionListener(this);
        previewButton.addActionListener(this);
        addButton.addActionListener(this);
        
        buttonPanel.add(backButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(addButton);
        
        footerPanel.add(progressBar, BorderLayout.NORTH);
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
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
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 45));
        
        return button;
    }
    
    private void setupValidation() {
        // Real-time validation for email
        emailField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validateEmail();
            }
        });
        
        // Real-time validation for phone
        phoneField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                validatePhone();
            }
        });
    }
    
    private void validateEmail() {
        String email = getFieldText(emailField);
        if (!email.isEmpty()) {
            Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
            if (!emailPattern.matcher(email).matches()) {
                emailField.setBorder(BorderFactory.createLineBorder(DANGER_COLOR, 2));
                showValidationMessage("Invalid email format", DANGER_COLOR);
            } else {
                emailField.setBorder(BorderFactory.createLineBorder(SUCCESS_COLOR, 2));
                clearValidationMessage();
            }
        }
    }
    
    private void validatePhone() {
        String phone = getFieldText(phoneField);
        if (!phone.isEmpty()) {
            if (!phone.matches("\\d{10}")) {
                phoneField.setBorder(BorderFactory.createLineBorder(DANGER_COLOR, 2));
                showValidationMessage("Phone number must be 10 digits", DANGER_COLOR);
            } else {
                phoneField.setBorder(BorderFactory.createLineBorder(SUCCESS_COLOR, 2));
                clearValidationMessage();
            }
        }
    }
    
    private void showValidationMessage(String message, Color color) {
        validationLabel.setText(message);
        validationLabel.setForeground(color);
    }
    
    private void clearValidationMessage() {
        validationLabel.setText("");
    }
    
    private String getFieldText(JTextField field) {
        String text = field.getText().trim();
        // Check if it's placeholder text
        if (text.equals("Enter full name") || text.equals("10-digit phone number") || 
            text.equals("employee@company.com") || text.equals("Job title/position") || 
            text.equals("Annual salary")) {
            return "";
        }
        return text;
    }
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (getFieldText(nameField).isEmpty()) errors.append("• Name is required\n");
        if (getFieldText(phoneField).isEmpty()) errors.append("• Phone number is required\n");
        if (getFieldText(emailField).isEmpty()) errors.append("• Email is required\n");
        if (getFieldText(positionField).isEmpty()) errors.append("• Position is required\n");
        if (getFieldText(salaryField).isEmpty()) errors.append("• Salary is required\n");
        if (addressArea.getText().trim().isEmpty()) errors.append("• Address is required\n");
        
        // Validate phone number format
        String phone = getFieldText(phoneField);
        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            errors.append("• Phone number must be 10 digits\n");
        }
        
        // Validate email format
        String email = getFieldText(emailField);
        if (!email.isEmpty()) {
            Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
            if (!emailPattern.matcher(email).matches()) {
                errors.append("• Invalid email format\n");
            }
        }
        
        // Validate salary is numeric
        String salary = getFieldText(salaryField);
        if (!salary.isEmpty()) {
            try {
                Double.parseDouble(salary);
            } catch (NumberFormatException e) {
                errors.append("• Salary must be a valid number\n");
            }
        }
        
        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this, 
                "Please fix the following errors:\n\n" + errors.toString(),
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void clearForm() {
        nameField.setText("Enter full name");
        nameField.setForeground(Color.GRAY);
        salaryField.setText("Annual salary");
        salaryField.setForeground(Color.GRAY);
        phoneField.setText("10-digit phone number");
        phoneField.setForeground(Color.GRAY);
        emailField.setText("employee@company.com");
        emailField.setForeground(Color.GRAY);
        positionField.setText("Job title/position");
        positionField.setForeground(Color.GRAY);
        addressArea.setText("");
        educationCombo.setSelectedIndex(0);
        departmentCombo.setSelectedIndex(0);
        genderCombo.setSelectedIndex(0);
        ageSpinner.setValue(25);
        joiningDateField.setValue(new Date());
        clearValidationMessage();
    }
    
    private void showPreview() {
        if (!validateForm()) return;
        
        StringBuilder preview = new StringBuilder();
        preview.append("Employee Preview:\n\n");
        preview.append("ID: ").append(employeeId).append("\n");
        preview.append("Name: ").append(getFieldText(nameField)).append("\n");
        preview.append("Age: ").append(ageSpinner.getValue()).append("\n");
        preview.append("Gender: ").append(genderCombo.getSelectedItem()).append("\n");
        preview.append("Phone: ").append(getFieldText(phoneField)).append("\n");
        preview.append("Email: ").append(getFieldText(emailField)).append("\n");
        preview.append("Position: ").append(getFieldText(positionField)).append("\n");
        preview.append("Department: ").append(departmentCombo.getSelectedItem()).append("\n");
        preview.append("Salary: ₹").append(getFieldText(salaryField)).append("\n");
        preview.append("Education: ").append(educationCombo.getSelectedItem()).append("\n");
        preview.append("Address: ").append(addressArea.getText().trim()).append("\n");
        preview.append("Joining Date: ").append(joiningDateField.getText()).append("\n");
        
        JOptionPane.showMessageDialog(this, preview.toString(), 
                                    "Employee Preview", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addEmployee();
        } else if (e.getSource() == backButton) {
            dispose();
            new Main_class();
        } else if (e.getSource() == clearButton) {
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to clear all fields?",
                "Confirm Clear", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                clearForm();
            }
        } else if (e.getSource() == previewButton) {
            showPreview();
        }
    }
    
    private void addEmployee() {
        if (!validateForm()) return;
        
        // Show progress bar
        progressBar.setVisible(true);
        addButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    if (dbConnection == null || dbConnection.statement == null) {
                        throw new Exception("Database connection not available");
                    }
                    
                    // Use PreparedStatement for security
                    String query = "INSERT INTO employee (id, name, age, gender, phone, email, position, department, salary, education, address, joining_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    
                    try (PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query)) {
                        pstmt.setString(1, String.valueOf(employeeId));
                        pstmt.setString(2, getFieldText(nameField));
                        pstmt.setInt(3, (Integer) ageSpinner.getValue());
                        pstmt.setString(4, (String) genderCombo.getSelectedItem());
                        pstmt.setString(5, getFieldText(phoneField));
                        pstmt.setString(6, getFieldText(emailField));
                        pstmt.setString(7, getFieldText(positionField));
                        pstmt.setString(8, (String) departmentCombo.getSelectedItem());
                        pstmt.setString(9, getFieldText(salaryField));
                        pstmt.setString(10, (String) educationCombo.getSelectedItem());
                        pstmt.setString(11, addressArea.getText().trim());
                        pstmt.setString(12, joiningDateField.getText());
                        
                        pstmt.executeUpdate();
                        return true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
                addButton.setEnabled(true);
                
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(AddEmployee.this,
                            "Employee added successfully!\nEmployee ID: " + employeeId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new Main_class();
                    } else {
                        JOptionPane.showMessageDialog(AddEmployee.this,
                            "Failed to add employee. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AddEmployee.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
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
            new AddEmployee();
        });
    }
}