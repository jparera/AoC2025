import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Day05 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input05.txt");
        var ranges = Lines.asBlocks(path).get(0).parallelStream().map(Range::parse).toList();
        var ids = Lines.asBlocks(path).get(1).parallelStream().mapToLong(Long::parseLong).toArray();

        var part1 = Arrays.stream(ids).parallel().filter(id -> spoiled(id, ranges)).count();
        var part2 = countFreshIds(ranges);

        terminal.println(part1);
        terminal.println(part2);
    }

    private static boolean spoiled(long id, List<Range> ranges) {
        return ranges.parallelStream().anyMatch(range -> range.contains(id));
    }

    private static long countFreshIds(List<Range> ranges) {
        ranges = new ArrayList<>(ranges);
        ranges.sort(Comparator.comparingLong(Range::start));
        var fused = new LinkedList<Range>();
        fused.offer(ranges.getFirst());
        for (var range : ranges) {
            var previous = fused.getLast();
            if (range.overlaps(previous)) {
                fused.removeLast();
                fused.offer(new Range(
                        Math.min(range.start(), previous.start()),
                        Math.max(range.end(), previous.end())));
            } else {
                fused.offer(range);
            }
        }
        return fused.parallelStream().mapToLong(Range::length).sum();
    }

    record Range(long start, long end) {
        public boolean contains(long value) {
            return start <= value && value <= end;
        }

        public long length() {
            return end - start + 1;
        }

        public boolean overlaps(Range other) {
            return this.start <= other.end && other.start <= this.end;
        }

        public static Range parse(String s) {
            var parts = s.split("-");
            return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        }
    }
}
