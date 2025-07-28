import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class RemoveEmployee extends JFrame implements ActionListener, ItemListener {
    // Constants for modern design
    private static final int WINDOW_WIDTH = 900;
    private static final int WINDOW_HEIGHT = 650;
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
    private JComboBox<EmployeeItem> employeeComboBox;
    private JButton deleteButton, backButton, refreshButton, previewButton;
    private JLabel nameValueLabel, phoneValueLabel, emailValueLabel, positionValueLabel, 
                   departmentValueLabel, salaryValueLabel, addressValueLabel, statusLabel;
    private JPanel employeeInfoPanel;
    private JProgressBar progressBar;
    
    // Data
    private conn dbConnection;
    private EmployeeDetails selectedEmployee;
    
    // Inner classes for data management
    private static class EmployeeItem {
        private final String id;
        private final String name;
        
        public EmployeeItem(String id, String name) {
            this.id = id;
            this.name = name;
        }
        
        public String getId() { return id; }
        public String getName() { return name; }
        
        @Override
        public String toString() {
            return id + " - " + name;
        }
    }
    
    private static class EmployeeDetails {
        String id, name, phone, email, position, department, salary, address, education, gender, joiningDate;
        int age;
        
        EmployeeDetails(String id, String name, int age, String gender, String phone, String email, 
                       String position, String department, String salary, String education, 
                       String address, String joiningDate) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.phone = phone;
            this.email = email;
            this.position = position;
            this.department = department;
            this.salary = salary;
            this.education = education;
            this.address = address;
            this.joiningDate = joiningDate;
        }
    }
    
    public RemoveEmployee() {
        initializeDatabase();
        initializeUI();
        loadEmployeeData();
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
        setTitle("Remove Employee - EMS");
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
        
        // Create content
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Create footer with buttons
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 40, 20, 40));
        
        // Title section
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Remove Employee");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Select an employee to remove from the system");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Warning icon and message
        JPanel warningPanel = createWarningPanel();
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(warningPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createWarningPanel() {
        JPanel warningPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        warningPanel.setOpaque(false);
        
        // Warning icon
        JLabel warningIcon = new JLabel("⚠");
        warningIcon.setFont(new Font("Segoe UI", Font.BOLD, 24));
        warningIcon.setForeground(WARNING_COLOR);
        
        JLabel warningText = new JLabel("<html><div style='text-align: right;'>Caution!<br/>This action cannot be undone</div></html>");
        warningText.setFont(new Font("Segoe UI", Font.BOLD, 12));
        warningText.setForeground(DANGER_COLOR);
        
        warningPanel.add(warningIcon);
        warningPanel.add(Box.createHorizontalStrut(10));
        warningPanel.add(warningText);
        
        return warningPanel;
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        
        // Selection panel
        JPanel selectionPanel = createSelectionPanel();
        contentPanel.add(selectionPanel, BorderLayout.NORTH);
        
        // Employee info panel
        employeeInfoPanel = createEmployeeInfoPanel();
        contentPanel.add(employeeInfoPanel, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private JPanel createSelectionPanel() {
        JPanel selectionPanel = new JPanel(new BorderLayout());
        selectionPanel.setOpaque(false);
        selectionPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        selectionPanel.setBackground(CARD_COLOR);
        
        // Selection content
        JPanel selectionContent = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        selectionContent.setOpaque(false);
        
        JLabel selectLabel = new JLabel("Select Employee:");
        selectLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        selectLabel.setForeground(SECONDARY_COLOR);
        
        employeeComboBox = new JComboBox<>();
        employeeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeComboBox.setPreferredSize(new Dimension(300, 40));
        employeeComboBox.setBackground(INPUT_COLOR);
        employeeComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        employeeComboBox.addItemListener(this);
        
        refreshButton = createModernButton("Refresh", SUCCESS_COLOR, new Dimension(100, 40));
        refreshButton.addActionListener(this);
        
        selectionContent.add(selectLabel);
        selectionContent.add(employeeComboBox);
        selectionContent.add(Box.createHorizontalStrut(20));
        selectionContent.add(refreshButton);
        
        // Status label
        statusLabel = new JLabel("Select an employee to view details");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(127, 140, 141));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        selectionPanel.add(selectionContent, BorderLayout.NORTH);
        selectionPanel.add(statusLabel, BorderLayout.SOUTH);
        
        return selectionPanel;
    }
    
    private JPanel createEmployeeInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(0, 0, 20, 0)));
        infoPanel.setBackground(CARD_COLOR);
        
        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(DANGER_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel headerLabel = new JLabel("Employee Details");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        
        // Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 20);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create info fields
        createInfoField(contentPanel, gbc, "Name:", 0, 0);
        nameValueLabel = createValueLabel();
        gbc.gridx = 1;
        contentPanel.add(nameValueLabel, gbc);
        
        createInfoField(contentPanel, gbc, "Phone:", 2, 0);
        phoneValueLabel = createValueLabel();
        gbc.gridx = 3;
        contentPanel.add(phoneValueLabel, gbc);
        
        createInfoField(contentPanel, gbc, "Email:", 0, 1);
        emailValueLabel = createValueLabel();
        gbc.gridx = 1; gbc.gridy = 1;
        contentPanel.add(emailValueLabel, gbc);
        
        createInfoField(contentPanel, gbc, "Position:", 2, 1);
        positionValueLabel = createValueLabel();
        gbc.gridx = 3;
        contentPanel.add(positionValueLabel, gbc);
        
        createInfoField(contentPanel, gbc, "Department:", 0, 2);
        departmentValueLabel = createValueLabel();
        gbc.gridx = 1; gbc.gridy = 2;
        contentPanel.add(departmentValueLabel, gbc);
        
        createInfoField(contentPanel, gbc, "Salary:", 2, 2);
        salaryValueLabel = createValueLabel();
        gbc.gridx = 3;
        contentPanel.add(salaryValueLabel, gbc);
        
        createInfoField(contentPanel, gbc, "Address:", 0, 3);
        addressValueLabel = createValueLabel();
        addressValueLabel.setPreferredSize(new Dimension(500, 30));
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 3;
        contentPanel.add(addressValueLabel, gbc);
        
        infoPanel.add(headerPanel, BorderLayout.NORTH);
        infoPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Initially hide the panel
        infoPanel.setVisible(false);
        
        return infoPanel;
    }
    
    private void createInfoField(JPanel parent, GridBagConstraints gbc, String labelText, int x, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(SECONDARY_COLOR);
        label.setPreferredSize(new Dimension(100, 30));
        gbc.gridx = x; gbc.gridy = y; gbc.gridwidth = 1;
        parent.add(label, gbc);
    }
    
    private JLabel createValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(SECONDARY_COLOR);
        label.setPreferredSize(new Dimension(150, 30));
        return label;
    }
    
    private JPanel createFooterPanel() {
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setOpaque(false);
        footerPanel.setBorder(new EmptyBorder(20, 40, 30, 40));
        
        // Progress bar
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        progressBar.setStringPainted(true);
        progressBar.setString("Removing employee...");
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        backButton = createModernButton("Back", new Color(108, 117, 125), new Dimension(120, 45));
        previewButton = createModernButton("Preview", PRIMARY_COLOR, new Dimension(120, 45));
        deleteButton = createModernButton("Delete Employee", DANGER_COLOR, new Dimension(140, 45));
        
        // Initially disable delete button
        deleteButton.setEnabled(false);
        previewButton.setEnabled(false);
        
        backButton.addActionListener(this);
        previewButton.addActionListener(this);
        deleteButton.addActionListener(this);
        
        buttonPanel.add(backButton);
        buttonPanel.add(previewButton);
        buttonPanel.add(deleteButton);
        
        footerPanel.add(progressBar, BorderLayout.NORTH);
        footerPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return footerPanel;
    }
    
    private JButton createModernButton(String text, Color bgColor, Dimension size) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color currentColor = bgColor;
                if (!isEnabled()) {
                    currentColor = new Color(189, 195, 199);
                } else if (getModel().isPressed()) {
                    currentColor = bgColor.darker();
                } else if (getModel().isRollover()) {
                    currentColor = bgColor.brighter();
                }
                
                g2d.setColor(currentColor);
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
        button.setPreferredSize(size);
        
        return button;
    }
    
    private void loadEmployeeData() {
        try {
            if (dbConnection == null || dbConnection.statement == null) {
                statusLabel.setText("Database connection not available");
                statusLabel.setForeground(DANGER_COLOR);
                return;
            }
            
            employeeComboBox.removeAllItems();
            employeeComboBox.addItem(new EmployeeItem("", "-- Select Employee --"));
            
            ResultSet rs = dbConnection.statement.executeQuery("SELECT id, name FROM employee ORDER BY name");
            int count = 0;
            while (rs.next()) {
                employeeComboBox.addItem(new EmployeeItem(rs.getString("id"), rs.getString("name")));
                count++;
            }
            
            statusLabel.setText(count + " employees available");
            statusLabel.setForeground(SUCCESS_COLOR);
            
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading employee data");
            statusLabel.setForeground(DANGER_COLOR);
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadEmployeeDetails(String employeeId) {
        if (employeeId == null || employeeId.isEmpty()) {
            clearEmployeeDetails();
            return;
        }
        
        try {
            String query = "SELECT * FROM employee WHERE id = ?";
            try (PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query)) {
                pstmt.setString(1, employeeId);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    selectedEmployee = new EmployeeDetails(
                        rs.getString("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("gender"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("position"),
                        rs.getString("department"),
                        rs.getString("salary"),
                        rs.getString("education"),
                        rs.getString("address"),
                        rs.getString("joining_date")
                    );
                    
                    updateEmployeeDisplay();
                    employeeInfoPanel.setVisible(true);
                    deleteButton.setEnabled(true);
                    previewButton.setEnabled(true);
                    
                    statusLabel.setText("Employee details loaded");
                    statusLabel.setForeground(SUCCESS_COLOR);
                } else {
                    clearEmployeeDetails();
                    statusLabel.setText("Employee not found");
                    statusLabel.setForeground(DANGER_COLOR);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            clearEmployeeDetails();
            statusLabel.setText("Error loading employee details");
            statusLabel.setForeground(DANGER_COLOR);
        }
    }
    
    private void updateEmployeeDisplay() {
        if (selectedEmployee == null) return;
        
        nameValueLabel.setText(selectedEmployee.name);
        phoneValueLabel.setText(selectedEmployee.phone);
        emailValueLabel.setText(selectedEmployee.email);
        positionValueLabel.setText(selectedEmployee.position);
        departmentValueLabel.setText(selectedEmployee.department);
        salaryValueLabel.setText("₹" + selectedEmployee.salary);
        addressValueLabel.setText(selectedEmployee.address);
    }
    
    private void clearEmployeeDetails() {
        selectedEmployee = null;
        nameValueLabel.setText("-");
        phoneValueLabel.setText("-");
        emailValueLabel.setText("-");
        positionValueLabel.setText("-");
        departmentValueLabel.setText("-");
        salaryValueLabel.setText("-");
        addressValueLabel.setText("-");
        
        employeeInfoPanel.setVisible(false);
        deleteButton.setEnabled(false);
        previewButton.setEnabled(false);
    }
    
    private void showPreview() {
        if (selectedEmployee == null) return;
        
        StringBuilder preview = new StringBuilder();
        preview.append("⚠ DELETION PREVIEW ⚠\n\n");
        preview.append("The following employee will be PERMANENTLY removed:\n\n");
        preview.append("ID: ").append(selectedEmployee.id).append("\n");
        preview.append("Name: ").append(selectedEmployee.name).append("\n");
        preview.append("Position: ").append(selectedEmployee.position).append("\n");
        preview.append("Department: ").append(selectedEmployee.department).append("\n");
        preview.append("Phone: ").append(selectedEmployee.phone).append("\n");
        preview.append("Email: ").append(selectedEmployee.email).append("\n");
        preview.append("Salary: ₹").append(selectedEmployee.salary).append("\n");
        preview.append("Address: ").append(selectedEmployee.address).append("\n\n");
        preview.append("⚠ WARNING: This action cannot be undone! ⚠");
        
        JOptionPane.showMessageDialog(this, preview.toString(), 
                                    "Deletion Preview", JOptionPane.WARNING_MESSAGE);
    }
    
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            EmployeeItem selectedItem = (EmployeeItem) employeeComboBox.getSelectedItem();
            if (selectedItem != null && !selectedItem.getId().isEmpty()) {
                loadEmployeeDetails(selectedItem.getId());
            } else {
                clearEmployeeDetails();
                statusLabel.setText("Select an employee to view details");
                statusLabel.setForeground(new Color(127, 140, 141));
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == deleteButton) {
            confirmAndDeleteEmployee();
        } else if (e.getSource() == backButton) {
            dispose();
            new Main_class();
        } else if (e.getSource() == refreshButton) {
            loadEmployeeData();
        } else if (e.getSource() == previewButton) {
            showPreview();
        }
    }
    
    private void confirmAndDeleteEmployee() {
        if (selectedEmployee == null) return;
        
        // Multi-step confirmation
        String[] options = {"Cancel", "Yes, Delete"};
        int option = JOptionPane.showOptionDialog(this,
            "Are you absolutely sure you want to delete:\n\n" +
            selectedEmployee.name + " (ID: " + selectedEmployee.id + ")?\n\n" +
            "⚠ This action cannot be undone! ⚠",
            "Confirm Employee Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE,
            null,
            options,
            options[0]);
        
        if (option == 1) { // Yes, Delete
            // Second confirmation
            String confirmText = JOptionPane.showInputDialog(this,
                "To confirm deletion, please type: DELETE\n\n" +
                "Employee: " + selectedEmployee.name + " (ID: " + selectedEmployee.id + ")",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE);
            
            if ("DELETE".equals(confirmText)) {
                deleteEmployee();
            } else if (confirmText != null) {
                JOptionPane.showMessageDialog(this,
                    "Deletion cancelled. Text did not match 'DELETE'.",
                    "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void deleteEmployee() {
        // Show progress bar
        progressBar.setVisible(true);
        deleteButton.setEnabled(false);
        previewButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    if (dbConnection == null || dbConnection.statement == null) {
                        throw new Exception("Database connection not available");
                    }
                    
                    String query = "DELETE FROM employee WHERE id = ?";
                    try (PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query)) {
                        pstmt.setString(1, selectedEmployee.id);
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
                
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(RemoveEmployee.this,
                            "Employee '" + selectedEmployee.name + "' deleted successfully!\n" +
                            "Employee ID: " + selectedEmployee.id,
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Refresh data and clear selection
                        loadEmployeeData();
                        clearEmployeeDetails();
                        
                    } else {
                        JOptionPane.showMessageDialog(RemoveEmployee.this,
                            "Failed to delete employee. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        deleteButton.setEnabled(true);
                        previewButton.setEnabled(true);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(RemoveEmployee.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    deleteButton.setEnabled(true);
                    previewButton.setEnabled(true);
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
            new RemoveEmployee();
        });
    }
}