import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day11 {
    public static void main() throws IOException {
        var input = Path.of("input11.txt");
        Devices.from(input).execute();
    }

    static class Devices {
        private final Map<String, String[]> devices;

        private final Map<String, Long> memo = new HashMap<>();

        public Devices(Map<String, String[]> devices) {
            this.devices = devices;
        }

        public void execute() {
            var terminal = Terminal.get();
            var part1 = paths("you", "out");
            var srv_fft = paths("svr", "fft");
            var fft_dac = paths("fft", "dac");
            var dac_out = paths("dac", "out");
            var part2 = srv_fft * fft_dac * dac_out;
            terminal.println(part1);
            terminal.println(part2);
        }

        private long paths(String from, String to) {
            memo.clear();
            return dfs(from, to);
        }

        private long dfs(String from, String to) {
            if (from.equals(to)) {
                return 1;
            }
            var cached = memo.get(from);
            if (cached != null) {
                return cached;
            }

            var neighbors = devices.get(from);
            if (neighbors == null) {
                return 0L;
            }
            var count = 0L;
            for (String neighbor : neighbors) {
                count += dfs(neighbor, to);
            }
            memo.put(from, count);
            return count;
        }

        public static Devices from(Path input) throws IOException {
            var devices = Lines.asStrings(input).stream().map(Devices::parse)
                    .collect(Collectors.toMap(k -> k[0], v -> Arrays.copyOfRange(v, 1, v.length)));
            return new Devices(devices);
        }

        private static String[] parse(String line) {
            return Pattern.compile("[a-z]{3}").matcher(line).results().map(MatchResult::group).toArray(String[]::new);
        }
    }
}
