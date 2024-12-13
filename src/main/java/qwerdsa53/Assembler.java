package qwerdsa53;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Assembler {

    private static final Map<String, Byte> COMMANDS = Map.of(
            "LOAD", (byte) 0x01,
            "STORE", (byte) 0x02,
            "ADD", (byte) 0x03,
            "SUB", (byte) 0x04,
            "HALT", (byte) 0xFF
    );

    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Usage: java Assembler <source_file> <output_binary> <log_file>");
            return;
        }

        String sourceFile = args[0];
        String outputBinary = args[1];
        String logFile = args[2];

        List<String> lines = Files.readAllLines(Path.of(sourceFile));
        ByteArrayOutputStream binaryOutput = new ByteArrayOutputStream();
        StringBuilder logOutput = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0];
            byte opcode = COMMANDS.getOrDefault(command, (byte) 0x00);

            logOutput.append("command=").append(command).append(", opcode=").append(String.format("0x%02X", opcode));

            binaryOutput.write(opcode);
            if (parts.length > 1) {
                String operandString = parts[1].replace(",", "");
                byte operand;
                if (operandString.startsWith("0x")) {
                    operand = (byte) Integer.parseInt(operandString.substring(2), 16);
                } else {
                    operand = Byte.parseByte(operandString);
                }
                logOutput.append(", operand=").append(operand);
                binaryOutput.write(operand);
            }

            logOutput.append("\n");
        }

        Files.write(Path.of(outputBinary), binaryOutput.toByteArray());
        Files.write(Path.of(logFile), logOutput.toString().getBytes());
    }
}

