package fs;

public class HighLevelFileSystem {
  private final LowLevelFileSystem lowLevelFileSystem;

  public HighLevelFileSystem(
      LowLevelFileSystem lowLevelFileSystem) {
    this.lowLevelFileSystem = lowLevelFileSystem;
  }

  public OpenedFile open(String path) {
    int idFile = lowLevelFileSystem.openFile(path);
    if (idFile == -1) {
      throw new CanNotOpenFileException();
    }
    return new OpenedFile(idFile, lowLevelFileSystem);
  }

}
