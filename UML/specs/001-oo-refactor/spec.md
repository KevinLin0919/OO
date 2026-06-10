# Feature Specification: OopsUMLEditor 物件導向重構

**Feature Branch**: `001-oo-refactor`
**Created**: 2026-06-10
**Status**: Draft
**Input**: User description: "OopsUMLEditor 期末重構：在保留所有現有功能的前提下，將現有的 Java Swing UML Editor 重構為清晰的物件導向設計。主要目標：(1) 抽出 UMLModel 實現 MVC 分層，讓 Canvas 只負責繪製；(2) 新增 ShapeFactory/LinkFactory 實現 Factory Pattern，消除 CreateObjectMode 和 CreateLinkMode 中的 if-else/switch；(3) 新增 ModelObserver 介面實現 Observer Pattern，讓 UMLModel 通知 Canvas 重繪；(4) 在程式碼和 Class Diagram 中明確標示 Strategy、Composite、Template Method、Factory Method、Observer、MVC 六種設計模式。功能維持不變：建立 Rect/Oval 物件、建立三種連線（Association/Generalization/Composition）、Select/Move/Resize、Group/Ungroup、Label 自訂。"

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - 功能完整保留（重構後所有功能正常運作）(Priority: P1)

使用者（學生 / 評分教師）在重構後的版本中，能夠執行與期中版本完全一致的所有操作，不出現任何功能退化或操作異常。

**Why this priority**: 評分標準第一項即為功能完整度，重構若破壞任何既有功能等同直接失分，且此 story 是其他所有 story 的驗收基準。

**Independent Test**: 啟動程式，依序執行建立 Rect/Oval、拖曳三種連線、單選/框選/移動/Resize、Group/Ungroup、Edit > Label，每個操作均正常完成即通過。

**Acceptance Scenarios**:

1. **Given** 程式啟動、畫布空白，**When** 拖曳 Rect 按鈕到畫布，**Then** 矩形物件出現在放開位置；點擊 Rect 按鈕後在畫布按下拖曳，**Then** 以拖曳範圍大小建立矩形（兩種建立路徑均有效）。
2. **Given** 兩個基本物件存在於畫布，**When** 在 Association 模式下從一個物件的 port 拖曳到另一個物件的 port，**Then** 帶有 V 形箭頭的連線正確繪製；Generalization 顯示空心三角；Composition 顯示空心菱形。
3. **Given** 多個物件被框選，**When** 點擊 Edit > Group，**Then** 合併為 Composite 並顯示藍色虛線外框；再點擊 Edit > Ungroup，**Then** 恢復為各自獨立物件。
4. **Given** 單一基本物件被選取，**When** 點擊 Edit > Label，輸入名稱並選取顏色後按 OK，**Then** 物件顯示對應文字與背景色。
5. **Given** 物件被選取，**When** 拖曳角落或邊緣的 port，**Then** 物件大小隨之調整，最小不低於 20px。

---

### User Story 2 - MVC 分層（Canvas 只負責繪製，業務邏輯移至 UMLModel）(Priority: P1)

開發者（評分教師）審閱程式碼時，能夠清楚看到 `Canvas` 不包含任何業務邏輯，所有物件/連線的資料管理與操作邏輯集中在獨立的 `UMLModel` 類別中。

**Why this priority**: MVC 是本次期末評分的核心 OO 原則之一，且是 Factory Pattern 與 Observer Pattern 的架構基礎；若此層未分離，其他模式無法正確接入。

**Independent Test**: 閱讀 `Canvas.java`，確認不含 `List<UMLObject>`、`List<ConnectionLine>` 欄位宣告，以及 `groupSelectedObjects()`、`ungroupSelectedObject()`、`selectObjectsInRect()` 等業務方法；所有功能仍正常運作。

**Acceptance Scenarios**:

