import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;

public class Day03 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input03.txt");
        var banks = Lines.asStrings(path);
        var part1 = banks.parallelStream().mapToLong(bank -> jolts(bank, 2)).sum();
        var part2 = banks.parallelStream().mapToLong(bank -> jolts(bank, 12)).sum();
        terminal.println(part1);
        terminal.println(part2);
    }

    private static long jolts(String bank, int cells) {
        int current = 0;
        long jolts = 0L;
        while (cells-- > 0) {
            for (int i = 0; i < 9; i++) {
                var found = bank.indexOf('9' - i, current);
                if (found != -1 && found + cells < bank.length()) {
                    current = found + 1;
                    jolts = jolts * 10 + (9 - i);
                    break;
                }
            }
        }
        return jolts;
    }
}
