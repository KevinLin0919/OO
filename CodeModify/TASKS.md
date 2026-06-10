# Implementation Tasks

> 對應規格：[SPEC.md](SPEC.md)  
> 規則：不重構架構，只新增功能。每個 task 完成後標記 [x]。

---

## Phase 0 — 準備工作

- [ ] **T0-1** 複製圖示檔：將 `icon/association_line.jpg` 複製為 `icon/dependency_line.jpg`（作為暫時 icon）

---

## Phase 1 — Feature 1：DependencyLine（虛線箭頭）

- [ ] **T1-1** 建立 `Project/src/mod/instance/DependencyLine.java`
  - 繼承 JPanel，實作 IFuncComponent + ILinePainter
  - 欄位：`from/to/fromSide/toSide/fp/tp/arrowSize/panelExtendSize/isSelect/isHighlight/cph`
  - 方法：`paintComponent()` — 使用 `Graphics2D` + `BasicStroke` 畫虛線；`paintArrow()` — 開放三角箭頭；`setConnect()` / `renewConnect()` / `getConnectPoint()` / `reSize()` / `paintSelect()`
  - 一併加入 `isConnectedToPort(JPanel, int)` 和 `setHighlight(boolean)` / `isHighlight()`（Feature 4 用）

- [ ] **T1-2** 修改 `FuncPanelHandler.java`
  - `getIcon(6)` → `new ImageIcon("icon/dependency_line.jpg")`
  - `getFunc(6)` → `new DependencyLine(core.getCanvasPanelHandler())`

- [ ] **T1-3** 修改 `Core.java`
  - 新增 `isDependencyLine(Object obj)` → `obj instanceof DependencyLine`
  - `isLine()` 加入 DependencyLine → 回傳 `3`
  - `isFuncComponent()` 加入 DependencyLine → 回傳 `6`
  - 加入 `import mod.instance.DependencyLine`

- [ ] **T1-4** 修改 `CanvasPanelHandler.java` — 支援 DependencyLine
  - `ActionPerformed(DragPack dp)`：`case 1: case 2: case 3:` 改為 `case 1: case 2: case 3: case 6:`
  - `addLine()`：switch 加入 `case 3: ((DependencyLine) funcObj).setConnect(dPack); break;`
  - `setSelectAllType()`：加入 `case 6: ((DependencyLine) obj).setSelect(isSelect); break;`
  - 加入 `import mod.instance.DependencyLine`

---

## Phase 2 — Feature 2：Port 紅點提示

- [ ] **T2-1** 修改 `CPHActionListener.java`
  - `mouseMoved()` 補上：呼叫 `((CanvasPanelHandler) handler).setPortHoverPoint(e.getPoint())`

- [ ] **T2-2** 修改 `CanvasPanelHandler.java` — 啟用 MouseMotionListener
  - `initContextPanel()` 中：建立 listener 實例，同時呼叫 `addMouseListener` 和 `addMouseMotionListener`

- [ ] **T2-3** 修改 `CanvasPanelHandler.java` — 新增 portHoverPoint 欄位與方法
  - 新增欄位：`Point portHoverPoint = null`
  - 新增方法：`public void setPortHoverPoint(Point p)` → 設值後呼叫 `contextPanel.repaint()`

- [ ] **T2-4** 修改 `CanvasPanelHandler.java` — contextPanel 改為匿名 JPanel
  - `initContextPanel()` 中，`contextPanel = new JPanel()` 改為匿名子類，覆寫 `paintComponent(Graphics g)` 呼叫 `drawPortHints(g)`

- [ ] **T2-5** 修改 `CanvasPanelHandler.java` — 實作 drawPortHints()
  - 判斷目前是 line 模式（index 1/2/3/6）且 portHoverPoint 非 null
  - 計算所有 members 的 4 個 port 位置
  - 對距離 ≤ 10px 的 port 畫紅色實心圓（半徑 5px）

---

## Phase 3 — Feature 3：拖曳建立指定大小物件

- [ ] **T3-1** 修改 `BasicClass.java`
  - 新增欄位：`int customWidth = -1; int customHeight = -1;`
  - 新增方法：`public void setCustomSize(int width, int height)`
  - 修改 `reSize()`：若 customWidth/Height > 0 則使用自訂大小，否則走原有邏輯

- [ ] **T3-2** 修改 `UseCase.java`
  - 同 T3-1，新增 `customWidth/Height` 欄位、`setCustomSize()`、修改 `reSize()`

- [ ] **T3-3** 修改 `CanvasPanelHandler.java` — addObjectByDrag()
  - 新增方法 `void addObjectByDrag(JPanel funcObj, DragPack dp)`
  - 邏輯：from == to → return；計算 width/height（加 min 限制）；呼叫 setCustomSize；呼叫 addObject(funcObj, location)

- [ ] **T3-4** 修改 `CanvasPanelHandler.java` — ActionPerformed(DragPack) case 4/5
  - 將 `case 4: case 5: break;` 改為呼叫 `addObjectByDrag(core.getCurrentFunc(), dp)`

---

## Phase 4 — Feature 4：Select 模式點擊 Port Highlight 線

- [ ] **T4-1** 修改 `AssociationLine.java`
  - 新增 `isConnectedToPort(JPanel obj, int side)` 方法
  - 新增 `isHighlight` 欄位、`setHighlight(boolean)` / `isHighlight()` 方法
  - 修改 `paintComponent()`：`isHighlight` 為 true 時以藍色重繪線段

- [ ] **T4-2** 修改 `CompositionLine.java`（同 T4-1）

- [ ] **T4-3** 修改 `GeneralizationLine.java`（同 T4-1）

- [ ] **T4-4** 修改 `CanvasPanelHandler.java` — 新增輔助方法
  - `int getClickedPortSide(JPanel obj, Point click)` — 偵測點擊點是否在 port 範圍內，回傳 side 或 -1
  - `void clearAllLineHighlights()` — 清除 contextPanel 所有線的 highlight
  - `void highlightLinesConnectedTo(JPanel obj, int side)` — 遍歷 contextPanel components 找連接線並 highlight

- [ ] **T4-5** 修改 `CanvasPanelHandler.java` — 重構 selectByClick()
  - 在原有邏輯前：先清除所有 highlight
  - 偵測是否點擊到 port；若是 → highlight 連線並 return（不走物件選取）
  - 否則 → 執行原有物件選取邏輯

---

## Phase 5 — 整合驗證

- [ ] **T5-1** 編譯確認無錯誤（`javac` 或 IDE build）
- [ ] **T5-2** 執行程式，測試 Feature 1（Dependency Line 建立 + 視覺）
- [ ] **T5-3** 執行程式，測試 Feature 2（Port 紅點提示）
- [ ] **T5-4** 執行程式，測試 Feature 3（拖曳建立物件大小）
- [ ] **T5-5** 執行程式，測試 Feature 4（點擊 port highlight 線）
- [ ] **T5-6** 確認現有功能未損壞（Select/Group/Ungroup/現有三種 Line）

---

## 任務依賴關係

```
T0-1 ──► T1-1 ──► T1-2
                  T1-3
                  T1-4 ──┐
T2-1 ──────────────────── ► T5-x
T2-2, T2-3, T2-4, T2-5 ──┤
T3-1, T3-2, T3-3, T3-4 ──┤
T4-1, T4-2, T4-3 ─────────┤
T4-4, T4-5 ───────────────┘
```

> T1-1 中已含 Feature 4 需要的 `isConnectedToPort` 和 `setHighlight`，一次到位。
