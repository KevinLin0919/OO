package oops;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import oops.mode.Mode;
import oops.model.*;

// MVC - View / Observer Pattern (implements ModelObserver)
/**
 * Canvas 是 MVC 架構中的 View 層，同時實作 Observer Pattern 的 ModelObserver 介面。
 * 職責只有兩件事：
 *   1. 將 UMLModel 的資料繪製到畫面上（paintComponent）
 *   2. 將滑鼠事件委派給目前的 Mode 處理（Strategy Pattern）
 *
 * 所有 business logic 已移至 UMLModel。
 */
public class Canvas extends JPanel implements MouseListener, MouseMotionListener, ModelObserver {

    private final UMLModel model;
    private Mode currentMode;

    public Canvas(UMLModel model) {
        this.model = model;
        model.addObserver(this);
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // ======== Observer Pattern ========

    @Override
    public void onModelChanged() {
        repaint();
    }

    // ======== Model 存取（供 Mode 使用）========

    public UMLModel getModel() {
        return model;
    }

    // ======== 繪製 ========

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (UMLObject obj : model.getObjects()) {
            obj.draw(g2d);
        }

        for (ConnectionLine line : model.getLines()) {
            line.draw(g2d);
        }

        if (currentMode != null) {
            currentMode.draw(g2d);
        }
    }

    // ======== Mode 管理 ========

    public void setMode(Mode mode) { this.currentMode = mode; }
    public Mode getMode() { return currentMode; }

    // ======== 滑鼠事件委派給目前的 Mode ========

    @Override
    public void mousePressed(MouseEvent e) {
        if (currentMode != null) currentMode.mousePressed(e, this);
    }
    @Override
    public void mouseDragged(MouseEvent e) {
        if (currentMode != null) currentMode.mouseDragged(e, this);
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (currentMode != null) currentMode.mouseReleased(e, this);
    }
    @Override
    public void mouseMoved(MouseEvent e) {
        if (currentMode != null) currentMode.mouseMoved(e, this);
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}
