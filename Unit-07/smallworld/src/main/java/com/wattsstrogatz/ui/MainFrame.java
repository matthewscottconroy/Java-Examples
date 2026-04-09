package com.wattsstrogatz.ui;

import com.wattsstrogatz.model.*;
import com.wattsstrogatz.simulation.WattsStrogatzSimulation;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Main application window for the Watts-Strogatz small-world simulation.
 *
 * <p>Assembles the {@link NetworkPanel}, {@link ControlPanel},
 * {@link PhaseDiagramPanel}, and a menu bar.  Owns the
 * {@link WattsStrogatzSimulation} instance and the auto-step {@link Timer}.
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Step / run / reset controls with preset configurations</li>
 *   <li>Live edge highlight during animation (last-visited edge)</li>
 *   <li>Node hover info (degree, local clustering coefficient)</li>
 *   <li>Phase diagram panel: C(p)/C(0) and L(p)/L(0) vs p sweep</li>
 *   <li>Export the network visualisation as PNG (File menu)</li>
 * </ul>
 */
public final class MainFrame extends JFrame {

    private WattsStrogatzSimulation simulation;
    private final NetworkPanel       networkPanel;
    private final ControlPanel       controlPanel;
    private final PhaseDiagramPanel  phaseDiagramPanel;
    private Timer                    autoStepTimer;

    /**
     * Constructs and shows the main window.
     *
     * @param initialConfig starting configuration; must not be null
     */
    public MainFrame(NetworkConfig initialConfig) {
        super("Watts–Strogatz Small-World Network Simulator");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(4, 4));

        simulation        = new WattsStrogatzSimulation(initialConfig);
        networkPanel      = new NetworkPanel();
        phaseDiagramPanel = new PhaseDiagramPanel();
        phaseDiagramPanel.setConfigSupplier(() -> simulation.getConfig());
        phaseDiagramPanel.setCurrentP(initialConfig.getRewiringProbability());

        controlPanel = new ControlPanel(new ControlPanel.SimulationListener() {
            @Override public void onStep()                    { doStep(1);        }
            @Override public void onStepMany(int n)           { doStep(n);        }
            @Override public void onRunToggled(boolean start) { toggleRun(start); }
            @Override public void onReset(NetworkConfig cfg)  { doReset(cfg);     }
        });

        // Title bar
        JLabel title = new JLabel(
            "<html><b>Watts–Strogatz (1998)</b>  ·  " +
            "<i>\"Collective dynamics of 'small-world' networks\"</i></html>",
            SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.PLAIN, 12));
        title.setBorder(BorderFactory.createEmptyBorder(6, 8, 4, 8));

        add(title,             BorderLayout.NORTH);
        add(networkPanel,      BorderLayout.CENTER);
        add(controlPanel,      BorderLayout.EAST);
        add(phaseDiagramPanel, BorderLayout.SOUTH);

        // Menu bar
        setJMenuBar(buildMenuBar());

        refresh();
        pack();
        setMinimumSize(new Dimension(800, 700));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // -------------------------------------------------------------------------
    // Simulation control
    // -------------------------------------------------------------------------

    private void doStep(int count) {
        for (int i = 0; i < count && !simulation.isComplete(); i++)
            simulation.step();
        refresh();
        if (simulation.isComplete()) { controlPanel.forceStop(); stopTimer(); }
    }

    private void toggleRun(boolean start) {
        if (start) startTimer(); else stopTimer();
    }

    private void doReset(NetworkConfig cfg) {
        stopTimer();
        boolean nkChanged = cfg.getNodeCount() != simulation.getConfig().getNodeCount()
                         || cfg.getK()         != simulation.getConfig().getK();
        simulation = new WattsStrogatzSimulation(cfg);
        networkPanel.setHighlightEdge(null);
        phaseDiagramPanel.setCurrentP(cfg.getRewiringProbability());
        if (nkChanged) phaseDiagramPanel.clearSweep();
        phaseDiagramPanel.repaint();
        refresh();
    }

    // -------------------------------------------------------------------------
    // Refresh
    // -------------------------------------------------------------------------

    private void refresh() {
        // Compute metrics once — getCurrentMetrics() is O(n²).
        NetworkMetrics.MetricsSnapshot current  = simulation.getCurrentMetrics();
        NetworkMetrics.MetricsSnapshot relative = simulation.getRelativeMetrics(current);

        networkPanel.setHighlightEdge(simulation.getLastVisitedEdge());
        networkPanel.setNetwork(simulation.getNetwork());   // also repaints

        controlPanel.updateStats(
            simulation.getEdgesVisited(), simulation.getTotalEdges(),
            current, relative,
            simulation.isComplete());

        phaseDiagramPanel.setCurrentP(simulation.getConfig().getRewiringProbability());
        phaseDiagramPanel.repaint();
    }

    // -------------------------------------------------------------------------
    // Timer
    // -------------------------------------------------------------------------

    private void startTimer() {
        stopTimer();
        autoStepTimer = new Timer(controlPanel.getStepDelayMs(), e -> doStep(1));
        autoStepTimer.start();
    }

    private void stopTimer() {
        if (autoStepTimer != null) { autoStepTimer.stop(); autoStepTimer = null; }
    }

    // -------------------------------------------------------------------------
    // Menu bar
    // -------------------------------------------------------------------------

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        // --- File ---
        JMenu fileMenu = new JMenu("File");
        JMenuItem exportItem = new JMenuItem("Export Network as PNG…");
        exportItem.setAccelerator(
            KeyStroke.getKeyStroke('E', Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()));
        exportItem.addActionListener(e -> exportPng());
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(e -> dispose());
        fileMenu.add(closeItem);
        bar.add(fileMenu);

        // --- Simulation ---
        JMenu simMenu = new JMenu("Simulation");
        JMenuItem sweepItem = new JMenuItem("Run Phase-Diagram Sweep");
        sweepItem.setToolTipText("Compute C(p)/C(0) and L(p)/L(0) across 20 p values");
        sweepItem.addActionListener(e -> phaseDiagramPanel.getRunSweepButton().doClick());
        simMenu.add(sweepItem);
        bar.add(simMenu);

        // --- Help ---
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);
        bar.add(helpMenu);

        return bar;
    }

    // -------------------------------------------------------------------------
    // Export PNG
    // -------------------------------------------------------------------------

    private void exportPng() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Network as PNG");
        chooser.setSelectedFile(new File("network.png"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".png"))
            file = new File(file.getPath() + ".png");

        try {
            BufferedImage img = new BufferedImage(
                networkPanel.getWidth(), networkPanel.getHeight(),
                BufferedImage.TYPE_INT_RGB);
            networkPanel.paint(img.getGraphics());
            ImageIO.write(img, "png", file);
            JOptionPane.showMessageDialog(this,
                "Saved: " + file.getName(), "Export successful",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                "Export failed: " + ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // -------------------------------------------------------------------------
    // About dialog
    // -------------------------------------------------------------------------

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "<html><b>Watts–Strogatz Small-World Simulator</b><br><br>" +
            "Based on: Watts &amp; Strogatz, <i>Nature</i> 393, 440–442 (1998).<br>" +
            "<i>\"Collective dynamics of 'small-world' networks\"</i><br><br>" +
            "Controls:<br>" +
            "  • Step / Run to rewire edges one at a time<br>" +
            "  • Hover over nodes for degree and clustering info<br>" +
            "  • Presets apply n, k, p and reset immediately<br>" +
            "  • Run Sweep plots C(p)/C(0) and L(p)/L(0) vs p<br>" +
            "  • File ▸ Export PNG saves the network diagram</html>",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }
}
