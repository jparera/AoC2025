package util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Sets {
    public static <K, V> HashSet<V> computeHashSet(K key) {
        return new HashSet<>();
    }

    public static Set<Integer> toSet(int[] values) {
        return new HashSet<>(IntStream.of(values).boxed().toList());
    }

    public static Set<Long> toSet(long[] values) {
        return new HashSet<>(LongStream.of(values).boxed().toList());
    }

    public static <T> Set<T> intersection(Set<T> s1, Set<T> s2) {
        return new HashSet<>(s1.stream().filter(s2::contains).toList());
    }
}
