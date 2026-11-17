# WKU 2026 Fall CPS1231 Project

大一 Java 课程项目：使用 K近邻（KNN）算法识别单个手写数字，提供 Swing 画板和最近邻展示。

A first-year Java course project that recognizes single handwritten digits using K-nearest neighbors (KNN), with a Swing drawing canvas and nearest-neighbor display.

## 功能 / Features

- **文件加载 / File loading**：从 UCI 文件读取参考样本与独立测试样本。Load reference samples and a separate test set from UCI files.
- **数字识别 / Digit recognition**：比较64个特征的平方欧氏距离，由 K 个邻居投票预测0～9。Compare 64 features using squared Euclidean distance and predict 0-9 by K-neighbor voting.
- **手写交互 / Drawing controls**：书写、识别、清空重写；空白画板会提示先书写。Draw, recognize, and clear; an empty canvas prompts the user to write first.
- **结果解释 / Result explanation**：展示处理后的笔迹、参与投票的邻居、真实标签及平方距离。Show the processed drawing, voting neighbors, true labels, and squared distances.
- **双语提示 / Bilingual guidance**：界面、悬停提示、控制台输出及代码注释均提供中英文对照。The interface, tooltips, console output, and code comments include Chinese and English.

## 在 IDEA 中运行 / Run in IntelliJ IDEA

1. 打开整个项目，使用 JDK 17 或更高版本，无需第三方 Java 库。<br>
   Open the whole project with JDK 17 or later. No third-party Java libraries are required.
2. 将运行配置的工作目录设为项目根目录，即同时包含 `src` 和 `data` 的目录。<br>
   Set the run configuration's working directory to the project root containing both `src` and `data`.
3. 运行 `DigitRecognizerUI.main`，按住鼠标或触控板拖动，每次写一个完整数字。<br>
   Run `DigitRecognizerUI.main`. Drag with a mouse or trackpad to write one complete digit at a time.
4. 点击“识别数字 / Recognize”，查看预测结果与参与投票的最近邻；关闭结果窗口后可继续操作。<br>
   Click **Recognize** to view the prediction and its voting neighbors. Close the result dialog to continue.
5. 点击“清空重写 / Clear”重新书写；停留在按钮或结果说明上可查看双语提示。<br>
   Click **Clear** to start again. Hover over controls or result details for bilingual tips.

默认使用三个邻居（K=3）。平方距离越小表示特征越接近，距离不是置信度或正确率。

The default is three neighbors (K=3). Smaller squared distances indicate closer features; distance is not confidence or accuracy.

## 评估与范围 / Evaluation and scope

运行 `Main.main` 进行控制台评估。参考集有3823个样本，独立测试集有1797个样本；当前 K=3 的测试正确率为 **97.83%**。

Run `Main.main` for console evaluation. The reference set has 3,823 samples and the separate test set has 1,797 samples. The current K=3 implementation achieves **97.83%** test accuracy.

该结果不代表画板手写输入的正确率。当前目标为单个0～9数字；空白画板会被拦截，其他输入仍可能得到一个数字预测。

This result does not measure accuracy on canvas drawings. The application targets single digits 0-9. It rejects blank drawings; other inputs may still receive a digit prediction.

## 数据路径与格式 / Data paths and format

```text
data/optical+recognition+of+handwritten+digits/
├── optdigits.tra    参考样本 / Reference samples
├── optdigits.tes    独立测试集 / Separate test set
└── optdigits.names  数据说明 / Dataset description
```

每行包含64个0～16的整数特征和最后一列的0～9标签，以逗号分隔；不含标题、空白行或缺失值。加载方法假设文件满足这些条件。

Each row contains 64 integer features in 0-16 followed by a label in 0-9, separated by commas. The loader assumes no header, blank lines, or missing values.

`Main` 与 `DigitRecognizerUI` 分别设置了数据路径。参考样本用于预测，测试标签只在预测后核对，不能作为输入特征。

`Main` and `DigitRecognizerUI` configure their data paths separately. Reference samples support prediction; test labels are used only to check predictions and must not become input features.

## 项目结构 / Project structure

