import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Day07 {
    public static void main() throws IOException {
        var input = Path.of("input07.txt");
        Manifold.parse(input).execute();
    }

    private static class Manifold {
        private static final char START = 'S';

        private static final char SPLITTER = '^';

        private final Map<Integer, ? extends List<Splitter>> splittersByCol;

        private final Map<Beam, Long> memo = new HashMap<>();

        private final Set<Splitter> part1 = new HashSet<>();

        private final Beam start;

        private Manifold(Beam start, Map<Integer, ? extends List<Splitter>> splittersByCol) {
            this.start = start;
            this.splittersByCol = splittersByCol;
        }

        public static Manifold parse(Path input) throws IOException {
            var terminal = Terminal.get();
            var manifold = Lines.asCharMatrix(input);
            var rows = manifold.length;
            var cols = manifold[0].length;
            Beam start = null;
            var splitters = new ArrayList<Splitter>();
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (manifold[r][c] == START) {
                        terminal.printf("Start position: (%s, %s)%n", r, c);
                        start = new Beam(r, c);
                    } else if (manifold[r][c] == SPLITTER) {
                        splitters.add(new Splitter(r, c));
                    }
                }
            }
            if (start == null) {
                throw new IllegalStateException("No start position found!");
            }
            terminal.printf("Manifold size: (%s, %s)%n", rows, cols);
            terminal.printf("Start: %s%n", start);
            terminal.printf("Splitters: %s%n", splitters.size());

            splitters.sort(Comparator.comparingInt(Splitter::row));
            var splittersByCol = splitters.stream().collect(Collectors.groupingBy(Splitter::col));

            return new Manifold(start, splittersByCol);
        }

        public void execute() {
            var part2 = countPaths(this.start);
            var terminal = Terminal.get();
            terminal.println(part1.size());
            terminal.println(part2);
        }

        private long countPaths(Beam beam) {
            if (memo.containsKey(beam)) {
                return memo.get(beam);
            }
            long paths = 0;
            var splitter = findFirstSplitterAtOrBelow(beam);
            if (splitter != null) {
                this.part1.add(splitter);
                for (var nextBeam : splitter.split()) {
                    paths = Math.addExact(paths, countPaths(nextBeam));
                }
            } else {
                paths = 1;
            }
            memo.put(beam, paths);
            return paths;
        }

        private Splitter findFirstSplitterAtOrBelow(Beam beam) {
            var splittersOnPath = splittersByCol.get(beam.col());
            if (splittersOnPath == null) {
                return null;
            }
            for (var splitter : splittersOnPath) {
                if (splitter.row() >= beam.row()) {
                    return splitter;
                }
            }
            return null;
        }
    }

    record Splitter(int row, int col) {
        List<Beam> split() {
            return List.of(new Beam(row, col - 1), new Beam(row, col + 1));
        }
    }

    record Beam(int row, int col) {
    }
}
