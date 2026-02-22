import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;

public class ComplexLayoutDemo {

    // ---- Small set of layout constants (keeps magic numbers under control) ----
    private static final int GAP = 8;
    private static final int PAD = 8;

    private static final Dimension WINDOW_PREFERRED = new Dimension(1080, 700);
    private static final Dimension INSPECTOR_PREFERRED = new Dimension(300, 400);
    private static final Dimension STATUS_PROGRESS_SIZE = new Dimension(180, 18);

    private final JFrame frame;

    public ComplexLayoutDemo() {
        this.frame = new JFrame("Complex Layout Demo");
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.frame.setContentPane(buildRoot());
        this.frame.setMinimumSize(new Dimension(900, 550));
        this.frame.pack(); // pack based on preferred sizes
        this.frame.setLocationRelativeTo(null);

        // Nudge toward the original target size while still respecting pack()
        Dimension packed = this.frame.getSize();
        this.frame.setSize(
                Math.max(packed.width, WINDOW_PREFERRED.width),
                Math.max(packed.height, WINDOW_PREFERRED.height)
        );
    }

    public void show() {
        frame.setVisible(true);
    }

    private JComponent buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildToolbar(), BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildLeftNav(),
                buildCenterRight()
        );
        mainSplit.setResizeWeight(0.22);
        mainSplit.setContinuousLayout(true);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        root.add(mainSplit, BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JComponent buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(GAP, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, 0));
        left.add(new JButton(appAction("New", () -> {})));
        left.add(new JButton(appAction("Open", () -> {})));
        left.add(new JButton(appAction("Save", () -> {})));
        left.add(new JSeparator(SwingConstants.VERTICAL));
        left.add(new JButton(appAction("Export", () -> {})));
        bar.add(left, BorderLayout.WEST);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, GAP, 0));
        center.add(new JLabel("Quick Search:"));
        JTextField search = new JTextField(24);
        center.add(search);

        JButton go = new JButton(appAction("Go", () -> {}));
        // Example accelerator (Ctrl+F focuses search)
        search.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke('F', InputEvent.CTRL_DOWN_MASK), "focusSearch");
        search.getActionMap().put("focusSearch", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                search.requestFocusInWindow();
                search.selectAll();
            }
        });

        center.add(go);
        bar.add(center, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.TRAILING, GAP, 0));
        right.add(new JLabel("Profile:"));
        right.add(new JComboBox<>(new String[]{"Default", "Power User", "Minimal"}));
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JComponent buildLeftNav() {
        JPanel left = new JPanel(new BorderLayout(GAP, GAP));
        left.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        left.setPreferredSize(new Dimension(240, 400));

        JPanel filter = new JPanel(new BorderLayout(GAP, 0));
        filter.add(new JLabel("Filter:"), BorderLayout.WEST);
        filter.add(new JTextField(), BorderLayout.CENTER);
        left.add(filter, BorderLayout.NORTH);

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Projects");
        DefaultMutableTreeNode a = new DefaultMutableTreeNode("Alpha");
        a.add(new DefaultMutableTreeNode("src"));
        a.add(new DefaultMutableTreeNode("resources"));
        DefaultMutableTreeNode b = new DefaultMutableTreeNode("Beta");
        b.add(new DefaultMutableTreeNode("docs"));
        b.add(new DefaultMutableTreeNode("tests"));
        root.add(a);
        root.add(b);

        JTree tree = new JTree(root);
        left.add(new JScrollPane(tree), BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEADING, GAP, 0));
        footer.add(new JButton(appAction("Add", () -> {})));
        footer.add(new JButton(appAction("Remove", () -> {})));
        left.add(footer, BorderLayout.SOUTH);

        return left;
    }

    private JComponent buildCenterRight() {
        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildCenterTabs(),
                buildInspector()
        );
        split.setResizeWeight(0.7);
        split.setContinuousLayout(true);
        split.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return split;
    }

    private JComponent buildCenterTabs() {
        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Details", buildDetailsForm());

        String[] cols = {"ID", "Name", "Status", "Owner"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) {
                return false; // common default for “viewer” tables
            }
        };
        model.addRow(new Object[]{1, "Item A", "Open", "Alice"});
        model.addRow(new Object[]{2, "Item B", "In Progress", "Bob"});
        model.addRow(new Object[]{3, "Item C", "Closed", "Carol"});

        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        tabs.addTab("Table", new JScrollPane(table));

        JTextArea logs = new JTextArea();
        logs.setEditable(false);
        logs.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logs.setText(
                "[INFO] Application started...\n" +
                "[WARN] Example warning message\n" +
                "[DEBUG] Diagnostic details go here\n"
        );
        tabs.addTab("Logs", new JScrollPane(logs));

        return tabs;
    }

    private JComponent buildDetailsForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Title
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(new JLabel("Title:"), g);

        JTextField title = new JTextField(24);
        g.gridx = 1; g.gridy = row; g.weightx = 1;
        form.add(title, g);
        row++;

        // Category + Priority
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(new JLabel("Category:"), g);

        g.gridx = 1; g.gridy = row; g.weightx = 1;
        form.add(new JComboBox<>(new String[]{"Feature", "Bug", "Task"}), g);

        g.gridx = 2; g.gridy = row; g.weightx = 0;
        form.add(new JLabel("Priority:"), g);

        g.gridx = 3; g.gridy = row; g.weightx = 0;
        form.add(new JComboBox<>(new String[]{"Low", "Medium", "High"}), g);
        row++;

        // Tags (span)
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(new JLabel("Tags:"), g);

        g.gridx = 1; g.gridy = row; g.gridwidth = 3; g.weightx = 1;
        form.add(new JTextField(), g);
        g.gridwidth = 1;
        row++;

        // Options
        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 0));
        options.add(new JCheckBox("Enabled"));
        options.add(new JCheckBox("Pinned"));
        options.add(new JCheckBox("Archived"));

        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(new JLabel("Options:"), g);

        g.gridx = 1; g.gridy = row; g.gridwidth = 3; g.weightx = 1;
        form.add(options, g);
        g.gridwidth = 1;
        row++;

        // Assignment radios
        JPanel radios = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 0));
        ButtonGroup group = new ButtonGroup();
        JRadioButton r1 = new JRadioButton("Unassigned", true);
        JRadioButton r2 = new JRadioButton("Team A");
        JRadioButton r3 = new JRadioButton("Team B");
        group.add(r1); group.add(r2); group.add(r3);
        radios.add(r1); radios.add(r2); radios.add(r3);

        g.gridx = 0; g.gridy = row; g.weightx = 0;
        form.add(new JLabel("Assign To:"), g);

        g.gridx = 1; g.gridy = row; g.gridwidth = 3; g.weightx = 1;
        form.add(radios, g);
        g.gridwidth = 1;
        row++;

        // Description (span + grows)
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        g.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description:"), g);

        JTextArea description = new JTextArea(6, 40);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        JScrollPane descPane = new JScrollPane(description);

        g.gridx = 1; g.gridy = row;
        g.gridwidth = 3;
        g.weightx = 1;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        form.add(descPane, g);

        // reset constraints
        g.gridwidth = 1;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.WEST;
        row++;

        // Buttons row
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 0));
        btns.add(new JButton(appAction("Reset", () -> {
            title.setText("");
            description.setText("");
        })));
        btns.add(new JButton(appAction("Validate", () -> {})));
        btns.add(new JButton(appAction("Apply", () -> {})));

        g.gridx = 0; g.gridy = row; g.gridwidth = 4; g.weightx = 1;
        form.add(btns, g);

        return form;
    }

    private JComponent buildInspector() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
        right.setPreferredSize(INSPECTOR_PREFERRED);

        right.add(titledPanel("Properties", buildPropertiesPanel()));
        right.add(Box.createVerticalStrut(GAP));
        right.add(titledPanel("Appearance", buildAppearancePanel()));
        right.add(Box.createVerticalStrut(GAP));
        right.add(titledPanel("Metrics", buildMetricsPanel()));
        right.add(Box.createVerticalGlue()); // keeps panels packed at top

        return new JScrollPane(right);
    }

    private JPanel titledPanel(String title, JComponent content) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                title,
                TitledBorder.LEADING,
                TitledBorder.TOP
        ));
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private JComponent buildPropertiesPanel() {
        JPanel p = new JPanel(new GridLayout(0, 2, GAP, GAP));
        p.add(new JLabel("ID:"));      p.add(new JTextField("AUTO"));
        p.add(new JLabel("Owner:"));   p.add(new JTextField("—"));
        p.add(new JLabel("State:"));   p.add(new JComboBox<>(new String[]{"Open", "In Progress", "Closed"}));
        p.add(new JLabel("Version:")); p.add(new JTextField("1.0.0"));
        p.add(new JLabel("License:")); p.add(new JComboBox<>(new String[]{"Apache-2.0", "MIT", "GPL-3.0"}));
        return p;
    }

    private JComponent buildAppearancePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        g.gridx = 0; g.gridy = row; g.weightx = 0; p.add(new JLabel("Theme:"), g);
        g.gridx = 1; g.gridy = row; g.weightx = 1; p.add(new JComboBox<>(new String[]{"Light", "Dark", "System"}), g);
        row++;

        g.gridx = 0; g.gridy = row; g.weightx = 0; p.add(new JLabel("Font Size:"), g);
        g.gridx = 1; g.gridy = row; g.weightx = 1; p.add(new JSlider(10, 32, 14), g);
        row++;

        g.gridx = 0; g.gridy = row; g.weightx = 0; p.add(new JLabel("Line Spacing:"), g);
        g.gridx = 1; g.gridy = row; g.weightx = 1; p.add(new JSlider(0, 10, 2), g);
        row++;

        JCheckBox wrap = new JCheckBox("Wrap Text");
        JCheckBox showGrid = new JCheckBox("Show Grid");
        JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 0));
        flags.add(wrap);
        flags.add(showGrid);

        g.gridx = 0; g.gridy = row; g.gridwidth = 2; g.weightx = 1;
        p.add(flags, g);

        return p;
    }

    private JComponent buildMetricsPanel() {
        JPanel p = new JPanel(new GridLayout(0, 1, GAP, GAP));

        JProgressBar cpu = new JProgressBar(0, 100); cpu.setValue(35);
        JProgressBar mem = new JProgressBar(0, 100); mem.setValue(62);
        JProgressBar io  = new JProgressBar(0, 100); io.setValue(10);

        cpu.setStringPainted(true);
        mem.setStringPainted(true);
        io.setStringPainted(true);

        cpu.setString("CPU 35%");
        mem.setString("Memory 62%");
        io.setString("I/O 10%");

        p.add(cpu);
        p.add(mem);
        p.add(io);

        return p;
    }

    private JComponent buildStatusBar() {
        JPanel status = new JPanel(new BorderLayout());
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel left = new JLabel("Ready");

        JProgressBar progress = new JProgressBar();
        progress.setIndeterminate(true);
        progress.setPreferredSize(STATUS_PROGRESS_SIZE);

        status.add(left, BorderLayout.WEST);
        status.add(progress, BorderLayout.EAST);

        return status;
    }

    // Small helper: avoids repeating anonymous listener boilerplate for “no-op” actions
    private static Action appAction(String name, Runnable r) {
        return new AbstractAction(name) {
            @Override public void actionPerformed(ActionEvent e) { r.run(); }
        };
    }

    private static void setNimbusIfAvailable() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    return;
                }
            }
        } catch (Exception ignored) { }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setNimbusIfAvailable();
            new ComplexLayoutDemo().show();
        });
    }
}
