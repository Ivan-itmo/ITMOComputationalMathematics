package exceptions;

public class BadSolveException extends RuntimeException {
    public BadSolveException(String message) {
        super(message);
    }
}