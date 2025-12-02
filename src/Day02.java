import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.LongStream;

public class Day02 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input02.txt");

        var ranges = Lines.asStrings(path).stream()
                .flatMap(Pattern.compile(",")::splitAsStream)
                .map(Range::parse)
                .toList();

        var part1 = ranges.parallelStream()
                .flatMapToLong(Range::invalidsIdsPart1)
                .sum();

        var part2 = ranges.parallelStream()
                .flatMapToLong(Range::invalidsIdsPart2)
                .sum();

        terminal.println(part1);
        terminal.println(part2);
    }

    record Range(String start, String end) {
        public LongStream invalidsIdsPart1() {
            return invalidsIds(length -> length % 2 != 0 ? List.of() : List.of(2));
        }

        public LongStream invalidsIdsPart2() {
            return invalidsIds(Range::divisors);
        }

        private LongStream invalidsIds(Function<Integer, List<Integer>> divisors) {
            var min = Long.parseLong(this.start);
            var max = Long.parseLong(this.end);
            var invalids = new HashSet<Long>();
            for (var length : List.of(start.length(), end.length())) {
                for (var divisor : divisors.apply(length)) {
                    var blockLength = length / divisor;
                    var numbers = numbers(blockLength);
                    var factor = Math.powExact(10, blockLength);
                    for (var number : numbers) {
                        long value = 0;
                        for (int i = 0; i < divisor; i++) {
                            value = value * factor + number;
                        }
                        if (value >= min && value <= max) {
                            invalids.add(value);
                        }
                    }
                }
            }
            return invalids.stream().mapToLong(Long::longValue);
        }

        private static final Map<Integer, List<Long>> MEMO_NUMBERS = new ConcurrentHashMap<>();

        private static List<Long> numbers(int length) {
            var cached = MEMO_NUMBERS.get(length);
            if (cached != null) {
                return cached;
            }
            var numbers = new ArrayList<Long>();
            var start = Math.powExact(10L, length - 1);
            var end = Math.powExact(10L, length);
            for (long i = start; i < end; i++) {
                numbers.add(i);
            }
            MEMO_NUMBERS.put(length, numbers);
            return numbers;
        }

        private static final Map<Integer, List<Integer>> MEMO_DIVISORS = new ConcurrentHashMap<>();

        private static List<Integer> divisors(int value) {
            var cached = MEMO_DIVISORS.get(value);
            if (cached != null) {
                return cached;
            }
            var divisors = new ArrayList<Integer>();
            for (int i = 2; i <= value; i++) {
                if (value % i == 0) {
                    divisors.add(i);
                }
            }
            MEMO_DIVISORS.put(value, divisors);
            return divisors;
        }

        public static Range parse(String value) {
            var parts = value.split("-");
            return new Range(parts[0], parts[1]);
        }
    }
}
