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
    private JTable tasksTable, employeesTable;
    private DefaultTableModel tasksModel, employeesModel;
    private JTextField taskTitleField, taskDescField, dueDateField, searchField;
    private JComboBox<String> priorityCombo, statusCombo, departmentFilterCombo;
    private JTextArea aiRecommendationArea;
    private JButton createTaskBtn, assignTaskBtn, updateTaskBtn, deleteTaskBtn, 
                   aiAnalyzeBtn, backBtn, refreshBtn, exportBtn;
    private JLabel statsLabel, aiStatusLabel;
    private JSpinner hoursSpinner;
    
    // Data
    private conn dbConnection;
    private Map<Integer, Employee> employeeMap = new HashMap<>();
    private Map<Integer, Task> taskMap = new HashMap<>();
    private AITaskAnalyzer aiAnalyzer = new AITaskAnalyzer();
    
    // Data Models
    private static class Employee {
        int id;
        String name, department, position, email;
        double workload, skillScore;
        
        Employee(int id, String name, String department, String position, String email) {
            this.id = id; this.name = name; this.department = department;
            this.position = position; this.email = email;
            this.workload = 0.0; this.skillScore = Math.random() * 100;
        }
    }
    
    private static class Task {
        int id, assignedTo = -1, estimatedHours;
        String title, description, status = "Pending", priority;
        java.util.Date dueDate, createdDate = new java.util.Date();
        
        Task(int id, String title, String description, String priority, java.util.Date dueDate, int estimatedHours) {
            this.id = id; this.title = title; this.description = description;
            this.priority = priority; this.dueDate = dueDate; this.estimatedHours = estimatedHours;
        }
    }
    
    // AI Task Analyzer
    private static class AITaskAnalyzer {
        public String analyzeTaskAssignment(Task task, Map<Integer, Employee> employees) {
            StringBuilder analysis = new StringBuilder("AI Task Assignment Analysis\n═══════════════════════════════\n\n");
            analysis.append("Task: ").append(task.title).append("\nPriority: ").append(task.priority)
                   .append("\nDue Date: ").append(new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate))
                   .append("\nEstimated Hours: ").append(task.estimatedHours).append("\n\nRecommended Assignments:\n─────────────────────────────\n");
            
            List<Employee> candidates = findBestCandidates(task, employees);
            for (int i = 0; i < Math.min(3, candidates.size()); i++) {
                Employee emp = candidates.get(i);
                double score = calculateAssignmentScore(task, emp);
                analysis.append(String.format("%d. %s (%s) - Match: %.1f%% - Workload: %.1f%% - Skill: %.1f/100\n", 
                    i + 1, emp.name, emp.department, score, emp.workload, emp.skillScore));
            }
            
            analysis.append("\n🧠 AI Insights:\n─────────────────\n");
            if (task.priority.equals("High")) analysis.append("• High priority - assign experienced member\n");
            if ((task.dueDate.getTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24) < 3) 
                analysis.append("• Urgent deadline - immediate assignment needed\n");
            if (task.estimatedHours > 40) analysis.append("• Large task - consider breaking into subtasks\n");
            
            return analysis.toString();
        }
        
        private List<Employee> findBestCandidates(Task task, Map<Integer, Employee> employees) {
            List<Employee> candidates = new ArrayList<>(employees.values());
            candidates.sort((e1, e2) -> Double.compare(calculateAssignmentScore(task, e2), calculateAssignmentScore(task, e1)));
            return candidates;
        }
        
        private double calculateAssignmentScore(Task task, Employee emp) {
            double score = emp.skillScore * 0.4 + (100 - emp.workload) * 0.3;
            if (isDepartmentRelevant(task, emp.department)) score += 20;
            if (task.priority.equals("High") && emp.skillScore > 70) score += 10;
            return Math.min(100, Math.max(0, score));
        }
        
        private boolean isDepartmentRelevant(Task task, String department) {
            String taskLower = task.description.toLowerCase();
            return taskLower.contains(department.toLowerCase()) || 
                   (department.equals("IT") && (taskLower.contains("software") || taskLower.contains("system"))) ||
                   (department.equals("Marketing") && (taskLower.contains("campaign") || taskLower.contains("promotion")));
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
            if (!checkEmployeeTableExists()) {
                JOptionPane.showMessageDialog(this, "Employee table not found. Please setup employee system first.", 
                    "Database Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            createTaskTables();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database connection failed: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean checkEmployeeTableExists() {
        try {
            dbConnection.statement.executeQuery("SELECT COUNT(*) FROM employee LIMIT 1").close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    
    private void createTaskTables() {
        try {
            // Check employee ID type
            String employeeIdType = "INT";
            try {
                ResultSet rs = dbConnection.statement.executeQuery("DESCRIBE employee");
                while (rs.next()) {
                    if ("id".equals(rs.getString("Field"))) {
                        employeeIdType = rs.getString("Type").toLowerCase().contains("bigint") ? "BIGINT" : "INT";
                        break;
                    }
                }
                rs.close();
            } catch (SQLException e) { /* Use default INT */ }
            
            // Create tables
            dbConnection.statement.executeUpdate("DROP TABLE IF EXISTS task_history, tasks");
            
            String createTasksTable = "CREATE TABLE tasks (" +
                "id " + employeeIdType + " PRIMARY KEY AUTO_INCREMENT, " +
                "title VARCHAR(255) NOT NULL, description TEXT, " +
                "priority ENUM('Low', 'Medium', 'High') DEFAULT 'Medium', " +
                "status ENUM('Pending', 'In Progress', 'Completed', 'Cancelled') DEFAULT 'Pending', " +
                "assigned_to " + employeeIdType + " NULL, estimated_hours INT DEFAULT 8, " +
                "due_date DATE, created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
            
            dbConnection.statement.executeUpdate(createTasksTable);
            
            try {
                dbConnection.statement.executeUpdate("ALTER TABLE tasks ADD CONSTRAINT fk_tasks_employee " +
                    "FOREIGN KEY (assigned_to) REFERENCES employee(id) ON DELETE SET NULL");
            } catch (SQLException e) { /* FK constraint optional */ }
            
        } catch (SQLException e) {
            // Fallback basic table
            try {
                dbConnection.statement.executeUpdate("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, title VARCHAR(255) NOT NULL, description TEXT, " +
                    "priority VARCHAR(10) DEFAULT 'Medium', status VARCHAR(20) DEFAULT 'Pending', " +
                    "assigned_to INT NULL, estimated_hours INT DEFAULT 8, " +
                    "due_date DATE, created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            } catch (SQLException fallbackError) {
                JOptionPane.showMessageDialog(null, "Failed to create database tables.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void initializeUI() {
        setTitle("AI-Powered Task Assignment System");
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.add(createLeftPanel(), BorderLayout.WEST);
        contentPanel.add(createCenterPanel(), BorderLayout.CENTER);
        contentPanel.add(createRightPanel(), BorderLayout.EAST);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(createFooterPanel(), BorderLayout.SOUTH);
        
        add(mainPanel);
        setVisible(true);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JLabel titleLabel = new JLabel("AI Task Assignment System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        statsLabel = new JLabel("Loading statistics...");
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(PRIMARY_COLOR);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(statsLabel, BorderLayout.EAST);
        return header;
    }
    
    private JPanel createLeftPanel() {
        JPanel panel = createStyledPanel("Task Management", INFO_COLOR);
        panel.setPreferredSize(new Dimension(350, 0));
        
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        content.add(createTaskForm());
        content.add(Box.createVerticalStrut(15));
        content.add(createControlButtons());
        content.add(Box.createVerticalStrut(15));
        content.add(createSearchAndFilter());
        
        panel.add(content, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createTaskForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Task Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        String[] labels = {"Title:", "Description:", "Priority:", "Due Date:", "Est. Hours:"};
        Component[] fields = {
            taskTitleField = createStyledTextField(),
            taskDescField = createStyledTextField(),
            priorityCombo = createStyledComboBox(new String[]{"Low", "Medium", "High"}),
            dueDateField = createStyledTextField(),
            hoursSpinner = createStyledSpinner()
        };
        
        dueDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(
            new java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)));
        
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.anchor = GridBagConstraints.WEST;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
            form.add(fields[i], gbc);
        }
        
        return form;
    }
    
    private JPanel createControlButtons() {
        JPanel buttons = new JPanel(new GridLayout(2, 2, 10, 10));
        buttons.add(createTaskBtn = createModernButton("Create", SUCCESS_COLOR, ""));
        buttons.add(aiAnalyzeBtn = createModernButton("AI Analyze", INFO_COLOR, ""));
        buttons.add(assignTaskBtn = createModernButton("Assign", WARNING_COLOR, ""));
        buttons.add(updateTaskBtn = createModernButton("Update", PRIMARY_COLOR, ""));
        return buttons;
    }
    
    private JPanel createSearchAndFilter() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Search & Filter"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); gbc.fill = GridBagConstraints.HORIZONTAL;
        
        searchField = createStyledTextField();
        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterTasks(); }
        });
        
        departmentFilterCombo = createStyledComboBox(new String[]{"All", "IT", "HR", "Finance", "Marketing", "Operations", "Sales"});
        statusCombo = createStyledComboBox(new String[]{"All", "Pending", "In Progress", "Completed", "Cancelled"});
        
        departmentFilterCombo.addActionListener(e -> filterTasks());
        statusCombo.addActionListener(e -> filterTasks());
        
        String[] filterLabels = {"Search:", "Department:", "Status:"};
        Component[] filterFields = {searchField, departmentFilterCombo, statusCombo};
        
        for (int i = 0; i < filterLabels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i;
            panel.add(new JLabel(filterLabels[i]), gbc);
            gbc.gridx = 1;
            panel.add(filterFields[i], gbc);
        }
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = createStyledPanel("Task Dashboard", PRIMARY_COLOR);
        panel.setPreferredSize(new Dimension(500, 0));
        
        String[] taskColumns = {"ID", "Title", "Priority", "Status", "Assigned To", "Due Date"};
        tasksModel = new DefaultTableModel(taskColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        tasksTable = new JTable(tasksModel);
        styleTable(tasksTable);
        
        // Priority color renderer
        tasksTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    String priority = value.toString();
                    c.setForeground(priority.equals("High") ? DANGER_COLOR : 
                                  priority.equals("Medium") ? WARNING_COLOR : SUCCESS_COLOR);
                }
                return c;
            }
        });
        
        panel.add(new JScrollPane(tasksTable), BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(400, 0));
        
        // AI Panel
        JPanel aiPanel = createStyledPanel("AI Recommendations", INFO_COLOR);
        aiPanel.setPreferredSize(new Dimension(400, 300));
        
        aiRecommendationArea = new JTextArea("🤖 AI Assistant Ready\n\nSelect task → Click 'AI Analyze'");
        aiRecommendationArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        aiRecommendationArea.setEditable(false);
        aiRecommendationArea.setBackground(BACKGROUND_COLOR);
        
        aiPanel.add(new JScrollPane(aiRecommendationArea), BorderLayout.CENTER);
        
        aiStatusLabel = new JLabel("🟢 AI Assistant Active");
        aiStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        aiStatusLabel.setForeground(SUCCESS_COLOR);
        aiStatusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        aiPanel.add(aiStatusLabel, BorderLayout.SOUTH);
        
        // Employee Panel
        JPanel empPanel = createStyledPanel("Team Members", SECONDARY_COLOR);
        
        String[] empColumns = {"ID", "Name", "Department", "Workload %", "Skill"};
        employeesModel = new DefaultTableModel(empColumns, 0) {
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        employeesTable = new JTable(employeesModel);
        styleTable(employeesTable);
        
        // Workload color renderer
        employeesTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value != null) {
                    try {
                        double workload = Double.parseDouble(value.toString().replace("%", ""));
                        c.setForeground(workload > 80 ? DANGER_COLOR : workload > 60 ? WARNING_COLOR : SUCCESS_COLOR);
                    } catch (NumberFormatException e) { }
                }
                return c;
            }
        });
        
        empPanel.add(new JScrollPane(employeesTable), BorderLayout.CENTER);
        
        panel.add(aiPanel, BorderLayout.NORTH);
        panel.add(empPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(10, 30, 20, 30));
        footer.setOpaque(false);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setOpaque(false);
        
        refreshBtn = createModernButton("Refresh", PRIMARY_COLOR, "🔄");
        exportBtn = createModernButton("Export", SUCCESS_COLOR, "📊");
        deleteTaskBtn = createModernButton("Delete", DANGER_COLOR, "🗑️");
        backBtn = createModernButton("Back to Main", SECONDARY_COLOR, "⬅️");
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        buttonPanel.add(deleteTaskBtn);
        buttonPanel.add(backBtn);
        
        JLabel versionLabel = new JLabel("Task Assignment System v2.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(127, 140, 141));
        
        footer.add(versionLabel, BorderLayout.WEST);
        footer.add(buttonPanel, BorderLayout.EAST);
        return footer;
    }
    
    private JPanel createStyledPanel(String title, Color headerColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), new EmptyBorder(0, 0, 0, 0)));
        
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
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR), BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setPreferredSize(new Dimension(200, 35));
        return field;
    }
    
    private JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        combo.setPreferredSize(new Dimension(200, 35));
        return combo;
    }
    
    private JSpinner createStyledSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(8, 1, 100, 1));
        spinner.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        spinner.setPreferredSize(new Dimension(100, 35));
        return spinner;
    }
    
    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setSelectionBackground(new Color(41, 128, 185, 50));
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setBackground(BACKGROUND_COLOR);
        table.getTableHeader().setForeground(SECONDARY_COLOR);
    }
    
    private JButton createModernButton(String text, Color bgColor, String icon) {
        JButton button = new JButton(icon + " " + text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color color = getModel().isPressed() ? bgColor.darker() : 
                             getModel().isRollover() ? bgColor.brighter() : bgColor;
                
                g2d.setColor(color);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                g2d.setColor(Color.WHITE);
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent()) / 2 - 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 40));
        return button;
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
        
        tasksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tasksTable.getSelectedRow() >= 0) {
                loadTaskDetails(tasksTable.getSelectedRow());
            }
        });
        
        employeesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateAssignmentRecommendations();
        });
    }
    
    private void loadData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                loadEmployees();
                loadTasks();
                return null;
            }
            protected void done() { updateStatistics(); }
        };
        worker.execute();
    }
    
    private void loadEmployees() {
        try {
            employeesModel.setRowCount(0);
            employeeMap.clear();
            
            ResultSet rs = dbConnection.statement.executeQuery("SELECT id, name, department, position, email FROM employee");
            while (rs.next()) {
                Employee emp = new Employee(rs.getInt("id"), rs.getString("name"), rs.getString("department"), 
                    rs.getString("position"), rs.getString("email"));
                emp.workload = calculateEmployeeWorkload(emp.id);
                employeeMap.put(emp.id, emp);
                
                employeesModel.addRow(new Object[]{emp.id, emp.name, emp.department, 
                    String.format("%.1f%%", emp.workload), String.format("%.1f", emp.skillScore)});
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
            
            ResultSet rs = dbConnection.statement.executeQuery(
                "SELECT t.*, e.name as employee_name FROM tasks t LEFT JOIN employee e ON t.assigned_to = e.id ORDER BY t.created_date DESC");
            
            while (rs.next()) {
                java.sql.Date sqlDueDate = rs.getDate("due_date");
                Task task = new Task(rs.getInt("id"), rs.getString("title"), rs.getString("description"), 
                    rs.getString("priority"), new java.util.Date(sqlDueDate.getTime()), rs.getInt("estimated_hours"));
                
                task.status = rs.getString("status");
                task.assignedTo = rs.getInt("assigned_to");
                
                Timestamp timestamp = rs.getTimestamp("created_date");
                if (timestamp != null) task.createdDate = new java.util.Date(timestamp.getTime());
                
                taskMap.put(task.id, task);
                
                String assignedName = rs.getString("employee_name");
                if (assignedName == null) assignedName = "Unassigned";
                
                tasksModel.addRow(new Object[]{task.id, task.title, task.priority, task.status, 
                    assignedName, new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate)});
            }
            rs.close();
        } catch (SQLException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
        }
    }
    
    private double calculateEmployeeWorkload(int employeeId) {
        try {
            PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(
                "SELECT SUM(estimated_hours) as total_hours FROM tasks WHERE assigned_to = ? AND status IN ('Pending', 'In Progress')");
            pstmt.setInt(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int totalHours = rs.getInt("total_hours");
                return Math.min(100.0, (totalHours / 40.0) * 100);
            }
            rs.close(); pstmt.close();
        } catch (SQLException e) {
            System.err.println("Error calculating workload: " + e.getMessage());
        }
        return 0.0;
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
        int completedTasks = (int) taskMap.values().stream().filter(t -> "Completed".equals(t.status)).count();
        int pendingTasks = (int) taskMap.values().stream().filter(t -> "Pending".equals(t.status)).count();
        
        statsLabel.setText(String.format("📊 Tasks: %d Total | %d Completed | %d Pending", totalTasks, completedTasks, pendingTasks));
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
                
                tasksModel.addRow(new Object[]{task.id, task.title, task.priority, task.status, 
                    assignedName, new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate)});
            }
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == createTaskBtn) createTask();
        else if (e.getSource() == assignTaskBtn) assignTask();
        else if (e.getSource() == updateTaskBtn) updateTask();
        else if (e.getSource() == deleteTaskBtn) deleteTask();
        else if (e.getSource() == aiAnalyzeBtn) performAIAnalysis();
        else if (e.getSource() == refreshBtn) loadData();
        else if (e.getSource() == exportBtn) exportData();
        else if (e.getSource() == backBtn) {
            dispose();
            try {
                new Main_class().setVisible(true);
            } catch (Exception ex) {
                System.err.println("Error opening Main_class: " + ex.getMessage());
                System.exit(0);
            }
        }
    }
    
    private void createTask() {
        if (!validateTaskForm()) return;
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                try {
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(
                        "INSERT INTO tasks (title, description, priority, due_date, estimated_hours) VALUES (?, ?, ?, ?, ?)");
                    
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
            
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(Task_ass.this, "Task created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        clearTaskForm();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this, "Failed to create task.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void assignTask() {
        int selectedTaskRow = tasksTable.getSelectedRow();
        int selectedEmpRow = employeesTable.getSelectedRow();
        
        if (selectedTaskRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to assign.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedEmpRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an employee.", "No Employee Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int taskId = (Integer) tasksModel.getValueAt(selectedTaskRow, 0);
        int employeeId = (Integer) employeesModel.getValueAt(selectedEmpRow, 0);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                try {
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(
                        "UPDATE tasks SET assigned_to = ?, status = 'In Progress' WHERE id = ?");
                    pstmt.setInt(1, employeeId);
                    pstmt.setInt(2, taskId);
                    
                    int result = pstmt.executeUpdate();
                    pstmt.close();
                    return result > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            protected void done() {
                try {
                    if (get()) {
                        String employeeName = (String) employeesModel.getValueAt(selectedEmpRow, 1);
                        String taskTitle = (String) tasksModel.getValueAt(selectedTaskRow, 1);
                        JOptionPane.showMessageDialog(Task_ass.this, String.format("Task '%s' assigned to %s!", taskTitle, employeeName),
                            "Assignment Successful", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this, "Failed to assign task.", "Assignment Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to update.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!validateTaskForm()) return;
        
        int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                try {
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement(
                        "UPDATE tasks SET title = ?, description = ?, priority = ?, due_date = ?, estimated_hours = ? WHERE id = ?");
                    
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
            
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(Task_ass.this, "Task updated successfully!", "Update Successful", JOptionPane.INFORMATION_MESSAGE);
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this, "Failed to update task.", "Update Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void deleteTask() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String taskTitle = (String) tasksModel.getValueAt(selectedRow, 1);
        int option = JOptionPane.showConfirmDialog(this, String.format("Delete task '%s'?", taskTitle),
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option != JOptionPane.YES_OPTION) return;
        
        int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {
                try {
                    PreparedStatement pstmt = dbConnection.statement.getConnection().prepareStatement("DELETE FROM tasks WHERE id = ?");
                    pstmt.setInt(1, taskId);
                    int result = pstmt.executeUpdate();
                    pstmt.close();
                    return result > 0;
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
            
            protected void done() {
                try {
                    if (get()) {
                        JOptionPane.showMessageDialog(Task_ass.this, "Task deleted successfully!", "Delete Successful", JOptionPane.INFORMATION_MESSAGE);
                        clearTaskForm();
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(Task_ass.this, "Failed to delete task.", "Delete Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(Task_ass.this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void performAIAnalysis() {
        int selectedRow = tasksTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a task for AI analysis.", "No Task Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        aiStatusLabel.setText("🟡 AI Analyzing...");
        aiStatusLabel.setForeground(WARNING_COLOR);
        
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                Thread.sleep(1000); // Simulate processing
                
                int taskId = (Integer) tasksModel.getValueAt(selectedRow, 0);
                Task task = taskMap.get(taskId);
                return task != null ? aiAnalyzer.analyzeTaskAssignment(task, employeeMap) : "Error: Task not found";
            }
            
            protected void done() {
                try {
                    aiRecommendationArea.setText(get());
                    aiStatusLabel.setText("🟢 AI Analysis Complete");
                    aiStatusLabel.setForeground(SUCCESS_COLOR);
                } catch (Exception ex) {
                    aiRecommendationArea.setText("AI analysis failed: " + ex.getMessage());
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
                if (!fileName.endsWith(".csv")) fileName += ".csv";
                
                try (java.io.PrintWriter writer = new java.io.PrintWriter(fileName)) {
                    writer.println("Task ID,Title,Priority,Status,Assigned To,Due Date,Estimated Hours");
                    
                    for (Task task : taskMap.values()) {
                        String assignedName = "Unassigned";
                        if (task.assignedTo > 0 && employeeMap.containsKey(task.assignedTo)) {
                            assignedName = employeeMap.get(task.assignedTo).name;
                        }
                        
                        writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%d%n", task.id,
                            task.title.replace("\"", "\"\""), task.priority, task.status, assignedName,
                            new SimpleDateFormat("dd/MM/yyyy").format(task.dueDate), task.estimatedHours);
                    }
                    
                    JOptionPane.showMessageDialog(this, "Data exported successfully to:\n" + fileName,
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Export Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateTaskForm() {
        if (taskTitleField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task title.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            taskTitleField.requestFocus();
            return false;
        }
        
        if (taskDescField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a task description.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            taskDescField.requestFocus();
            return false;
        }
        
        try {
            java.sql.Date.valueOf(dueDateField.getText());
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid date (YYYY-MM-DD).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            dueDateField.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void clearTaskForm() {
        taskTitleField.setText("");
        taskDescField.setText("");
        priorityCombo.setSelectedIndex(1);
        dueDateField.setText(new SimpleDateFormat("yyyy-MM-dd").format(
            new java.util.Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)));
        hoursSpinner.setValue(8);
    }
    
    @Override
    public void dispose() {
        if (dbConnection != null) {
            try {
                if (dbConnection.statement != null) dbConnection.statement.close();
                if (dbConnection.connection != null) dbConnection.connection.close();
            } catch (SQLException e) {
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
            new Task_ass();
        });
    }
}