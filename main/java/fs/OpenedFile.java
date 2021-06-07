package fs;

import java.util.function.Consumer;

public class OpenedFile {

  int descriptor;
  LowLevelFileSystem llfs;

  public OpenedFile(int idFile, LowLevelFileSystem llfs) {
    this.descriptor = idFile;
    this.llfs = llfs;
  }

  public int getDescriptor() {
    return this.descriptor;
  }

  public void close() {
    this.llfs.closeFile(this.descriptor);
  }

  public void read(Buffer buffer) {
    int bytesLeidos = llfs.syncReadFile(this.descriptor, buffer.getBytes(),
        buffer.getStart(), buffer.getEnd());
    if (bytesLeidos == -1) {
      throw new CanNotReadFileException();
    }
    buffer.limit(bytesLeidos);
  }

  public void write(Buffer buffer) {
    llfs.syncWriteFile(this.descriptor, buffer.getBytes(), buffer.getStart(),
        buffer.getEnd());
  }

  public void asyncRead(Consumer<Buffer> callback, Buffer buffer) {
    llfs.asyncReadFile(descriptor,
        buffer.getBytes(),
        buffer.getStart(),
        buffer.getEnd(),
        readBytes -> {
          if (readBytes == -1) {
            throw new CanNotReadFileException();
          }
          buffer.limit(readBytes);
          callback.accept(buffer);
        });

  }

  public void asyncWrite(Runnable callback, Buffer buffer) {
    llfs.asyncWriteFile(descriptor,
        buffer.getBytes(),
        buffer.getStart(),
        buffer.getEnd(),
        callback);
  }

}
