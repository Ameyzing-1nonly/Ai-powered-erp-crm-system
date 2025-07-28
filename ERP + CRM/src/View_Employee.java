import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.print.PrinterException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import net.proteanit.sql.DbUtils;

public class View_Employee extends JFrame implements ActionListener {
    // Constants for modern design
    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 800;
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color CARD_COLOR = new Color(255, 255, 255);
    private static final Color BACKGROUND_COLOR = new Color(236, 240, 241);
    private static final Color INPUT_COLOR = new Color(248, 249, 250);
    private static final Color BORDER_COLOR = new Color(220, 221, 225);
    private static final Color TABLE_HEADER_COLOR = new Color(52, 73, 94);
    private static final Color TABLE_ALTERNATE_COLOR = new Color(248, 249, 250);
    
    // UI Components
    private JButton searchButton, printButton, updateButton, backButton, refreshButton, exportButton, deleteButton;
    private JTable employeeTable;
    private JComboBox<EmployeeItem> employeeComboBox;
    private JTextField searchField;
    private JLabel totalEmployeesLabel, statusLabel;
    private JPanel filterPanel;
    private JComboBox<String> departmentFilter, positionFilter;
    private TableRowSorter<DefaultTableModel> tableSorter;
    private DefaultTableModel tableModel;
    private conn dbConnection;
    
    // Inner class for ComboBox items
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
    
    public View_Employee() {
        initializeDatabase();
        initializeUI();
        loadEmployeeData();
        setupTableSorting();
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
        setTitle("Employee Management - View & Search");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        
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
        
        // Create search and filter panel
        JPanel searchPanel = createSearchPanel();
        mainPanel.add(searchPanel, BorderLayout.CENTER);
        
        // Create table panel
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(30, 40, 20, 40));
        
