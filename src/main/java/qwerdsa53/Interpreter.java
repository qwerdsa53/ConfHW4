package qwerdsa53;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Interpreter {

    private static final int MEMORY_SIZE = 256;
    private final byte[] memory = new byte[MEMORY_SIZE];
    private int pointer = 0;

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java Interpreter <binary_file> <output_result> <memory_range>");
            return;
        }

        String binaryFile = args[0];
        String outputResult = args[1];
        String[] memoryRange = args[2].split("-");
        int start = Integer.parseInt(memoryRange[0]);
        int end = Integer.parseInt(memoryRange[1]);

        Interpreter interpreter = new Interpreter();
        interpreter.execute(binaryFile, outputResult, start, end);
    }

    public void execute(String binaryFile, String outputResult, int start, int end) throws IOException {
        byte[] program = Files.readAllBytes(Path.of(binaryFile));
        int pc = 0;

        while (pc < program.length) {
            byte opcode = program[pc++];
            switch (opcode) {
                case 0x00: // NO-OP (пустая команда)
                    break;
                case 0x01: // LOAD
                    pointer = program[pc++];
                    break;
                case 0x02: // STORE
                    memory[pointer] = program[pc++];
                    break;
                case 0x03: // ADD
                    memory[pointer] += program[pc++];
                    break;
                case 0x04: // SUB
                    memory[pointer] -= program[pc++];
                    break;
                case (byte) 0xFF: // HALT
                    pc = program.length; // Exit program
                    break;
                case (byte) 0xEB: // LOAD CONSTANT
                    int value = (program[pc++] & 0xFF) << 24 | (program[pc++] & 0xFF) << 16 | (program[pc++] & 0xFF) << 8 | (program[pc++] & 0xFF);
                    memory[pointer++] = (byte) (value >>> 24); // старший байт
                    memory[pointer++] = (byte) (value >>> 16); // второй байт
                    memory[pointer++] = (byte) (value >>> 8);  // третий байт
                    memory[pointer++] = (byte) value;          // младший байт
                    break;


                case (byte) 0x3D: // READ FROM MEMORY
                    if (pointer == 0) throw new IllegalStateException("Stack underflow"); // проверка стека
                    int addressToRead = memory[--pointer] & 0xFF; // уменьшаем указатель
                    memory[pointer] = memory[addressToRead]; // записываем значение обратно
                    pointer++; // увеличиваем указатель после записи
                    break;


                case (byte) 0x3E: // WRITE TO MEMORY
                    if (pointer < 2) throw new IllegalStateException("Not enough values on stack"); // проверка стека
                    int valueToWrite = memory[--pointer] & 0xFF; // значение
                    int addressToWrite = memory[--pointer] & 0xFF; // адрес
                    memory[addressToWrite] = (byte) valueToWrite; // записываем значение в память
                    break;


                case (byte) 0x15: // UNARY NOT
                    if (pointer == 0) throw new IllegalStateException("Stack underflow"); // проверка стека
                    memory[pointer - 1] = (byte) ~memory[pointer - 1]; // применяем побитовое НЕ
                    break;

                default:
                    throw new IllegalArgumentException("Unknown opcode: " + opcode);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(outputResult))) {
            writer.write("memory:\n");
            for (int i = start; i <= end; i++) {
                writer.write("  - address: " + i + "\n");
                writer.write("    value: " + memory[i] + "\n");
            }
        }
    }
}
