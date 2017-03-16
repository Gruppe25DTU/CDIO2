package socket;

public class CDIOException extends Exception {

	private static final long serialVersionUID = -5502156707462626683L;

	CDIOException() {
		super();
	}
	CDIOException(String msg) {
		super(msg);
	}
	CDIOException(String msg, Throwable cause) {
		super(msg, cause);
	}
}

class IllegalCommandException extends CDIOException {}
