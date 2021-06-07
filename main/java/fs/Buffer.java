package fs;

public class Buffer {

  private final int start;
  private int end;
  private final byte[] bytes;
  private final int maxSize;
  private int currentSize;

  public Buffer(int tamanio) {
    this.end = tamanio - 1; //porque el primero es 0
    this.maxSize = tamanio;
    this.start = 0;
    this.bytes = new byte[tamanio];
    this.currentSize = tamanio;
  }

  public void limit(int desplazamiento) {
    currentSize = desplazamiento;
    end = start + desplazamiento - 1;
  }

  public int getMaxSize() {
    return maxSize;
  }

  public int getCurrentSize() {
    return currentSize;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int getStart() {
    return start;
  }

  public int getEnd() {
    return end;
  }
}
