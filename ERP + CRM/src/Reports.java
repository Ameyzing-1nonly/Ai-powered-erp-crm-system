import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.io.PrintWriter;

public class Reports extends JFrame implements ActionListener {
    // Design Constants
    private static final int WINDOW_WIDTH = 1300;
    private static final int WINDOW_HEIGHT = 800;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(155, 89, 182);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(220, 221, 225);
    
    // UI Components
    private JPanel mainPanel, leftPanel, centerPanel, summaryPanel;
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeCombo, departmentCombo, statusCombo;
    private JTextField searchField;
    private JTextArea summaryArea;
    private JButton generateBtn, exportBtn, refreshBtn, backBtn;
    private JLabel totalEmployeesLabel, activeTasksLabel, completionRateLabel, statusLabel;
    
    // Data
    private conn dbConnection;
    
    public Reports() {
        initializeDatabase();
        initializeUI();
        loadInitialData();
        setupEventListeners();
    }
    
    private void initializeDatabase() {
        try {
            dbConnection = new conn();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void initializeUI() {
        setTitle("Employee Management System - Reports");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        
        createMainLayout();
        applyModernStyling();
        
        setVisible(true);
    }
    
    private void createMainLayout() {
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                GradientPaint gradient = new GradientPaint(0, 0, BACKGROUND_COLOR, 
                                                         getWidth(), getHeight(), Color.WHITE);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        // Create header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Create main content area
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Left panel - Controls
        leftPanel = createLeftPanel();
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        // Center panel - Report Table
        centerPanel = createCenterPanel();
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Summary panel
        summaryPanel = createSummaryPanel();
        contentPanel.add(summaryPanel, BorderLayout.EAST);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        // Footer
        JPanel footerPanel = createFooterPanel();
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Title section
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Reports & Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Generate comprehensive reports and insights");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        titlePanel.add(titleLabel);
        
        // Status section
        statusLabel = new JLabel("System Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(SUCCESS_COLOR);
        
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        statusPanel.add(statusLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(subtitleLabel, BorderLayout.CENTER);
        header.add(statusPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = createStyledPanel("Report Controls", PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(300, 0));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Report type selection
        content.add(createReportTypePanel());
        content.add(Box.createVerticalStrut(20));
        
        // Filters
        content.add(createFiltersPanel());
        content.add(Box.createVerticalStrut(20));
        
        // Control buttons
        content.add(createControlButtons());
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createReportTypePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "Report Type"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Select Report:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        reportTypeCombo = new JComboBox<>(new String[]{
            "Employee Summary", 
            "Department Analysis", 
            "Task Reports", 
            "Performance Overview",
            "Attendance Summary"
        });
        styleComboBox(reportTypeCombo);
        panel.add(reportTypeCombo, gbc);
        
        return panel;
    }
    
    private JPanel createFiltersPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "Filters"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Search field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 0; gbc.gridy = 1;
        searchField = createStyledTextField();
        panel.add(searchField, gbc);
        
        // Department filter
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 0; gbc.gridy = 3;
        departmentCombo = new JComboBox<>(new String[]{"All", "IT", "HR", "Finance", "Marketing", "Operations", "Sales"});
        styleComboBox(departmentCombo);
        panel.add(departmentCombo, gbc);
        
        // Status filter (for task reports)
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 0; gbc.gridy = 5;
        statusCombo = new JComboBox<>(new String[]{"All", "Active", "Inactive", "Pending"});
        styleComboBox(statusCombo);
        panel.add(statusCombo, gbc);
        
        return panel;
    }
    
    private JPanel createControlButtons() {
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        
        generateBtn = createModernButton("Generate", SUCCESS_COLOR, "");
        exportBtn = createModernButton("Export CSV", INFO_COLOR, "");
        refreshBtn = createModernButton("Refresh", WARNING_COLOR, "");
        backBtn = createModernButton("← Back", SECONDARY_COLOR, "");
        
        buttons.add(generateBtn);
        buttons.add(exportBtn);
        buttons.add(refreshBtn);
        buttons.add(backBtn);
        
        return buttons;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = createStyledPanel("Report Data", SECONDARY_COLOR);
        
        // Create table with dynamic columns
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        styleTable(reportTable);
        
        JScrollPane scrollPane = new JScrollPane(reportTable);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = createStyledPanel("Summary & Statistics", INFO_COLOR);
        panel.setPreferredSize(new Dimension(300, 0));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Statistics cards
        content.add(createStatCard("Total Employees", "0", SUCCESS_COLOR));
        content.add(Box.createVerticalStrut(10));
        content.add(createStatCard("Active Tasks", "0", PRIMARY_COLOR));
        content.add(Box.createVerticalStrut(10));
        content.add(createStatCard("Completion Rate", "0%", WARNING_COLOR));
        content.add(Box.createVerticalStrut(20));
        
        // Summary text area
        JLabel summaryLabel = new JLabel("Report Summary:");
        summaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        content.add(summaryLabel);
        content.add(Box.createVerticalStrut(10));
        
        summaryArea = new JTextArea(8, 20);
        summaryArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        summaryArea.setEditable(false);
        summaryArea.setBackground(new Color(248, 249, 250));
        summaryArea.setText("Select a report type and click 'Generate' to view detailed analytics and insights.");
        summaryArea.setLineWrap(true);
        summaryArea.setWrapStyleWord(true);
        
        JScrollPane summaryScroll = new JScrollPane(summaryArea);
        summaryScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        content.add(summaryScroll);
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            new EmptyBorder(10, 15, 10, 15)));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        valueLabel.setForeground(color);
        
        // Store references for updating
        if (title.equals("Total Employees")) {
            totalEmployeesLabel = valueLabel;
        } else if (title.equals("Active Tasks")) {
            activeTasksLabel = valueLabel;
        } else if (title.equals("Completion Rate")) {
            completionRateLabel = valueLabel;
        }
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 30, 20, 30));
        
        // System info
        JLabel infoLabel = new JLabel("Reports Module v1.0 | Generated: " + 
                                     new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        
        footer.add(infoLabel, BorderLayout.CENTER);
        
        return footer;
    }
    
    private JPanel createStyledPanel(String title, Color headerColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(10, BORDER_COLOR),
            new EmptyBorder(0, 0, 0, 0)));
        panel.setBackground(CARD_COLOR);
        
