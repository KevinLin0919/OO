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
 *   1. 在某個基本物件的 port 範圍（10×10）內按下滑鼠
 *   2. 拖曳到另一個基本物件的 port 範圍內放開
 *   3. 建立連線
 *
 * Spec 明確規定：判斷座標是否在 port 的範圍內，不適用 composite 物件。
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

        // Spec B.1：必須點在基本物件的 port 範圍（10×10）內才能開始連線
        Port port = canvas.getPortAt(mx, my);
        if (port != null) {
            sourceObject = port.getOwner();
            sourcePort = port;
            currentMouse = new Point(mx, my);
            dragging = true;
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

        // Spec B.2：必須放在另一個基本物件的 port 範圍內才能建立連線
        Port destPort = canvas.getPortAt(mx, my);
        if (destPort != null && destPort.getOwner() != sourceObject) {
            ConnectionLine line = createLine(sourceObject, sourcePort, destPort.getOwner(), destPort);
            canvas.addConnectionLine(line);
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
