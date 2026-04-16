package oops;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

import oops.mode.*;

/**
 * 左側工具列，包含 6 個按鈕：
 *   - Select、Association、Generalization、Composition → 持久模式按鈕（點擊切換）
 *   - Rect、Oval → 建立物件按鈕，支援兩種操作方式：
 *       (1) 點擊按鈕 → 按鈕變深色 → 點擊畫布建立物件 → 自動回到原模式
 *       (2) 按住按鈕拖曳到畫布放開 → 直接建立物件 → 自動回到原模式
 */
public class ToolPanel extends JPanel {

    private final Canvas canvas;
    private JButton highlightedButton; // 目前被高亮的按鈕

    public ToolPanel(Canvas canvas) {
        this.canvas = canvas;
        setPreferredSize(new Dimension(160, 0));
        setLayout(new GridLayout(6, 1, 4, 4));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 4 個持久模式按鈕
        JButton selectBtn = createModeButton("Select", "select", SelectMode::new);
        JButton assocBtn  = createModeButton("Association", "association",
                () -> new CreateLinkMode(CreateLinkMode.LinkType.ASSOCIATION));
        JButton genBtn    = createModeButton("Generalization", "generalization",
                () -> new CreateLinkMode(CreateLinkMode.LinkType.GENERALIZATION));
        JButton compBtn   = createModeButton("Composition", "composition",
                () -> new CreateLinkMode(CreateLinkMode.LinkType.COMPOSITION));

        // 2 個建立物件按鈕（和模式按鈕一樣是持久模式，可連續建立）
        JButton rectBtn = createModeButton("Rect", "rect",
                () -> new CreateObjectMode(true));
        JButton ovalBtn = createModeButton("Oval", "oval",
                () -> new CreateObjectMode(false));

        add(selectBtn);
        add(assocBtn);
        add(genBtn);
        add(compBtn);
        add(rectBtn);
        add(ovalBtn);

        // 預設啟用 Select 模式
        highlightButton(selectBtn);
        canvas.setMode(new SelectMode());
    }

    // ======== 按鈕高亮管理 ========

    /** 將指定按鈕設為高亮（深色），同時取消之前按鈕的高亮 */
    private void highlightButton(JButton btn) {
        if (highlightedButton != null) {
            highlightedButton.setBackground(UIManager.getColor("Button.background"));
            highlightedButton.setForeground(UIManager.getColor("Button.foreground"));
        }
        highlightedButton = btn;
        btn.setBackground(new Color(60, 60, 60));
        btn.setForeground(Color.WHITE);
    }

    // ======== 建立按鈕 ========

    /** 建立模式按鈕（所有 6 個按鈕共用） */
    private JButton createModeButton(String text, String iconType, Supplier<Mode> modeFactory) {
        JButton btn = new JButton(text);
        styleButton(btn, iconType);

        btn.addActionListener(e -> {
            highlightButton(btn);
            canvas.setMode(modeFactory.get());
            canvas.setCursor(Cursor.getDefaultCursor());
        });
        return btn;
    }

    /** 共用的按鈕外觀設定 */
    private void styleButton(JButton btn, String iconType) {
        btn.setIcon(createIcon(iconType));
        btn.setHorizontalTextPosition(SwingConstants.LEFT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setOpaque(true);
    }

    /**
     * 為每個按鈕建立一個小圖示（程式繪製），符合 Spec 示意圖中的圖示樣式。
     */
    private Icon createIcon(String type) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setStroke(new BasicStroke(1.5f));

                switch (type) {
                    case "select":
                        g2d.setColor(Color.BLACK);
                        int[] px = {x+2, x+2, x+7, x+9, x+13, x+10, x+14};
                        int[] py = {y+1, y+15, y+12, y+17, y+15, y+11, y+11};
                        g2d.fillPolygon(px, py, 7);
                        break;
                    case "association":
                        g2d.setColor(Color.BLACK);
                        g2d.drawLine(x+3, y+9, x+19, y+9);
                        g2d.drawLine(x+3, y+9, x+9, y+4);
                        g2d.drawLine(x+3, y+9, x+9, y+14);
                        break;
                    case "generalization":
                        g2d.setColor(Color.BLACK);
                        g2d.drawLine(x+11, y+9, x+19, y+9);
                        Polygon tri = new Polygon();
                        tri.addPoint(x+2, y+9);
                        tri.addPoint(x+11, y+3);
                        tri.addPoint(x+11, y+15);
                        g2d.setColor(Color.WHITE);
                        g2d.fillPolygon(tri);
                        g2d.setColor(Color.BLACK);
                        g2d.drawPolygon(tri);
                        break;
                    case "composition":
                        g2d.setColor(Color.BLACK);
                        g2d.drawLine(x+13, y+9, x+19, y+9);
                        Polygon dia = new Polygon();
                        dia.addPoint(x+2, y+9);
                        dia.addPoint(x+7, y+3);
                        dia.addPoint(x+13, y+9);
                        dia.addPoint(x+7, y+15);
                        g2d.setColor(Color.WHITE);
                        g2d.fillPolygon(dia);
                        g2d.setColor(Color.BLACK);
                        g2d.drawPolygon(dia);
                        break;
                    case "rect":
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.fillRect(x+3, y+4, 14, 10);
                        g2d.setColor(Color.BLACK);
                        g2d.drawRect(x+3, y+4, 14, 10);
                        break;
                    case "oval":
                        g2d.setColor(Color.DARK_GRAY);
                        g2d.fillOval(x+3, y+3, 14, 12);
                        g2d.setColor(Color.BLACK);
                        g2d.drawOval(x+3, y+3, 14, 12);
                        break;
                }
                g2d.dispose();
            }
            @Override public int getIconWidth() { return 22; }
            @Override public int getIconHeight() { return 18; }
        };
    }
}
