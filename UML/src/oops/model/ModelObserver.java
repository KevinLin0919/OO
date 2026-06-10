package oops.model;

// Observer Pattern
/**
 * ModelObserver 是 Observer Pattern 中的觀察者介面。
 * Canvas 實作此介面，當 UMLModel 狀態改變時接收通知並重繪。
 */
public interface ModelObserver {
    void onModelChanged();
}
