# Tasks: OopsUMLEditor 物件導向重構

**Input**: Design documents from `/specs/001-oo-refactor/`
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ contracts/ ✅

**Organization**: 依 User Story 分 Phase，每個 Phase 完成後可獨立驗收。

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可平行執行（不同檔案、無依賴）
- **[Story]**: 對應的 User Story（US2=MVC、US3=Factory、US4=Observer、US5=標示）
- US1（功能完整性）為跨 Phase 驗收標準，在 Polish Phase 集中確認

---

## Phase 1: Setup（基準確認）

**Purpose**: 確認現有程式碼在任何修改前可正常編譯與執行，建立 baseline。

- [X] T001 執行 `build.bat` 確認現有程式碼編譯成功，執行 `run.bat` 確認所有功能正常（Rect/Oval 建立、三種連線、Select/Move/Resize、Group/Ungroup、Label）

**Checkpoint**: Baseline 確認通過，可開始重構

---

## Phase 2: Foundational（新介面與 Model 骨架）

**Purpose**: 建立 `ModelObserver` 介面與 `UMLModel` 類別，是所有後續 Phase 的前提條件。

**⚠️ CRITICAL**: 此 Phase 完成前，所有 User Story 的實作均無法開始

- [X] T002 [P] [US4] 建立 `src/oops/model/ModelObserver.java`：`public interface ModelObserver { void onModelChanged(); }`，類別宣告上方加 `// Observer Pattern` 標示
- [X] T003 [P] [US2] 建立 `src/oops/model/UMLModel.java`：宣告所有欄位（`List<UMLObject> objects`、`List<ConnectionLine> lines`、`List<ModelObserver> observers`、`UMLObject hoveredObject`）及全部 public 方法骨架（方法體暫留空或直接從 `Canvas.java` 複製對應實作）。需包含的方法：`addObject`、`addConnectionLine`、`getObjects`、`getLines`、`getObjectAt`、`getBasicObjectAt`、`getPortAt`、`bringToFront`、`deselectAll`、`setHoveredObject`、`getHoveredObject`、`selectObjectsInRect`、`getSelectedObjects`、`groupSelectedObjects`、`ungroupSelectedObject`、`addObserver`、`notifyObservers`（private）。類別宣告上方加 `// MVC - Model` 標示

**Checkpoint**: 兩個新檔案可獨立編譯（`javac src/oops/model/UMLModel.java src/oops/model/ModelObserver.java -cp out -d out`）

---

## Phase 3: User Story 2 + 4（P1+P2）— MVC 分層與 Observer 串接 🎯 MVP

**Goal**: Canvas 只負責繪製，所有業務邏輯移至 UMLModel；UMLModel 透過 ModelObserver 通知 Canvas 重繪。

**Independent Test**: 執行程式後，所有功能正常；審閱 `Canvas.java` 確認不含 `List<UMLObject>`、`List<ConnectionLine>` 欄位，也不含 `groupSelectedObjects` 等業務方法；審閱 `UMLModel.java` 確認無任何 `javax.swing` import。

- [X] T004 [US2] 更新 `src/oops/Canvas.java`：（a）新增 `private UMLModel model` 欄位；（b）建構子改為 `Canvas(UMLModel model)`，儲存 model 並呼叫 `model.addObserver(this)`；（c）宣告 `implements ModelObserver`，實作 `onModelChanged() { repaint(); }`；（d）新增 `public UMLModel getModel() { return model; }`；（e）更新 `paintComponent`，改從 `model.getObjects()` 和 `model.getLines()` 讀取資料繪製
- [X] T005 [US2] 更新 `src/oops/Canvas.java`：移除所有舊欄位（`objects`、`lines`、`hoveredObject`）及所有業務邏輯方法（`addObject`、`addConnectionLine`、`getObjectAt`、`getBasicObjectAt`、`getPortAt`、`bringToFront`、`deselectAll`、`setHoveredObject`、`getHoveredObject`、`selectObjectsInRect`、`getSelectedObjects`、`groupSelectedObjects`、`ungroupSelectedObject`）；Canvas 類別宣告上方加 `// MVC - View` 標示（依賴 T004）
- [X] T006 [US2] 更新 `src/oops/mode/SelectMode.java`：將所有直接呼叫 `canvas.*()` 業務方法的地方改為 `canvas.getModel().*()` 形式（例如 `canvas.getObjects()` → `canvas.getModel().getObjects()`、`canvas.deselectAll()` → `canvas.getModel().deselectAll()` 等），並在類別宣告上方加 `// Strategy Pattern` 標示（依賴 T005）
- [X] T007 [US2] 更新 `src/oops/mode/CreateLinkMode.java`：將 `canvas.addConnectionLine(line)` 改為 `canvas.getModel().addConnectionLine(line)`；更新 `canvas.setHoveredObject()` 改為 `canvas.getModel().setHoveredObject()`（依賴 T005）
- [X] T008 [US2] 更新 `src/oops/UMLEditor.java`：`showLabelDialog()` 中取得選取物件改為 `canvas.getModel().getSelectedObjects()`；更新 `UMLModel` 中所有 mutating 方法（`addObject`、`addConnectionLine`、`bringToFront`、`deselectAll`、`selectObjectsInRect`、`setHoveredObject`、`groupSelectedObjects`、`ungroupSelectedObject`）的實作，確保每個方法最後呼叫 `notifyObservers()`；驗證 `UMLModel` 無任何 `javax.swing` import（依賴 T005）
- [X] T009 [US2] 更新 `src/oops/ToolPanel.java`：建構子中 `new Canvas()` 改為先 `new UMLModel()` 再 `new Canvas(model)`；或改由 `UMLEditor` 建立 model 後傳入（依據 plan.md 中 UMLEditor 為 Controller 的角色，建議在 UMLEditor 建構子中建立 `UMLModel model = new UMLModel()`，再傳給 Canvas 和 ToolPanel）（依賴 T005）

