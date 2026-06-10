package oops.model;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

// MVC - Model
/**
 * UMLModel 是 MVC 架構中的 Model 層。
 * 集中管理所有 UMLObject 與 ConnectionLine 的資料，以及所有 business logic。
 * 透過 ModelObserver（Observer Pattern）通知 View 層（Canvas）更新畫面。
 * 本類別不含任何 javax.swing 或 GUI 相關程式碼。
 */
public class UMLModel {

    private final List<UMLObject> objects = new ArrayList<>();
    private final List<ConnectionLine> lines = new ArrayList<>();
    private final List<ModelObserver> observers = new ArrayList<>();
    private UMLObject hoveredObject;

    // ======== Observer Pattern ========

    public void addObserver(ModelObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (ModelObserver o : observers) {
            o.onModelChanged();
        }
    }

    /**
     * 供 Mode 在直接操作物件屬性（如拖曳移動、resize）後手動觸發重繪通知。
     * 僅在 object 屬性已在外部改變、但不經過 UMLModel mutating 方法的情況下使用。
     */
    public void refresh() {
        notifyObservers();
    }

    // ======== 物件管理 ========

    public void addObject(UMLObject obj) {
        objects.add(obj);
        notifyObservers();
    }

    public void addConnectionLine(ConnectionLine line) {
        lines.add(line);
        notifyObservers();
    }

    public List<UMLObject> getObjects() {
        return objects;
    }

    public List<ConnectionLine> getLines() {
        return lines;
    }

    /**
     * 找到座標 (x, y) 上最頂層的物件（從 list 尾端往前找）。
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
     * 找到座標上最頂層的「基本物件」（穿透 Composite 找裡面的 Rect/Oval）。
     */
    public UMLObject getBasicObjectAt(int x, int y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof CompositeObject) {
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

    /**
     * 找到座標 (x, y) 所在的 Port（穿透 Composite 找基本物件的 port）。
     */
    public Port getPortAt(int x, int y) {
        for (int i = objects.size() - 1; i >= 0; i--) {
            UMLObject obj = objects.get(i);
            if (obj instanceof CompositeObject) {
                for (UMLObject basic : ((CompositeObject) obj).getAllBasicObjects()) {
                    Port p = basic.getPortAt(x, y);
                    if (p != null) return p;
                }
            } else {
                Port p = obj.getPortAt(x, y);
                if (p != null) return p;
            }
        }
        return null;
    }

    /** 將物件移到 list 最後面（繪製在最上層） */
    public void bringToFront(UMLObject obj) {
        if (objects.remove(obj)) {
            objects.add(obj);
        }
        notifyObservers();
    }

    // ======== 選取與 Hover 管理 ========

    /** 取消所有物件的選取狀態 */
    public void deselectAll() {
        for (UMLObject obj : objects) {
            obj.setSelected(false);
        }
        notifyObservers();
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
        notifyObservers();
    }

    public UMLObject getHoveredObject() {
        return hoveredObject;
    }

    /** 選取所有完全落在矩形 rect 內的物件 */
    public void selectObjectsInRect(Rectangle rect) {
        for (UMLObject obj : objects) {
            int ox = obj.getX(), oy = obj.getY();
            int ow = obj.getWidth(), oh = obj.getHeight();
            if (rect.contains(ox, oy) && rect.contains(ox + ow, oy + oh)) {
                obj.setSelected(true);
            }
        }
        notifyObservers();
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

    // ======== 群組操作 ========

    /** Group：將 >=2 個被選取的物件合併為一個 Composite */
    public void groupSelectedObjects() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.size() < 2) return;

        CompositeObject composite = new CompositeObject(selected);
        for (UMLObject obj : selected) {
            objects.remove(obj);
        }
        composite.setSelected(true);
        objects.add(composite);
        notifyObservers();
    }

    /** Ungroup：解構 1 個被選取的 Composite 的最外層 */
    public void ungroupSelectedObject() {
        List<UMLObject> selected = getSelectedObjects();
        if (selected.size() != 1) return;
        if (!(selected.get(0) instanceof CompositeObject)) return;

        CompositeObject composite = (CompositeObject) selected.get(0);
        objects.remove(composite);

        for (UMLObject child : composite.getChildren()) {
            child.setSelected(true);
            objects.add(child);
        }
        notifyObservers();
    }
}
