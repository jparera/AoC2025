package util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;

public class Terminal {
    private static final String DEFAULT_SEPARATOR = ",";

    private final Output output;

    interface Output {
        void print(String value);
    }

    Terminal(Output output) {
        this.output = output;
    }

    public static Terminal get() {
        return new Terminal(System.out::print);
    }

    public Terminal enterAlternateScreen() {
        return execute(Job.ENTER_ALTERNATE_SCREEN);
    }

    public Terminal exitAlternateScreen() {
        return execute(Job.EXIT_ALTERNATE_SCREEN);
    }

    public Terminal clear() {
        return execute(Job.CLEAR);
    }

    public Terminal cursorHome() {
        return execute(Job.CURSOR_HOME);
    }

    public Terminal print(int[][] matrix) {
        return print(matrix, _ -> false, _ -> false);
    }

    public Terminal print(int[][] matrix, IntPredicate highlightRow) {
        return print(matrix, highlightRow, _ -> false);
    }

    public Terminal print(int[][] matrix, IntPredicate highlightRow, IntPredicate highlightCol) {
        var maxDigits = Arrays.stream(matrix).flatMapToInt(Arrays::stream)
                .map(Terminal::digits).reduce(0, Integer::max);
        var max = digits(matrix.length - 1);
        for (int i = 0; i < matrix.length; i++) {
            printf("%s: ", leftPad(Integer.toString(i), max));
            print(matrix[i], maxDigits, highlightRow.test(i), highlightCol);
        }
        return this;
    }

    public Terminal print(int[] array) {
        return print(array, _ -> false);
    }

    public Terminal print(int[] array, IntPredicate highlightCol) {
        var max = Arrays.stream(array).map(Terminal::digits).reduce(0, Integer::max);
        return print(array, max, false, highlightCol);
    }

    private Terminal print(
            int[] array,
            int leftPad,
            boolean highlightRow,
            IntPredicate highlightCol) {
        return iterate(
                0, array.length,
                i -> leftPad(Long.toString(array[i]), leftPad),
                DEFAULT_SEPARATOR, highlightRow, highlightCol);
    }

    public Terminal print(char[][] matrix) {
        return print(matrix, _ -> false, _ -> false);
    }

    public Terminal print(char[][] matrix, IntPredicate highlightRow) {
        return print(matrix, highlightRow, _ -> false);
    }

    public Terminal print(char[][] matrix, IntPredicate highlightRow, IntPredicate highlightCol) {
        var max = digits(matrix.length - 1);
        for (int i = 0; i < matrix.length; i++) {
            printf("%s: ", leftPad(Integer.toString(i), max));
            print(matrix[i], highlightRow.test(i), highlightCol);
        }
        return this;
    }

    public Terminal print(char[] array) {
        return print(array, _ -> false);
    }

    public Terminal print(char[] array, IntPredicate highlightCol) {
        return print(array, false, highlightCol);
    }

    private Terminal print(
            char[] array,
            boolean highlightRow,
            IntPredicate highlightCol) {
        return iterate(
                0, array.length,
                i -> Character.toString(array[i]),
                "", highlightRow, highlightCol);
    }

    public Terminal print(long[][] matrix) {
        return print(matrix, _ -> false, _ -> false);
    }

    public Terminal print(long[][] matrix, IntPredicate highlightRow, long[][] ls) {
        return print(ls, highlightRow, _ -> false);
    }

    public Terminal print(long[][] matrix, IntPredicate highlightRow, IntPredicate highlightCol) {
        var maxDigits = Arrays.stream(matrix).flatMapToLong(Arrays::stream)
                .mapToInt(Terminal::digits)
                .reduce(0, Integer::max);
        var max = digits(matrix.length - 1);
        for (int i = 0; i < matrix.length; i++) {
            printf("%s: ", leftPad(Integer.toString(i), max));
            print(matrix[i], maxDigits, highlightRow.test(i), highlightCol);
        }
        return this;
    }

    public Terminal print(long[] array) {
        return print(array, _ -> false);
    }

    public Terminal print(long[] array, IntPredicate highlightCol) {
        var max = Arrays.stream(array).mapToInt(Terminal::digits).reduce(0, Integer::max);
        return print(array, max, false, highlightCol);
    }

    private Terminal print(
            long[] array,
            int leftPad,
            boolean highlightBackground,
            IntPredicate highlightCol) {
        return iterate(
                0, array.length,
                i -> leftPad(Long.toString(array[i]), leftPad),
                DEFAULT_SEPARATOR, highlightBackground, highlightCol);
    }

    public <T> Terminal print(T[][] matrix) {
        return print(matrix, _ -> false, _ -> false);
    }

    public <T> Terminal print(T[][] matrix, IntPredicate highlightRow) {
        return print(matrix, highlightRow, _ -> false);
    }

