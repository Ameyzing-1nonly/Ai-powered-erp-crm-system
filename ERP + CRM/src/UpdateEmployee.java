import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UpdateEmployee extends JFrame implements ActionListener {
    // Constants for modern design
    private static final int WINDOW_WIDTH = 1000;
    private static final int WINDOW_HEIGHT = 750;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color INPUT_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(220, 221, 225);
    
    // UI Components
    private JButton updateButton, backButton, clearButton, previewButton, resetButton;
    private JTextField nameField, salaryField, phoneField, emailField, positionField;
    private JTextArea addressArea;
    private JComboBox<String> educationCombo, departmentCombo, genderCombo;
    private JLabel idLabel, nameLabel, validationLabel;
    private JSpinner ageSpinner;
    private JFormattedTextField joiningDateField;
    private JProgressBar progressBar;
    
    // Data
    private String employeeId;
    private conn dbConnection;
    private Employee originalEmployee;
    
    // Inner class to store original employee data
    private static class Employee {
        String id, name, salary, phone, address, email, position, education, department, gender, joiningDate;
        int age;
        
        Employee(String id, String name, int age, String gender, String salary, String phone, 
                String address, String email, String position, String education, String department, String joiningDate) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.salary = salary;
            this.phone = phone;
            this.address = address;
            this.email = email;
            this.position = position;
            this.education = education;
            this.department = department;
            this.joiningDate = joiningDate;
        }
    }
    
    public UpdateEmployee(String employeeId) {
        this.employeeId = employeeId;
        initializeDatabase();
        initializeUI();
        loadEmployeeData();
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
        setTitle("Update Employee - EMS");
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
        
        JLabel titleLabel = new JLabel("Update Employee Details");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Modify employee information and save changes");
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
        
        idLabel = new JLabel(employeeId);
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
        
        // Name field (read-only)
        gbc.gridx = 0; gbc.gridy = 0;
        content.add(createFieldLabel("Full Name"), gbc);
        gbc.gridx = 1;
        nameLabel = new JLabel();
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(SECONDARY_COLOR);
        nameLabel.setPreferredSize(new Dimension(200, 30));
        content.add(nameLabel, gbc);
        
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
        phoneField = createStyledTextField();
        content.add(phoneField, gbc);
        
        // Email field
        gbc.gridx = 2; gbc.gridy = 0;
        content.add(createFieldLabel("Email Address *"), gbc);
        gbc.gridx = 3;
        emailField = createStyledTextField();
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
        positionField = createStyledTextField();
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
        salaryField = createStyledTextField();
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
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField(15);
        styleTextField(field);
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
        progressBar.setString("Updating employee...");
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        backButton = createModernButton("Back", new Color(108, 117, 125));
        resetButton = createModernButton("Reset Changes", WARNING_COLOR);
        previewButton = createModernButton("Preview", PRIMARY_COLOR);
        updateButton = createModernButton("Update Employee", SUCCESS_COLOR);
        
        backButton.addActionListener(this);
        resetButton.addActionListener(this);
        previewButton.addActionListener(this);
        updateButton.addActionListener(this);
        
        buttonPanel.add(backButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(updateButton);
        
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
    
    private void loadEmployeeData() {
        if (employeeId == null || employeeId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Invalid employee ID provided.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        
        try {
            if (dbConnection == null || dbConnection.statement == null) {
                throw new Exception("Database connection not available");
            }
            
            String query = "SELECT * FROM employee WHERE id = ?";
            try (PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query)) {
                pstmt.setString(1, employeeId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    // Store original data
                    originalEmployee = new Employee(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("salary"),
                        rs.getString("phone"),
                        rs.getString("address"),
                        rs.getString("email"),
                        rs.getString("position"),
                        rs.getString("education"),
                        rs.getString("department"),
                        rs.getString("joining_date")
                    );
                    
                    // Populate form fields
                    populateFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Employee not found with ID: " + employeeId,
                                                "Employee Not Found", JOptionPane.WARNING_MESSAGE);
                    dispose();
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    private void populateFields() {
        if (originalEmployee == null) return;
        
        // Set read-only fields
        nameLabel.setText(originalEmployee.name);
        idLabel.setText(originalEmployee.id);
        
        // Set editable fields
        ageSpinner.setValue(originalEmployee.age);
        salaryField.setText(originalEmployee.salary);
        phoneField.setText(originalEmployee.phone);
        addressArea.setText(originalEmployee.address);
        emailField.setText(originalEmployee.email);
        positionField.setText(originalEmployee.position);
        
        // Set combo boxes
        setComboBoxValue(genderCombo, originalEmployee.gender);
        setComboBoxValue(educationCombo, originalEmployee.education);
        setComboBoxValue(departmentCombo, originalEmployee.department);
        
        // Set joining date
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date date = sdf.parse(originalEmployee.joiningDate);
            joiningDateField.setValue(date);
        } catch (Exception e) {
            joiningDateField.setValue(new Date());
        }
    }
    
    private void setComboBoxValue(JComboBox<String> combo, String value) {
        if (value != null) {
            for (int i = 0; i < combo.getItemCount(); i++) {
                if (combo.getItemAt(i).equals(value)) {
                    combo.setSelectedIndex(i);
                    break;
                }
            }
        }
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
        String email = emailField.getText().trim();
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
        String phone = phoneField.getText().trim();
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
    
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();
        
        if (phoneField.getText().trim().isEmpty()) errors.append("• Phone number is required\n");
        if (emailField.getText().trim().isEmpty()) errors.append("• Email is required\n");
        if (positionField.getText().trim().isEmpty()) errors.append("• Position is required\n");
        if (salaryField.getText().trim().isEmpty()) errors.append("• Salary is required\n");
        if (addressArea.getText().trim().isEmpty()) errors.append("• Address is required\n");
        
        // Validate phone number format
        String phone = phoneField.getText().trim();
        if (!phone.isEmpty() && !phone.matches("\\d{10}")) {
            errors.append("• Phone number must be 10 digits\n");
        }
        
        // Validate email format
        String email = emailField.getText().trim();
        if (!email.isEmpty()) {
            Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
            if (!emailPattern.matcher(email).matches()) {
                errors.append("• Invalid email format\n");
            }
        }
        
        // Validate salary is numeric
        String salary = salaryField.getText().trim();
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
    
    private void resetToOriginal() {
        if (originalEmployee != null) {
            populateFields();
            clearValidationMessage();
        }
    }
    
    private void showPreview() {
        if (!validateForm()) return;
        
        StringBuilder preview = new StringBuilder();
        preview.append("Employee Update Preview:\n\n");
        preview.append("ID: ").append(employeeId).append("\n");
        preview.append("Name: ").append(nameLabel.getText()).append("\n");
        preview.append("Age: ").append(ageSpinner.getValue()).append("\n");
        preview.append("Gender: ").append(genderCombo.getSelectedItem()).append("\n");
        preview.append("Phone: ").append(phoneField.getText().trim()).append("\n");
        preview.append("Email: ").append(emailField.getText().trim()).append("\n");
        preview.append("Position: ").append(positionField.getText().trim()).append("\n");
        preview.append("Department: ").append(departmentCombo.getSelectedItem()).append("\n");
        preview.append("Salary: ₹").append(salaryField.getText().trim()).append("\n");
        preview.append("Education: ").append(educationCombo.getSelectedItem()).append("\n");
        preview.append("Address: ").append(addressArea.getText().trim()).append("\n");
        preview.append("Joining Date: ").append(joiningDateField.getText()).append("\n");
        
        // Show changes
        if (originalEmployee != null) {
            preview.append("\n--- CHANGES ---\n");
            if (!originalEmployee.salary.equals(salaryField.getText().trim())) {
                preview.append("Salary: ").append(originalEmployee.salary).append(" → ").append(salaryField.getText().trim()).append("\n");
            }
            if (!originalEmployee.phone.equals(phoneField.getText().trim())) {
                preview.append("Phone: ").append(originalEmployee.phone).append(" → ").append(phoneField.getText().trim()).append("\n");
            }
            if (!originalEmployee.email.equals(emailField.getText().trim())) {
                preview.append("Email: ").append(originalEmployee.email).append(" → ").append(emailField.getText().trim()).append("\n");
            }
        }
        
        JOptionPane.showMessageDialog(this, preview.toString(), 
                                    "Update Preview", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == updateButton) {
            updateEmployee();
        } else if (e.getSource() == backButton) {
            dispose();
            new View_Employee();
        } else if (e.getSource() == resetButton) {
            int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset all changes?",
                "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                resetToOriginal();
            }
        } else if (e.getSource() == previewButton) {
            showPreview();
        }
    }
    
    private void updateEmployee() {
        if (!validateForm()) return;
        
        // Show progress bar
        progressBar.setVisible(true);
        updateButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    if (dbConnection == null || dbConnection.statement == null) {
                        throw new Exception("Database connection not available");
                    }
                    
                    // Use PreparedStatement for security
                    String query = "UPDATE employee SET age=?, gender=?, phone=?, email=?, position=?, department=?, salary=?, education=?, address=?, joining_date=? WHERE id=?";
                    
                    try (PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query)) {
                        pstmt.setInt(1, (Integer) ageSpinner.getValue());
                        pstmt.setString(2, (String) genderCombo.getSelectedItem());
                        pstmt.setString(3, phoneField.getText().trim());
                        pstmt.setString(4, emailField.getText().trim());
                        pstmt.setString(5, positionField.getText().trim());
                        pstmt.setString(6, (String) departmentCombo.getSelectedItem());
                        pstmt.setString(7, salaryField.getText().trim());
                        pstmt.setString(8, (String) educationCombo.getSelectedItem());
                        pstmt.setString(9, addressArea.getText().trim());
                        pstmt.setString(10, joiningDateField.getText());
                        pstmt.setString(11, employeeId);
                        
                        int rowsAffected = pstmt.executeUpdate();
                        return rowsAffected > 0;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                progressBar.setVisible(false);
                updateButton.setEnabled(true);
                
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(UpdateEmployee.this,
                            "Employee details updated successfully!\nEmployee ID: " + employeeId,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        new View_Employee();
                    } else {
                        JOptionPane.showMessageDialog(UpdateEmployee.this,
                            "Failed to update employee details. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(UpdateEmployee.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private boolean hasChanges() {
        if (originalEmployee == null) return true;
        
        return !originalEmployee.salary.equals(salaryField.getText().trim()) ||
               !originalEmployee.phone.equals(phoneField.getText().trim()) ||
               !originalEmployee.email.equals(emailField.getText().trim()) ||
               !originalEmployee.position.equals(positionField.getText().trim()) ||
               !originalEmployee.address.equals(addressArea.getText().trim()) ||
               !originalEmployee.education.equals((String) educationCombo.getSelectedItem()) ||
               !originalEmployee.department.equals((String) departmentCombo.getSelectedItem()) ||
               !originalEmployee.gender.equals((String) genderCombo.getSelectedItem()) ||
               originalEmployee.age != (Integer) ageSpinner.getValue();
    }
    
    @Override
    public void dispose() {
        // Check for unsaved changes
        if (hasChanges()) {
            int option = JOptionPane.showConfirmDialog(this,
                "You have unsaved changes. Are you sure you want to exit?",
                "Unsaved Changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (option != JOptionPane.YES_OPTION) {
                return;
            }
        }
        
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
            new UpdateEmployee("100001"); // Example employee ID for testing
        });
    }
}