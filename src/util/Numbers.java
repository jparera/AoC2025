package util;

import java.util.Collection;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Numbers {
    private static final Pattern NUMBERS = Pattern.compile("-?\\d+");

    public static List<int[]> asIntArrays(Collection<? extends String> lines) {
        return asIntArrays(lines.stream()).toList();
    }

    public static Stream<int[]> asIntArrays(Stream<? extends String> lines) {
        return asIntStreams(lines).map(IntStream::toArray);
    }

    public static Stream<IntStream> asIntStreams(Stream<? extends String> lines) {
        return lines.map(Numbers::asIntStream);
    }

    public static IntStream asIntStream(String line) {
        return NUMBERS.matcher(line).results().map(MatchResult::group).mapToInt(Integer::parseInt);
    }

    public static List<long[]> asLongArrays(Collection<? extends String> lines) {
        return asLongArrays(lines.stream()).toList();
    }

    public static Stream<long[]> asLongArrays(Stream<? extends String> lines) {
        return asLongStreams(lines).map(LongStream::toArray);
    }

    public static Stream<LongStream> asLongStreams(Stream<? extends String> lines) {
        return lines.map(Numbers::asLongStream);
    }

    public static LongStream asLongStream(String line) {
        return NUMBERS.matcher(line).results().map(MatchResult::group).mapToLong(Long::parseLong);
    }
}
