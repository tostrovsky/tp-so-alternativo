package fs;

public class CanNotReadFileException extends RuntimeException {
  public CanNotReadFileException() {
    super("No se puede leer el File");
  }
}