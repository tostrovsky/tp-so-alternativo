package fs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;

class HighLevelFileSystemTest {

  private LowLevelFileSystem lowLevelFileSystem;
  private HighLevelFileSystem fileSystem;

  @BeforeEach
  void initFileSystem() {
    lowLevelFileSystem = mock(LowLevelFileSystem.class);
    fileSystem = new HighLevelFileSystem(lowLevelFileSystem);
  }

  @Test
  void sePuedeAbrirUnArchivo() {
    when(lowLevelFileSystem.openFile("unArchivo.txt")).thenReturn(42);
    OpenedFile file = fileSystem.open("unArchivo.txt");
    Assertions.assertEquals(file.getDescriptor(), 42);
  }

  @Test
  void siLaAperturaFallaUnaExcepcionEsLanzada() {
    when(lowLevelFileSystem.openFile("otroArchivo.txt")).thenReturn(-1);
    Assertions.assertThrows(CanNotOpenFileException.class, () -> fileSystem.open("otroArchivo.txt"));
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoNoHayNadaParaLeer() {
    Buffer buffer = new Buffer(100);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 100)).thenReturn(0);

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.read(buffer);

    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(-1, buffer.getEnd());
    Assertions.assertEquals(0, buffer.getCurrentSize());
  }

  @Test
  void sePuedeLeerSincronicamenteUnArchivoCuandoHayAlgoParaLeer() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    when(lowLevelFileSystem.syncReadFile(42, buffer.getBytes(), 0, 9)).thenAnswer(invocation -> {
      Arrays.fill(buffer.getBytes(), 0, 4, (byte) 3);
      return 4;
    });

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.read(buffer);

    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(3, buffer.getEnd());
    Assertions.assertEquals(4, buffer.getCurrentSize());
    Assertions.assertArrayEquals(buffer.getBytes(), new byte[]{3, 3, 3, 3, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void siLaLecturaSincronicaFallaUnaExcepcionEsLanzada() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("archivoMalito.txt")).thenReturn(13);
    when(lowLevelFileSystem.syncReadFile(anyInt(), any(), anyInt(), anyInt())).thenReturn(-1);

    OpenedFile file = fileSystem.open("archivoMalito.txt");

    Assertions.assertThrows(CanNotReadFileException.class, () -> file.read(buffer));
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoNoHayNadaParaEscribir() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doNothing().when(lowLevelFileSystem).syncWriteFile(42, buffer.getBytes(), 0, 9);

    //lowLevelFileSystem.syncWriteFile(42, buffer.getBytes(), 0,9);
    Arrays.fill(buffer.getBytes(), 0, 0, (byte) 0);

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.write(buffer);

    verify(lowLevelFileSystem).syncWriteFile(42, buffer.getBytes(), 0, 9);
    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(9, buffer.getEnd());
    Assertions.assertEquals(10, buffer.getCurrentSize());
    Assertions.assertArrayEquals(buffer.getBytes(), new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void sePuedeEscribirSincronicamenteUnArchivoCuandoHayAlgoParaEscribir() {
    Buffer buffer = new Buffer(10);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doNothing().when(lowLevelFileSystem).syncWriteFile(42, buffer.getBytes(), 0, 9);

    //lowLevelFileSystem.syncWriteFile(42, buffer.getBytes(), 0,9);
    Arrays.fill(buffer.getBytes(), 0, 4, (byte) 3);

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.write(buffer);

    verify(lowLevelFileSystem).syncWriteFile(42, buffer.getBytes(), 0, 9);
    Assertions.assertEquals(0, buffer.getStart());
    Assertions.assertEquals(9, buffer.getEnd());
    Assertions.assertEquals(10, buffer.getCurrentSize());
    Assertions.assertArrayEquals(buffer.getBytes(), new byte[]{3, 3, 3, 3, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void sePuedeLeerAsincronicamenteUnArchivo() {
    Buffer buffer = new Buffer(10);
    Consumer<Buffer> callback = mock(Consumer.class);

    Consumer<Integer> callbackInt = readBytes -> {
      buffer.limit(readBytes);
      callback.accept(buffer);
    };

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doNothing().when(lowLevelFileSystem).asyncReadFile(42, buffer.getBytes(), 0, 9, callbackInt);

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.asyncRead(callback, buffer);

    verify(lowLevelFileSystem).asyncReadFile(anyInt(), any(), anyInt(), anyInt(), any(Consumer.class));
  }


  @Test
  void sePuedeEscribirAsincronicamenteUnArchivo() {
    Buffer buffer = new Buffer(10);
    Runnable callback = mock(Runnable.class);

    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doNothing().when(lowLevelFileSystem).asyncWriteFile(42, buffer.getBytes(), 0, 9, callback);

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.asyncWrite(callback, buffer);

    verify(lowLevelFileSystem).asyncWriteFile(42, buffer.getBytes(), 0, 9, callback);
  }

  @Test
  void sePuedeCerrarUnArchivo() {
    when(lowLevelFileSystem.openFile("ejemplo.txt")).thenReturn(42);
    doNothing().when(lowLevelFileSystem).closeFile(42);

    OpenedFile file = fileSystem.open("ejemplo.txt");
    file.close();

    verify(lowLevelFileSystem).closeFile(42);
  }

  @Test
  @Disabled("Punto Bonus")
  void sePuedeSaberSiUnPathEsUnArchivoRegular() {
    Assertions.fail("Completar: te va a convenir extraer una nueva abstracción para diferenciar a los paths de los archivos");
  }

  @Test
  @Disabled("Punto Bonus")
  void sePuedeSaberSiUnPathEsUnDirectorio() {
    Assertions.fail("Completar: te va a convenir extraer una nueva abstracción para diferenciar a los paths de los archivos");
  }

  @Test
  @Disabled("Punto Bonus")
  void sePuedeSaberSiUnPathExiste() {
    Assertions.fail("Completar: te va a convenir extraer una nueva abstracción para diferenciar a los paths de los archivos");
  }
}
