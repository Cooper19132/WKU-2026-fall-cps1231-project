import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * 数据读取、K近邻分类和测试集评估。
 * 本类的 main 用于控制台评估；手写演示请运行 DigitRecognizerUI。
 * samples[i]、labels[i]、distances[i] 始终通过同一个样本下标 i 对应。
 */
public class Main {

    // 测试集标签只在预测后核对答案，不作为特征传给 predict。
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
        System.out.println("参考样本数：" + samples.length);
        System.out.println("测试样本数：" + testSamples.length);
        System.out.println("每个样本的特征数：" + samples[0].length);

        // 识别第一个真实测试样本
        int k = 3;

        int predicted = predict(
                testSamples[0], samples, labels, k
        );

        int actual = testLabels[0];

        System.out.println("预测数字：" + predicted);
        System.out.println("真实标签：" + actual);
        System.out.println("是否正确：" + (predicted == actual));
        // 小数组用于检查下标排序，应输出1、3、4，不参与测试集正确率计算。
        int[] checkDistances = {45, 1, 38, 2, 3};
        int[] nearest = findNearestIndices(checkDistances, 3);

        System.out.print("最近邻下标检查（应为1 3 4）：");

        for (int i = 0; i < nearest.length; i++) {
            System.out.print(nearest[i] + " ");
        }

        System.out.println();
        double accuracy = calculateAccuracy(
                samples, labels, testSamples, testLabels, k
        );

