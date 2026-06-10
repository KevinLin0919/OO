# Data Model: OopsUMLEditor 物件導向重構

**Phase**: 1 | **Date**: 2026-06-10 | **Feature**: 001-oo-refactor

---

## 類別結構總覽（重構後）

```
oops
├── UMLEditor           (Controller / JFrame)
├── Canvas              (View / JPanel / ModelObserver) ← 大幅精簡
└── ToolPanel           (View / JPanel) ← 小幅修改

oops.model
├── ModelObserver       ★NEW interface
├── UMLModel            ★NEW class
├── UMLObject           abstract (Template Method)
│   ├── RectObject
│   ├── OvalObject
│   └── CompositeObject (Composite Pattern)
├── ConnectionLine      abstract (Template Method)
│   ├── AssociationLine
│   ├── GeneralizationLine
│   └── CompositionLine
└── Port

oops.factory
├── ShapeFactory        ★NEW interface (Factory Pattern)
│   ├── RectShapeFactory
│   └── OvalShapeFactory
└── LinkFactory         ★NEW interface (Factory Pattern)
    ├── AssociationLinkFactory
    ├── GeneralizationLinkFactory
    └── CompositionLinkFactory

oops.mode
├── Mode                interface (Strategy Pattern)
├── SelectMode          ← 改用 canvas.getModel() 存取資料
├── CreateObjectMode    ← 改吃 ShapeFactory 注入
└── CreateLinkMode      ← 改吃 LinkFactory 注入
```

---

## 新增類別詳細設計

### `oops.model.ModelObserver`（介面）

```
介面：ModelObserver
方法：
  + onModelChanged() : void
```

實作者：`Canvas`
呼叫者：`UMLModel.notifyObservers()`

---

### `oops.model.UMLModel`

```
類別：UMLModel
欄位（private）：
  - objects  : List<UMLObject>
  - lines    : List<ConnectionLine>
  - observers : List<ModelObserver>
  - hoveredObject : UMLObject

方法（public）：
  # 物件管理
  + addObject(obj: UMLObject) : void
  + addConnectionLine(line: ConnectionLine) : void
  + getObjects() : List<UMLObject>
  + getLines() : List<ConnectionLine>

  # 查詢
  + getObjectAt(x: int, y: int) : UMLObject
  + getBasicObjectAt(x: int, y: int) : UMLObject
  + getPortAt(x: int, y: int) : Port
  + getSelectedObjects() : List<UMLObject>

  # 選取與 hover 管理
  + deselectAll() : void
  + selectObjectsInRect(rect: Rectangle) : void
  + setHoveredObject(obj: UMLObject) : void
  + getHoveredObject() : UMLObject
  + bringToFront(obj: UMLObject) : void

  # 業務邏輯
  + groupSelectedObjects() : void
  + ungroupSelectedObject() : void

  # Observer
  + addObserver(o: ModelObserver) : void
  - notifyObservers() : void   ← 所有 mutating 方法結尾呼叫
```

**不變動規則**：`UMLModel` 中不得出現任何 `javax.swing.*` 或 `java.awt.*` GUI import。

---

### `oops.factory.ShapeFactory`（介面）

```
介面：ShapeFactory
方法：
  + create(x: int, y: int, width: int, height: int) : UMLObject
```

具體實作：

| 類別 | create() 回傳 |
|------|--------------|
| `RectShapeFactory` | `new RectObject(x, y, w, h)` |
| `OvalShapeFactory` | `new OvalObject(x, y, w, h)` |

---

### `oops.factory.LinkFactory`（介面）

```
介面：LinkFactory
方法：
  + create(src: UMLObject, srcPort: Port,
           dest: UMLObject, destPort: Port) : ConnectionLine
```

具體實作：

| 類別 | create() 回傳 |
|------|--------------|
| `AssociationLinkFactory` | `new AssociationLine(...)` |
| `GeneralizationLinkFactory` | `new GeneralizationLine(...)` |
| `CompositionLinkFactory` | `new CompositionLine(...)` |

