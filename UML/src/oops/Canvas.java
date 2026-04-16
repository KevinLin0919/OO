package oops;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import oops.model.*;
import oops.mode.Mode;

/**
 * 畫布（Canvas），是 UML Editor 的核心繪圖區域。
 * 負責：
 *   - 管理所有物件（objects）和連線（lines）
 *   - 將滑鼠事件委派給目前的 Mode 處理（Strategy Pattern）
 *   - 繪製所有物件、連線、以及模式特有的視覺元素
 *
 * 物件順序：list 中越後面的物件越「上層」（越先接收滑鼠事件、越晚被繪製覆蓋在上方）
 */
public class Canvas extends JPanel implements MouseListener, MouseMotionListener {

    private final List<UMLObject> objects = new ArrayList<>();
    private final List<ConnectionLine> lines = new ArrayList<>();
    private Mode currentMode;
    private UMLObject hoveredObject;

    public Canvas() {
        setBackground(Color.WHITE);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    // ======== 繪製 ========

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 繪製物件（依 list 順序，前面的先畫 = 在下層）
        for (UMLObject obj : objects) {
            obj.draw(g2d);
        }

        // 2. 繪製連線（畫在物件上方，確保線段清楚可見）
        for (ConnectionLine line : lines) {
            line.draw(g2d);
        }

        // 3. 繪製 Mode 特有的暫時視覺元素（框選矩形、暫時連線等）
        if (currentMode != null) {
            currentMode.draw(g2d);
        }
    }

    // ======== 物件管理 ========

    public void addObject(UMLObject obj) {
        objects.add(obj);
        repaint();
    }

    public void addConnectionLine(ConnectionLine line) {
        lines.add(line);
    }

    /**
     * 找到座標 (x, y) 上最頂層的物件（從 list 尾端往前找）。
     * 用於 Select 模式的點選和移動判定。
     */
    public UMLObject getObjectAt(int x, int y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            if (objects.get(i).contains(x, y)) {
                return objects.get(i);
            }
        }
        return null;
    }

    /**
     * 找到座標上最頂層的「基本物件」（穿透 Composite 去找裡面的 Rect/Oval）。
     * 用於連線模式，因為 link 只能連接基本物件，不能連接 Composite。
     */
    public UMLObject getBasicObjectAt(int x, int y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof CompositeObject) {
                // 遞迴搜尋 Composite 內的基本物件
                List<UMLObject> basics = ((CompositeObject) obj).getAllBasicObjects();
                for (int j = basics.size() - 1; j >= 0; j--) {
                    if (basics.get(j).contains(x, y)) {
                        return basics.get(j);
                    }
                }
            } else {
                if (obj.contains(x, y)) {
                    return obj;
                }
            }
        }
        return null;
    }

    /** 將物件移到 list 最後面（= 繪製在最上層 = 深度最小） */
    public void bringToFront(UMLObject obj) {
        if (objects.remove(obj)) {
            objects.add(obj);
        }
    }

    /** 取消所有物件的選取狀態 */
    public void deselectAll() {
        for (UMLObject obj : objects) {
            obj.setSelected(false);
        }
    }

    /** 更新 hover 的物件 */
    public void setHoveredObject(UMLObject obj) {
        if (hoveredObject != null && hoveredObject != obj) {
            hoveredObject.setHovered(false);
        }
        hoveredObject = obj;
        if (hoveredObject != null) {
            hoveredObject.setHovered(true);
        }
    }

    public UMLObject getHoveredObject() {
        return hoveredObject;
    }

    /** 選取所有「完全」落在矩形 rect 內的物件 */
    public void selectObjectsInRect(Rectangle rect) {
        for (UMLObject obj : objects) {
            int ox = obj.getX(), oy = obj.getY();
            int ow = obj.getWidth(), oh = obj.getHeight();
            // 物件的四個角都必須在框選矩形內
            if (rect.contains(ox, oy) && rect.contains(ox + ow, oy + oh)) {
                obj.setSelected(true);
            }
        }
    }

    /** 取得所有被選取的物件 */
    public List<UMLObject> getSelectedObjects() {
        List<UMLObject> selected = new ArrayList<>();
        for (UMLObject obj : objects) {
            if (obj.isSelected()) {
                selected.add(obj);
            }
        }
        return selected;
    }

    /** Group（Use Case D Case 1）：將 >=2 個被選取的物件合併為一個 Composite */
    public void groupSelectedObjects() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.size() < 2) return;

        CompositeObject composite = new CompositeObject(selected);
        for (UMLObject obj : selected) {
            objects.remove(obj);
        }
        composite.setSelected(true);
        objects.add(composite);
        repaint();
    }

    /** Ungroup（Use Case D Case 2）：解構 1 個被選取的 Composite 的最外層 */
    public void ungroupSelectedObject() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.size() != 1) return;
        if (!(selected.get(0) instanceof CompositeObject)) return;

        CompositeObject composite = (CompositeObject) selected.get(0);
        objects.remove(composite);

        // 把 Composite 的直接子物件放回頂層 list
        for (UMLObject child : composite.getChildren()) {
            child.setSelected(true);
            objects.add(child);
        }
        repaint();
    }

    public List<UMLObject> getObjects() { return objects; }
    public List<ConnectionLine> getLines() { return lines; }

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