        System.out.println("UCI 独立测试集正确率：" + accuracy * 100 + "%");
    }// main 方法在这里结束

    // 读取非空文件的第一行。保留此入门练习方法；完整加载使用 readAllRows。
    // throws 将“文件找不到”的异常交给调用者处理。
    public static String readFirstLine(String filePath)
            throws FileNotFoundException {

        File file = new File(filePath);
        Scanner reader = new Scanner(file);

        String line = reader.nextLine();

        reader.close();

        return line;
    }

    /**
     * 对 input 分类：计算距离 → 选择最近 K 个样本 → 投票 → 返回数字标签。
     * 要求样本非空、特征长度与 input 相同，且每个样本都有0～9之间的标签。
     * k 是邻居数量，不是数组下标；可以等于全部参考样本数量。
     */
    public static int predict(int[] input, int[][] samples,
                              int[] labels, int k) {
        if (k < 1 || k > samples.length) {
            throw new IllegalArgumentException(
                    "K 必须在1到" + samples.length + "之间"
            );
        }

        int[] distances = calculateDistances(input, samples);
        int[] nearestIndices = findNearestIndices(distances, k);
        int[] votes = countVotes(nearestIndices, labels, k);

        return findMostVotedLabel(votes);
    }

    /**
     * 计算等长特征数组的平方欧氏距离：累加 (a[i] - b[i])²。
     * 平方避免正负差抵消；只比较远近时不必开平方，排序结果相同。
     * 本项目特征为64个0～16的整数，最大距离为64×16²，int 足够保存。
     */
    public static int distanceSquared(int[] a, int[] b) {
        int sum = 0;

        for (int i = 0; i < a.length; i++) {
            int diff = a[i] - b[i];
            sum += diff * diff;
        }

        return sum;
    }

    // distances[i] 保存 input 与 samples[i] 的距离；特征和标签不混在一起比较。
    public static int[] calculateDistances(int[] input, int[][] samples) {
        int[] distances = new int[samples.length];

        for (int i = 0; i < samples.length; i++) {
            distances[i] = distanceSquared(input, samples[i]);
        }

        return distances;
    }

    // 完整选择排序，保留供学习对照；当前预测使用 findNearestIndices，只选前 K 名。
    // 只交换下标，避免直接排序距离后丢失它与样本、标签之间的对应关系。
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

    /**
     * sortedIndices 是按距离排列的样本下标（至少 k 个）。
     * 返回 votes：下标表示数字类别，元素值表示该类别获得的票数。
     * 例如样本下标1的标签为7，则给 votes[7] 加一，不能给 votes[1] 加一。
     */
    public static int[] countVotes(int[] sortedIndices,
                                   int[] labels, int k) {
        int[] votes = new int[10];

        for (int i = 0; i < k; i++) {
            int sampleIndex = sortedIndices[i]; // 邻居排名位置 → 原样本下标
            int label = labels[sampleIndex];    // 原样本下标 → 数字标签
            votes[label]++;
        }

        return votes;
    }

    // 返回类别下标（数字0～9），不是票数。传入的 votes 长度应为10。
    // 从小到大扫描并使用 >，因此平票时保留较小数字。
    public static int findMostVotedLabel(int[] votes) {
        int prediction = 0;

        for (int digit = 1; digit < votes.length; digit++) {
            if (votes[digit] > votes[prediction]) {
                prediction = digit;
            }
        }

        return prediction;
    }
    // 输入为逗号分隔的整数字符串；文件不应有标题行、空白行或缺失数字。
    // split 拆分字符串，trim 去掉两端空白，parseInt 将字符串转换为整数。
    public static int[] parseLine(String line) {
        String[] parts = line.split(",");
        int[] numbers = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            numbers[i] = Integer.parseInt(parts[i].trim());
        }

        return numbers;
    }
    // 每行至少含一个特征和一个标签；最后一列是答案，必须从特征中排除。
    public static int[] extractFeatures(int[] row) {
        int[] features = new int[row.length - 1];

        for (int i = 0; i < features.length; i++) {
            features[i] = row[i];
        }

        return features;
    }
    // row.length 是元素数量，最后一个元素的下标要减一。
    public static int extractLabel(int[] row) {
        return row[row.length - 1];
    }
    // 第一遍扫描：统计行数，供数组分配空间。nextLine 会消耗一行，避免无限循环。
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
    /**
     * 读取整个数据文件：先统计行数，再逐行解析，返回包含标签的二维数组。
     * 文件读取两遍，使用已学过的数组；假设读取过程中内容不变。
     */
    public static int[][] readAllRows(String filePath)
            throws FileNotFoundException {

        int rowCount = countLines(filePath);
        int[][] rows = new int[rowCount][]; // 先分配行数，每行数组由 parseLine 创建

        Scanner reader = new Scanner(new File(filePath));

        for (int i = 0; i < rows.length; i++) {
            String line = reader.nextLine();
            rows[i] = parseLine(line);
        }

        reader.close();

        return rows;
    }
    // 每行去掉最后的标签，生成新的特征数组；保持原行顺序。
    public static int[][] buildSamples(int[][] rows) {
        int[][] samples = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            samples[i] = extractFeatures(rows[i]);
        }

        return samples;
    }

    // 与 buildSamples 使用相同行顺序，保证 samples[i] 和 labels[i] 一一对应。
    public static int[] buildLabels(int[][] rows) {
        int[] labels = new int[rows.length];

        for (int i = 0; i < rows.length; i++) {
            labels[i] = extractLabel(rows[i]);
        }

        return labels;
    }
    /**
     * 对独立测试集逐个预测，返回0～1的正确率；显示百分比时再乘100。
     * 测试样本与测试标签须一一对应。这不是画板手写输入的实测正确率。
     */
    public static double calculateAccuracy(
            int[][] samples, int[] labels,
            int[][] testSamples, int[] testLabels, int k) {

        if (testSamples.length == 0) {
            throw new IllegalArgumentException(
                    "测试样本不能为空"
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

        // 转成 double 再除，避免整数除法把4/5截成0。
        return (double) correctCount / testSamples.length;
    }
    /**
     * 局部选择排序：每轮从未选出的样本中找一个最近邻，只选 k 轮。
     * 要求 1 <= k <= distances.length；直接调用本方法也须遵守此约束。
     * 约比较 k×n 次，完整选择排序约比较 n² 次，K较小时可减少计算。
     * 距离相等时不主动替换候选；算法不保证相同距离保持原顺序。
     */
    public static int[] findNearestIndices(int[] distances, int k) {
        int[] indices = new int[distances.length];

        for (int i = 0; i < indices.length; i++) {
            indices[i] = i;
        }

        int[] nearestIndices = new int[k];

        // 只选出前 K 名
        for (int i = 0; i < k; i++) {
            int minPosition = i; // indices 中的位置，不是距离值或数字标签

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
