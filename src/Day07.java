import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;

public class Day07 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("test.txt");
        var part1 = 0L;
        Lines.asStrings(path).forEach(terminal::println);
        terminal.println(part1);
    }
}
