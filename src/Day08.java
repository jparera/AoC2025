import util.Lines;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Day08 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var input = Path.of("input08.txt");

        var boxes = Lines.asStrings(input).stream().map(Box::parse).toArray(Box[]::new);

        var heap = new PriorityQueue<Pair>();
        for (int i = 0; i < boxes.length; i++) {
            for (int j = i + 1; j < boxes.length; j++) {
                heap.add(new Pair(i, j, boxes[i].distance(boxes[j])));
            }
        }

        var circuits = new int[boxes.length];
        var size = new int[boxes.length];
        for (int i = 0; i < circuits.length; i++) {
            circuits[i] = i;
            size[i] = 1;
        }

        int count = 1000;
        while (!heap.isEmpty()) {
            var pair = heap.poll();
            var i_root = findRoot(circuits, pair.i());
            var j_root = findRoot(circuits, pair.j());
            if (i_root != j_root) {
                if (size[i_root] < size[j_root]) {
                    circuits[i_root] = j_root;
                    size[j_root] += size[i_root];
                } else {
                    circuits[j_root] = i_root;
                    size[i_root] += size[j_root];
                }
            }
            // Part 1
            if (--count == 0) {
                var part1 = new ArrayList<Integer>();
                for (int i = 0; i < circuits.length; i++) {
                    if (circuits[i] == i) {
                        part1.add(size[i]);
                    }
                }
                part1.sort(Comparator.reverseOrder());
                terminal.println(part1.get(0) * part1.get(1) * part1.get(2));
            }
            // Part 2
            if (Math.max(size[i_root], size[j_root]) == boxes.length) {
                long part2 = Math.multiplyFull(boxes[pair.i()].x(), boxes[pair.j()].x());
                terminal.println(part2);
                break;
            }
        }
    }

    private static int findRoot(int[] circuits, int i) {
        if (circuits[i] != i) {
            circuits[i] = findRoot(circuits, circuits[i]);
        }
        return circuits[i];
    }

    record Pair(int i, int j, long distance) implements Comparable<Pair> {
        @Override
        public int compareTo(Pair o) {
            return Long.compare(this.distance(), o.distance());
        }
    }

    record Box(int x, int y, int z) {
        public long distance(Box other) {
            long x = this.x - other.x;
            long y = this.y - other.y;
            long z = this.z - other.z;
            return x * x + y * y + z * z;
        }

        public static Box parse(String line) {
            var parts = line.split(",");
            return new Box(Integer.parseInt(parts[0]),
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]));
        }
    }
}
