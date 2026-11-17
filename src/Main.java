import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

    // main 方法：放程序需要执行的操作
    public static void main(String[] args)
            throws FileNotFoundException {

        String dataFolder =
                "data/optical+recognition+of+handwritten+digits/";

        // 读取参考数据
        int[][] rows = readAllRows(dataFolder + "optdigits.tra");
        int[][] samples = buildSamples(rows);
        int[] labels = buildLabels(rows);

        // 读取独立测试数据
        int[][] testRows = readAllRows(dataFolder + "optdigits.tes");
        int[][] testSamples = buildSamples(testRows);
        int[] testLabels = buildLabels(testRows);

        // 检查数据规模
        System.out.println("Reference samples: " + samples.length);
        System.out.println("Test samples: " + testSamples.length);
        System.out.println("Features per sample: " + samples[0].length);

        // 识别第一个真实测试样本
        int k = 3;

        int predicted = predict(
                testSamples[0], samples, labels, k
        );

        int actual = testLabels[0];

        System.out.println("Predicted: " + predicted);
        System.out.println("Actual: " + actual);
        System.out.println("Correct: " + (predicted == actual));
        int[] checkDistances = {45, 1, 38, 2, 3};
        int[] nearest = findNearestIndices(checkDistances, 3);

        System.out.print("Nearest indices: ");

        for (int i = 0; i < nearest.length; i++) {
            System.out.print(nearest[i] + " ");
        }

        System.out.println();
        double accuracy = calculateAccuracy(
                samples, labels, testSamples, testLabels, k
        );

        System.out.println("Test accuracy: " + accuracy * 100 + "%");
    }// main 方法在这里结束

    // 独立方法：读取文件第一行
    public static String readFirstLine(String filePath)
            throws FileNotFoundException {

        File file = new File(filePath);
        Scanner reader = new Scanner(file);

        String line = reader.nextLine();

        reader.close();

        return line;
    }

    // 独立方法：组织整个预测过程
    public static int predict(int[] input, int[][] samples,
                              int[] labels, int k) {
        if (k < 1 || k > samples.length) {
            throw new IllegalArgumentException(
                    "k must be between 1 and " + samples.length
            );
        }

        int[] distances = calculateDistances(input, samples);
        int[] nearestIndices = findNearestIndices(distances, k);
        int[] votes = countVotes(nearestIndices, labels, k);

        return findMostVotedLabel(votes);
    }

    // 独立方法：计算两个样本的平方距离
    public static int distanceSquared(int[] a, int[] b) {
        int sum = 0;

        for (int i = 0; i < a.length; i++) {
            int diff = a[i] - b[i];
            sum += diff * diff;
        }

        return sum;
    }

    // 独立方法：计算输入与所有参考样本的距离
    public static int[] calculateDistances(int[] input, int[][] samples) {
        int[] distances = new int[samples.length];

        for (int i = 0; i < samples.length; i++) {
            distances[i] = distanceSquared(input, samples[i]);
        }

        return distances;
    }

    // 独立方法：根据距离排列样本下标
    public static int[] sortIndicesByDistance(int[] distances) {
        int[] indices = new int[distances.length];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        for (int i = 0; i < indices.length - 1; i++) {
            int minPosition = i;

            for (int j = i + 1; j < indices.length; j++) {
                if (distances[indices[j]]
                        < distances[indices[minPosition]]) {
                    minPosition = j;
                }
            }

            int temp = indices[i];
            indices[i] = indices[minPosition];
            indices[minPosition] = temp;
        }

        return indices;
    }

    // 独立方法：统计最近 K 个样本的投票
    public static int[] countVotes(int[] sortedIndices,
                                   int[] labels, int k) {
        int[] votes = new int[10];

        for (int i = 0; i < k; i++) {
            int sampleIndex = sortedIndices[i];
            int label = labels[sampleIndex];
            votes[label]++;
        }

        return votes;
    }

    // 独立方法：返回票数最多的数字
    public static int findMostVotedLabel(int[] votes) {
        int prediction = 0;

        for (int digit = 1; digit < votes.length; digit++) {
            if (votes[digit] > votes[prediction]) {
                prediction = digit;
            }
        }

        return prediction;
        // 将一行逗号分隔的数字解析为整数数组

    }
    // 将一行逗号分隔的数字解析为整数数组
    public static int[] parseLine(String line) {
        String[] parts = line.split(",");
        int[] numbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Integer.parseInt(parts[i].trim());
        }

        return numbers;
    }
    // 取出最后一个元素之前的所有元素
    public static int[] extractFeatures(int[] row) {
        int[] features = new int[row.length - 1];

        for (int i = 0; i < features.length; i++) {
            features[i] = row[i];
        }

        return features;
    }
    // 最后一个元素就是标签
    public static int extractLabel(int[] row) {
        return row[row.length - 1];
    }
    public static int countLines(String filePath)
            throws FileNotFoundException {

        Scanner reader = new Scanner(new File(filePath));
        int count = 0;

        while (reader.hasNextLine()) {
            reader.nextLine();
            count++;
        }

        reader.close();

        return count;
    }
    public static int[][] readAllRows(String filePath)
            throws FileNotFoundException {

        int rowCount = countLines(filePath);
        int[][] rows = new int[rowCount][];

        Scanner reader = new Scanner(new File(filePath));

        for (int i = 0; i < rows.length; i++) {
            String line = reader.nextLine();
            rows[i] = parseLine(line);
        }

        reader.close();

        return rows;
    }
    // 将所有行的特征整理成样本数组
    public static int[][] buildSamples(int[][] rows) {
        int[][] samples = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            samples[i] = extractFeatures(rows[i]);
        }

        return samples;
    }

    // 将所有行的标签整理成标签数组
    public static int[] buildLabels(int[][] rows) {
        int[] labels = new int[rows.length];

        for (int i = 0; i < rows.length; i++) {
            labels[i] = extractLabel(rows[i]);
        }

        return labels;
    }
    public static double calculateAccuracy(
            int[][] samples, int[] labels,
            int[][] testSamples, int[] testLabels, int k) {

        if (testSamples.length == 0) {
            throw new IllegalArgumentException(
                    "Test samples must not be empty"
            );
        }

        int correctCount = 0;

        for (int i = 0; i < testSamples.length; i++) {
            int predicted = predict(testSamples[i], samples, labels, k);
            int actual = testLabels[i];

            if (predicted == actual) {
                correctCount++;
            }
        }

        return (double) correctCount / testSamples.length;
    }
    // 返回最近 K 个样本的下标，按距离从小到大排列
    public static int[] findNearestIndices(int[] distances, int k) {
        int[] indices = new int[distances.length];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        int[] nearestIndices = new int[k];

        // 只选出前 K 名
        for (int i = 0; i < k; i++) {
            int minPosition = i;

            for (int j = i + 1; j < indices.length; j++) {
                if (distances[indices[j]]
                        < distances[indices[minPosition]]) {
                    minPosition = j;
                }
            }

            int temp = indices[i];
            indices[i] = indices[minPosition];
            indices[minPosition] = temp;

            // 当前第 i 名已经确定，保存它的样本下标
            nearestIndices[i] = indices[i];
        }

        return nearestIndices;
    }

}// Main 类在这里结束