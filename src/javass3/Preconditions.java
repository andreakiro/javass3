package javass3;

public class Preconditions {
	
	private Preconditions() {}

	/**
	 * if boolean b false throws an exception
	 * 
	 * @param b (boolean)
	 * @throws IllegalArgumentException
	 */
	public static void checkArgument(boolean b) throws IllegalArgumentException {
		if (!b)
			throw new IllegalArgumentException();
	}

	/**
	 * if index less then zero or index bigger or equal than size throws an
	 * exception
	 * 
	 * @param index (int)
	 * @param size  (int)
	 * @return index (int) - if there is no exception
	 * @throws IndexOutOfBoundsException
	 */
	public static int checkIndex(int index, int size) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size)
			throw new IndexOutOfBoundsException();
		return index;
	}
}