package kr.co.adflow.exception;

public class FilterException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * <p>
	 * Constructor for HandleException.
	 * </p>
	 */
	public FilterException() {
		super();
	}

	/**
	 * <p>
	 * Constructor for HandleException.
	 * </p>
	 */
	public FilterException(String message) {
		super(message);
	}

	/**
	 * <p>
	 * Constructor for HandleException.
	 * </p>
	 * 
	 * @param message
	 *            a {@link java.lang.String} object.
	 * @param cause
	 *            a {@link java.lang.Throwable} object.
	 */
	public FilterException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * <p>
	 * Constructor for HandleException.
	 * </p>
	 * 
	 * @param cause
	 *            a {@link java.lang.Throwable} object.
	 */
	public FilterException(Throwable cause) {
		super(cause);
	}

}