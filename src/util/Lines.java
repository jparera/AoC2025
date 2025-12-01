package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Lines {
    public static List<List<String>> asBlocks(Path file) throws IOException {
        var blocks = new ArrayList<List<String>>();
        blocks.add(new ArrayList<>());
        try (var lines = Files.lines(file)) {
            var it = lines.iterator();
            while (it.hasNext()) {
                var line = it.next();
                if (line.isEmpty()) {
                    blocks.add(new ArrayList<>());
                    continue;
                }
                blocks.getLast().add(line);
            }
        }
        return blocks;
    }

    public static List<String> asStrings(Path file) throws IOException {
        try (var lines = Files.lines(file)) {
            return lines.toList();
        }
    }

    public static int[][] asIntMatrix(Path file) throws IOException {
        return asIntArrays(file).stream().toArray(int[][]::new);
    }

    public static IntMatrix asIntMatrixElements(Path file) throws IOException {
        var matrix = asIntMatrix(file);
        var rows = matrix.length;
        var cols = matrix[0].length;
        var elements = new ArrayList<IntElement>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                elements.add(new IntElement(new Position(r, c), matrix[r][c]));
            }
        }
        return new IntMatrix(rows, cols, matrix, elements);
    }

    public static long[][] asLongMatrix(Path file) throws IOException {
        return asLongArrays(file).stream().toArray(long[][]::new);
    }

    public static LongMatrix asLongMatrixElements(Path file) throws IOException {
        var matrix = asLongMatrix(file);
        var rows = matrix.length;
        var cols = matrix[0].length;
        var elements = new ArrayList<LongElement>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                elements.add(new LongElement(new Position(r, c), matrix[r][c]));
            }
        }
        return new LongMatrix(rows, cols, matrix, elements);
    }

    public static char[][] asCharMatrix(Path file) throws IOException {
        try (var lines = Files.lines(file)) {
            return Strings.asCharMatrix(lines);
        }
    }

    public static CharMatrix asCharMatrixElements(Path file) throws IOException {
        var matrix = asCharMatrix(file);
        var rows = matrix.length;
        var cols = matrix[0].length;
        var elements = new ArrayList<CharElement>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                elements.add(new CharElement(new Position(r, c), matrix[r][c]));
            }
        }
        return new CharMatrix(rows, cols, matrix, elements);
    }

    public static List<int[]> asIntArrays(Path file) throws IOException {
        try (var lines = Files.lines(file)) {
            return Numbers.asIntArrays(lines).toList();
        }
    }

    public static List<long[]> asLongArrays(Path file) throws IOException {
        try (var lines = Files.lines(file)) {
            return Numbers.asLongArrays(lines).toList();
        }
    }

    public static List<String[]> asStringArrays(Path file, Pattern delimiter) throws IOException {
        try (var lines = Files.lines(file)) {
            return lines.map(delimiter::split).toList();
        }
    }

    public static List<List<Integer>> asIntegerLists(Path file) throws IOException {
        return asIntArrays(file).stream().map(l -> IntStream.of(l).boxed().toList()).toList();
    }

    public static List<List<Long>> asLongLists(Path file) throws IOException {
        return asLongArrays(file).stream().map(l -> LongStream.of(l).boxed().toList()).toList();
    }

    public static List<List<String>> asStringLists(Path file, Pattern delimiter) throws IOException {
        return asStringArrays(file, delimiter).stream().map(l -> Stream.of(l).toList()).toList();
    }

    public record Position(int row, int col) {
        public Position add(Position p) {
            return new Position(row + p.row, col + p.col);
        }

        public Position substract(Position p) {
            return new Position(row - p.row, col - p.col);
        }
    }

    public record IntElement(Position position, int value) {
    }

    public record LongElement(Position position, long value) {
    }

    public record CharElement(Position position, char value) {
    }

    public record ObjectElement<T>(Position position, T value) {
    }

    public static class Matrix<E, T> {
        private int rows;

        private int cols;

        private T[] arrays;

        private List<E> elements;

        Matrix(int rows, int cols, T[] arrays, List<E> elements) {
            this.rows = rows;
            this.cols = cols;
            this.arrays = arrays;
            this.elements = new ArrayList<>(elements);
        }

        public boolean contains(Position p) {
            return p.row >= 0 && p.row < rows && p.col >= 0 && p.col < cols;
        }

        public int rows() {
            return rows;
        }

        public int cols() {
            return cols;
        }

        public T[] arrays() {
            return arrays;
        }

        public List<E> elements() {
            return elements;
        }
    }

    public static class IntMatrix extends Matrix<IntElement, int[]> {
        public IntMatrix(int rows, int cols, int[][] arrays, List<IntElement> elements) {
            super(rows, cols, arrays, elements);
        }
    }

    public static class LongMatrix extends Matrix<LongElement, long[]> {
        public LongMatrix(int rows, int cols, long[][] arrays, List<LongElement> elements) {
            super(rows, cols, arrays, elements);
        }
    }

    public static class CharMatrix extends Matrix<CharElement, char[]> {
        public CharMatrix(int rows, int cols, char[][] arrays, List<CharElement> elements) {
            super(rows, cols, arrays, elements);
        }
    }

    public static class ObjectMatrix<T> extends Matrix<ObjectElement<T>, T[]> {
        public ObjectMatrix(int rows, int cols, T[][] arrays, List<ObjectElement<T>> elements) {
            super(rows, cols, arrays, elements);
        }
    }
}