        // Title section
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 5));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("Employee Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(SECONDARY_COLOR);
        
        JLabel subtitleLabel = new JLabel("View, search, and manage employee records");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        
        // Stats panel
        JPanel statsPanel = createStatsPanel();
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        headerPanel.add(statsPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        statsPanel.setOpaque(false);
        
        totalEmployeesLabel = new JLabel("Total Employees: 0");
        totalEmployeesLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalEmployeesLabel.setForeground(PRIMARY_COLOR);
        totalEmployeesLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        statsPanel.add(totalEmployeesLabel);
        statsPanel.add(statusLabel);
        
        return statsPanel;
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        
        // Search section
        JPanel searchSection = createSearchSection();
        
        // Filter section
        filterPanel = createFilterSection();
        
        searchPanel.add(searchSection, BorderLayout.NORTH);
        searchPanel.add(filterPanel, BorderLayout.SOUTH);
        
        return searchPanel;
    }
    
    private JPanel createSearchSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        section.setBackground(CARD_COLOR);
        
        // Search by ID section
        JPanel idSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        idSearchPanel.setOpaque(false);
        
        JLabel searchByIdLabel = new JLabel("Search by Employee:");
        searchByIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchByIdLabel.setForeground(SECONDARY_COLOR);
        
        employeeComboBox = new JComboBox<>();
        employeeComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        employeeComboBox.setPreferredSize(new Dimension(300, 35));
        employeeComboBox.setBackground(INPUT_COLOR);
        
        // Search by text section
        JLabel searchByTextLabel = new JLabel("Quick Search:");
        searchByTextLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchByTextLabel.setForeground(SECONDARY_COLOR);
        
        searchField = new JTextField(15);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        searchField.setBackground(INPUT_COLOR);
        searchField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {}
            
            @Override
            public void keyReleased(KeyEvent e) {
                filterTable();
            }
        });
        
        idSearchPanel.add(searchByIdLabel);
        idSearchPanel.add(employeeComboBox);
        idSearchPanel.add(Box.createHorizontalStrut(20));
        idSearchPanel.add(searchByTextLabel);
        idSearchPanel.add(searchField);
        
        // Button panel
        JPanel buttonPanel = createSearchButtonPanel();
        
        section.add(idSearchPanel, BorderLayout.WEST);
        section.add(buttonPanel, BorderLayout.EAST);
        
        return section;
    }
    
    private JPanel createSearchButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        searchButton = createModernButton("Search", PRIMARY_COLOR, new Dimension(100, 35));
        refreshButton = createModernButton("Refresh", SUCCESS_COLOR, new Dimension(100, 35));
        
        searchButton.addActionListener(this);
        refreshButton.addActionListener(this);
        
        buttonPanel.add(searchButton);
        buttonPanel.add(refreshButton);
        
        return buttonPanel;
    }
    
    private JPanel createFilterSection() {
        JPanel section = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        section.setOpaque(false);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)));
        section.setBackground(CARD_COLOR);
        
        JLabel filterLabel = new JLabel("Filter by:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        filterLabel.setForeground(SECONDARY_COLOR);
        
        JLabel deptLabel = new JLabel("Department:");
        deptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        deptLabel.setForeground(SECONDARY_COLOR);
        
        departmentFilter = new JComboBox<>(new String[]{"All Departments", "IT", "HR", "Finance", "Marketing", "Operations", "Sales", "Other"});
        departmentFilter.setPreferredSize(new Dimension(150, 30));
        departmentFilter.addActionListener(e -> filterTable());
        
        JLabel posLabel = new JLabel("Position:");
        posLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        posLabel.setForeground(SECONDARY_COLOR);
        
        positionFilter = new JComboBox<>();
        positionFilter.setPreferredSize(new Dimension(150, 30));
        positionFilter.addActionListener(e -> filterTable());
        
        section.add(filterLabel);
        section.add(deptLabel);
        section.add(departmentFilter);
        section.add(Box.createHorizontalStrut(10));
        section.add(posLabel);
        section.add(positionFilter);
        
        return section;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.setBorder(new EmptyBorder(0, 40, 20, 40));
        
        // Create table
        employeeTable = new JTable();
        styleTable();
        
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.setPreferredSize(new Dimension(WINDOW_WIDTH - 80, 400));
        
        // Action buttons panel
        JPanel actionPanel = createActionButtonPanel();
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }
    
    private void styleTable() {
        employeeTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        employeeTable.setRowHeight(35);
        employeeTable.setGridColor(BORDER_COLOR);
        employeeTable.setSelectionBackground(PRIMARY_COLOR.brighter());
        employeeTable.setSelectionForeground(Color.WHITE);
        employeeTable.setShowGrid(true);
        employeeTable.setIntercellSpacing(new Dimension(1, 1));
        
        // Style table header
        JTableHeader header = employeeTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(TABLE_HEADER_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        
        // Alternate row colors
        employeeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(Color.WHITE);
                    } else {
                        c.setBackground(TABLE_ALTERNATE_COLOR);
                    }
                }
                
                return c;
            }
        });
    }
    
    private JPanel createActionButtonPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        actionPanel.setOpaque(false);
        
        printButton = createModernButton("Print Table", new Color(108, 117, 125), new Dimension(120, 40));
        exportButton = createModernButton("Export CSV", WARNING_COLOR, new Dimension(120, 40));
        deleteButton = createModernButton("Delete", DANGER_COLOR, new Dimension(120, 40));
        updateButton = createModernButton("Update", PRIMARY_COLOR, new Dimension(120, 40));
        backButton = createModernButton("Back", SECONDARY_COLOR, new Dimension(120, 40));
        
        printButton.addActionListener(this);
        exportButton.addActionListener(this);
        deleteButton.addActionListener(this);
        updateButton.addActionListener(this);
        backButton.addActionListener(this);
        
        actionPanel.add(printButton);
        actionPanel.add(exportButton);
        actionPanel.add(deleteButton);
        actionPanel.add(updateButton);
        actionPanel.add(backButton);
        
        return actionPanel;
    }
    
    private JButton createModernButton(String text, Color bgColor, Dimension size) {
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
            
            // Load employee combo box
            employeeComboBox.removeAllItems();
            employeeComboBox.addItem(new EmployeeItem("ALL", "All Employees"));
            
            // Load position filter
            positionFilter.removeAllItems();
            positionFilter.addItem("All Positions");
            
            ResultSet rs = dbConnection.statement.executeQuery("SELECT DISTINCT position FROM employee ORDER BY position");
            while (rs.next()) {
                positionFilter.addItem(rs.getString("position"));
            }
            
            // Load employee data
            rs = dbConnection.statement.executeQuery("SELECT id, name FROM employee ORDER BY name");
            while (rs.next()) {
                employeeComboBox.addItem(new EmployeeItem(rs.getString("id"), rs.getString("name")));
            }
            
            // Load table data
            loadTableData("SELECT * FROM employee ORDER BY id");
            
            statusLabel.setText("Data loaded successfully");
            statusLabel.setForeground(SUCCESS_COLOR);
            
        } catch (SQLException e) {
            e.printStackTrace();
            statusLabel.setText("Error loading data");
            statusLabel.setForeground(DANGER_COLOR);
            JOptionPane.showMessageDialog(this, "Error loading employee data: " + e.getMessage(),
                                        "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTableData(String query) {
        try {
            ResultSet rs = dbConnection.statement.executeQuery(query);
            employeeTable.setModel(DbUtils.resultSetToTableModel(rs));
            
            // Update total count
            int rowCount = employeeTable.getRowCount();
            totalEmployeesLabel.setText("Total Employees: " + rowCount);
            
            // Setup table sorting after loading data
            setupTableSorting();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void setupTableSorting() {
        if (employeeTable.getModel() instanceof DefaultTableModel) {
            tableModel = (DefaultTableModel) employeeTable.getModel();
            tableSorter = new TableRowSorter<>(tableModel);
            employeeTable.setRowSorter(tableSorter);
        }
    }
    
    private void filterTable() {
        if (tableSorter == null) return;
        
        String searchText = searchField.getText().trim();
        String selectedDept = (String) departmentFilter.getSelectedItem();
        String selectedPos = (String) positionFilter.getSelectedItem();
        
        if (searchText.isEmpty() && "All Departments".equals(selectedDept) && "All Positions".equals(selectedPos)) {
            tableSorter.setRowFilter(null);
        } else {
            RowFilter<DefaultTableModel, Object> rf = new RowFilter<DefaultTableModel, Object>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Object> entry) {
                    boolean matchesSearch = true;
                    boolean matchesDept = true;
                    boolean matchesPos = true;
                    
                    if (!searchText.isEmpty()) {
                        matchesSearch = false;
                        for (int i = 0; i < entry.getValueCount(); i++) {
                            if (entry.getStringValue(i).toLowerCase().contains(searchText.toLowerCase())) {
                                matchesSearch = true;
                                break;
                            }
                        }
                    }
                    
                    if (!"All Departments".equals(selectedDept)) {
                        // Assuming department is in column 7 (adjust based on your table structure)
                        if (entry.getValueCount() > 7) {
                            matchesDept = selectedDept.equals(entry.getStringValue(7));
                        }
                    }
                    
                    if (!"All Positions".equals(selectedPos)) {
                        // Assuming position is in column 6 (adjust based on your table structure)
                        if (entry.getValueCount() > 6) {
                            matchesPos = selectedPos.equals(entry.getStringValue(6));
                        }
                    }
                    
                    return matchesSearch && matchesDept && matchesPos;
                }
            };
            tableSorter.setRowFilter(rf);
        }
        
        // Update displayed count
        int visibleRows = employeeTable.getRowCount();
        totalEmployeesLabel.setText("Showing: " + visibleRows + " employees");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == searchButton) {
            searchEmployee();
        } else if (e.getSource() == printButton) {
            printTable();
        } else if (e.getSource() == updateButton) {
            updateEmployee();
        } else if (e.getSource() == deleteButton) {
            deleteEmployee();
        } else if (e.getSource() == refreshButton) {
            refreshData();
        } else if (e.getSource() == exportButton) {
            exportToCSV();
        } else if (e.getSource() == backButton) {
            dispose();
            new Main_class();
        }
    }
    
    private void searchEmployee() {
        EmployeeItem selectedItem = (EmployeeItem) employeeComboBox.getSelectedItem();
        if (selectedItem == null) return;
        
        try {
            String query;
            if ("ALL".equals(selectedItem.getId())) {
                query = "SELECT * FROM employee ORDER BY id";
                statusLabel.setText("Showing all employees");
            } else {
                query = "SELECT * FROM employee WHERE id='" + selectedItem.getId() + "'";
                statusLabel.setText("Showing employee: " + selectedItem.getName());
            }
            
            loadTableData(query);
            statusLabel.setForeground(SUCCESS_COLOR);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            statusLabel.setText("Search failed");
            statusLabel.setForeground(DANGER_COLOR);
        }
    }
    
    private void printTable() {
        try {
            boolean printed = employeeTable.print(JTable.PrintMode.FIT_WIDTH,
                    null, null, true, null, true);
            if (printed) {
                statusLabel.setText("Table printed successfully");
                statusLabel.setForeground(SUCCESS_COLOR);
            }
        } catch (PrinterException ex) {
            ex.printStackTrace();
            statusLabel.setText("Print failed");
            statusLabel.setForeground(DANGER_COLOR);
            JOptionPane.showMessageDialog(this, "Error printing table: " + ex.getMessage(),
                                        "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to update.",
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get the actual row in case of sorting/filtering
        int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
        String employeeId = employeeTable.getModel().getValueAt(modelRow, 0).toString();
        
        dispose();
        new UpdateEmployee(employeeId);
    }
    
    private void deleteEmployee() {
        int selectedRow = employeeTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an employee to delete.",
                                        "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = employeeTable.convertRowIndexToModel(selectedRow);
        String employeeId = employeeTable.getModel().getValueAt(modelRow, 0).toString();
        String employeeName = employeeTable.getModel().getValueAt(modelRow, 1).toString();
        
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete employee:\n" + employeeName + " (ID: " + employeeId + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (option == JOptionPane.YES_OPTION) {
            try {
                String query = "DELETE FROM employee WHERE id='" + employeeId + "'";
                int result = dbConnection.statement.executeUpdate(query);
                
                if (result > 0) {
                    JOptionPane.showMessageDialog(this, "Employee deleted successfully!",
                                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete employee.",
                                                "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void refreshData() {
        statusLabel.setText("Refreshing data...");
        statusLabel.setForeground(WARNING_COLOR);
        loadEmployeeData();
        searchField.setText("");
        departmentFilter.setSelectedIndex(0);
        positionFilter.setSelectedIndex(0);
    }
    
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Employee Data as CSV");
        fileChooser.setSelectedFile(new java.io.File("employee_data.csv"));
        
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                java.io.FileWriter csvWriter = new java.io.FileWriter(fileToSave);
                
                // Write headers
                for (int i = 0; i < employeeTable.getColumnCount(); i++) {
                    csvWriter.append(employeeTable.getColumnName(i));
                    if (i < employeeTable.getColumnCount() - 1) {
                        csvWriter.append(",");
                    }
                }
                csvWriter.append("\n");
                
                // Write data
                for (int i = 0; i < employeeTable.getRowCount(); i++) {
                    for (int j = 0; j < employeeTable.getColumnCount(); j++) {
                        Object value = employeeTable.getValueAt(i, j);
                        csvWriter.append(value != null ? value.toString() : "");
                        if (j < employeeTable.getColumnCount() - 1) {
                            csvWriter.append(",");
                        }
                    }
                    csvWriter.append("\n");
                }
                
                csvWriter.flush();
                csvWriter.close();
                
                statusLabel.setText("Data exported successfully");
                statusLabel.setForeground(SUCCESS_COLOR);
                JOptionPane.showMessageDialog(this, "Employee data exported successfully!",
                                            "Export Complete", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (java.io.IOException ex) {
                ex.printStackTrace();
                statusLabel.setText("Export failed");
                statusLabel.setForeground(DANGER_COLOR);
                JOptionPane.showMessageDialog(this, "Error exporting data: " + ex.getMessage(),
                                            "Export Error", JOptionPane.ERROR_MESSAGE);
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
            new View_Employee();
        });
    }
}