# Feature 3 — 拖曳建立指定大小物件（Class / Use Case 模式）

## 需求描述

在建立 **BasicClass（index 4）** 或 **UseCase（index 5）** 的模式下，允許透過拖曳建立指定大小的物件。

---

## 行為規格

| 操作 | 行為 |
|------|------|
| **單純點擊**（起迄座標相同） | 建立**預設大小**物件（維持現有行為） |
| **拖曳**（起迄座標不同） | 建立以 press 座標為左上角、release 座標決定大小的物件 |

### 拖曳建立細節

- **起始點（左上角）：** `min(from.x, to.x), min(from.y, to.y)`
- **寬度：** `abs(to.x - from.x)`，若 < `MIN_DRAG_WIDTH` 則強制使用 `MIN_DRAG_WIDTH`
- **高度：** `abs(to.y - from.y)`，若 < `MIN_DRAG_HEIGHT` 則強制使用 `MIN_DRAG_HEIGHT`

### 最小尺寸限制

| 物件 | MIN_DRAG_WIDTH | MIN_DRAG_HEIGHT |
|------|----------------|-----------------|
| BasicClass | 100 px | 50 px |
| UseCase    | 100 px | 40 px |

> 數值可微調，但需確保物件不會太小導致文字無法顯示。

---

## 事件流分析

```
單純點擊：
  mousePressed  → from = point
  mouseReleased → to = point (相同位置)
    → ActionPerformed(DragPack)  → from==to → 不處理
  mouseClicked  → ActionPerformed(MouseEvent) → addObject(預設大小)  ✓

拖曳：
  mousePressed  → from = pressPoint
  mouseDragged  → （目前不處理）
  mouseReleased → to = releasePoint (不同位置)
    → ActionPerformed(DragPack)  → from≠to → addObjectByDrag(自訂大小)  ✓
  mouseClicked  → 不觸發（因為有拖曳）
```

> **關鍵：** Java Swing 的 `mouseClicked` 在有 `mouseDragged` 事件時**不觸發**，  
> 因此拖曳與點擊的事件路徑天然分離，不需額外判斷。

---

## 實作設計

### 1. 在 BasicClass 新增 setCustomSize()

```java
// BasicClass.java
int customWidth  = -1;
int customHeight = -1;

public void setCustomSize(int width, int height) {
    this.customWidth  = width;
    this.customHeight = height;
}

@Override
public void reSize() {
    if (customWidth > 0 && customHeight > 0) {
        this.setSize(customWidth, customHeight);
    } else {
        // 原有邏輯
        switch (texts.size()) {
            case 0:  this.setSize(defSize); break;
            default: this.setSize(defSize.width, defSize.height * texts.size()); break;
        }
    }
}
```

### 2. 在 UseCase 新增 setCustomSize()

```java
// UseCase.java
int customWidth  = -1;
int customHeight = -1;

public void setCustomSize(int width, int height) {
    this.customWidth  = width;
    this.customHeight = height;
}

@Override
public void reSize() {
    if (customWidth > 0 && customHeight > 0) {
        this.setSize(customWidth, customHeight);
    } else {
        // 原有邏輯
        switch (texts.size()) {
            case 0:  this.setSize(defSize); break;
            default: this.setSize(defSize.width, defSize.height); break;
        }
    }
}
```

### 3. CanvasPanelHandler — ActionPerformed(DragPack) 補充 case 4/5

```java
// 現有：
case 4:
case 5:
    break;   // 目前什麼都不做

// 改為：
case 4:
case 5:
    addObjectByDrag(core.getCurrentFunc(), dp);
    break;
```

### 4. 新增 addObjectByDrag() 方法

```java
void addObjectByDrag(JPanel funcObj, DragPack dp) {
    Point from = dp.getFrom();
    Point to   = dp.getTo();

    // 起迄相同 → 單純點擊，交由 mouseClicked 處理
    if (from.x == to.x && from.y == to.y) return;

    final int MIN_W = 100;
    final int MIN_H_CLASS = 50;
    final int MIN_H_CASE  = 40;

    int rawW = Math.abs(to.x - from.x);
    int rawH = Math.abs(to.y - from.y);

    if (funcObj instanceof BasicClass) {
        int w = Math.max(rawW, MIN_W);
        int h = Math.max(rawH, MIN_H_CLASS);
        ((BasicClass) funcObj).setCustomSize(w, h);
    } else if (funcObj instanceof UseCase) {
        int w = Math.max(rawW, MIN_W);
        int h = Math.max(rawH, MIN_H_CASE);
        ((UseCase) funcObj).setCustomSize(w, h);
    }

    Point location = new Point(Math.min(from.x, to.x), Math.min(from.y, to.y));
    addObject(funcObj, location);
}
```

---

## 影響的檔案與修改摘要

| 檔案 | 修改點 |
|------|--------|
| `mod/instance/BasicClass.java` | 新增 `customWidth/Height` 欄位、`setCustomSize()`；修改 `reSize()` |
| `mod/instance/UseCase.java` | 同上 |
| `bgWork/handler/CanvasPanelHandler.java` | `ActionPerformed(DragPack)` case 4/5 改呼叫 `addObjectByDrag()`；新增 `addObjectByDrag()` |

---

## 驗收標準

- [ ] 切換到 BasicClass 模式，單純點擊 → 建立 150×50 預設大小物件
- [ ] 切換到 BasicClass 模式，拖曳較大範圍 → 物件大小符合拖曳範圍
- [ ] 拖曳範圍小於最小值 → 以最小值建立，以 press 點為左上角
- [ ] 切換到 UseCase 模式，同上兩項測試
- [ ] 現有的點擊行為不受影響
