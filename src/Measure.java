import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Objects;

public class Measure {
    private static final String DEFAULT_LOOPS = "10";

    private static final Solution[] SOLUTIONS = {
            Day01::main,
    };

    public static void main(String[] args) throws Exception {
        var out = System.out;
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));

        var loops = Integer.parseInt(parameterValue(args, "--loops", DEFAULT_LOOPS));
        int day = Integer.parseInt(args[args.length - 1]);
        var solution = Objects.requireNonNull(SOLUTIONS[day - 1]);

        out.printf("Day %02d solution benchmarking\n", day);

        out.print("Warming up...");
        var warmupLoops = Math.min(Math.max(loops / 10, 3), 100);
        for (int i = 0; i < Math.max(loops / 10, 3); i++) {
            solution.call(new String[0]);
        }
        out.printf(" done after %d iterations\n", warmupLoops);

        out.print("Measuring...");
        var start = System.nanoTime();
        for (int i = 0; i < loops; i++) {
            solution.call(new String[0]);
        }
        var end = System.nanoTime();
        out.printf(" done after %d iterations\n", loops);

        var tdelta = (double) end - start;
        var ldelta = tdelta / loops;
        var tconv = NanoConversor.find(tdelta);
        var lconv = NanoConversor.find(ldelta);
        var ttime = tconv.convert(tdelta);
        var ltime = lconv.convert(ldelta);
        out.printf("Total execution time: %.1f %s\n", ttime, tconv.text());
        out.printf("Execution time per iteration: %.1f %s\n", ltime, lconv.text());
    }

    private static String parameterValue(String[] args, String name, String defaultValue) {
        String value = null;
        for (var i = 0; i < args.length; i++) {
            if (name.equals(args[i]) && i + 1 < args.length && !args[i + 1].startsWith("-")) {
                value = args[i + 1];
            }
        }
        return Objects.toString(value, defaultValue);
    }

    private record NanoConversor(String text, int factor) {
        private final static NanoConversor IDENTITY = new NanoConversor("ns", 1);

        private final static NanoConversor[] CONVERSORS = {
                new NanoConversor("s", 1000000000),
                new NanoConversor("ms", 1000000),
                new NanoConversor("μs", 1000),
                IDENTITY,
        };

        static NanoConversor find(double nanos) {
            return Arrays.stream(CONVERSORS)
                    .filter(c -> c.convert(nanos) > 1)
                    .findFirst().orElse(IDENTITY);
        }

        double convert(double nanos) {
            return nanos / factor;
        }
    }

    @FunctionalInterface
    private interface Solution {
        void call(String[] args) throws Exception;
    }
}
