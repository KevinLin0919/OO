package oops.factory;

import java.awt.Graphics2D;

import oops.model.RectObject;
import oops.model.UMLObject;

// Factory Method Pattern - Concrete Factory
public class RectShapeFactory implements ShapeFactory {
    @Override
    public UMLObject create(int x, int y, int w, int h) {
        return new RectObject(x, y, w, h);
    }

    @Override
    public void drawPreview(Graphics2D g, int x, int y, int w, int h) {
        g.drawRect(x, y, w, h);
    }
}
