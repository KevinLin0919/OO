package oops;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Supplier;

import oops.mode.*;
import oops.model.*;

/**
 * 左側工具列，包含 6 個按鈕：
 *
 *   Select / Association / Generalization / Composition
 *     → 持久模式按鈕：點擊後切換模式，保持到下次切換為止。
 *
 *   Rect / Oval（Use Case A）支援兩種建立路徑：
 *
 *     路徑 1 — 拖曳路徑（Spec 規定）：
 *       在按鈕上按住不放 → 拖曳到畫布 → 放開 → 立即建立物件 → 自動回到原模式
 *       由 MouseListener.mouseReleased 透過座標轉換處理。
 *
 *     路徑 2 — 點擊路徑（使用者直覺）：
 *       點擊按鈕 → 進入建立模式（十字游標）→ 點畫布 → 建立物件 → 自動回到原模式
 *       由 ActionListener 切換為 CreateObjectMode 處理。
 */
public class ToolPanel extends JPanel {

    private final Canvas canvas;
    private JButton highlightedButton;

    // 按下 Rect/Oval 按鈕前所記住的狀態，用於建立後恢復
    private Mode    savedMode;
    private JButton savedButton;

    // 防止拖曳路徑和點擊路徑同時觸發建立（drag 已建立則 ActionListener 略過）
    private boolean dragCreated = false;

    public ToolPanel(Canvas canvas) {
        this.canvas = canvas;
        setPreferredSize(new Dimension(160, 0));
        setLayout(new GridLayout(6, 1, 4, 4));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton selectBtn = createModeButton("Select", "select", SelectMode::new);
        JButton assocBtn  = createModeButton("Association", "association",
                () -> new CreateLinkMode(CreateLinkMode.LinkType.ASSOCIATION));
        JButton genBtn    = createModeButton("Generalization", "generalization",
                () -> new CreateLinkMode(CreateLinkMode.LinkType.GENERALIZATION));
        JButton compBtn   = createModeButton("Composition", "composition",
                () -> new CreateLinkMode(CreateLinkMode.LinkType.COMPOSITION));

        JButton rectBtn = createObjectButton("Rect", "rect", true);
        JButton ovalBtn = createObjectButton("Oval", "oval", false);

        add(selectBtn);
        add(assocBtn);
        add(genBtn);
        add(compBtn);
        add(rectBtn);
        add(ovalBtn);

        highlightButton(selectBtn);
        canvas.setMode(new SelectMode());
    }

    // ======== 按鈕高亮管理 ========

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

    /** 建立持久模式按鈕（Select / Association / Generalization / Composition） */
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

    /**
     * 建立一次性物件按鈕（Rect / Oval）。
     * 同時掛載 MouseListener（拖曳路徑）和 ActionListener（點擊路徑）。
     */
    private JButton createObjectButton(String text, String iconType, boolean isRect) {
        JButton btn = new JButton(text);
        styleButton(btn, iconType);

        // ── 拖曳路徑 ──────────────────────────────────────────────────
        // Swing 規則：mouseReleased 永遠送回給 pressed 的那個元件，
        // 所以拖曳到畫布再放開，事件仍在按鈕上觸發，透過座標轉換判斷位置。
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragCreated = false;
                savedMode   = canvas.getMode();
                savedButton = highlightedButton;
                highlightButton(btn);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 將按鈕本地座標轉換為畫布座標
                Point pt = SwingUtilities.convertPoint(btn, e.getPoint(), canvas);
                if (canvas.contains(pt)) {
                    // 在畫布上放開 → 建立物件
                    int w = 150, h = 100;
                    UMLObject obj = isRect
                            ? new RectObject(pt.x - w / 2, pt.y - h / 2, w, h)
                            : new OvalObject(pt.x - w / 2, pt.y - h / 2, w, h);
                    canvas.addObject(obj);
                    dragCreated = true;
                    restoreSavedState(); // 恢復原模式和按鈕
                }
                // 若在按鈕上放開（沒拖到畫布），交給 ActionListener 切換模式
            }
        });

        // ── 點擊路徑 ──────────────────────────────────────────────────
        // ActionListener 在 mouseReleased on button 後觸發。
        // 若拖曳路徑已建立（dragCreated=true），則略過。
        btn.addActionListener(e -> {
            if (!dragCreated) {
                // 取出儲存的原狀態（由 mousePressed 設定）
                Mode    pm = savedMode;
                JButton pb = savedButton;
                savedMode   = null;
                savedButton = null;

                // 切換到 CreateObjectMode，建立後自動恢復
                canvas.setMode(new CreateObjectMode(isRect, () -> {
                    if (pb != null) highlightButton(pb);
                    if (pm != null) canvas.setMode(pm);
                    canvas.setCursor(Cursor.getDefaultCursor());
                }));
                // btn 已在 mousePressed 時 highlight，不需再呼叫
            }
            dragCreated = false;
        });

        return btn;
    }

    /** 恢復按下 Rect/Oval 前所記住的模式和按鈕高亮 */
    private void restoreSavedState() {
        if (savedButton != null) {
            highlightButton(savedButton);
            savedButton = null;
        }
        if (savedMode != null) {
            canvas.setMode(savedMode);
            savedMode = null;
        }
    }

    // ======== 按鈕外觀 ========

    private void styleButton(JButton btn, String iconType) {
        btn.setIcon(createIcon(iconType));
        btn.setHorizontalTextPosition(SwingConstants.LEFT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setOpaque(true);
    }

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
            @Override public int getIconWidth()  { return 22; }
            @Override public int getIconHeight() { return 18; }
        };
    }
}
