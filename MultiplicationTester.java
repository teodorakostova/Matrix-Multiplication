import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;

public class MultiplicationTester {
	private Matrix left;
	private Matrix right;
	private Matrix resultFromFile;
	static final int NUMBER_OF_ITERATIONS = 1;

	public MultiplicationTester(String leftPath, String rightPath,
			String resultPath) {
		try {
			left = new Matrix(leftPath);
			right = new Matrix(rightPath);
			resultFromFile = new Matrix(resultPath);

			left.load();
			right.load();
			resultFromFile.load();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Input/Output error");
		}
	}

	public static boolean equal(final double[][] lhs, final double[][] rhs) {
		if (lhs == null)
			return rhs == null;
		if (rhs == null)
			return false;
		if (lhs.length != rhs.length)
			return false;

		for (int i = 0; i < lhs.length; i++) {
			for (int j = 0; j < lhs[i].length; j++) {
				if (lhs[i][j] != rhs[i][j]) {
					System.out.println("left[" + i + "][" + j + "] =  "
							+ lhs[i][j] + " != right[" + i + "][" + j + "] =  "
							+ rhs[i][j]);
					return false;
				}
			}
		}
		return true;
	}

	public static void smallMatricesLinearTest() {
		double x[][] = { { 3, 2, 3 }, { 5, 9, 8 } };

		double y[][] = { { 4, 7 }, { 9, 3 }, { 8, 1 } };

		Matrix lhs = new Matrix(x);
		Matrix rhs = new Matrix(y);

		Matrix res = lhs.multiply(rhs);

		res.print();
	}

	public static void smallMatricesParallelTest() {
		double x[][] = { { 3, 2, 3, 6, 1, 1, 1, 1 },
				{ 5, 9, 8, 1, 1, 1, 1, 0 }, { 3, 2, 5, 6, 1, 1, 0, 0 } };

		double y[][] = { { 4, 7, 1 }, { 9, 1, 3 }, { 0, 8, 1 }, { 3, 1, 2 },
				{ 2, 1, 2 }, { 6, 1, 0 }, { 3, 0, 1 }, { 5, 3, 0 } };

		Matrix lhs = new Matrix(x);
		Matrix rhs = new Matrix(y);
		Matrix linResult = lhs.multiply(rhs);
		Matrix parallelResult = new Matrix(lhs.getRows(), rhs.getCols());

		ForkJoinPool pool = new ForkJoinPool();
		long t1 = System.currentTimeMillis();

		MultiplicationTask task = new MultiplicationTask(lhs, rhs,
				parallelResult, 0, lhs.getRows());
		pool.invoke(task);

		long t2 = System.currentTimeMillis() - t1;
		System.out.println("Time: " + t2);

		if (equal(linResult.getMatrix(), parallelResult.getMatrix()))
			System.out.println("OK");
		else
			System.out.println("incorrect result");
	}

	/*
	 * With 1 threads: 11.948109152956974 
	 * With 2 threads: 6.30533234689121 With
	 * 3 threads: 6.783314873180891
	 * EX2
	 *  Average linear time: 25729 for 10 iterations
	 * Average time for 1 threads: 238282 
	 * Average time for 2 threads: 148615
	 * Average time for 3 threads: 152034 
	 * Average time for 4 threads: 143880
	 * Acceleration for 1 threads: 9.26122274476272 
	 * Acceleration for 2 threads: 5.77616697112208
	 * Acceleration for 3 threads: 5.90905204244238
	 * Acceleration for 4 threads: 5.592133390337751
	 */

	public void testParallel() {
		int cores = Runtime.getRuntime().availableProcessors();
		List<Double> acceleration = new ArrayList<>();
		List<List<Long>> timeForMultiplication = new ArrayList<>();

		for (int i = 1; i <= 2 * cores; i++) {
			timeForMultiplication.add(new ArrayList<Long>());

			for (int j = 0; j < NUMBER_OF_ITERATIONS; j++) {
				ForkJoinPool pool = new ForkJoinPool(i);
				Matrix myResult = new Matrix(left.getRows(), right.getCols());
				MultiplicationTask task = new MultiplicationTask(left, right,
						myResult, 0, left.getRows());
				long time = System.currentTimeMillis();
				pool.invoke(task);

				if (!equal(myResult.getMatrix(), resultFromFile.getMatrix())) {
					System.out.println("Incorrect result");
					return;

				} else {
					timeForMultiplication.get(i - 1).add(
							System.currentTimeMillis() - time);
				}
			}
		}

		double averageFromLinear = testLinear();
		int i = 1;
		for (List<Long> list : timeForMultiplication) {
			long sum = 0;
			for (Long l : list) {
				sum += l;
			}
			System.out.println("Average time for " + i + " threads: " + sum);
			BigDecimal truncated = new BigDecimal(sum / averageFromLinear);
			truncated.setScale(4, BigDecimal.ROUND_HALF_UP);
			acceleration.add(truncated.doubleValue());
			i++;
		}

		i = 1;
		for (Double a : acceleration) {
			System.out.println("Acceleration for " + i + " threads: " + a);
			i++;

		}

	}

	// returns the average time in ms for a stated number of iterations
	public double testLinear() {
		List<Long> timeForMultiplication = new ArrayList<Long>();

		Matrix result;
		for (int i = 0; i < NUMBER_OF_ITERATIONS; i++) {
			long time = System.currentTimeMillis();
			result = left.multiply(right);

			if (!equal(result.getMatrix(), resultFromFile.getMatrix())) {
				System.out.println("Incorrect result");
				return 0;
			} else {
				timeForMultiplication.add(System.currentTimeMillis() - time);
			}
		}
		long sum = 0;
		for (Long t : timeForMultiplication) {
			sum += t;
		}

		System.out.println("Average time: " + sum / NUMBER_OF_ITERATIONS
				+ " for " + NUMBER_OF_ITERATIONS + " iterations");

		return sum / NUMBER_OF_ITERATIONS;

	}

	public static void main(String args[]) {
		int userChoice = 0;
		System.out.println("For linear muliplication press 1"
				+ "\nFor parallel multiplication press 2"
				+ "\nFor both press 3");

		Scanner sc = new Scanner(System.in);
		userChoice = sc.nextInt();

		MultiplicationTester tester = new MultiplicationTester("left",
				"right", "result");

		if (userChoice == 1) {
			tester.testLinear();
		} else if (userChoice == 2) {
			tester.testParallel();
		} else if (userChoice == 3) {
			tester.testLinear();
			tester.testParallel();
		} else {
			System.out.println("Wrong choice");
		}

		sc.close();
	}

}
