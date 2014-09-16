package ply;

public class InvalidFileException extends RuntimeException {
    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException() {
        super();
    }
}