---

## 修改後類別詳細設計

### `Canvas`（精簡後）

**移除**：
- `List<UMLObject> objects`
- `List<ConnectionLine> lines`
- `UMLObject hoveredObject`
- `addObject()`, `addConnectionLine()`, `getObjectAt()`, `getBasicObjectAt()`, `getPortAt()`
- `bringToFront()`, `deselectAll()`, `setHoveredObject()`, `getHoveredObject()`
- `selectObjectsInRect()`, `getSelectedObjects()`
- `groupSelectedObjects()`, `ungroupSelectedObject()`

**新增**：
- `UMLModel model` 欄位
- `implements ModelObserver`
- `onModelChanged()` → 呼叫 `repaint()`
- `getModel(): UMLModel` → 供 Mode 存取

**保留**：
- `paintComponent(Graphics g)`（改從 `model` 讀取資料）
- `Mode currentMode` 欄位與滑鼠事件委派
- `setMode()`, `getMode()`

---

### `CreateObjectMode`（修改後）

**移除**：`boolean isRect` 欄位

**新增**：`ShapeFactory factory` 欄位（建構子注入）

**建構子**：`CreateObjectMode(ShapeFactory factory, Runnable onCreated)`

**`mouseReleased` 中的建立邏輯**：
```
// 舊：
UMLObject obj = isRect ? new RectObject(x, y, w, h) : new OvalObject(x, y, w, h);
// 新：
UMLObject obj = factory.create(x, y, w, h);
```

---

### `CreateLinkMode`（修改後）

**移除**：`LinkType linkType` 欄位與 `createLine()` 中的 `switch`

**新增**：`LinkFactory factory` 欄位（建構子注入）

**建構子**：`CreateLinkMode(LinkFactory factory)`

**`mouseReleased` 中的建立邏輯**：
```
// 舊：
ConnectionLine line = createLine(sourceObject, sourcePort, destPort.getOwner(), destPort);
// （createLine 內有 switch）
// 新：
ConnectionLine line = factory.create(sourceObject, sourcePort, destPort.getOwner(), destPort);
```

---

### `ToolPanel`（修改後）

建立 Rect/Oval 按鈕時改為：
```java
createObjectButton("Rect", "rect", new RectShapeFactory());
createObjectButton("Oval", "oval", new OvalShapeFactory());
```

建立連線按鈕時改為：
```java
createModeButton("Association",    ..., () -> new CreateLinkMode(new AssociationLinkFactory()));
createModeButton("Generalization", ..., () -> new CreateLinkMode(new GeneralizationLinkFactory()));
createModeButton("Composition",    ..., () -> new CreateLinkMode(new CompositionLinkFactory()));
```

---

## 設計模式對應表

| 模式 | 關鍵類別/介面 | 標示位置 |
|------|-------------|---------|
| Strategy Pattern | `Mode`（介面）+ 三個 Mode 實作 | `Mode.java` 類別宣告上方 |
| Composite Pattern | `UMLObject`（抽象）+ `CompositeObject` | `UMLObject.java`、`CompositeObject.java` 上方 |
| Template Method Pattern | `UMLObject.draw()`/`contains()`/`initPorts()`；`ConnectionLine.draw()`/`drawArrowHead()` | `UMLObject.java`、`ConnectionLine.java` 上方 |
| Factory Method Pattern | `ShapeFactory`（介面）+ 兩具體工廠；`LinkFactory`（介面）+ 三具體工廠 | 各 Factory 介面/類別上方 |
| Observer Pattern | `ModelObserver`（介面）+ `UMLModel`（Subject）+ `Canvas`（Observer） | `ModelObserver.java`、`UMLModel.java`、`Canvas.java` 上方 |
| MVC | `UMLModel`（M）、`Canvas`（V）、`Mode + UMLEditor`（C） | 各類別 Javadoc 中說明角色 |