**Checkpoint**: 執行 `build.bat` + `run.bat`，所有功能與重構前相同；`Canvas.java` 無業務邏輯；`UMLModel.java` 無 GUI import

---

## Phase 4: User Story 3（P2）— Factory Pattern

**Goal**: 消除 `CreateObjectMode` 的 `boolean isRect` 判斷和 `CreateLinkMode` 的 `switch`，改由注入的 Factory 介面建立物件與連線。

**Independent Test**: 執行程式後三種連線與兩種物件均可正常建立；審閱 `CreateObjectMode.java` 不含 `new RectObject`/`new OvalObject`；審閱 `CreateLinkMode.java` 不含 `switch(linkType)`。

- [ ] T010 [P] [US3] 建立 `src/oops/factory/ShapeFactory.java`（介面，含 `UMLObject create(int x, int y, int width, int height)`，加 `// Factory Method Pattern` 標示）；建立 `src/oops/factory/RectShapeFactory.java`（實作，`return new RectObject(x, y, width, height)`）；建立 `src/oops/factory/OvalShapeFactory.java`（實作，`return new OvalObject(x, y, width, height)`）
- [ ] T011 [P] [US3] 建立 `src/oops/factory/LinkFactory.java`（介面，含 `ConnectionLine create(UMLObject src, Port srcPort, UMLObject dest, Port destPort)`，加 `// Factory Method Pattern` 標示）；建立 `src/oops/factory/AssociationLinkFactory.java`、`GeneralizationLinkFactory.java`、`CompositionLinkFactory.java`（各自回傳對應連線實例）
- [ ] T012 [US3] 更新 `src/oops/mode/CreateObjectMode.java`：將 `boolean isRect` 欄位替換為 `ShapeFactory factory` 欄位；建構子改為 `CreateObjectMode(ShapeFactory factory, Runnable onCreated)`；`mouseReleased` 中的物件建立改為 `factory.create(x, y, w, h)`（依賴 T010）
- [ ] T013 [US3] 更新 `src/oops/mode/CreateLinkMode.java`：移除 `LinkType` enum 欄位與 `createLine()` 方法（含 switch）；新增 `LinkFactory factory` 欄位；建構子改為 `CreateLinkMode(LinkFactory factory)`；`mouseReleased` 中改為 `factory.create(sourceObject, sourcePort, destPort.getOwner(), destPort)`（依賴 T011）
- [ ] T014 [US3] 更新 `src/oops/ToolPanel.java`：`createObjectButton("Rect", ...)` 傳入 `new RectShapeFactory()`；`createObjectButton("Oval", ...)` 傳入 `new OvalShapeFactory()`；三個連線按鈕分別傳入 `new AssociationLinkFactory()`、`new GeneralizationLinkFactory()`、`new CompositionLinkFactory()`（依賴 T012、T013）

**Checkpoint**: 執行 `build.bat` + `run.bat`，三種連線與兩種物件正常建立；`CreateObjectMode` 無直接 `new` 具體物件；`CreateLinkMode` 無 `switch`

---

## Phase 5: User Story 5（P3）— 設計模式標示與 Class Diagram

**Goal**: 程式碼中六種設計模式清晰可辨；Class Diagram 完整對應程式碼結構。

**Independent Test**: 審閱所有核心類別，各模式標示存在；對照 Class Diagram 與程式碼，所有類別/介面/關係一一對應。

- [ ] T015 [P] [US5] 補齊所有尚未加模式標示的核心類別（逐一確認）：`src/oops/model/UMLObject.java` 加 `// Template Method Pattern (draw, contains, initPorts)`；`src/oops/model/CompositeObject.java` 加 `// Composite Pattern`；`src/oops/model/ConnectionLine.java` 加 `// Template Method Pattern (drawArrowHead)`；`src/oops/Canvas.java` 加 `// MVC - View / Observer Pattern (implements ModelObserver)`；`src/oops/mode/Mode.java` 加 `// Strategy Pattern`；`src/oops/mode/CreateObjectMode.java` 加 `// Strategy Pattern`；`src/oops/mode/CreateLinkMode.java` 加 `// Strategy Pattern`
- [ ] T016 [US5] 更新 `OopsUMLEditor_ClassDiagram.puml`（或對應的 Class Diagram 原始檔）：繪製重構後完整架構，標示六種設計模式參與類別，確保所有新增類別（`UMLModel`、`ModelObserver`、`ShapeFactory`、`OvalShapeFactory`、`RectShapeFactory`、`LinkFactory`、`AssociationLinkFactory`、`GeneralizationLinkFactory`、`CompositionLinkFactory`）及修改後的 `Canvas`、`CreateObjectMode`、`CreateLinkMode` 均出現在圖中且關係正確

