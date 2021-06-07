package fs;

public class CanNotOpenFileException extends RuntimeException {
  public CanNotOpenFileException() {
    super("No se puede abrir el File");
  }
}
