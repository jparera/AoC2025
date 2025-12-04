import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day04 {
    private static final char ROLL = '@';

    public static void main() throws IOException {
        var terminal = Terminal.get();
        var path = Path.of("input04.txt");

        var map = Lines.asCharMatrix(path);
        var positions = new HashSet<Position>();

        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                if (map[row][col] == ROLL) {
                    positions.add(new Position(row, col));
                }
            }
        }

        // Find all removable positions
        var toRemove = positions.parallelStream()
                .filter(p -> removable(p, positions))
                .collect(Collectors.toSet());
        var part1 = toRemove.size();
        var part2 = 0;
        while (!toRemove.isEmpty()) {
            positions.removeAll(toRemove);
            part2 += toRemove.size();
            // Check neighbours of removed positions for further removals
            toRemove = toRemove.parallelStream()
                    .flatMap(Position::neighbours)
                    .filter(p -> removable(p, positions))
                    .collect(Collectors.toSet());
        }

        terminal.println(part1);
        terminal.println(part2);
    }

    private static boolean removable(Position p, Set<Position> positions) {
        if(!positions.contains(p)) return false;
        int count = 0;
        for (int[] offset : OFFSETS) {
            var neighbor = p.add(offset);
            if (positions.contains(neighbor)) {
                count++;
            }
        }
        return count < 4;
    }

    private static final int[][] OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

    record Position(int row, int col) {
        public Stream<Position> neighbours() {
            return Arrays.stream(OFFSETS).map(this::add);
        }

        public Position add(int[] offset) {
            return new Position(row + offset[0], col + offset[1]);
        }
    }
}
