import util.Lines;
import util.Numbers;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Day12 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var input = Path.of("input12.txt");

        var shapes = Shape.parse(input);
        var shapeSide = shapes[0].grid().length;
        for (var shape : shapes) {
            if (shape.grid().length != shape.grid()[0].length || shape.grid().length != shapeSide) {
                throw new IllegalArgumentException("All shapes must be square and have the same size.");
            }
            terminal.printf("Shape %d:%n", shape.index());
            terminal.print(shape.grid());
        }

        var variants = Arrays.stream(shapes).flatMap(shape -> Arrays.stream(shape.allRotationsAndFlips())).toArray(Shape[]::new);
        var regions = Region.parse(input);
        var presentsPerRegion = Arrays.stream(regions).mapToInt(Region::presents).summaryStatistics();

        terminal.printf("Shapes: %d%n", shapes.length);
        terminal.printf("All shape variants: %d%n", variants.length);
        terminal.printf("Regions: %d%n", regions.length);
        terminal.printf("Presents per region: min=%d, max=%d%n",
                presentsPerRegion.getMin(), presentsPerRegion.getMax());

        var part1 = Arrays.stream(regions).parallel()
                .map(region -> region.checkTiling(shapes, variants))
                .filter(fits -> fits)
                .count();
        terminal.println(part1);
    }

    record Shape(int index, int area, char[][] grid) {
        private static final char FILLED = '#';

        public Mask mask(Region region, int variant, int row, int col) {
            int shapeRows = grid.length;
            int shapeCols = grid[0].length;
            // ensure the whole shape fits inside the region
            if (row < 0 || col < 0 || row + shapeRows > region.size().rows() || col + shapeCols > region.size().cols()) {
                throw new IllegalArgumentException("Shape does not fit at the specified position.");
            }
            var bitSet = new BitSet(region.size.area());
            for (int r = 0; r < shapeRows; r++) {
                for (int c = 0; c < shapeCols; c++) {
                    if (grid[r][c] == FILLED) {
                        int gr = row + r;
                        int gc = col + c;
                        int idx = gr * region.size().cols() + gc;
                        bitSet.set(idx);
                    }
                }
            }
            return new Mask(index, variant, bitSet);
        }

        public Shape[] allRotationsAndFlips() {
            Set<Shape> transformations = new java.util.HashSet<>();
            Shape current = this;
            for (int i = 0; i < 4; i++) {
                transformations.add(current);
                transformations.add(current.flip());
                current = current.rotate();
            }
            return filterDuplicateShapes(transformations.toArray(Shape[]::new));
        }

        private static Shape[] filterDuplicateShapes(Shape[] shapes) {
            var uniques = new ArrayList<Shape>();
            for (var shape : shapes) {
                boolean isDuplicate = false;
                for (var unique : uniques) {
                    if (shape.equals(unique)) {
                        isDuplicate = true;
                        break;
                    }
                }
                if (!isDuplicate) {
                    uniques.add(shape);
                }
            }
            return uniques.toArray(Shape[]::new);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Shape other)) return false;
            if (grid.length != other.grid.length || grid[0].length != other.grid[0].length) return false;
            for (int r = 0; r < grid.length; r++) {
                if (!Arrays.equals(grid[r], other.grid[r])) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(grid);
        }

        public Shape rotate() {
            int rows = grid.length;
            int cols = grid[0].length;
            char[][] rotated = new char[cols][rows];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    rotated[c][rows - 1 - r] = grid[r][c];
                }
            }
            return new Shape(index, area, rotated);
        }

        public Shape flip() {
            int rows = grid.length;
            int cols = grid[0].length;
            char[][] flipped = new char[rows][cols];
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    flipped[r][cols - 1 - c] = grid[r][c];
                }
            }
            return new Shape(index, area, flipped);
        }

        public static Shape[] parse(Path input) throws IOException {
            var blocks = Lines.asBlocks(input);
            return blocks.stream().limit(blocks.size() - 1).map(block -> {
                var index = block.getFirst().substring(0, block.getFirst().indexOf(':'));
                var grid = block.subList(1, block.size()).stream().map(String::toCharArray).toArray(char[][]::new);
                var area = calculateArea(grid);
                return new Shape(Integer.parseInt(index), area, grid);
            }).toArray(Shape[]::new);
        }

        private static int calculateArea(char[][] grid) {
            int area = 0;
            for (char[] row : grid) {
                for (char cell : row) {
                    if (cell == '#') {
                        area++;
                    }
                }
            }
            return area;
        }
    }

    record Region(Size size, int[] presentsPerShape) {
        public static Region[] parse(Path input) throws IOException {
            var regions = Lines.asBlocks(input).getLast();
            return regions.stream().map(region -> {
                var values = Numbers.asIntStream(region).toArray();
                var presents = Arrays.copyOfRange(values, 2, values.length);
                return new Region(new Size(values[1], values[0]), presents);
            }).toArray(Region[]::new);
        }

        public int presents() {
            return Arrays.stream(presentsPerShape).sum();
        }

        @Override
        public String toString() {
            return String.format("Region %s: %s", size, Arrays.toString(presentsPerShape));
        }

        public boolean checkTiling(Shape[] shapes, Shape[] variants) {
            return TilingChecker.of(shapes, variants, this).check();
        }

        private static class TilingChecker {
            private final Shape[] shapes;

            private final Region region;

            private final int totalArea;

            private final List<List<Mask>> candidatesPerCell = new ArrayList<>();

            private final Map<BitSet, Integer> best = new HashMap<>();

            private final Map<BitSet, Set<Integer>> visited = new HashMap<>();

            private TilingChecker(Shape[] shapes, Region region, int totalArea, List<List<Mask>> candidatesPerCell) {
                this.shapes = shapes;
                this.region = region;
                this.totalArea = totalArea;
                this.candidatesPerCell.addAll(candidatesPerCell);
            }

            public static TilingChecker of(Shape[] shapes, Shape[] variants, Region region) {
                var totalArea = region.size().area();
                var masks = region.allPlacementMasks(variants);
                var candidatesPerCell = new ArrayList<List<Mask>>();
                for (int i = 0; i < totalArea; i++) {
                    candidatesPerCell.add(new ArrayList<>());
                }
                for (var mask : masks) {
                    var cells = mask.bitSet();
                    for (int i = cells.nextSetBit(0); i >= 0; i = cells.nextSetBit(i + 1)) {
                        candidatesPerCell.get(i).add(mask);
                    }
                }
                return new TilingChecker(shapes, region, totalArea, candidatesPerCell);
            }

            public boolean check() {
                var presentsPerShape = region.presentsPerShape();
                // First, check if total shape area of presents fits in region.
                int requiredArea = 0;
                for (int i = 0; i < presentsPerShape.length; i++) {
                    requiredArea += presentsPerShape[i] * shapes[i].area();
                }
                // If total shape area exceeds region area, return false.
                if (requiredArea > totalArea) {
                    return false;
                }
                var filled = new BitSet(totalArea);
                var presents = Arrays.copyOf(presentsPerShape, presentsPerShape.length);
                return dfs(filled, 0, presents, requiredArea);
            }

            private boolean dfs(BitSet filled, int from, int[] presentsPerShape, int requiredArea) {
                 if (requiredArea == 0) {
                    return true;
                }
                // Not enough space left to fill the required area.
                if (totalArea - from < requiredArea) {
                    return false;
                }

                var freeArea = totalArea - filled.cardinality();
                if (requiredArea > freeArea) {
                    return false;
                }

                var visited = this.visited.computeIfAbsent(filled, _ -> new HashSet<>());
                if (visited.contains(from)) {
                    return false;
                }
                visited.add(from);

                var previous = best.get(filled);
                if (previous != null && requiredArea > previous) {
                    return false;
                }
                best.put(filled, requiredArea);

                int cell = filled.nextClearBit(from);
                if (cell >= totalArea) {
                    return false;
                }

                var candidates = candidatesPerCell.get(cell);
                for (var candidate : candidates) {
                    var test = (BitSet) candidate.bitSet().clone();
                    test.and(filled);
                    if (!test.isEmpty()) {
                        // Overlaps with already filled cells.
                        continue;
                    }
                    int shapeIndex = candidate.index();
                    if (presentsPerShape[shapeIndex] <= 0) {
                        // No more presents of this shape left.
                        continue;
                    }
                    // Place the shape.
                    var newFilled = (BitSet) filled.clone();
                    newFilled.or(candidate.bitSet());
                    presentsPerShape[shapeIndex]--;
                    int newRequiredArea = requiredArea - shapes[shapeIndex].area();
                    if (dfs(newFilled, from + 1, presentsPerShape, newRequiredArea)) {
                        return true;
                    }
                    // Backtrack
                    presentsPerShape[shapeIndex]++;
                }

                // Skip this cell for now without forbidding future placements that may cover it:
                return dfs(filled, cell + 1, presentsPerShape, requiredArea);
            }
        }

        private List<Mask> allPlacementMasks(Shape[] variants) {
            var masks = new ArrayList<Mask>();
            for (int index = 0; index < variants.length; index++) {
                var variant = variants[index];
                for (int r = 0; r <= size().rows() - variant.grid().length; r++) {
                    for (int c = 0; c <= size().cols() - variant.grid()[0].length; c++) {
                        masks.add(variant.mask(this, index, r, c));
                    }
                }
            }
            return masks;
        }
    }

    record Mask(int index, int variant, BitSet bitSet) {
    }

    record Size(int rows, int cols) {
        public int area() {
            return rows * cols;
        }

        @Override
        public String toString() {
            return String.format("(%dx%d)", rows, cols);
        }
    }
}
