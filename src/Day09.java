import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Day09 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var input = Path.of("input09.txt");

        // Read red tiles are the corners of the shape
        var cornerTiles = Lines.asStrings(input).stream().map(Tile::parse).toArray(Tile[]::new);

        // Compute all perimeter tiles (including corners)
        var perimeterTiles = getPerimeterTiles(cornerTiles);

        // Compute all outside tiles using a flood fill from outside the shape
        var outsideTiles = getOutsideTiles(cornerTiles, perimeterTiles);

        terminal.printf("Corner tiles: %d%n", cornerTiles.length);
        terminal.printf("Perimeter tiles: %d%n", perimeterTiles.size());
        terminal.printf("Outside tiles: %d%n", outsideTiles.size());

        var heap = new PriorityQueue<Pair>();
        var columnIndex = buildColumnIndex(outsideTiles);
        for (int i = 0; i < cornerTiles.length; i++) {
            for (int j = i + 1; j < cornerTiles.length; j++) {
                var area = cornerTiles[i].area(cornerTiles[j]);
                heap.add(new Pair(i, j, area));
            }
        }
        var part1 = heap.peek();
        terminal.println(part1 == null ? 0 : part1.area());
        var part2 = 0L;
        while (!heap.isEmpty()) {
            var pair = heap.poll();
            if (isValid(cornerTiles[pair.i()], cornerTiles[pair.j()], columnIndex)) {
                part2 = pair.area();
                break;
            }
        }
        terminal.println(part2);
    }

    private static HashSet<Tile> getOutsideTiles(Tile[] cornerTiles, HashSet<Tile> perimeterTiles) {
        var outsideTiles = new HashSet<Tile>();
        var stack = new ArrayDeque<Tile>();
        var visited = new HashSet<Tile>();
        var start = new Tile(Integer.MAX_VALUE, Integer.MAX_VALUE);
        for (var tiles : cornerTiles) {
            if (tiles.col() < start.col() || (tiles.col() == start.col() && tiles.row() < start.row())) {
                start = tiles;
            }
        }
        stack.push(new Tile(start.col() - 1, start.row() - 1));
        while (!stack.isEmpty()) {
            var current = stack.pop();
            if (perimeterTiles.contains(current) || visited.contains(current)) {
                continue;
            }
            visited.add(current);
            var neighborTiles = current.neighbors();
            var foundPerimeter = false;
            for (var neighbor : neighborTiles) {
                if (perimeterTiles.contains(neighbor)) {
                    foundPerimeter = true;
                    break;
                }
            }
            if (!foundPerimeter) {
                continue;
            }
            outsideTiles.add(current);
            for (var neighbor : neighborTiles) {
                stack.push(neighbor);
            }
        }
        return outsideTiles;
    }

    private static boolean isValid(Tile a, Tile b, NavigableMap<Integer, NavigableSet<Integer>> columnIndex) {
        var minCol = Math.min(a.col(), b.col());
        var maxCol = Math.max(a.col(), b.col());
        var minRow = Math.min(a.row(), b.row());
        var maxRow = Math.max(a.row(), b.row());
        var sub = columnIndex.subMap(minCol, true, maxCol, true);
        for (var entry : sub.entrySet()) {
            NavigableSet<Integer> rows = entry.getValue();
            Integer r = rows.ceiling(minRow);
            if (r != null && r <= maxRow) {
                return false;
            }
        }
        return true;
    }

    private static NavigableMap<Integer, NavigableSet<Integer>> buildColumnIndex(Set<Tile> outside) {
        var index = new TreeMap<Integer, NavigableSet<Integer>>();
        for (var t : outside) {
            index.computeIfAbsent(t.col(), _ -> new TreeSet<>()).add(t.row());
        }
        return index;
    }

    private static HashSet<Tile> getPerimeterTiles(Tile[] cornerTiles) {
        var perimeterTails = new HashSet<Tile>();
        for (int i = 0; i < cornerTiles.length; i++) {
            int j = (i + 1) % cornerTiles.length;
            if (cornerTiles[i].col() - cornerTiles[j].col() == 0) {
                var min = Math.min(cornerTiles[i].row(), cornerTiles[j].row());
                var max = Math.max(cornerTiles[i].row(), cornerTiles[j].row());
                for (int k = min; k <= max; k++) {
                    perimeterTails.add(new Tile(cornerTiles[i].col(), k));
                }
            } else {
                var min = Math.min(cornerTiles[i].col(), cornerTiles[j].col());
                var max = Math.max(cornerTiles[i].col(), cornerTiles[j].col());
                for (int k = min; k <= max; k++) {
                    perimeterTails.add(new Tile(k, cornerTiles[i].row()));
                }
            }
        }
        return perimeterTails;
    }

    record Pair(int i, int j, long area) implements Comparable<Pair> {
        @Override
        public int compareTo(Pair o) {
            return Long.compare(o.area, this.area);
        }
    }

    record Tile(int col, int row) {
        public long area(Tile other) {
            return Math.multiplyFull(Math.abs(this.col - other.col) + 1, Math.abs(this.row - other.row) + 1);
        }

        public List<Tile> neighbors() {
            return Arrays.asList(
                    new Tile(col + 1, row + 1),
                    new Tile(col - 1, row + 1),
                    new Tile(col + 1, row - 1),
                    new Tile(col - 1, row - 1),
                    new Tile(col + 1, row),
                    new Tile(col - 1, row),
                    new Tile(col, row + 1),
                    new Tile(col, row - 1)
            );
        }

        public static Tile parse(String line) {
            var parts = line.split(",");
            return new Tile(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
        }
    }
}
