package oops.factory;

import oops.model.UMLObject;

import java.awt.Graphics2D;

// Factory Method Pattern
/**
 * ShapeFactory 是 Factory Method Pattern 的 Product Factory 介面。
 * 每個實作類別負責建立一種具體 UMLObject（RectObject 或 OvalObject），
 * 讓 CreateObjectMode 和 ToolPanel 的拖曳路徑不依賴具體類別。
 */
public interface ShapeFactory {
    UMLObject create(int x, int y, int w, int h);

    /** 在拖曳預覽時以對應形狀繪製虛線外框 */
    void drawPreview(Graphics2D g, int x, int y, int w, int h);
}