        // Header
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(headerColor);
        header.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        header.add(titleLabel);
        
        panel.add(header, BorderLayout.NORTH);
        
        return panel;
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(200, 35));
        return field;
    }
    
    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        combo.setPreferredSize(new Dimension(200, 35));
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(41, 128, 185, 50));
        table.setSelectionForeground(SECONDARY_COLOR);
        table.setGridColor(BORDER_COLOR);
        table.setShowVerticalLines(true);
        table.setShowHorizontalLines(true);
        
        // Style header
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setBackground(BACKGROUND_COLOR);
        header.setForeground(SECONDARY_COLOR);
        header.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }
    
    private JButton createModernButton(String text, Color bgColor, String icon) {
        JButton button = new JButton(icon + " " + text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color color = bgColor;
                if (getModel().isPressed()) {
                    color = bgColor.darker();
                } else if (getModel().isRollover()) {
                    color = bgColor.brighter();
                }
                
                g2d.setColor(color);
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
        button.setPreferredSize(new Dimension(130, 40));
        
        return button;
    }
    
    // Custom rounded border class
    private static class RoundedBorder implements Border {
        private int radius;
        private Color color;
        
        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }
        
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius+1, radius+1, radius+2, radius);
        }
        
        @Override
        public boolean isBorderOpaque() {
            return true;
        }
        
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width-1, height-1, radius, radius);
        }
    }
    
    private void applyModernStyling() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    private void setupEventListeners() {
        generateBtn.addActionListener(this);
        exportBtn.addActionListener(this);
        refreshBtn.addActionListener(this);
        backBtn.addActionListener(this);
        
        // Add listeners for filters
        reportTypeCombo.addActionListener(e -> updateReportColumns());
        
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterReportData();
            }
        });
    }
    
    private void loadInitialData() {
        updateStatistics();
        updateReportColumns();
    }
    
    private void updateStatistics() {
        try {
            // Get total employees
            String empQuery = "SELECT COUNT(*) as total FROM employee";
            ResultSet empRs = dbConnection.statement.executeQuery(empQuery);
            if (empRs.next()) {
                totalEmployeesLabel.setText(String.valueOf(empRs.getInt("total")));
            }
            empRs.close();
            
            // Get active tasks (if tasks table exists)
            try {
                String taskQuery = "SELECT COUNT(*) as active FROM tasks WHERE status IN ('Pending', 'In Progress')";
                ResultSet taskRs = dbConnection.statement.executeQuery(taskQuery);
                if (taskRs.next()) {
                    activeTasksLabel.setText(String.valueOf(taskRs.getInt("active")));
                }
                taskRs.close();
                
                // Calculate completion rate
                String completionQuery = "SELECT " +
                    "(SELECT COUNT(*) FROM tasks WHERE status = 'Completed') * 100.0 / COUNT(*) as rate " +
                    "FROM tasks";
                ResultSet compRs = dbConnection.statement.executeQuery(completionQuery);
                if (compRs.next()) {
                    completionRateLabel.setText(String.format("%.1f%%", compRs.getDouble("rate")));
                }
                compRs.close();
            } catch (SQLException e) {
                // Tasks table doesn't exist, set default values
                activeTasksLabel.setText("N/A");
                completionRateLabel.setText("N/A");
            }
            
        } catch (SQLException e) {
            System.err.println("Error updating statistics: " + e.getMessage());
        }
    }
    
    private void updateReportColumns() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        tableModel.setRowCount(0);
        
        switch (reportType) {
            case "Employee Summary":
                tableModel.setColumnIdentifiers(new String[]{"ID", "Name", "Department", "Position", "Email", "Phone"});
                break;
            case "Department Analysis":
                tableModel.setColumnIdentifiers(new String[]{"Department", "Employee Count", "Avg Salary", "Active Projects"});
                break;
            case "Task Reports":
                tableModel.setColumnIdentifiers(new String[]{"Task ID", "Title", "Status", "Priority", "Assigned To", "Due Date"});
                break;
            case "Performance Overview":
                tableModel.setColumnIdentifiers(new String[]{"Employee", "Department", "Tasks Completed", "Performance Score"});
                break;
            case "Attendance Summary":
                tableModel.setColumnIdentifiers(new String[]{"Employee", "Department", "Days Present", "Days Absent", "Attendance %"});
                break;
        }
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String department = (String) departmentCombo.getSelectedItem();
        
        // Update status without progress indication
        statusLabel.setText("Generating Report...");
        statusLabel.setForeground(WARNING_COLOR);
        
        try {
            tableModel.setRowCount(0);
            
            switch (reportType) {
                case "Employee Summary":
                    generateEmployeeSummary(department);
                    break;
                case "Department Analysis":
                    generateDepartmentAnalysis();
                    break;
                case "Task Reports":
                    generateTaskReports(department);
                    break;
                case "Performance Overview":
                    generatePerformanceOverview(department);
                    break;
                case "Attendance Summary":
                    generateAttendanceSummary(department);
                    break;
            }
            
            // Update status to completed
            statusLabel.setText("✅ Report Generated Successfully");
            statusLabel.setForeground(SUCCESS_COLOR);
            generateReportSummary();
            
        } catch (SQLException e) {
            System.err.println("Error generating report: " + e.getMessage());
            statusLabel.setText("❌ Error Generating Report");
            statusLabel.setForeground(DANGER_COLOR);
            JOptionPane.showMessageDialog(this, 
                "Error generating report: " + e.getMessage(), 
                "Report Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateEmployeeSummary(String department) throws SQLException {
        String query = "SELECT id, name, department, position, email, phone FROM employee";
        if (!"All".equals(department)) {
            query += " WHERE department = '" + department + "'";
        }
        
        ResultSet rs = dbConnection.statement.executeQuery(query);
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getString("position"),
                rs.getString("email"),
                rs.getString("phone")
            });
        }
        rs.close();
    }
    
    private void generateDepartmentAnalysis() throws SQLException {
        String query = "SELECT department, COUNT(*) as emp_count, AVG(salary) as avg_sal " +
                      "FROM employee GROUP BY department";
        
        ResultSet rs = dbConnection.statement.executeQuery(query);
        while (rs.next()) {
            tableModel.addRow(new Object[]{
                rs.getString("department"),
                rs.getInt("emp_count"),
                String.format("$%.2f", rs.getDouble("avg_sal")),
                "N/A" // Placeholder for active projects
            });
        }
        rs.close();
    }
    
    private void generateTaskReports(String department) throws SQLException {
        try {
            String query = "SELECT t.id, t.title, t.status, t.priority, e.name as assigned_to, t.due_date " +
                          "FROM tasks t LEFT JOIN employee e ON t.assigned_to = e.id";
            
            if (!"All".equals(department)) {
                query += " WHERE e.department = '" + department + "'";
            }
            
            ResultSet rs = dbConnection.statement.executeQuery(query);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("status"),
                    rs.getString("priority"),
                    rs.getString("assigned_to") != null ? rs.getString("assigned_to") : "Unassigned",
                    rs.getDate("due_date") != null ? new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("due_date")) : "N/A"
                });
            }
            rs.close();
        } catch (SQLException e) {
            // Tasks table doesn't exist
            tableModel.addRow(new Object[]{"N/A", "No task data available", "N/A", "N/A", "N/A", "N/A"});
        }
    }
    
    private void generatePerformanceOverview(String department) throws SQLException {
        String query = "SELECT name, department FROM employee";
        if (!"All".equals(department)) {
            query += " WHERE department = '" + department + "'";
        }
        
        ResultSet rs = dbConnection.statement.executeQuery(query);
        while (rs.next()) {
            // Simulate performance data
            int completedTasks = (int) (Math.random() * 20);
            double performanceScore = 60 + (Math.random() * 40);
            
            tableModel.addRow(new Object[]{
                rs.getString("name"),
                rs.getString("department"),
                completedTasks,
                String.format("%.1f", performanceScore)
            });
        }
        rs.close();
    }
    
    private void generateAttendanceSummary(String department) throws SQLException {
        String query = "SELECT name, department FROM employee";
        if (!"All".equals(department)) {
            query += " WHERE department = '" + department + "'";
        }
        
        ResultSet rs = dbConnection.statement.executeQuery(query);
        while (rs.next()) {
            // Simulate attendance data
            int daysPresent = 20 + (int) (Math.random() * 10);
            int daysAbsent = (int) (Math.random() * 5);
            double attendanceRate = (daysPresent * 100.0) / (daysPresent + daysAbsent);
            
            tableModel.addRow(new Object[]{
                rs.getString("name"),
                rs.getString("department"),
                daysPresent,
                daysAbsent,
                String.format("%.1f%%", attendanceRate)
            });
        }
        rs.close();
    }
    
    private void generateReportSummary() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        int rowCount = tableModel.getRowCount();
        
        StringBuilder summary = new StringBuilder();
        summary.append("Report Type: ").append(reportType).append("\n");
        summary.append("Generated: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date())).append("\n");
        summary.append("Total Records: ").append(rowCount).append("\n\n");
        
        switch (reportType) {
            case "Employee Summary":
                summary.append("This report shows all employee details including contact information and department assignments.");
                break;
            case "Department Analysis":
                summary.append("Analysis of employee distribution and average compensation across departments.");
                break;
            case "Task Reports":
                summary.append("Overview of all tasks with their current status, priority levels, and assignment details.");
                break;
            case "Performance Overview":
                summary.append("Employee performance metrics including task completion rates and performance scores.");
                break;
            case "Attendance Summary":
                summary.append("Attendance tracking report showing presence, absence, and attendance rates for all employees.");
                break;
        }
        
        summaryArea.setText(summary.toString());
    }
    
    private void filterReportData() {
        // Simple filtering based on search text
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            generateReport();
            return;
        }
        
        // Filter existing table data
        DefaultTableModel filteredModel = new DefaultTableModel();
        filteredModel.setColumnIdentifiers(getColumnNames());
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            boolean matchFound = false;
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object value = tableModel.getValueAt(i, j);
                if (value != null && value.toString().toLowerCase().contains(searchText)) {
                    matchFound = true;
                    break;
                }
            }
            if (matchFound) {
                Object[] row = new Object[tableModel.getColumnCount()];
                for (int j = 0; j < tableModel.getColumnCount(); j++) {
                    row[j] = tableModel.getValueAt(i, j);
                }
                filteredModel.addRow(row);
            }
        }
        
        reportTable.setModel(filteredModel);
        styleTable(reportTable);
    }
    
    private String[] getColumnNames() {
        String[] columns = new String[tableModel.getColumnCount()];
        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            columns[i] = tableModel.getColumnName(i);
        }
        return columns;
    }
    
    private void exportToCSV() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Report");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                if (!fileName.endsWith(".csv")) {
                    fileName += ".csv";
                }
                
                try (PrintWriter writer = new PrintWriter(fileName)) {
                    // Write header
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        writer.print(tableModel.getColumnName(i));
                        if (i < tableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                    
                    // Write data
                    for (int i = 0; i < tableModel.getRowCount(); i++) {
                        for (int j = 0; j < tableModel.getColumnCount(); j++) {
                            Object value = tableModel.getValueAt(i, j);
                            String cellValue = value != null ? value.toString().replace(",", ";") : "";
                            writer.print("\"" + cellValue + "\"");
                            if (j < tableModel.getColumnCount() - 1) {
                                writer.print(",");
                            }
                        }
                        writer.println();
                    }
                    
                    JOptionPane.showMessageDialog(this,
                        "Report exported successfully to:\n" + fileName,
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error exporting report: " + ex.getMessage(),
                "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == generateBtn) {
            generateReport();
        } else if (e.getSource() == exportBtn) {
            if (tableModel.getRowCount() == 0) {
                JOptionPane.showMessageDialog(this, "Please generate a report first before exporting.",
                                            "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            exportToCSV();
        } else if (e.getSource() == refreshBtn) {
            updateStatistics();
            generateReport();
        } else if (e.getSource() == backBtn) {
            // Close current window and return to Main_class
            dispose();
            try {
                new Main_class();
            } catch (Exception ex) {
                System.err.println("Error opening Main_class: " + ex.getMessage());
                // If Main_class constructor fails, just close this window
            }
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
                if (dbConnection.connection != null) {
                    dbConnection.connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        super.dispose();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    System.err.println("Could not set system look and feel: " + e.getMessage());
                }
                new Reports();
            }
        });
    }
}