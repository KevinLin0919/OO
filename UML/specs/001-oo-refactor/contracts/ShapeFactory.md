# Contract: ShapeFactory

**Pattern**: Factory Method
**Package**: `oops.factory`

## Interface Definition

```java
public interface ShapeFactory {
    UMLObject create(int x, int y, int width, int height);
}
```

## Contract Rules

- `create()` 必須回傳非 null 的 `UMLObject` 實例
- 回傳的物件必須已完成 `initPorts()` 初始化（由 `UMLObject` 建構子保證）
- 傳入的 width / height 若小於 `UMLObject.MIN_SIZE`（20），由呼叫端保證合理值；工廠本身不做 clamp

## Implementations

| 類別 | 回傳類型 | 備註 |
|------|---------|------|
| `RectShapeFactory` | `RectObject` | 8 個 port |
| `OvalShapeFactory` | `OvalObject` | 4 個 port |

## Consumers

- `CreateObjectMode`（透過建構子注入）
