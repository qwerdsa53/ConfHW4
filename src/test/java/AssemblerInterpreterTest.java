import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import qwerdsa53.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssemblerInterpreterTest {

    private static final String BINARY_FILE = "test.bin";
    private static final String RESULT_FILE = "result.yaml";

    @BeforeEach
    void setUp() throws IOException {
        Files.deleteIfExists(Path.of(BINARY_FILE));
        Files.deleteIfExists(Path.of(RESULT_FILE));
    }

    @Test
    void testLoadConstant() throws IOException {
        Files.write(Path.of(BINARY_FILE), new byte[]{
                (byte) 0xEB, 0x00, 0x00, 0x02, 0x5F // 607 в формате 4 байт
        });

        Interpreter.main(new String[]{BINARY_FILE, RESULT_FILE, "0-10"});

        assertTrue(Files.exists(Path.of(RESULT_FILE)));
        String resultContent = Files.readString(Path.of(RESULT_FILE));

        System.out.println(resultContent);

        assertTrue(resultContent.contains("address: 0"));
        assertTrue(resultContent.contains("value: 0"));  // старший байт
        assertTrue(resultContent.contains("address: 1"));
        assertTrue(resultContent.contains("value: 0"));  // второй байт
        assertTrue(resultContent.contains("address: 2"));
        assertTrue(resultContent.contains("value: 2"));  // третий байт
        assertTrue(resultContent.contains("address: 3"));
        assertTrue(resultContent.contains("value: 95")); // младший байт
    }


    @Test
    void testReadFromMemory() throws IOException {
        Files.write(Path.of(BINARY_FILE), new byte[]{
                (byte) 0xEB, 0x00, 0x00, 0x00, 0x03, // LOAD CONSTANT 3
                (byte) 0x3D                           // READ FROM MEMORY
        });

        Interpreter.main(new String[]{BINARY_FILE, RESULT_FILE, "0-10"});

        assertTrue(Files.exists(Path.of(RESULT_FILE)));
        String resultContent = Files.readString(Path.of(RESULT_FILE));
        assertTrue(resultContent.contains("address: 3")); // правильный адрес
        assertTrue(resultContent.contains("value: 0"));  // значение в памяти
    }

    @Test
    void testWriteToMemory() throws IOException {
        Files.write(Path.of(BINARY_FILE), new byte[]{(byte) 0xEB, 0x00, 0x00, 0x00, 0x01, (byte) 0xEB, 0x00, 0x00, 0x00, 0x05, (byte) 0x3E});

        Interpreter.main(new String[]{BINARY_FILE, RESULT_FILE, "0-10"});

        assertTrue(Files.exists(Path.of(RESULT_FILE)));
        String resultContent = Files.readString(Path.of(RESULT_FILE));
        assertTrue(resultContent.contains("address: 1"));
        assertTrue(resultContent.contains("value: 5"));
    }


    @Test
    void testUnaryNotOperation() throws IOException {
        Files.write(Path.of(BINARY_FILE), new byte[]{
                (byte) 0xEB, 0x00, 0x00, 0x00, (byte) 0xFF, // LOAD CONSTANT -1
                (byte) 0x15                          // UNARY NOT
        });

        Interpreter.main(new String[]{BINARY_FILE, RESULT_FILE, "0-10"});

        assertTrue(Files.exists(Path.of(RESULT_FILE)));
        String resultContent = Files.readString(Path.of(RESULT_FILE));
        assertTrue(resultContent.contains("value: 0")); // побитовая инверсия -1 -> 0
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(Path.of(BINARY_FILE));
        Files.deleteIfExists(Path.of(RESULT_FILE));
    }

}
