
import java.io.IOException;
import java.nio.file.Path;
import util.Lines;
import util.Terminal;

public class Day01 {
    private static final int DIAL_START = 50;
    private static final int DIAL_LENGTH = 100;

    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input01.txt");

        var dial = DIAL_START;
        var part1 = 0;
        var part2 = 0;

        for (var instruction : Lines.asStrings(path)) {
            var direction = instruction.substring(0, 1).charAt(0);
            var count = Integer.parseInt(instruction.substring(1));

            var complete = count / DIAL_LENGTH;
            var remainder = count % DIAL_LENGTH;

            int end = dial + (switch (direction) {
                case 'L' -> -remainder;
                case 'R' -> remainder;
                default -> throw new IllegalStateException("Unexpected direction: " + direction);
            });

            if (end % 100 == 0) {
                part1++; 
            }

            if (complete > 0) {
                part2 += complete;
            }
            if (remainder > 0 && dial != 0 && (end <= 0 || end >= DIAL_LENGTH)) {
                part2++;
            }

            dial = end % DIAL_LENGTH;
            if (dial < 0) {
                dial += DIAL_LENGTH;
            }
        }

        terminal.printf("%s\n", part1);
        terminal.printf("%s\n", part2);
    }
}
