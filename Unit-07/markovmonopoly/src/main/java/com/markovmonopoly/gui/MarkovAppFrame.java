package com.markovmonopoly.gui;

import com.markovmonopoly.core.MarkovChain;
import com.markovmonopoly.examples.*;
import com.markovmonopoly.monopoly.board.MonopolyBoard;
import com.markovmonopoly.monopoly.markov.MonopolyMarkovChainBuilder;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Root window for the interactive Markov-chain explorer.
 *
 * <p>Layout:
 * <pre>
 *   ┌──────────────────────────────────────────────────────────┐
 *   │  [chain selector combo]   ← top bar                     │
 *   ├───────────┬──────────────────────────────────────────────┤
 *   │  sidebar  │  tabbed pane: Graph | Matrix | Analysis | Board │
 *   └───────────┴──────────────────────────────────────────────┘
 * </pre>
 *
 * <p>Selecting a chain from the combo fires an event to all sub-panels so they
 * refresh themselves. The Matrix panel can also push an edited chain back via
 * {@link #pushChain}, which propagates the change to the other panels.
 */
public final class MarkovAppFrame extends JFrame {

    // ── Chain catalogue ───────────────────────────────────────────────────────

    private static final String[] CHAIN_NAMES = {
        "Weather Model",
        "Gambler's Ruin (fair, p=0.50)",
        "Gambler's Ruin (unfair, p=0.40)",
        "Gambler's Ruin (favorable, p=0.60)",
        "PageRank (α=0.15)",
        "Ehrenfest Urn (10 balls)",
        "Ehrenfest Urn (20 balls)",
        "Monopoly (Theoretical)",
    };

    // ── Panels ────────────────────────────────────────────────────────────────

    private final GraphPanel         graphPanel;
    private final MatrixPanel        matrixPanel;
    private final AnalysisPanel      analysisPanel;
    private final MonopolyBoardPanel boardPanel;
    private final JTabbedPane        tabs;
    private final JComboBox<String>  chainCombo;
    private final JLabel             chainDesc;

    // ── State ─────────────────────────────────────────────────────────────────

    private MarkovChain currentChain;
    private final List<Consumer<MarkovChain>> listeners = new ArrayList<>();

    // ── Construction ──────────────────────────────────────────────────────────

    public MarkovAppFrame() {
        super("Markov Chain Explorer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(new Color(22, 26, 40));
        getContentPane().setBackground(new Color(22, 26, 40));

        graphPanel    = new GraphPanel(this);
        matrixPanel   = new MatrixPanel(this);
        analysisPanel = new AnalysisPanel();
        boardPanel    = new MonopolyBoardPanel();

        listeners.add(graphPanel::onChainChanged);
        listeners.add(matrixPanel::onChainChanged);
        listeners.add(analysisPanel::onChainChanged);
        listeners.add(boardPanel::onChainChanged);

        tabs = buildTabs();
        chainCombo = new JComboBox<>(CHAIN_NAMES);
        chainDesc  = descLabel();

        setLayout(new BorderLayout(0, 0));
        add(buildTopBar(), BorderLayout.NORTH);
        add(tabs,          BorderLayout.CENTER);

        // Load first chain
        chainCombo.addActionListener(e -> loadSelectedChain());
        loadSelectedChain();

        pack();
        setMinimumSize(new Dimension(1100, 720));
        setLocationRelativeTo(null);
    }

    // ── Chain loading ─────────────────────────────────────────────────────────

    private void loadSelectedChain() {
        int idx = chainCombo.getSelectedIndex();
        MarkovChain chain = switch (idx) {
            case 0 -> WeatherExample.buildChain();
            case 1 -> GamblersRuinExample.buildGamblersRuin(8, 0.50);
            case 2 -> GamblersRuinExample.buildGamblersRuin(8, 0.40);
            case 3 -> GamblersRuinExample.buildGamblersRuin(8, 0.60);
            case 4 -> PageRankExample.buildChain();
            case 5 -> EhrenfestExample.buildEhrenfest(10);
            case 6 -> EhrenfestExample.buildEhrenfest(20);
            case 7 -> MonopolyMarkovChainBuilder.buildTheoretical(MonopolyBoard.standard());
            default -> WeatherExample.buildChain();
        };
        boolean isMonopoly = (idx == 7);
        tabs.setEnabledAt(3, isMonopoly);
        if (!isMonopoly && tabs.getSelectedIndex() == 3) tabs.setSelectedIndex(0);

        pushChain(chain);
        chainDesc.setText(chain.getDescription());
    }

    /** Propagates a (possibly edited) chain to all panels. */
    public void pushChain(MarkovChain chain) {
        currentChain = chain;
        for (Consumer<MarkovChain> l : listeners) l.accept(chain);
    }

    public MarkovChain getCurrentChain() { return currentChain; }

    // ── UI helpers ────────────────────────────────────────────────────────────

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(new Color(30, 36, 54));
        bar.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel title = new JLabel("Markov Chain Explorer");
        title.setForeground(new Color(180, 200, 255));
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(title);

        JLabel comboLbl = new JLabel("Chain:");
        comboLbl.setForeground(Color.LIGHT_GRAY);
        comboLbl.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        chainCombo.setBackground(new Color(45, 52, 75));
        chainCombo.setForeground(Color.WHITE);
        chainCombo.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        chainCombo.setPreferredSize(new Dimension(260, 24));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(comboLbl);
        right.add(chainCombo);

        bar.add(left,      BorderLayout.WEST);
        bar.add(chainDesc, BorderLayout.CENTER);
        bar.add(right,     BorderLayout.EAST);
        return bar;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tp = new JTabbedPane();
        tp.setBackground(new Color(22, 26, 40));
        tp.setForeground(new Color(180, 200, 255));
        tp.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));

        tp.addTab("Graph",    icon("●"), graphPanel,    "Animated state graph with random walker");
        tp.addTab("Matrix",   icon("▦"), matrixPanel,   "Edit transition probabilities");
        tp.addTab("Analysis", icon("π"), analysisPanel, "Stationary distribution and state classification");
        tp.addTab("Board",    icon("♟"), boardPanel,    "Monopoly board heat-map (Monopoly chain only)");
        tp.setEnabledAt(3, false);

        return tp;
    }

    private static JLabel descLabel() {
        JLabel l = new JLabel(" ");
        l.setForeground(new Color(140, 160, 200));
        l.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 11));
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    private static Icon icon(String text) {
        return new Icon() {
            public void paintIcon(Component c, java.awt.Graphics g, int x, int y) {
                g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
                g.setColor(new Color(160, 185, 230));
                g.drawString(text, x, y + 11);
            }
            public int getIconWidth()  { return 14; }
            public int getIconHeight() { return 14; }
        };
    }
}
