import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Day04 {
    private static final char ROLL = '@';

    private static final int[][] OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1}, {0, 1},
            {1, -1}, {1, 0}, {1, 1}
    };

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

        var toRemove = rollsToRemove(positions);
        var part1 = toRemove.size();
        var part2 = 0;
        while (!toRemove.isEmpty()) {
            positions.removeAll(toRemove);
            part2 += toRemove.size();
            toRemove = rollsToRemove(positions);
        }

        terminal.println(part1);
        terminal.println(part2);
    }

    private static Collection<Position> rollsToRemove(Set<Position> positions) {
        return positions.parallelStream()
                .filter(p -> removable(p, positions))
                .toList();
    }

    private static boolean removable(Position p, Set<Position> positions) {
        int count = 0;
        for (var offset : OFFSETS) {
            var neighbor = p.offset(offset);
            if (positions.contains(neighbor)) {
                count++;
            }
        }
        return count < 4;
    }

    record Position(int row, int col) {
        public Position offset(int[] offset) {
            return new Position(row + offset[0], col + offset[1]);
        }
    }
}
