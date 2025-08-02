import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class Task_ass extends JFrame implements ActionListener {
    // Design Constants
    private static final int WINDOW_WIDTH = 1400;
    private static final int WINDOW_HEIGHT = 900;
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
    private JPanel mainPanel, leftPanel, rightPanel, centerPanel;
    private JTable tasksTable, employeesTable;
    private DefaultTableModel tasksModel, employeesModel;
    private JTextField taskTitleField, taskDescField, dueDateField, searchField;
    private JComboBox<String> priorityCombo, statusCombo, departmentFilterCombo;
    private JTextArea aiRecommendationArea;
    private JButton createTaskBtn, assignTaskBtn, updateTaskBtn, deleteTaskBtn, 
                   aiAnalyzeBtn, backBtn, refreshBtn, exportBtn;
    private JProgressBar taskProgressBar;
    private JLabel statsLabel, aiStatusLabel;
    private JSpinner hoursSpinner;
    
    // Data
    private conn dbConnection;
    private Map<Integer, Employee> employeeMap;
    private Map<Integer, Task> taskMap;
    private AITaskAnalyzer aiAnalyzer;
    
    // Data Models
    private static class Employee {
        int id;
        String name, department, position, email;
        double workload, skillScore;
        
        Employee(int id, String name, String department, String position, String email) {
            this.id = id;
            this.name = name;
            this.department = department;
            this.position = position;
            this.email = email;
            this.workload = 0.0;
            this.skillScore = Math.random() * 100; // Simulated skill score
        }
    }
    
    private static class Task {
        int id;
        String title, description, status, priority;
        java.util.Date dueDate, createdDate; // Fixed: Explicitly use java.util.Date
        int assignedTo, estimatedHours;
        
        // Fixed: Updated constructor to match field assignments
        Task(int id, String title, String description, String priority, 
             java.util.Date dueDate, int estimatedHours) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.priority = priority;
            this.dueDate = dueDate;
            this.estimatedHours = estimatedHours;
            this.status = "Pending";
            this.createdDate = new java.util.Date(); // Fixed: Explicitly use java.util.Date
            this.assignedTo = -1;
        }
    }
    
    // AI Task Analyzer
    private static class AITaskAnalyzer {
        public String analyzeTaskAssignment(Task task, Map<Integer, Employee> employees) {
            StringBuilder analysis = new StringBuilder();
            analysis.append("AI Task Assignment Analysis\n");
            analysis.append("═══════════════════════════════\n\n");
            
            // Priority Analysis
            analysis.append("📊 Task Priority: ").append(task.priority).append("\n");
            analysis.append("⏰ Due Date: ").append(new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate)).append("\n");
            analysis.append("🕒 Estimated Hours: ").append(task.estimatedHours).append("\n\n");
            
            // Find best candidates
            List<Employee> candidates = findBestCandidates(task, employees);
            
            analysis.append("🎯 Recommended Assignments:\n");
            analysis.append("─────────────────────────────\n");
            
            for (int i = 0; i < Math.min(3, candidates.size()); i++) {
                Employee emp = candidates.get(i);
                double score = calculateAssignmentScore(task, emp);
                
                analysis.append(String.format("%d. %s (%s)\n", i + 1, emp.name, emp.department));
                analysis.append(String.format("   📈 Match Score: %.1f%%\n", score));
                analysis.append(String.format("   💼 Current Workload: %.1f%%\n", emp.workload));
                analysis.append(String.format("   🎓 Skill Level: %.1f/100\n", emp.skillScore));
                
                if (score > 80) {
                    analysis.append("   ✅ Excellent Match\n");
                } else if (score > 60) {
                    analysis.append("   ⚡ Good Match\n");
                } else {
                    analysis.append("   ⚠️ Average Match\n");
                }
                analysis.append("\n");
            }
            
            // Additional insights
            analysis.append("🧠 AI Insights:\n");
            analysis.append("─────────────────\n");
            
            if (task.priority.equals("High")) {
                analysis.append("• High priority task - consider experienced team members\n");
            }
            
            long daysUntilDue = (task.dueDate.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
            if (daysUntilDue < 3) {
                analysis.append("• Urgent deadline - assign to available team member immediately\n");
            }
            
            if (task.estimatedHours > 40) {
                analysis.append("• Large task - consider breaking into smaller subtasks\n");
            }
            
            analysis.append("• Optimal assignment balances workload and expertise\n");
            
            return analysis.toString();
        }
        
        private List<Employee> findBestCandidates(Task task, Map<Integer, Employee> employees) {
            List<Employee> candidates = new ArrayList<>(employees.values());
            candidates.sort(new Comparator<Employee>() {
                @Override
                public int compare(Employee e1, Employee e2) {
                    return Double.compare(
                        calculateAssignmentScore(task, e2), 
                        calculateAssignmentScore(task, e1)
                    );
                }
            });
            return candidates;
        }
        
        private double calculateAssignmentScore(Task task, Employee emp) {
            double score = 0;
            
            // Skill-based scoring
            score += emp.skillScore * 0.4;
            
            // Workload-based scoring (lower workload = higher score)
            score += (100 - emp.workload) * 0.3;
            
            // Department relevance
            if (isDepartmentRelevant(task, emp.department)) {
                score += 20;
            }
            
            // Priority adjustment
            if (task.priority.equals("High") && emp.skillScore > 70) {
                score += 10;
            }
            
            return Math.min(100, Math.max(0, score));
        }
        
        private boolean isDepartmentRelevant(Task task, String department) {
            String taskLower = task.description.toLowerCase();
            String deptLower = department.toLowerCase();
            
            return taskLower.contains(deptLower) || 
                   (department.equals("IT") && (taskLower.contains("software") || taskLower.contains("system"))) ||
                   (department.equals("Marketing") && (taskLower.contains("campaign") || taskLower.contains("promotion"))) ||
                   (department.equals("HR") && (taskLower.contains("employee") || taskLower.contains("recruitment")));
        }
    }
    
    public Task_ass() {
        initializeDatabase();
        initializeUI();
        loadData();
        setupEventListeners();
    }
    
    private void initializeDatabase() {
        try {
            dbConnection = new conn();
            
            // Check if employee table exists
            if (!checkEmployeeTableExists()) {
                JOptionPane.showMessageDialog(this, 
                    "Employee table not found in database.\n" +
                    "Please make sure the employee management system has been set up first.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            createTaskTables();
            aiAnalyzer = new AITaskAnalyzer();
            employeeMap = new HashMap<>();
            taskMap = new HashMap<>();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private boolean checkEmployeeTableExists() {
        try {
            String query = "SELECT COUNT(*) FROM employee LIMIT 1";
            ResultSet rs = dbConnection.statement.executeQuery(query);
            rs.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Employee table does not exist: " + e.getMessage());
            return false;
        }
    }
    
    private void createTaskTables() {
        try {
            // First, check what data type the employee.id column uses
            String checkEmployeeTable = "DESCRIBE employee";
            String employeeIdType = "INT"; // Default assumption
            
            try {
                ResultSet rs = dbConnection.statement.executeQuery(checkEmployeeTable);
                while (rs.next()) {
                    if ("id".equals(rs.getString("Field"))) {
                        String type = rs.getString("Type");
                        if (type.toLowerCase().contains("bigint")) {
                            employeeIdType = "BIGINT";
                        } else if (type.toLowerCase().contains("int")) {
                            employeeIdType = "INT";
                        }
                        break;
                    }
                }
                rs.close();
            } catch (SQLException e) {
                System.out.println("Could not check employee table structure, using default INT type");
            }
            
            // Drop existing tables if they exist to recreate with correct structure
            try {
                dbConnection.statement.executeUpdate("DROP TABLE IF EXISTS task_history");
                dbConnection.statement.executeUpdate("DROP TABLE IF EXISTS tasks");
            } catch (SQLException e) {
                System.out.println("No existing tables to drop: " + e.getMessage());
            }
            
            // Create tasks table with matching data type
            String createTasksTable = "CREATE TABLE tasks (" +
                "id " + employeeIdType + " PRIMARY KEY AUTO_INCREMENT, " +
                "title VARCHAR(255) NOT NULL, " +
                "description TEXT, " +
                "priority ENUM('Low', 'Medium', 'High') DEFAULT 'Medium', " +
                "status ENUM('Pending', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Pending', " +
                "assigned_to " + employeeIdType + " NULL, " +
                "estimated_hours INT DEFAULT 8, " +
                "due_date DATE, " +
                "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            
            dbConnection.statement.executeUpdate(createTasksTable);
            System.out.println("Tasks table created successfully with " + employeeIdType + " data type");
            
            // Add foreign key constraint separately
            try {
                String addForeignKey = "ALTER TABLE tasks ADD CONSTRAINT fk_tasks_employee " +
                                     "FOREIGN KEY (assigned_to) REFERENCES employee(id) ON DELETE SET NULL";
                dbConnection.statement.executeUpdate(addForeignKey);
                System.out.println("Foreign key constraint added successfully");
            } catch (SQLException e) {
                System.out.println("Warning: Could not add foreign key constraint: " + e.getMessage());
                System.out.println("The application will still work, but without referential integrity");
            }
            
            // Create task assignments history table
            String createTaskHistoryTable = "CREATE TABLE task_history (" +
                "id " + employeeIdType + " PRIMARY KEY AUTO_INCREMENT, " +
                "task_id " + employeeIdType + ", " +
                "employee_id " + employeeIdType + ", " +
                "action VARCHAR(100), " +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            
            dbConnection.statement.executeUpdate(createTaskHistoryTable);
            System.out.println("Task history table created successfully");
            
            // Add foreign key constraints for history table
            try {
                String addTaskFK = "ALTER TABLE task_history ADD CONSTRAINT fk_history_task " +
                                 "FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE";
                dbConnection.statement.executeUpdate(addTaskFK);
                
                String addEmployeeFK = "ALTER TABLE task_history ADD CONSTRAINT fk_history_employee " +
                                     "FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE";
                dbConnection.statement.executeUpdate(addEmployeeFK);
                System.out.println("History table foreign key constraints added successfully");
            } catch (SQLException e) {
                System.out.println("Warning: Could not add history table foreign key constraints: " + e.getMessage());
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
            
            // Try creating a basic table without foreign keys as fallback
            try {
                String basicTasksTable = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "description TEXT, " +
                    "priority VARCHAR(10) DEFAULT 'Medium', " +
                    "status VARCHAR(20) DEFAULT 'Pending', " +
                    "assigned_to INT NULL, " +
                    "estimated_hours INT DEFAULT 8, " +
                    "due_date DATE, " +
                    "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                
                dbConnection.statement.executeUpdate(basicTasksTable);
                System.out.println("Created basic tasks table without foreign keys");
                
                String basicHistoryTable = "CREATE TABLE IF NOT EXISTS task_history (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "task_id INT, " +
                    "employee_id INT, " +
                    "action VARCHAR(100), " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";
                
                dbConnection.statement.executeUpdate(basicHistoryTable);
                System.out.println("Created basic task history table without foreign keys");
                
            } catch (SQLException fallbackError) {
                System.err.println("Failed to create even basic tables: " + fallbackError.getMessage());
                JOptionPane.showMessageDialog(null, 
                    "Failed to create database tables. Please check your database connection and permissions.",
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void initializeUI() {
        setTitle("AI-Powered Task Assignment System");
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
        
        // Left panel - Task Creation & Controls
        leftPanel = createLeftPanel();
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        // Center panel - Tasks Table
        centerPanel = createCenterPanel();
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Right panel - AI Recommendations & Employee List
        rightPanel = createRightPanel();
        contentPanel.add(rightPanel, BorderLayout.EAST);
        
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
        
        JLabel titleLabel = new JLabel("🤖 AI Task Assignment System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("Intelligent task allocation and workload optimization");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        titlePanel.add(titleLabel);
        
        // Stats section
        statsLabel = new JLabel("📊 Loading statistics...");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(PRIMARY_COLOR);
        
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statsPanel.setOpaque(false);
        statsPanel.add(statsLabel);
        
        header.add(titlePanel, BorderLayout.WEST);
        header.add(subtitleLabel, BorderLayout.CENTER);
        header.add(statsPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = createStyledPanel("Task Creation & Management", INFO_COLOR);
        panel.setPreferredSize(new Dimension(350, 0));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Task form
        content.add(createTaskForm());
        content.add(Box.createVerticalStrut(20));
        
        // Control buttons
        content.add(createControlButtons());
        content.add(Box.createVerticalStrut(20));
        
        // Search and filter
        content.add(createSearchAndFilter());
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createTaskForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "New Task Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Task Title
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        taskTitleField = createStyledTextField();
        form.add(taskTitleField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        taskDescField = createStyledTextField();
        form.add(taskDescField, gbc);
        
        // Priority
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        priorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High"});
        styleComboBox(priorityCombo);
        form.add(priorityCombo, gbc);
        
        // Due Date
        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Due Date:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        dueDateField = createStyledTextField();
        dueDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(
            new java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L))); // Fixed: Explicitly use java.util.Date
        form.add(dueDateField, gbc);
        
        // Estimated Hours
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE;
        form.add(new JLabel("Est. Hours:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        hoursSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 100, 1));
        styleSpinner(hoursSpinner);
        form.add(hoursSpinner, gbc);
        
        return form;
    }
    
    private JPanel createControlButtons() {
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.setOpaque(false);
        
        createTaskBtn = createModernButton("Create Task", SUCCESS_COLOR, "➕");
        aiAnalyzeBtn = createModernButton("AI Analyze", INFO_COLOR, "🧠");
        assignTaskBtn = createModernButton("Assign Task", WARNING_COLOR, "👤");
        updateTaskBtn = createModernButton("Update Task", PRIMARY_COLOR, "✏️");
        
        buttons.add(createTaskBtn);
        buttons.add(aiAnalyzeBtn);
        buttons.add(assignTaskBtn);
        buttons.add(updateTaskBtn);
        
        return buttons;
    }
    
    private JPanel createSearchAndFilter() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), "Search & Filter"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Search field
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Search:"), gbc);
        gbc.gridx = 1;
        searchField = createStyledTextField();
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTasks();
            }
        });
        panel.add(searchField, gbc);
        
        // Department filter
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1;
        departmentFilterCombo = new JComboBox<>(new String[]{"All", "IT", "HR", "Finance", "Marketing", "Operations", "Sales"});
        styleComboBox(departmentFilterCombo);
        departmentFilterCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTasks();
            }
        });
        panel.add(departmentFilterCombo, gbc);
        
        // Status filter
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"All", "Pending", "In Progress", "Completed", "Cancelled"});
        styleComboBox(statusCombo);
        statusCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTasks();
            }
        });
        panel.add(statusCombo, gbc);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = createStyledPanel("Task Management Dashboard", PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(500, 0));
        
        // Tasks table
        String[] taskColumns = {"ID", "Title", "Priority", "Status", "Assigned To", "Due Date", "Progress"};
        tasksModel = new DefaultTableModel(taskColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tasksTable = new JTable(tasksModel);
        styleTable(tasksTable);
        
        // Custom renderer for priority column
        tasksTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    String priority = value.toString();
                    switch (priority) {
                        case "High":
                            c.setForeground(DANGER_COLOR);
                            break;
                        case "Medium":
                            c.setForeground(WARNING_COLOR);
                            break;
                        case "Low":
                            c.setForeground(SUCCESS_COLOR);
                            break;
                    }
                }
                return c;
            }
        });
        
        JScrollPane tasksScroll = new JScrollPane(tasksTable);
        tasksScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(tasksScroll, BorderLayout.CENTER);
        
        // Task progress panel
        JPanel progressPanel = new JPanel(new FlowLayout());
        progressPanel.setOpaque(false);
        progressPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        taskProgressBar = new JProgressBar();
        taskProgressBar.setStringPainted(true);
        taskProgressBar.setString("Task Completion Rate");
        taskProgressBar.setPreferredSize(new Dimension(300, 25));
        
        progressPanel.add(new JLabel("Overall Progress: "));
        progressPanel.add(taskProgressBar);
        
        panel.add(progressPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(400, 0));
        
        // AI Recommendations panel
        JPanel aiPanel = createStyledPanel("AI Recommendations", INFO_COLOR);
        aiPanel.setPreferredSize(new Dimension(400, 300));
        
        aiRecommendationArea = new JTextArea();
        aiRecommendationArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        aiRecommendationArea.setEditable(false);
        aiRecommendationArea.setBackground(new Color(248, 249, 250));
        aiRecommendationArea.setText("🤖 AI Assistant Ready\n\nSelect a task and click 'AI Analyze' to get intelligent assignment recommendations based on:\n\n• Employee workload\n• Skill matching\n• Department relevance\n• Task priority\n• Deadline urgency\n\nThe AI will provide optimized suggestions to improve productivity and task distribution.");
        
        JScrollPane aiScroll = new JScrollPane(aiRecommendationArea);
        aiScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        aiPanel.add(aiScroll, BorderLayout.CENTER);
        
        // AI status
        aiStatusLabel = new JLabel("🟢 AI Assistant Active");
        aiStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aiStatusLabel.setForeground(SUCCESS_COLOR);
        aiStatusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        aiPanel.add(aiStatusLabel, BorderLayout.SOUTH);
        
        // Employees panel
        JPanel empPanel = createStyledPanel("Team Members", SECONDARY_COLOR);
        
        String[] empColumns = {"ID", "Name", "Department", "Workload %", "Skill Score"};
        employeesModel = new DefaultTableModel(empColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        employeesTable = new JTable(employeesModel);
        styleTable(employeesTable);
        
        // Custom renderer for workload column
        employeesTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    try {
                        double workload = Double.parseDouble(value.toString().replace("%", ""));
                        if (workload > 80) {
                            c.setForeground(DANGER_COLOR);
                        } else if (workload > 60) {
                            c.setForeground(WARNING_COLOR);
                        } else {
                            c.setForeground(SUCCESS_COLOR);
                        }
                    } catch (NumberFormatException e) {
                        // Keep default color
                    }
                }
                return c;
            }
        });
        
        JScrollPane empScroll = new JScrollPane(employeesTable);
        empScroll.setBorder(new EmptyBorder(10, 10, 10, 10));
        empPanel.add(empScroll, BorderLayout.CENTER);
        
        panel.add(aiPanel, BorderLayout.NORTH);
        panel.add(empPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 30, 20, 30));
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        refreshBtn = createModernButton("Refresh", PRIMARY_COLOR, "🔄");
        exportBtn = createModernButton("Export", SUCCESS_COLOR, "📊");
        deleteTaskBtn = createModernButton("Delete Task", DANGER_COLOR, "🗑️");
        backBtn = createModernButton("Back to Dashboard", SECONDARY_COLOR, "⬅️");
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(deleteTaskBtn);
        buttonPanel.add(backBtn);
        
        // System info
        JLabel infoLabel = new JLabel("Task Assignment System v2.0 | AI-Powered Optimization");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        infoLabel.setForeground(new Color(127, 140, 141));
        
        footer.add(infoLabel, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.EAST);
        
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
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        spinner.setPreferredSize(new Dimension(100, 35));
        
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(Color.WHITE);
        }
    }
    
    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(35);
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
        
        // Set column widths
        if (table == tasksTable && table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(150); // Title
            table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Priority
            table.getColumnModel().getColumn(3).setPreferredWidth(100); // Status
            table.getColumnModel().getColumn(4).setPreferredWidth(120); // Assigned To
            table.getColumnModel().getColumn(5).setPreferredWidth(100); // Due Date
            table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Progress
        } else if (table == employeesTable && table.getColumnModel().getColumnCount() > 0) {
            table.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
            table.getColumnModel().getColumn(1).setPreferredWidth(120); // Name
            table.getColumnModel().getColumn(2).setPreferredWidth(100); // Department
            table.getColumnModel().getColumn(3).setPreferredWidth(80);  // Workload
            table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Skill Score
        }
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
        button.setPreferredSize(new Dimension(140, 40));
        
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
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set look and feel: " + e.getMessage());
        }
    }
    
    private void setupEventListeners() {
        createTaskBtn.addActionListener(this);
        assignTaskBtn.addActionListener(this);
        updateTaskBtn.addActionListener(this);
        deleteTaskBtn.addActionListener(this);
        aiAnalyzeBtn.addActionListener(this);
        refreshBtn.addActionListener(this);
        exportBtn.addActionListener(this);
        backBtn.addActionListener(this);
        
        // Table selection listeners
        tasksTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int selectedRow = tasksTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        loadTaskDetails(selectedRow);
                    }
                }
            }
        });
        
        employeesTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            @Override
            public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateAssignmentRecommendations();
                }
            }
        });
    }
    
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadEmployees();
                loadTasks();
                return null;
            }
            
            @Override
            protected void done() {
                updateStatistics();
                updateProgressBar();
            }
        };
        worker.execute();
    }
    
    private void loadEmployees() {
        try {
            employeesModel.setRowCount(0);
            employeeMap.clear();
            
            String query = "SELECT id, name, department, position, email FROM employee";
            ResultSet rs = dbConnection.statement.executeQuery(query);
            
            while (rs.next()) {
                Employee emp = new Employee(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("department"),
                    rs.getString("position"),
                    rs.getString("email")
                );
                
                // Calculate workload
                emp.workload = calculateEmployeeWorkload(emp.id);
                
                employeeMap.put(emp.id, emp);
                
                employeesModel.addRow(new Object[]{
                    emp.id,
                    emp.name,
                    emp.department,
                    String.format("%.1f%%", emp.workload),
                    String.format("%.1f", emp.skillScore)
                });
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error loading employees: " + e.getMessage());
        }
    }
    
    private void loadTasks() {
        try {
            tasksModel.setRowCount(0);
            taskMap.clear();
            
            String query = "SELECT t.*, e.name as employee_name FROM tasks t " +
                          "LEFT JOIN employee e ON t.assigned_to = e.id " +
                          "ORDER BY t.created_date DESC";
            ResultSet rs = dbConnection.statement.executeQuery(query);
            
            while (rs.next()) {
                // Fixed: Convert java.sql.Date to java.util.Date
                java.sql.Date sqlDueDate = rs.getDate("due_date");
                java.util.Date utilDueDate = new java.util.Date(sqlDueDate.getTime());
                
                Task task = new Task(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("priority"),
                    utilDueDate, // Fixed: Pass java.util.Date
                    rs.getInt("estimated_hours")
                );
                
                task.status = rs.getString("status");
                task.assignedTo = rs.getInt("assigned_to");
                
                // Fixed: Convert Timestamp to java.util.Date
                Timestamp timestamp = rs.getTimestamp("created_date");
                if (timestamp != null) {
                    task.createdDate = new java.util.Date(timestamp.getTime());
                }
                
                taskMap.put(task.id, task);
                
                String assignedName = rs.getString("employee_name");
                if (assignedName == null) assignedName = "Unassigned";
                
                String progress = calculateTaskProgress(task);
                
                tasksModel.addRow(new Object[]{
                    task.id,
                    task.title,
                    task.priority,
                    task.status,
                    assignedName,
                    new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate),
                    progress
                });
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }
    
    private double calculateEmployeeWorkload(int employeeId) {
        try {
            String query = "SELECT COUNT(*) as task_count, SUM(estimated_hours) as total_hours " +
                          "FROM tasks WHERE assigned_to = ? AND status IN ('Pending', 'In Progress')";
            PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query);
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int totalHours = rs.getInt("total_hours");
                // Assume 40 hours per week capacity
                return Math.min(100.0, (totalHours / 40.0) * 100);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error calculating workload: " + e.getMessage());
        }
        return 0.0;
    }
    
    private String calculateTaskProgress(Task task) {
        switch (task.status) {
            case "Pending": return "0%";
            case "In Progress": return "50%";
            case "Completed": return "100%";
            case "Cancelled": return "N/A";
            default: return "0%";
        }
    }
    
    private void loadTaskDetails(int selectedRow) {
        try {
            int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
            Task task = taskMap.get(taskId);
            
            if (task != null) {
                taskTitleField.setText(task.title);
                taskDescField.setText(task.description);
                priorityCombo.setSelectedItem(task.priority);
                dueDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(task.dueDate));
                hoursSpinner.setValue(task.estimatedHours);
            }
        } catch (Exception e) {
            System.err.println("Error loading task details: " + e.getMessage());
        }
    }
    
    private void updateStatistics() {
        int totalTasks = taskMap.size();
        int completedTasks = 0;
        int pendingTasks = 0;
        
        for (Task task : taskMap.values()) {
            if ("Completed".equals(task.status)) {
                completedTasks++;
            } else if ("Pending".equals(task.status)) {
                pendingTasks++;
            }
        }
        
        statsLabel.setText(String.format("📊 Tasks: %d Total | %d Completed | %d Pending", 
                                        totalTasks, completedTasks, pendingTasks));
    }
    
    private void updateProgressBar() {
        if (taskMap.isEmpty()) {
            taskProgressBar.setValue(0);
            taskProgressBar.setString("No tasks available");
            return;
        }
        
        int completedTasks = 0;
        for (Task task : taskMap.values()) {
            if ("Completed".equals(task.status)) {
                completedTasks++;
            }
        }
        
        int progressPercent = (int) ((completedTasks * 100) / taskMap.size());
        taskProgressBar.setValue(progressPercent);
        taskProgressBar.setString(String.format("Task Completion: %d%%", progressPercent));
    }
    
    private void updateAssignmentRecommendations() {
        int selectedTaskRow = tasksTable.getSelectedRow();
        
        if (selectedTaskRow >= 0) {
            int taskId = (Integer) tasksModel.getValueAt(selectedTaskRow, 0);
            Task task = taskMap.get(taskId);
            
            if (task != null) {
                String analysis = aiAnalyzer.analyzeTaskAssignment(task, employeeMap);
                aiRecommendationArea.setText(analysis);
                aiStatusLabel.setText("🟢 AI Analysis Complete");
                aiStatusLabel.setForeground(SUCCESS_COLOR);
            }
        }
    }
    
    private void filterTasks() {
        String searchText = searchField.getText().toLowerCase();
        String departmentFilter = (String) departmentFilterCombo.getSelectedItem();
        String statusFilter = (String) statusCombo.getSelectedItem();
        
        tasksModel.setRowCount(0);
        
        for (Task task : taskMap.values()) {
            boolean matchesSearch = searchText.isEmpty() || 
                                  task.title.toLowerCase().contains(searchText) ||
                                  task.description.toLowerCase().contains(searchText);
            
            boolean matchesDepartment = "All".equals(departmentFilter) || 
                                      (task.assignedTo > 0 && employeeMap.containsKey(task.assignedTo) &&
                                       employeeMap.get(task.assignedTo).department.equals(departmentFilter));
            
            boolean matchesStatus = "All".equals(statusFilter) || task.status.equals(statusFilter);
            
            if (matchesSearch && matchesDepartment && matchesStatus) {
                String assignedName = "Unassigned";
                if (task.assignedTo > 0 && employeeMap.containsKey(task.assignedTo)) {
                    assignedName = employeeMap.get(task.assignedTo).name;
                }
                
                String progress = calculateTaskProgress(task);
                
                tasksModel.addRow(new Object[]{
                    task.id,
                    task.title,
                    task.priority,
                    task.status,
                    assignedName,
                    new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate),
                    progress
                });
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createTaskBtn) {
            createTask();
        } else if (e.getSource() == assignTaskBtn) {
            assignTask();
        } else if (e.getSource() == updateTaskBtn) {
            updateTask();
        } else if (e.getSource() == deleteTaskBtn) {
            deleteTask();
        } else if (e.getSource() == aiAnalyzeBtn) {
            performAIAnalysis();
        } else if (e.getSource() == refreshBtn) {
            loadData();
        } else if (e.getSource() == exportBtn) {
            exportData();
        } else if (e.getSource() == backBtn) {
            dispose();
            new Main_class();
        }
    }
    
    private void createTask() {
        if (!validateTaskForm()) return;
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String query = "INSERT INTO tasks (title, description, priority, due_date, estimated_hours) VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query);
                    
                    pstmt.setString(1, taskTitleField.getText().trim());
                    pstmt.setString(2, taskDescField.getText().trim());
                    pstmt.setString(3, (String) priorityCombo.getSelectedItem());
                    pstmt.setDate(4, java.sql.Date.valueOf(dueDateField.getText()));
                    pstmt.setInt(5, (Integer) hoursSpinner.getValue());
                    
                    int result = pstmt.executeUpdate();
                    pstmt.close();
                    
                    return result > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Task created successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearTaskForm();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Failed to create task. Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void assignTask() {
        int selectedTaskRow = tasksTable.getSelectedRow();
        int selectedEmpRow = employeesTable.getSelectedRow();
        
        if (selectedTaskRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to assign.",
                                        "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedEmpRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee to assign the task to.",
                                        "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int taskId = (Integer) tasksModel.getValueAt(selectedTaskRow, 0);
        int employeeId = (Integer) employeesModel.getValueAt(selectedEmpRow, 0);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String query = "UPDATE tasks SET assigned_to = ?, status = 'In Progress' WHERE id = ?";
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query);
                    pstmt.setInt(1, employeeId);
                    pstmt.setInt(2, taskId);
                    
                    int result = pstmt.executeUpdate();
                    pstmt.close();
                    
                    // Log assignment history
                    if (result > 0) {
                        String historyQuery = "INSERT INTO task_history (task_id, employee_id, action) VALUES (?, ?, ?)";
                        PreparedStatement historyPstmt = dbConnection.statement.getConnection().prepareStatement(historyQuery);
                        historyPstmt.setInt(1, taskId);
                        historyPstmt.setInt(2, employeeId);
                        historyPstmt.setString(3, "Task Assigned");
                        historyPstmt.executeUpdate();
                        historyPstmt.close();
                    }
                    
                    return result > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        String employeeName = (String) employeesModel.getValueAt(selectedEmpRow, 1);
                        String taskTitle = (String) tasksModel.getValueAt(selectedTaskRow, 1);
                        
                        JOptionPane.showMessageDialog(Task_ass.this,
                            String.format("Task '%s' assigned to %s successfully!", taskTitle, employeeName),
                            "Assignment Successful", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Failed to assign task. Please try again.",
                            "Assignment Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to update.",
                                        "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateTaskForm()) return;
        
        int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String query = "UPDATE tasks SET title = ?, description = ?, priority = ?, due_date = ?, estimated_hours = ? WHERE id = ?";
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query);
                    
                    pstmt.setString(1, taskTitleField.getText().trim());
                    pstmt.setString(2, taskDescField.getText().trim());
                    pstmt.setString(3, (String) priorityCombo.getSelectedItem());
                    pstmt.setDate(4, java.sql.Date.valueOf(dueDateField.getText()));
                    pstmt.setInt(5, (Integer) hoursSpinner.getValue());
                    pstmt.setInt(6, taskId);
                    
                    int result = pstmt.executeUpdate();
                    pstmt.close();
                    
                    return result > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Task updated successfully!",
                            "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Failed to update task. Please try again.",
                            "Update Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void deleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.",
                                        "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String taskTitle = (String) tasksModel.getValueAt(selectedRow, 1);
        int option = JOptionPane.showConfirmDialog(this,
            String.format("Are you sure you want to delete the task '%s'?\nThis action cannot be undone.", taskTitle),
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option != JOptionPane.YES_OPTION) return;
        
        int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    String query = "DELETE FROM tasks WHERE id = ?";
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(query);
                    pstmt.setInt(1, taskId);
                    
                    int result = pstmt.executeUpdate();
                    pstmt.close();
                    
                    return result > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Task deleted successfully!",
                            "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
                        clearTaskForm();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this,
                            "Failed to delete task. Please try again.",
                            "Delete Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this,
                        "Error: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void performAIAnalysis() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task for AI analysis.",
                                        "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        aiStatusLabel.setText("🟡 AI Analyzing...");
        aiStatusLabel.setForeground(WARNING_COLOR);
        
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                // Simulate AI processing time
                Thread.sleep(1500);
                
                int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
                Task task = taskMap.get(taskId);
                
                if (task != null) {
                    return aiAnalyzer.analyzeTaskAssignment(task, employeeMap);
                }
                return "Error: Task not found";
            }
            
            @Override
            protected void done() {
                try {
                    String analysis = get();
                    aiRecommendationArea.setText(analysis);
                    aiStatusLabel.setText("🟢 AI Analysis Complete");
                    aiStatusLabel.setForeground(SUCCESS_COLOR);
                } catch (Exception ex) {
                    aiRecommendationArea.setText("Error performing AI analysis: " + ex.getMessage());
                    aiStatusLabel.setText("🔴 AI Analysis Failed");
                    aiStatusLabel.setForeground(DANGER_COLOR);
                }
            }
        };
        worker.execute();
    }
    
    private void exportData() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Task Data");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files", "csv"));
            
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                String fileName = fileChooser.getSelectedFile().getAbsolutePath();
                if (!fileName.endsWith(".csv")) {
                    fileName += ".csv";
                }
                
                try (java.io.PrintWriter writer = new java.io.PrintWriter(fileName)) {
                    // Write header
                    writer.println("Task ID,Title,Priority,Status,Assigned To,Due Date,Estimated Hours,Created Date");
                    
                    // Write data
                    for (Task task : taskMap.values()) {
                        String assignedName = "Unassigned";
                        if (task.assignedTo > 0 && employeeMap.containsKey(task.assignedTo)) {
                            assignedName = employeeMap.get(task.assignedTo).name;
                        }
                        
                        writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d,\"%s\"%n",
                            task.id,
                            task.title.replace("\"", "\"\""),
                            task.priority,
                            task.status,
                            assignedName,
                            new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate),
                            task.estimatedHours,
                            new SimpleDateFormat("dd/MM/yyyy HH:mm").format(task.createdDate)
                        );
                    }
                    
                    JOptionPane.showMessageDialog(this,
                        "Task data exported successfully to:\n" + fileName,
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error exporting data: " + ex.getMessage(),
                "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateTaskForm() {
        if (taskTitleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task title.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            taskTitleField.requestFocus();
            return false;
        }
        
        if (taskDescField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task description.",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            taskDescField.requestFocus();
            return false;
        }
        
        try {
            java.sql.Date.valueOf(dueDateField.getText());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date (YYYY-MM-DD).",
                                        "Validation Error", JOptionPane.ERROR_MESSAGE);
            dueDateField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void clearTaskForm() {
        taskTitleField.setText("");
        taskDescField.setText("");
        priorityCombo.setSelectedIndex(1); // Medium
        dueDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(
            new java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L))); // 7 days from now
        hoursSpinner.setValue(8);
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
                new Task_ass();
            }
        });
    }
}