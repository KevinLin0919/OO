package oops.mode;

import java.awt.*;
import java.awt.event.MouseEvent;

import oops.Canvas;
import oops.model.*;
import oops.factory.LinkFactory;

// Strategy Pattern
/**
 * 建立連線模式（Use Case B）。
 * 採用 Factory Method Pattern：透過注入的 LinkFactory 建立連線，
 * 不直接依賴具體連線類別（AssociationLine、GeneralizationLine、CompositionLine）。
 *
 * 操作流程：
 *   1. 在某個基本物件的 port 範圍（10×10）內按下滑鼠
 *   2. 拖曳到另一個基本物件的 port 範圍內放開
 *   3. 透過 LinkFactory 建立對應連線
 */
public class CreateLinkMode implements Mode {

    private final LinkFactory factory;
    private UMLObject sourceObject;
    private Port sourcePort;
    private Point currentMouse;
    private boolean dragging;

    public CreateLinkMode(LinkFactory factory) {
        this.factory = factory;
    }

    @Override
    public void mousePressed(MouseEvent e, Canvas canvas) {
        int mx = e.getX();
        int my = e.getY();

        Port port = canvas.getModel().getPortAt(mx, my);
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

        UMLObject obj = canvas.getModel().getBasicObjectAt(e.getX(), e.getY());
        canvas.getModel().setHoveredObject(obj != sourceObject ? obj : null);
    }

    @Override
    public void mouseReleased(MouseEvent e, Canvas canvas) {
        if (!dragging) return;

        int mx = e.getX();
        int my = e.getY();

        Port destPort = canvas.getModel().getPortAt(mx, my);
        if (destPort != null && destPort.getOwner() != sourceObject) {
            ConnectionLine line = factory.create(sourceObject, sourcePort, destPort.getOwner(), destPort);
            canvas.getModel().addConnectionLine(line);
        }

        dragging = false;
        sourceObject = null;
        sourcePort = null;
        currentMouse = null;
        canvas.getModel().setHoveredObject(null);
    }

    @Override
    public void mouseMoved(MouseEvent e, Canvas canvas) {
        UMLObject obj = canvas.getModel().getBasicObjectAt(e.getX(), e.getY());
        canvas.getModel().setHoveredObject(obj);
    }

    @Override
    public void draw(Graphics2D g) {
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
}
