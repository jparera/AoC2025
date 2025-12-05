import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

final String DEFAULT_LOOPS = "10";

final Solution[] SOLUTIONS = {
        Day01::main, Day02::main, Day03::main, Day04::main, Day05::main
};

void main(String[] args) throws Exception {
    var out = System.out;
    System.setOut(new PrintStream(OutputStream.nullOutputStream()));

    var loops = Integer.parseInt(parameterValue(args, "--loops", DEFAULT_LOOPS));
    int day = Integer.parseInt(args[args.length - 1]);
    var solution = Objects.requireNonNull(SOLUTIONS[day - 1]);

    out.printf("Day %02d solution benchmarking\n", day);

    out.print("Warming up...");
    var warmupLoops = Math.min(Math.max(loops / 10, 3), 100);
    for (int i = 0; i < warmupLoops; i++) {
        solution.call();
    }
    out.printf(" done after %d iterations\n", warmupLoops);

    out.print("Measuring...");
    var start = System.nanoTime();
    for (int i = 0; i < loops; i++) {
        solution.call();
    }
    var end = System.nanoTime();
    out.printf(" done after %d iterations\n", loops);

    var tdelta = (double) end - start;
    var ldelta = tdelta / loops;
    var tconv = NanoConverter.find(tdelta);
    var lconv = NanoConverter.find(ldelta);
    var ttime = tconv.convert(tdelta);
    var ltime = lconv.convert(ldelta);
    out.printf("Total execution time: %.1f %s\n", ttime, tconv.text());
    out.printf("Execution time per iteration: %.1f %s\n", ltime, lconv.text());
}

String parameterValue(String[] args, String name, String defaultValue) {
    String value = null;
    for (var i = 0; i < args.length; i++) {
        if (name.equals(args[i]) && i + 1 < args.length && !args[i + 1].startsWith("-")) {
            value = args[i + 1];
        }
    }
    return Objects.toString(value, defaultValue);
}

record NanoConverter(String text, int factor) {
    private final static NanoConverter IDENTITY = new NanoConverter("ns", 1);

    private final static NanoConverter[] CONVERTERS = {
            new NanoConverter("s", 1000000000),
            new NanoConverter("ms", 1000000),
            new NanoConverter("μs", 1000),
            IDENTITY,
    };

    static NanoConverter find(double nanos) {
        return Arrays.stream(CONVERTERS)
                .filter(c -> c.convert(nanos) > 1)
                .findFirst().orElse(IDENTITY);
    }

    double convert(double nanos) {
        return nanos / factor;
    }
}

@FunctionalInterface
interface Solution {
    void call() throws Exception;
}
