import util.Lines;
import util.Numbers;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class Day06 {
    private static final char ADD = '+';
    private static final char MUL = '*';

    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input06.txt");
        var lines = new LinkedList<>(Lines.asStrings(path));
        var operations = Pattern.compile("[+*]\\s*(?=\\s|$)")
                .matcher(lines.removeLast())
                .results()
                .map(MatchResult::group)
                .toArray(String[]::new);

        terminal.println(part1(lines, operations));
        terminal.println(part2(lines, operations));
    }

    private static long part1(List<String> numbers, String[] operations) {
        var operands = Numbers.asIntArrays(numbers);

        var output = initializeOutput(operations);
        for (var operand : operands) {
            for (int i = 0; i < operations.length; i++) {
                switch (operations[i].charAt(0)) {
                    case ADD -> output[i] += operand[i];
                    case MUL -> output[i] *= operand[i];
                    default -> throw new IllegalArgumentException("Unknown operation: " + operations[i]);
                }
            }
        }
        return Arrays.stream(output).sum();
    }

    private static long part2(List<String> numbers, String[] operations) {
        var matrix = numbers.stream().map(String::toCharArray).toArray(char[][]::new);
        var rows = matrix.length;
        var cols = matrix[0].length;

        var operands = new long[cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (matrix[i][j] == ' ') continue;
                operands[j] = operands[j] * 10 + (matrix[i][j] - '0');
            }
        }

        var output = initializeOutput(operations);
        for (int i = 0, j = 0; i < operations.length; i++) {
            int operandsCount = operations[i].length();
            for (int k = 0; k < operandsCount; k++) {
                switch (operations[i].charAt(0)) {
                    case ADD -> output[i] += operands[j + k];
                    case MUL -> output[i] *= operands[j + k];
                    default -> throw new IllegalArgumentException("Unknown operation: " + operations[i]);
                }
            }
            j += operations[i].length() + 1;
        }

        return Arrays.stream(output).sum();
    }

    private static long[] initializeOutput(String[] operations) {
        var output = new long[operations.length];
        for (int i = 0; i < operations.length; i++) {
            output[i] = switch (operations[i].charAt(0)) {
                case ADD -> 0;
                case MUL -> 1;
                default -> throw new IllegalArgumentException("Unknown operation: " + operations[i]);
            };
        }
        return output;
    }
}