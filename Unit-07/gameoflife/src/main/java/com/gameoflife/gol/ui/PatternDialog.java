package com.gameoflife.gol.ui;

import com.gameoflife.gol.Pattern;
import com.gameoflife.gol.PatternLibrary;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog for browsing and selecting a pattern from the library.
 */
public final class PatternDialog extends JDialog {

    private static final Color BG     = new Color(14, 16, 28);
    private static final Color BG2    = new Color(20, 22, 36);
    private static final Color FG     = new Color(190, 210, 235);
    private static final Color ACCENT = new Color(80, 200, 140);
    private static final Color DIM    = new Color(110, 125, 150);
    private static final Color SEL    = new Color(30, 70, 110);
    private static final Font  MONO   = new Font(Font.MONOSPACED, Font.PLAIN, 11);
    private static final Font  SMALL  = new Font(Font.MONOSPACED, Font.PLAIN, 10);

    private Pattern selectedPattern = null;

    public PatternDialog(Frame owner) {
        super(owner, "Pattern Library", true);
        setBackground(BG);
        setPreferredSize(new Dimension(650, 500));

        // ── Category list ────────────────────────────────────────────────────
        List<String> categories = PatternLibrary.categories();
        JList<String> catList = makeList(categories);
        catList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Pattern list ──────────────────────────────────────────────────────
        DefaultListModel<Pattern> patModel = new DefaultListModel<>();
        JList<Pattern> patList = new JList<>(patModel);
        styleList(patList);
        patList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ── Description & preview ────────────────────────────────────────────
        JTextArea descArea = makeTextArea(4);
        JTextArea prevArea = makeTextArea(15);
        prevArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));

        // ── Wire category selection ───────────────────────────────────────────
        catList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String cat = catList.getSelectedValue();
            if (cat == null) return;
            patModel.clear();
            PatternLibrary.inCategory(cat).forEach(patModel::addElement);
            if (!patModel.isEmpty()) patList.setSelectedIndex(0);
        });

        // ── Wire pattern selection ────────────────────────────────────────────
        patList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            Pattern p = patList.getSelectedValue();
            if (p == null) return;
            descArea.setText("  " + p.getName() + "  (" + p.getCellCount() + " cells)\n\n  " +
                wrapText(p.getDescription(), 55));
            prevArea.setText(p.toAscii());
        });

        // Select first category
        if (!categories.isEmpty()) catList.setSelectedIndex(0);

        // ── Layout ───────────────────────────────────────────────────────────
        JScrollPane catScroll = scroll(catList);
        catScroll.setPreferredSize(new Dimension(150, 300));
        JScrollPane patScroll = scroll(patList);
        patScroll.setPreferredSize(new Dimension(200, 300));
        JScrollPane prevScroll = scroll(prevArea);
        prevScroll.setPreferredSize(new Dimension(250, 200));

        JLabel catLabel  = hdr("Category");
        JLabel patLabel  = hdr("Pattern");
        JLabel prevLabel = hdr("Preview");
        JLabel descLabel = hdr("Description");

        JPanel left = col(catLabel, catScroll);
        JPanel mid  = col(patLabel, patScroll);
        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setBackground(BG);
        right.add(col(prevLabel, prevScroll), BorderLayout.CENTER);
        right.add(col(descLabel, scroll(descArea)), BorderLayout.SOUTH);

        JPanel main = new JPanel(new GridLayout(1, 3, 6, 0));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(8, 8, 4, 8));
        main.add(left);
        main.add(mid);
        main.add(right);

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton placeBtn  = btn("Place");
        JButton cancelBtn = btn("Cancel");

        placeBtn.addActionListener(e -> {
            selectedPattern = patList.getSelectedValue();
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        btnPanel.setBackground(BG);
        btnPanel.add(cancelBtn);
        btnPanel.add(placeBtn);

        setLayout(new BorderLayout());
        add(main,    BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    /** Returns the pattern the user chose, or null if cancelled. */
    public Pattern getSelectedPattern() { return selectedPattern; }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static <T> JList<T> makeList(List<T> items) {
        JList<T> l = new JList<>(items.toArray((T[]) new Object[0]));
        styleList(l);
        return l;
    }

    private static void styleList(JList<?> l) {
        l.setBackground(BG2);
        l.setForeground(FG);
        l.setFont(MONO);
        l.setSelectionBackground(SEL);
        l.setSelectionForeground(Color.WHITE);
        l.setFixedCellHeight(20);
        l.setBorder(null);
    }

    private static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(new Color(35, 45, 65), 1));
        sp.setBackground(BG);
        sp.getViewport().setBackground(BG2);
        return sp;
    }

    private static JTextArea makeTextArea(int rows) {
        JTextArea ta = new JTextArea();
        ta.setRows(rows);
        ta.setEditable(false);
        ta.setBackground(BG2);
        ta.setForeground(new Color(175, 195, 215));
        ta.setFont(MONO);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(4, 6, 4, 6));
        return ta;
    }

    private static JPanel col(JComponent... comps) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(BG);
        for (JComponent c : comps) {
            if (c instanceof JLabel) c.setAlignmentX(LEFT_ALIGNMENT);
            p.add(c);
            p.add(Box.createVerticalStrut(2));
        }
        return p;
    }

    private static JLabel hdr(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        l.setForeground(ACCENT);
        l.setBorder(new EmptyBorder(2, 0, 2, 0));
        return l;
    }

    private static JButton btn(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(28, 35, 55));
        b.setForeground(FG);
        b.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(50, 65, 90)),
            new EmptyBorder(4, 12, 4, 12)));
        return b;
    }

    private static String wrapText(String s, int width) {
        if (s.length() <= width) return s;
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int end = Math.min(i + width, s.length());
            if (end < s.length()) {
                int sp = s.lastIndexOf(' ', end);
                if (sp > i) end = sp + 1;
            }
            if (sb.length() > 0) sb.append("\n  ");
            sb.append(s, i, end);
            i = end;
        }
        return sb.toString();
    }
}
