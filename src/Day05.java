import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Day05 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input05.txt");
        var ranges = Lines.asBlocks(path).get(0).stream().map(Range::parse).toList();
        var ids = Lines.asBlocks(path).get(1).stream().mapToLong(Long::parseLong).toArray();

        var merged = merged(ranges);
        var part1 = Arrays.stream(ids).filter(id -> spoiled(id, merged)).count();
        var part2 = Arrays.stream(merged).mapToLong(Range::length).sum();

        terminal.println(part1);
        terminal.println(part2);
    }

    private static boolean spoiled(long id, Range[] merged) {
        int lo = 0, hi = merged.length - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            if (id < merged[mid].start()) {
                hi = mid - 1;
            } else if (id > merged[mid].end()) {
                lo = mid + 1;
            } else {
                return true;
            }
        }
        return false;
    }

    private static Range[] merged(List<Range> ranges) {
        if (ranges == null || ranges.isEmpty()) return new Range[0];

        var rs = ranges.toArray(Range[]::new);
        Arrays.sort(rs, Comparator.comparingLong(Range::start));

        var merged = new ArrayList<Range>();
        var currentStart = rs[0].start();
        var currentEnd = rs[0].end();
        for (int i = 1; i < ranges.size(); i++) {
            var nextStart = rs[i].start();
            var nextEnd = rs[i].end();
            if (nextStart <= currentEnd + 1) {
                if (nextEnd > currentEnd) {
                    currentEnd = nextEnd;
                }
            } else {
                merged.add(new Range(currentStart, currentEnd));
                currentStart = nextStart;
                currentEnd = nextEnd;
            }
        }
        merged.add(new Range(currentStart, currentEnd));
        return merged.toArray(Range[]::new);
    }

    record Range(long start, long end) {
        public long length() {
            return end - start + 1L;
        }

        public static Range parse(String s) {
            var parts = s.split("-");
            return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        }
    }
}