1. **Given** 重構後的 `Canvas.java`，**When** 審閱所有欄位宣告，**Then** 不存在 `List<UMLObject> objects` 或 `List<ConnectionLine> lines`；改為持有 `UMLModel` 的參照。
2. **Given** 重構後的 `Canvas.java`，**When** 審閱所有方法，**Then** 不存在 `groupSelectedObjects()`、`ungroupSelectedObject()`、`deselectAll()`、`selectObjectsInRect()`、`getObjectAt()` 等方法；這些方法全數屬於 `UMLModel`。
3. **Given** `UMLModel.java`，**When** 審閱所有 import，**Then** 不存在任何 `javax.swing` 或 `java.awt` 的 GUI 相關 import，Model 層與 View 層完全解耦。

---

### User Story 3 - Factory Pattern（物件與連線建立透過 Factory 介面）(Priority: P2)

開發者（評分教師）審閱程式碼時，能夠看到 `CreateObjectMode` 和 `CreateLinkMode` 不再直接 `new` 具體類別，而是透過注入的 Factory 介面建立物件，符合 Open-Closed Principle 與 Dependency Inversion Principle。

**Why this priority**: Factory Pattern 是本次重構最明顯可見的 Design Pattern 之一，直接消除 `if-else` 與 `switch` 這兩個 bad smell，體現課程 CH14/15 的核心概念。

**Independent Test**: 閱讀 `CreateObjectMode.java` 與 `CreateLinkMode.java`，確認不含直接 `new RectObject`/`new OvalObject`/`new AssociationLine` 等具體類別實體化；程式仍正常建立所有類型的物件與連線。

**Acceptance Scenarios**:

1. **Given** `CreateObjectMode.java`，**When** 審閱程式碼，**Then** 不含 `if (isRect)` 判斷或直接 `new RectObject()`/`new OvalObject()`；改為呼叫注入的 `ShapeFactory.create(x, y, w, h)`。
2. **Given** `CreateLinkMode.java`，**When** 審閱程式碼，**Then** 不含根據連線類型的 `switch` 或 `if-else`；改為呼叫注入的 `LinkFactory.create(src, srcPort, dest, destPort)`。
3. **Given** `ToolPanel.java`，**When** 審閱建立按鈕的程式碼，**Then** 建立 Rect/Oval 按鈕時分別注入 `RectShapeFactory`/`OvalShapeFactory`；建立連線按鈕時分別注入對應的 `LinkFactory` 具體實作。

---

### User Story 4 - Observer Pattern（UMLModel 狀態改變時通知 Canvas 重繪）(Priority: P2)

開發者（評分教師）審閱程式碼時，能夠看到 `UMLModel` 透過 `ModelObserver` 介面通知 `Canvas` 更新畫面，而非在各處直接呼叫 `canvas.repaint()`，符合 MVC 中 Model 與 View 解耦的要求。

**Why this priority**: Observer Pattern 是課程 CH14 明確指出的 MVC 內部機制（Model-View 之間的關係），也是讓 `UMLModel` 與 `javax.swing` 完全解耦的關鍵設計。

**Independent Test**: 閱讀 `UMLModel.java`，確認不含 `repaint()` 呼叫；在操作任何物件（新增、移動、群組）後，畫面仍即時更新。

**Acceptance Scenarios**:

1. **Given** `UMLModel.java`，**When** 審閱所有方法，**Then** 不含任何 `canvas.repaint()` 或直接操作 View 的呼叫；每個 mutating 方法（`addObject`、`addConnectionLine`、`groupSelectedObjects` 等）結尾呼叫 `notifyObservers()`。
2. **Given** `Canvas` 實作 `ModelObserver`，**When** `UMLModel.notifyObservers()` 被呼叫，**Then** `Canvas.onModelChanged()` 被觸發並執行 `repaint()`。
3. **Given** 使用者移動物件，**When** 拖曳期間，**Then** 畫面持續更新反映最新位置，視覺行為與重構前相同。

---

### User Story 5 - 設計模式標示（程式碼與 Class Diagram 對應）(Priority: P3)

評分教師審閱程式碼與 Class Diagram 時，能夠清楚辨識六種設計模式（Strategy、Composite、Template Method、Factory Method、Observer、MVC）的應用位置，且 Class Diagram 完整反映程式碼結構。

