# Quickstart: OopsUMLEditor 重構版

## 編譯與執行

```bat
build.bat
run.bat
```

`build.bat` 使用 `javac -encoding UTF-8 -d out -sourcepath src src\oops\UMLEditor.java`，
`-sourcepath src` 會自動找到所有 package（包含新增的 `oops.factory`），無需修改 build 腳本。

## 新增 Package 位置

```
src/
└── oops/
    ├── factory/        ← 新增，含 ShapeFactory、LinkFactory 及 5 個具體工廠
    └── model/
        └── UMLModel.java     ← 新增
        └── ModelObserver.java ← 新增
```

## 驗收測試清單

執行 `run.bat` 後依序測試：

- [ ] 拖曳 Rect/Oval 按鈕到畫布 → 物件出現
- [ ] 點擊 Rect/Oval 按鈕後在畫布拖曳 → 物件以拖曳大小建立
- [ ] Association 模式從 port 拖到 port → V 形箭頭連線
- [ ] Generalization 模式 → 空心三角形連線
- [ ] Composition 模式 → 空心菱形連線
- [ ] Select 模式單選、框選物件
- [ ] 拖曳移動物件（含群組後整批移動）
- [ ] 拖曳 port 調整大小（Resize）
- [ ] 選取 ≥2 物件 → Edit > Group → 藍色虛線外框
- [ ] 選取 Composite → Edit > Ungroup → 恢復獨立
- [ ] 選取單一基本物件 → Edit > Label → 設定名稱與顏色