    public <T> Terminal print(T[][] matrix, IntPredicate highlightRow, IntPredicate highlightCol) {
        var max = Arrays.stream(matrix).flatMap(Arrays::stream)
                .map(Objects::toString).mapToInt(String::length)
                .reduce(0, Integer::max);
        var maxRowDigits = digits(matrix.length - 1);
        for (int i = 0; i < matrix.length; i++) {
            printf("%s: ", leftPad(Integer.toString(i), maxRowDigits));
            print(matrix[i], max, highlightRow.test(i), highlightCol);
        }
        return this;
    }

    public <T> Terminal print(T[] array) {
        return print(array, _ -> false);
    }

    public <T> Terminal print(T[] array, IntPredicate highlightCol) {
        var max = Arrays.stream(array)
                .map(Objects::toString).mapToInt(String::length)
                .reduce(0, Integer::max);
        return print(array, max, false, highlightCol);
    }

    private <T> Terminal print(
            T[] array,
            int leftPad,
            boolean highlightBackground,
            IntPredicate highlightCol) {
        return iterate(
                0, array.length,
                i -> leftPad(Objects.toString(array[i]), leftPad),
                DEFAULT_SEPARATOR, highlightBackground, highlightCol);
    }

    private Terminal iterate(
            int start,
            int end,
            IntFunction<String> fn,
            String separator,
            boolean highlightRow,
            IntPredicate highlightCol) {
        var builder = Job.builder();
        builder.accentColor().append('[').turnOffAttributes();
        for (int i = start; i < end; i++) {
            var hc = highlightCol.test(i);
            if (highlightRow || hc) {
                builder.highlightBackground().highlightColor();
            } else {
                builder.defaultColor();
            }
            var value = fn.apply(i);
            builder.append(value);
            if (highlightRow || hc) {
                builder.turnOffAttributes();
                if (highlightRow) {
                    builder.highlightBackground().highlightColor();
                }
            }
            if (i < end - 1) {
                builder.accentColor().append(separator).turnOffAttributes();
            }
        }
        if (highlightRow) {
            builder.turnOffAttributes();
        }
        builder.accentColor().append(']').turnOffAttributes().lineSeparator();
        execute(builder.build());
        return this;
    }

    public Terminal lineSeparator() {
        return printf(System.lineSeparator());
    }

    public Terminal printf(String format, Object... args) {
        output.print(String.format(format, args));
        return this;
    }

    public Terminal println(Object text) {
        output.print(text + System.lineSeparator());
        return this;
    }

    private Terminal execute(Job job) {
        output.print(job.toString());
        return this;
    }

    private static int digits(long value) {
        int count = value <= 0 ? 1 : 0;
        value = Math.abs(value);
        while (value > 0) {
            value = value / 10;
            count++;
        }
        return count;
    }

    private static String leftPad(String value, int length) {
        if (value.length() >= length) {
            return value;
        }
        var buffer = new StringBuilder();
        int padding = length - value.length();
        buffer.repeat(' ', padding);
        buffer.append(value);
        return buffer.toString();
    }

    private record Job(String command) {
            private static final String ESC = "\u001B";

            static Job ENTER_ALTERNATE_SCREEN = new Job(ESC + "[?1049h");

        static Job EXIT_ALTERNATE_SCREEN = new Job(ESC + "[?1049l");

            static Job CLEAR = new Job(ESC + "[2J");

            static Job CURSOR_HOME = new Job(ESC + "[H");

            static Job TURNOFF_ATTRIBUTES = new Job(ESC + "[0m");

            static Job HIGHLIGHT_COLOR = color(0x99, 0xFF, 0x99);

            static Job ACCENT_COLOR = color(0xE6, 0x41, 0x0B);

            static Job DEFAULT_COLOR = color(0x00, 0x99, 0x00);

            static Job HIGHLIGHT_BACKGROUND = background(0x05, 0x05, 0x05);

        @Override
            public String toString() {
                return command;
            }

            public static Builder builder() {
                return new Builder();
            }

            public static Job color(int r, int g, int b) {
                return new Job(ESC + "[38;2;" + r + ";" + g + ";" + b + "m");
            }

            public static Job background(int r, int g, int b) {
                return new Job(ESC + "[48;2;" + r + ";" + g + ";" + b + "m");
            }

            static class Builder {
                private final StringBuilder buffer = new StringBuilder();

                public Job build() {
                    return new Job(buffer.toString());
                }

                public Builder turnOffAttributes() {
                    return append(TURNOFF_ATTRIBUTES);
                }

                public Builder highlightColor() {
                    return append(HIGHLIGHT_COLOR);
                }

                public Builder accentColor() {
                    return append(ACCENT_COLOR);
                }

                public Builder defaultColor() {
                    return append(DEFAULT_COLOR);
                }

                public Builder highlightBackground() {
                    return append(HIGHLIGHT_BACKGROUND);
                }

                public Builder lineSeparator() {
                    return append(System.lineSeparator());
                }

                public Builder append(Job job) {
                    return append(job.toString());
                }

                public Builder append(char c) {
                    buffer.append(c);
                    return this;
                }

                public Builder append(String text) {
                    buffer.append(text);
                    return this;
                }
            }
        }
}
