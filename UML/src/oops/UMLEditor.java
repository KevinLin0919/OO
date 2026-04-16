package oops;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import oops.model.*;

/**
 * Oops UML Editor 主程式。
 * 這是一個簡易的 Workflow Design 編輯器，支援：
 *   A. 建立 Rect/Oval 物件
 *   B. 建立 Association/Generalization/Composition 連線
 *   C. 選取/取消選取物件（單選 + 框選）
 *   D. 群組(Group)/解散群組(Ungroup)
 *   E. 移動物件
 *   F. 調整物件大小 (Resize)
 *   G. 自定義標籤 (Label)
 *
 * 架構說明：
 *   - Model 層：UMLObject（RectObject, OvalObject, CompositeObject）、ConnectionLine、Port
 *   - Mode 層（Strategy Pattern）：Mode 介面 + SelectMode、CreateLinkMode
 *   - View/Controller 層：UMLEditor（JFrame）、Canvas（JPanel）、ToolPanel（JPanel）
 */
public class UMLEditor extends JFrame {

    private final Canvas canvas;

    public UMLEditor() {
        setTitle("Oops UML Editor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // 建立畫布
        canvas = new Canvas();

        // 建立左側工具列
        ToolPanel toolPanel = new ToolPanel(canvas);

        // 建立選單列
        setJMenuBar(createMenuBar());

        // 排版：工具列在左，畫布在中央
        setLayout(new BorderLayout());
        add(toolPanel, BorderLayout.WEST);
        add(canvas, BorderLayout.CENTER);
    }

    /** 建立 Menu Bar（File + Edit） */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File 選單
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        // Edit 選單
        JMenu editMenu = new JMenu("Edit");

        JMenuItem groupItem = new JMenuItem("Group");
        groupItem.addActionListener(e -> canvas.groupSelectedObjects());
        editMenu.add(groupItem);

        JMenuItem ungroupItem = new JMenuItem("Ungroup");
        ungroupItem.addActionListener(e -> canvas.ungroupSelectedObject());
        editMenu.add(ungroupItem);

        editMenu.addSeparator();

        JMenuItem labelItem = new JMenuItem("Label");
        labelItem.addActionListener(e -> showLabelDialog());
        editMenu.add(labelItem);

        menuBar.add(editMenu);
        return menuBar;
    }

    /**
     * 顯示「自定義標籤」對話框（Use Case G）。
     * 前提：恰好有 1 個基本物件（非 Composite）被選取。
     */
    private void showLabelDialog() {
        List<UMLObject> selected = canvas.getSelectedObjects();
        if (selected.size() != 1) return;

        UMLObject obj = selected.get(0);
        if (obj instanceof CompositeObject) return;

        // 建立對話框
        JDialog dialog = new JDialog(this, "Customize Label Style", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name 欄位
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Name"), gbc);

        JTextField nameField = new JTextField(obj.getLabelName(), 15);
        gbc.gridx = 1;
        dialog.add(nameField, gbc);

        // Color 欄位（點擊按鈕開啟色彩選擇器）
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("Color"), gbc);

        JButton colorBtn = new JButton();
        Color initColor = obj.getLabelColor() != null ? obj.getLabelColor() : new Color(200, 200, 200);
        colorBtn.setBackground(initColor);
        colorBtn.setOpaque(true);
        colorBtn.setPreferredSize(new Dimension(120, 30));
        colorBtn.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(dialog, "Choose Label Color", colorBtn.getBackground());
            if (chosen != null) {
                colorBtn.setBackground(chosen);
            }
        });
        gbc.gridx = 1;
        dialog.add(colorBtn, gbc);

        // Cancel / OK 按鈕
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton okBtn = new JButton("OK");
        okBtn.addActionListener(e -> {
            obj.setLabelName(nameField.getText());
            obj.setLabelColor(colorBtn.getBackground());
            canvas.repaint();
            dialog.dispose();
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        dialog.add(btnPanel, gbc);

        dialog.setVisible(true);
    }

    /** 程式進入點 */
    public static void main(String[] args) {
        // 使用跨平台 Look & Feel，確保按鈕背景色等設定能正常運作
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            UMLEditor editor = new UMLEditor();
            editor.setVisible(true);
        });
    }
}