| 类 / Class | 中文职责 | Responsibility |
|---|---|---|
| `Main` | 文件解析、拆分特征与标签、距离、最近邻、投票及评估 | File parsing, feature/label separation, distances, neighbors, voting, and evaluation |
| `DrawingPanel` | 鼠标事件、保存笔迹、清空与重绘 | Mouse events, stroke storage, clearing, and repainting |
| `ImageProcessor` | 寻找笔迹范围、等比例缩放居中、生成64个特征 | Ink bounds, proportional scaling and centering, and 64-feature extraction |
| `NeighborResults` | 用同一批邻居投票并展示参考样本灰度示意图 | Vote with the displayed neighbors and render reference features as grayscale images |
| `DigitRecognizerUI` | 启动加载数据、创建窗口、响应按钮操作 | Load data at startup, build the window, and handle button actions |

### 执行流程 / Processing flow

```text
手写 / Draw
    → 寻找笔迹 / Find ink bounds
    → 缩放居中到32×32 / Resize and center to 32×32
    → 统计8×8区域 / Count ink in 8×8 blocks
    → 64个特征 / 64 features
    → 距离与最近K个样本 / Distances and K nearest samples
    → 标签投票 / Label voting
    → 预测与邻居展示 / Prediction and neighbor display
```

### 关键数组 / Key arrays

| 表达式 / Expression | 中文含义 | Meaning |
|---|---|---|
| `samples[i]` | 第 i 个样本的特征 | Features of reference sample i |
| `labels[i]` | 第 i 个样本的真实数字 | True digit label of sample i |
| `distances[i]` | 输入与第 i 个样本的平方距离 | Squared distance from the input to sample i |
| `nearestIndices[0]` | 最近样本在参考集中的下标 | Original reference index of the nearest sample |
| `labels[nearestIndices[0]]` | 最近样本的数字标签 | Digit label of the nearest sample |
| `votes[7]` | 数字7得到的票数 | Vote count for digit 7 |

建议从 `Main.predict` 开始，逐个阅读它调用的小方法，再读图片处理和按钮响应。代码中的双语注释解释参数约束和实现细节，README 说明整体使用方式与结构。

Start with `Main.predict`, follow its helper methods, then read image processing and button callbacks. Bilingual code comments explain contracts and implementation details; this README describes overall use and structure.

## 常见问题与现场修改 / Troubleshooting and live changes

| 问题 / Issue | 中文处理方式 | English guidance |
|---|---|---|
| 找不到文件 / File not found | 核对文件名、大小写、工作目录和入口中的路径 | Check filenames, case, working directory, and the entry point's data path |
| 类声明相关报错 / Declaration error | 检查多余的 `}`；方法应在类内、彼此并列 | Check extra closing braces; methods belong inside the class, not inside each other |
| 数组越界 / Array bounds | 长度 n 的合法下标是0～n−1；K是数量，允许等于样本数 | Valid indices are 0 through n-1; K is a count and may equal the sample count |
| 修改 K / Change K | 修改 `NeighborResults.showResults` 调用的最后一个参数；改标题不影响算法 | Change the final argument to `NeighborResults.showResults`; titles do not control voting |
| 平票 / Vote tie | 现有代码从小到大扫描并使用 `>`，选择较小数字 | The ascending scan with `>` retains the smaller digit |
| 识别错误 / Wrong prediction | 查看预处理图片、邻居、特征、距离和标签对应关系 | Inspect preprocessing, neighbors, features, distances, and label associations |
| 相同距离 / Equal distances | 局部选择排序不保证保持原始顺序 | Partial selection sort does not preserve original order for distance ties |

`readFirstLine` 和 `sortIndicesByDistance` 保留作学习对照，当前完整识别流程不调用它们。每个特征对应一个4×4区域的笔画像素数；参考样本图片只能还原区域深浅，不能恢复原始笔迹的精确位置。

`readFirstLine` and `sortIndicesByDistance` remain as learning examples and are not used by the current recognition flow. Each feature counts ink in a 4×4 block; reference previews reconstruct block intensity, not the exact original strokes.

## 数据来源与授权 / Dataset attribution and license

数据集 / Dataset: [UCI Optical Recognition of Handwritten Digits](https://doi.org/10.24432/C50P49).

> Alpaydin, E. & Kaynak, C. (1998). Optical Recognition of Handwritten Digits. UCI Machine Learning Repository. https://doi.org/10.24432/C50P49

数据集采用 [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) 授权，项目保留来源和原始说明。画板缩放、居中是简化实现，不完全复现 UCI 的预处理。

The dataset is licensed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/), with attribution and original documentation retained. Canvas scaling and centering are simplified and do not fully reproduce UCI preprocessing.