**Why this priority**: 評分標準第 4 項為「程式碼與設計對應程度」，Class Diagram 若無法對應程式結構，或程式碼中模式不清晰，均會失分。

**Independent Test**: 對照 Class Diagram 與程式碼，確認每個模式的參與類別、介面與關係都能在兩者中找到對應；程式碼中每個模式的核心類別都有標示採用的模式名稱。

**Acceptance Scenarios**:

1. **Given** 最終程式碼，**When** 審閱各核心類別的 Javadoc 或類別上方的單行註解，**Then** Strategy Pattern（`Mode` 及其實作）、Composite Pattern（`UMLObject`/`CompositeObject`）、Template Method（`ConnectionLine`/`UMLObject`）、Factory Method（`ShapeFactory`/`LinkFactory` 及具體實作）、Observer（`ModelObserver`/`UMLModel`/`Canvas`）均有對應的模式標示。
2. **Given** Class Diagram，**When** 對照程式碼中的類別與介面，**Then** 所有類別、介面、繼承關係、組合關係均能一一對應，不存在 Diagram 有但程式碼沒有、或程式碼有但 Diagram 沒有的項目。

---

### Edge Cases

- Composite 物件內的基本物件仍可被連線連接（穿透 Composite 找 port 的邏輯必須保留）。
- Resize 時反向拖曳（cross-drag）不崩潰，且有最小尺寸（20px）限制。
- Group 操作需至少選取 2 個物件；選取不足時不執行任何動作。
- Ungroup 操作需恰好選取 1 個 Composite；否則不執行任何動作。
- Label 對話框只對單一基本物件（非 Composite）有效；若選取 Composite 或多個物件，不顯示對話框。
- 連線兩端必須是不同的基本物件（不能自連）；放開時若起終點相同物件，不建立連線。
- `UMLModel.notifyObservers()` 若 observer 清單為空，不應拋出例外。

---

## Requirements *(mandatory)*

### Functional Requirements

#### MVC 架構與 Observer

- **FR-001**: 系統必須建立獨立的 `UMLModel` 類別，集中持有所有 `UMLObject` 與 `ConnectionLine` 的資料集合，以及全部的業務邏輯操作（新增物件/連線、群組、解群組、選取管理、hover 管理、物件查詢）。
- **FR-002**: `Canvas` 必須移除 `List<UMLObject>` 與 `List<ConnectionLine>` 欄位，改為持有 `UMLModel` 的參照，自身只保留繪製邏輯與滑鼠事件轉發。
- **FR-003**: 系統必須定義 `ModelObserver` 介面，包含 `onModelChanged()` 方法；`Canvas` 必須實作此介面。
- **FR-004**: `UMLModel` 必須維護 `ModelObserver` 的訂閱清單，每次狀態改變後呼叫 `notifyObservers()`；`UMLModel` 的程式碼中不得出現任何 `javax.swing` 或 GUI 相關的呼叫。

#### Factory Pattern

- **FR-005**: 系統必須定義 `ShapeFactory` 介面，包含 `create(int x, int y, int width, int height): UMLObject` 方法；並提供 `RectShapeFactory`、`OvalShapeFactory` 兩個具體實作。
- **FR-006**: 系統必須定義 `LinkFactory` 介面，包含 `create(UMLObject src, Port srcPort, UMLObject dest, Port destPort): ConnectionLine` 方法；並提供 `AssociationLinkFactory`、`GeneralizationLinkFactory`、`CompositionLinkFactory` 三個具體實作。
- **FR-007**: `CreateObjectMode` 必須透過注入的 `ShapeFactory` 建立物件，不得直接實體化 `RectObject` 或 `OvalObject`。
- **FR-008**: `CreateLinkMode` 必須透過注入的 `LinkFactory` 建立連線，不得含有根據連線類型分支的 `switch` 或 `if-else`。
- **FR-009**: `ToolPanel` 建立各模式按鈕時，必須注入對應的具體 Factory 實例至 `CreateObjectMode` 和 `CreateLinkMode`。

#### 設計模式標示

