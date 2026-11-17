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

Run `Main` to evaluate the classifier on the separate UCI test set. The current K=3 implementation scores 97.83% on its 1,797 test samples; this does not measure accuracy on drawings made in the application.

The reference data is loaded from:

```text
data/optical+recognition+of+handwritten+digits/optdigits.tra
```

The dataset is the UCI Optical Recognition of Handwritten Digits dataset:

> Alpaydin, E. & Kaynak, C. (1998). Optical Recognition of Handwritten Digits. UCI Machine Learning Repository. https://doi.org/10.24432/C50P49

The dataset is distributed under [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). The files in this project are provided by the UCI dataset and are included with attribution.

