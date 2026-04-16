package oops.model;

import java.awt.*;

/**
 * Connection Line 的抽象基底類別。
 * 每條 line 連接兩個基本物件的特定 port。
 * 子類別負責繪製各自的箭頭樣式（Association、Generalization、Composition）。
 */
public abstract class ConnectionLine {

    protected final UMLObject source;
    protected final UMLObject destination;
    protected final Port sourcePort;
    protected final Port destPort;

    public ConnectionLine(UMLObject source, Port sourcePort,
                          UMLObject destination, Port destPort) {
        this.source = source;
        this.sourcePort = sourcePort;
        this.destination = destination;
        this.destPort = destPort;
    }

    /** 繪製連線（線段 + 箭頭） */
    public void draw(Graphics2D g) {
        Point start = sourcePort.getCenter();
        Point end = destPort.getCenter();

        Stroke oldStroke = g.getStroke();
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(2.0f));

        // 畫線段
        g.drawLine(start.x, start.y, end.x, end.y);

        // 由子類別繪製箭頭
        drawArrowHead(g, start, end);

        g.setStroke(oldStroke);
    }

    /** 子類別實作不同的箭頭樣式 */
    protected abstract void drawArrowHead(Graphics2D g, Point start, Point end);

    /** 判斷此線是否連接到指定物件 */
    public boolean isConnectedTo(UMLObject obj) {
        return source == obj || destination == obj;
    }

    public UMLObject getSource() { return source; }
    public UMLObject getDestination() { return destination; }
}
