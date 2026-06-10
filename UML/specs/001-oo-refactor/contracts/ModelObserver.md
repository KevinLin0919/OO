# Contract: ModelObserver

**Pattern**: Observer
**Package**: `oops.model`

## Interface Definition

```java
public interface ModelObserver {
    void onModelChanged();
}
```

## Contract Rules

- `onModelChanged()` 由 `UMLModel.notifyObservers()` 在每次狀態改變後呼叫
- 實作者（`Canvas`）收到通知後應呼叫 `repaint()` 觸發重繪
- `UMLModel` 不得持有任何具體的 `Canvas` 或 `javax.swing` 型別參照，只能透過此介面通知

## Trigger Events（`UMLModel` 中會觸發通知的方法）

- `addObject()`
- `addConnectionLine()`
- `bringToFront()`
- `deselectAll()`
- `selectObjectsInRect()`
- `setHoveredObject()`
- `groupSelectedObjects()`
- `ungroupSelectedObject()`

## Implementations

| 類別 | 行為 |
|------|------|
| `Canvas` | 呼叫 `repaint()` |

## Subject

- `UMLModel`（維護 `List<ModelObserver> observers`）