**Checkpoint**: Class Diagram 可與程式碼逐一對照，無遺漏類別或錯誤關係

---

## Phase 6: Polish & US1 驗收（功能完整性確認）

**Purpose**: 整合驗收所有功能，確認無退化，程式碼可讀性最終確認。

- [ ] T017 執行 `build.bat` 確認零錯誤編譯；逐項操作 `quickstart.md` 驗收清單（Rect/Oval 兩種建立路徑、三種連線、框選/單選、移動、Resize、Group/Ungroup、Label 名稱與顏色），全數通過即 US1 驗收完成
- [ ] T018 [P] 程式碼可讀性最終確認：移除不必要的舊 `// TODO`/廢棄 import；確認所有 Javadoc 與類別說明符合重構後的職責描述；確認 `UMLModel.java` 無任何 `javax.swing` 或 `Canvas` 相關 import

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: 無前提，立即可開始
- **Phase 2 (Foundational)**: 需 Phase 1 通過 → 建立新介面/類別
- **Phase 3 (US2+US4)**: 需 Phase 2 完成 → 大幅修改現有類別
- **Phase 4 (US3)**: 需 Phase 3 完成（Canvas/Mode 已改好）→ 加入 Factory
- **Phase 5 (US5)**: 需 Phase 4 完成（最終程式碼穩定）→ 加標示、畫 Diagram
- **Phase 6 (Polish)**: 需 Phase 5 完成 → 整合驗收

### User Story Dependencies

- **US2 + US4**: Phase 3 實作（深度耦合，同時完成）
- **US3**: Phase 4 實作，依賴 US2 完成後的 Canvas/Mode 結構
- **US5**: Phase 5 實作，依賴所有程式碼穩定
- **US1**: Phase 6 驗收，橫跨全部 Phase

### Within Each Phase（有依賴的任務）

- T004 → T005 → T006, T007, T008, T009（同一檔案或依賴精簡後的 Canvas API）
- T010, T011 可平行 → T012（依賴 T010）、T013（依賴 T011）→ T014（依賴 T012、T013）

### Parallel Opportunities

| 可平行執行的任務 | 說明 |
|----------------|------|
| T002, T003 | 不同新檔案，無依賴 |
| T006, T007 | 不同 Mode 檔案，都依賴 T005 完成 |
| T010, T011 | 不同 Factory package 檔案 |
| T015, T016 | 標示與繪圖可同步進行 |
| T017, T018 | 手動驗收與程式碼審閱可同步 |

---

## Parallel Example: Phase 3

```
# T006 和 T007 可同步執行（不同檔案）：
Task A: 更新 SelectMode.java（canvas.* → canvas.getModel().*）
Task B: 更新 CreateLinkMode.java（canvas.addConnectionLine → canvas.getModel().addConnectionLine）
# 注意：兩者都依賴 T005（Canvas 精簡完成）才能開始
```

## Parallel Example: Phase 4

```
# T010 和 T011 可同步執行：
Task A: 建立 ShapeFactory + RectShapeFactory + OvalShapeFactory
Task B: 建立 LinkFactory + AssociationLinkFactory + GeneralizationLinkFactory + CompositionLinkFactory
# T012 等 T010，T013 等 T011，T014 等 T012 + T013
```

---

## Implementation Strategy

### MVP First（Phase 1–3 完成即可 Demo）

1. Phase 1: Baseline 確認
2. Phase 2: 建立新類別骨架
3. Phase 3: MVC 分層完成 → 程式仍可完整運作
4. **STOP and VALIDATE**: 確認所有功能正常，Canvas 已精簡，UMLModel 無 GUI import
5. 此時已滿足 US2 + US4 驗收，可先提交 demo

### Incremental Delivery

1. Phase 1–3 完成 → MVC + Observer 就位（US2+US4 ✅）
2. Phase 4 完成 → Factory Pattern 就位（US3 ✅）
3. Phase 5 完成 → 模式標示 + Class Diagram 就位（US5 ✅）
4. Phase 6 完成 → 功能全數驗收（US1 ✅）

---

## Notes

- [P] tasks = 不同檔案、無前置依賴，可平行執行
- [Story] label 對應 spec.md 中的 User Story 編號
- T004 和 T005 雖為同一檔案，但拆開執行較安全（先加後移）
- 每個 Phase 的 Checkpoint 通過後再進入下一 Phase，避免累積錯誤
- 編譯指令：`build.bat`（Windows）或 `javac -encoding UTF-8 -d out -sourcepath src src/oops/UMLEditor.java`
