package util;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public class Strings {
    public static char[][] asCharMatrix(Stream<? extends String> lines) {
        return asMatrix(lines, String::toCharArray, char[][]::new);
    }

    public static <R> R[] asMatrix(Stream<? extends String> lines,
            Function<? super String, ? extends R> mapper,
            IntFunction<R[]> generator) {
        return lines.map(mapper).toArray(generator);
    }
}
