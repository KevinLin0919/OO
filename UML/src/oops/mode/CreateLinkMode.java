package oops.mode;

import java.awt.*;
import java.awt.event.MouseEvent;

import oops.Canvas;
import oops.model.*;

/**
 * 建立連線模式（Use Case B）。
 * 適用於 Association、Generalization、Composition 三種連線類型。
 *
 * 操作流程：
 *   1. 在某個基本物件上按下滑鼠（找到最近的 port 作為起點）
 *   2. 拖曳到另一個基本物件上放開（找到最近的 port 作為終點）
 *   3. 建立連線
 */
public class CreateLinkMode implements Mode {

    public enum LinkType { ASSOCIATION, GENERALIZATION, COMPOSITION }

    private final LinkType linkType;
    private UMLObject sourceObject;
    private Port sourcePort;
    private Point currentMouse;
    private boolean dragging;

    public CreateLinkMode(LinkType linkType) {
        this.linkType = linkType;
    }

    @Override
    public void mousePressed(MouseEvent e, Canvas canvas) {
        int mx = e.getX();
        int my = e.getY();

        // 找到滑鼠位置的基本物件（不含 Composite）
        UMLObject obj = canvas.getBasicObjectAt(mx, my);
        if (obj != null) {
            // 使用最近的 port 作為起點
            Port port = obj.getNearestPort(mx, my);
            if (port != null) {
                sourceObject = obj;
                sourcePort = port;
                currentMouse = new Point(mx, my);
                dragging = true;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e, Canvas canvas) {
        if (!dragging) return;

        currentMouse = new Point(e.getX(), e.getY());

        // 更新 hover 狀態，讓拖曳經過的物件顯示 port（方便使用者看到連接目標）
        UMLObject obj = canvas.getBasicObjectAt(e.getX(), e.getY());
        canvas.setHoveredObject(obj != sourceObject ? obj : null);

        canvas.repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e, Canvas canvas) {
        if (!dragging) return;

        int mx = e.getX();
        int my = e.getY();

        // 找到終點的基本物件（必須和起點不同）
        UMLObject destObj = canvas.getBasicObjectAt(mx, my);
        if (destObj != null && destObj != sourceObject) {
            Port destPort = destObj.getNearestPort(mx, my);
            if (destPort != null) {
                // 根據連線類型建立對應的 ConnectionLine
                ConnectionLine line = createLine(sourceObject, sourcePort, destObj, destPort);
                canvas.addConnectionLine(line);
            }
        }

        // 重置狀態
        dragging = false;
        sourceObject = null;
        sourcePort = null;
        currentMouse = null;
        canvas.setHoveredObject(null);
        canvas.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e, Canvas canvas) {
        // 在連線模式下，hover 也要顯示 port，方便使用者識別可連接的物件
        UMLObject obj = canvas.getBasicObjectAt(e.getX(), e.getY());
        canvas.setHoveredObject(obj);
        canvas.repaint();
    }

    @Override
    public void draw(Graphics2D g) {
        // 拖曳過程中，繪製從起點 port 到滑鼠位置的暫時線段
        if (dragging && sourcePort != null && currentMouse != null) {
            Point start = sourcePort.getCenter();
            Stroke oldStroke = g.getStroke();
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f, new float[]{5.0f}, 0.0f));
            g.drawLine(start.x, start.y, currentMouse.x, currentMouse.y);
            g.setStroke(oldStroke);
        }
    }

    /** 根據 linkType 工廠方法建立對應的 ConnectionLine */
    private ConnectionLine createLine(UMLObject src, Port srcPort,
                                      UMLObject dest, Port destPort) {
        switch (linkType) {
            case GENERALIZATION:
                return new GeneralizationLine(src, srcPort, dest, destPort);
            case COMPOSITION:
                return new CompositionLine(src, srcPort, dest, destPort);
            case ASSOCIATION:
            default:
                return new AssociationLine(src, srcPort, dest, destPort);
        }
    }
}
