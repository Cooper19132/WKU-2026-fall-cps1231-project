# WKU 2026 Fall CPS1231 Project

Java handwritten digit recognizer using the K-nearest neighbors (KNN) algorithm.

## Features

- Load the UCI Optical Recognition of Handwritten Digits dataset.
- Compare 64-feature digit samples with squared Euclidean distance.
- Predict a digit using KNN voting.
- Draw a digit in a Swing window and recognize it.
- Display the processed drawing and the three nearest reference samples.

## Run

Open the project in IntelliJ IDEA and run `DigitRecognizerUI`.

The reference data is loaded from:

```text
data/optical+recognition+of+handwritten+digits/optdigits.tra
```

The dataset is the UCI Optical Recognition of Handwritten Digits dataset:

> Alpaydin, E. & Kaynak, C. (1998). Optical Recognition of Handwritten Digits. UCI Machine Learning Repository. https://doi.org/10.24432/C50P49

