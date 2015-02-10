import java.util.concurrent.RecursiveAction;

public class MultiplicationTask extends RecursiveAction {

	private static final long serialVersionUID = 1L;
	int end;
	int start;
	Matrix left, right, result;
	private static final int TRESHOLD = 100;

	public MultiplicationTask(Matrix left, Matrix right, Matrix result,
			int start, int end) {

		if (left.getCols() != right.getRows())
			throw new IllegalArgumentException("wrong dimensions");

		this.end = end;
		this.start = start;
		this.left = left;
		this.right = right;
		this.result = result;
	}

	public MultiplicationTask(Matrix left, Matrix right, Matrix result) {
		this(left, right, result, 0, 0);
	}

	@Override
	protected void compute() {
		final int length = end - start;

		if (length < TRESHOLD) {
			for (int i = start; i < end; i++) {
				multiplyRowByColumn(left, right, result, i);
			}

		} else {
			int middle = start + (end - start) / 2;

			MultiplicationTask one = new MultiplicationTask(left, right,
					result, start, middle);
			MultiplicationTask two = new MultiplicationTask(left, right,
					result, middle, end);

			one.fork();
			two.fork();

			one.join();
			two.join();
		}

	}

	public void multiplyRowByColumn(Matrix lhs, Matrix rhs, Matrix result,
			int row) {
			for (int j = 0; j < lhs.getCols(); j++) {
				for (int i = 0; i < rhs.getCols(); i++) {
				double val = result.getValueOnPos(row, i)
						+ lhs.getValueOnPos(row, j) * rhs.getValueOnPos(j, i);
				result.setValueOnPos(row, i, val);
			}
		}
	}
}