- **FR-010**: 程式碼中 Strategy、Composite、Template Method、Factory Method、Observer 五種模式的核心類別或介面，必須在類別宣告上方以單行註解標示所採用的設計模式名稱。
- **FR-011**: 專案必須附上 UML Class Diagram，完整涵蓋所有類別、介面及其關係，且與最終提交的程式碼版本完全對應。

#### 功能保留

- **FR-012**: 建立 Rect 與 Oval 物件的兩種路徑（拖曳按鈕到畫布、點擊按鈕後在畫布拖曳）均必須正常運作。
- **FR-013**: 在 Association、Generalization、Composition 三種模式下，從一個基本物件的 port 拖曳至另一基本物件的 port，必須正確建立對應樣式的連線。
- **FR-014**: Select 模式下，單選、框選、多物件移動、單物件 Resize（含最小尺寸 20px 限制）均必須正常運作。
- **FR-015**: Group（≥2 個選取物件）與 Ungroup（1 個選取的 Composite）功能必須正常運作。
- **FR-016**: Edit > Label 對話框（名稱輸入 + 顏色選取）針對單一基本物件必須正常運作。

### Key Entities

- **UMLModel**: 資料模型核心，持有物件清單、連線清單、observer 清單；負責所有業務邏輯；與 View 層完全解耦。
- **ModelObserver**: 觀察者介面，定義 `onModelChanged()`；由 `Canvas` 實作，被 `UMLModel` 呼叫。
- **ShapeFactory**: 物件建立介面；具體實作：`RectShapeFactory`、`OvalShapeFactory`。
- **LinkFactory**: 連線建立介面；具體實作：`AssociationLinkFactory`、`GeneralizationLinkFactory`、`CompositionLinkFactory`。
- **Canvas**: 精簡後的 View，持有 `UMLModel` 參照，實作 `ModelObserver`，負責繪製與滑鼠事件轉發。
- **UMLObject / CompositeObject**: 物件抽象層（Composite Pattern + Template Method）；不變動結構。
- **ConnectionLine / 子類別**: 連線抽象層（Template Method）；不變動結構。
- **Mode / 子類別**: 互動模式層（Strategy Pattern）；`CreateObjectMode`、`CreateLinkMode` 內部改用 Factory 注入。

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 重構後所有原有功能（建立物件與連線、選取、移動、Resize、Group/Ungroup、Label）均可正常操作，與重構前行為一致，無功能退化。
- **SC-002**: `Canvas` 原始碼中不含任何業務邏輯方法（group、ungroup、select、getObjectAt 等）；所有業務邏輯方法 100% 集中於 `UMLModel`。
- **SC-003**: `CreateObjectMode` 與 `CreateLinkMode` 中，具體物件/連線類別的直接 `new` 呼叫數量為零，改由 Factory 介面建立。
- **SC-004**: `UMLModel` 原始碼中，`javax.swing` 相關的 import 與方法呼叫數量為零。
- **SC-005**: Class Diagram 涵蓋程式碼中所有類別與介面，六種設計模式在圖中的參與類別與關係清晰可辨，與程式碼 100% 對應。
- **SC-006**: 程式碼中 Strategy、Composite、Template Method、Factory Method、Observer 五種模式的核心類別各有至少一處模式名稱標示。

---

## Assumptions

- 本專案為單人學生作業，不需考慮多執行緒並發問題，`UMLModel` 無需執行緒安全設計。
- UI 外觀、互動行為與使用者操作路徑維持與期中版本完全一致，本次重構不新增任何功能。
- Singleton Pattern 不強制引入（Canvas/Model 雖自然唯一，但對本評分標準貢獻有限，不值得引入額外複雜度）。
- Decorator Pattern 不引入（Label 功能已足夠簡單，強加 Decorator 反而降低程式碼可讀性）。
- Class Diagram 以 PlantUML 語法或手繪方式提交，需與最終提交程式碼版本一致。
- `oops.factory` 為新增 package，用於存放所有 Factory 介面與具體實作；`ModelObserver` 介面置於 `oops.model` package。
