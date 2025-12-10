import util.Lines;
import util.Numbers;
import util.Terminal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Day10 {
    public static void main() throws IOException {
        var terminal = Terminal.get();
        var input = Path.of("input10.txt");
        var machines = Lines.asStrings(input).stream().map(Machine::parse).toList();

        var minCounters = machines.stream().mapToInt(Machine::counters).min();
        var maxCounters = machines.stream().mapToInt(Machine::counters).max();

        terminal.println("Machines parsed: " + machines.size());
        terminal.printf("Min counters per machine: %d%n", minCounters.orElse(0));
        terminal.printf("Max counters per machine: %d%n", maxCounters.orElse(0));

        var part1 = machines.stream().mapToInt(Machine::shortestStepsToStart).sum();
        terminal.println(part1);
        var part2 = machines.stream().mapToInt(Machine::shortestStepsToConfigureEnergyLevels).sum();
        terminal.println(part2);
    }

    private static class Machine {
        private static final char ON = '#';

        private static final char OFF = '.';

        private final int started;

        private final List<Button> buttons;

        private final int[] joltageLevels;

        private int minSteps = Integer.MAX_VALUE;

        private Machine(int started, List<Button> buttons, int[] joltageLevels) {
            this.started = started;
            this.buttons = buttons;
            this.joltageLevels = joltageLevels;
        }

        @Override
        public String toString() {
            return "Machine{started=" + Integer.toBinaryString(this.started) +
                    ", buttons=" + this.buttons +
                    ", joltageLevels=" + Arrays.toString(this.joltageLevels) +
                    '}';
        }

        public int counters() {
            return this.joltageLevels.length;
        }

        public int shortestStepsToStart() {
            this.minSteps = Integer.MAX_VALUE;
            dfs_part1(0, null, 0);
            return this.minSteps;
        }

        private static final int MAX_DEPTH_PART1 = 10;

        private void dfs_part1(int current, Button previous, int depth) {
            if (depth >= this.minSteps || depth > MAX_DEPTH_PART1) {
                return;
            }
            if (current == this.started) {
                this.minSteps = depth;
                return;
            }
            for (var button : this.buttons) {
                if (button == previous) {
                    continue;
                }
                int next = current ^ button.toggle();
                dfs_part1(next, button, depth + 1);
            }
        }

        public int shortestStepsToConfigureEnergyLevels() {
            var rows = this.joltageLevels.length;
            var cols = this.buttons.size() + 1;
            var matrix = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                matrix[i][cols - 1] = this.joltageLevels[i];
                for (int j = 0; j < this.buttons.size(); j++) {
                    var button = this.buttons.get(j);
                    for (var index : button.indices()) {
                        matrix[index][j] = 1;
                    }
                }
            }
            gauss(matrix);
            var solution = findSmallSolution(matrix, 0, 512);
            assert solution != null;
            return Arrays.stream(solution).sum();
        }

        private static int[] findSmallSolution(int[][] matrix, int minVal, int maxVal) {
            int rows = matrix.length;
            int cols = matrix[0].length;
            int nVars = cols - 1;

            // Use BigInteger for exact arithmetic
            java.math.BigInteger[][] A = new java.math.BigInteger[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    A[i][j] = java.math.BigInteger.valueOf(matrix[i][j]);
                }
            }

            // find pivot column for each row (first non-zero coefficient)
            int[] pivotCol = new int[rows];
            Arrays.fill(pivotCol, -1);
            boolean[] isPivot = new boolean[nVars];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < nVars; j++) {
                    if (!A[i][j].equals(java.math.BigInteger.ZERO)) {
                        pivotCol[i] = j;
                        isPivot[j] = true;
                        break;
                    }
                }
                // inconsistent row check: 0 ... 0 | rhs != 0
                if (pivotCol[i] == -1 && !A[i][nVars].equals(java.math.BigInteger.ZERO)) {
                    return null;
                }
            }

            // collect free variable indices
            ArrayList<Integer> freeVars = new ArrayList<>();
            for (int j = 0; j < nVars; j++) {
                if (!isPivot[j]) freeVars.add(j);
            }

            // collect pivot rows as pairs (row, pivotCol) and sort by pivotCol descending
            ArrayList<int[]> pivots = new ArrayList<>();
            for (int i = 0; i < rows; i++) {
                if (pivotCol[i] != -1) pivots.add(new int[]{i, pivotCol[i]});
            }
            pivots.sort((a, b) -> Integer.compare(b[1], a[1])); // descending by pivot column

            int[] solution = new int[nVars];
            boolean[] assigned = new boolean[nVars];

            final int[] bestSolution = new int[nVars];
            final long[] bestSum = new long[]{Long.MAX_VALUE};
            final boolean[] found = new boolean[]{false};

            java.util.concurrent.atomic.AtomicLong trials = new java.util.concurrent.atomic.AtomicLong(0);
            long maxTrials = 1_000_000_000; // safety cap

            class Rec {
                void go(int idx, long sumFree) {
                    if (found[0] && sumFree >= bestSum[0]) return; // prune if already no better
                    if (trials.incrementAndGet() > maxTrials) return;
                    if (idx == freeVars.size()) {
                        // attempt to compute pivot variables by back-substitution
                        boolean ok = true;
                        for (int[] pr : pivots) {
                            int row = pr[0];
                            int col = pr[1];
                            java.math.BigInteger rhs = A[row][nVars];
                            java.math.BigInteger sum = java.math.BigInteger.ZERO;
                            for (int j = col + 1; j < nVars; j++) {
                                if (!A[row][j].equals(java.math.BigInteger.ZERO)) {
                                    if (!assigned[j]) {
                                        ok = false;
                                        break;
                                    }
                                    sum = sum.add(A[row][j].multiply(java.math.BigInteger.valueOf(solution[j])));
                                }
                            }
                            if (!ok) break;
                            java.math.BigInteger lhs = rhs.subtract(sum);
                            java.math.BigInteger pivotCoeff = A[row][col];
                            java.math.BigInteger[] qr = lhs.divideAndRemainder(pivotCoeff);
                            if (!qr[1].equals(java.math.BigInteger.ZERO)) {
                                ok = false;
                                break;
                            } // non-integer
                            java.math.BigInteger val = qr[0];
                            if (val.compareTo(java.math.BigInteger.valueOf(minVal)) < 0 ||
                                    val.compareTo(java.math.BigInteger.valueOf(maxVal)) > 0) {
                                ok = false;
                                break;
                            }
                            try {
                                int v = val.intValueExact();
                                solution[col] = v;
                                assigned[col] = true;
                            } catch (ArithmeticException ex) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            // final verification by substituting into all rows
                            if (validateSolution(A, solution, nVars, minVal, maxVal)) {
                                long total = 0L;
                                for (int k = 0; k < nVars; k++) total += solution[k];
                                if (total < bestSum[0]) {
                                    bestSum[0] = total;
                                    System.arraycopy(solution, 0, bestSolution, 0, nVars);
                                    found[0] = true;
                                }
                            } // else reject candidate as invalid
                        }
                        // undo pivot assignments for next try
                        for (int[] pr : pivots) {
                            assigned[pr[1]] = false;
                            solution[pr[1]] = 0;
                        }
                        return;
                    }

                    int var = freeVars.get(idx);
                    // lower bound pruning: sumFree + remainingFree * minVal
                    int remaining = freeVars.size() - idx;
                    long lowerBoundIfAllMin = sumFree + (long) (remaining) * (long) minVal;
                    if (found[0] && lowerBoundIfAllMin >= bestSum[0]) return;

                    for (int v = minVal; v <= maxVal; v++) {
                        if (found[0] && sumFree + v >= bestSum[0]) {
                            break;
                        }
                        solution[var] = v;
                        assigned[var] = true;
                        go(idx + 1, sumFree + v);
                        assigned[var] = false;
                        solution[var] = 0;
                        if (trials.get() > maxTrials) return;
                    }
                }
            }

            // start recursion with sumFree = 0
            new Rec().go(0, 0L);

            if (found[0]) {
                // final sanity check before returning
                if (validateSolution(A, bestSolution, nVars, minVal, maxVal)) {
                    return Arrays.copyOf(bestSolution, nVars);
                }
            }
            return null;
        }

        // helper: verify solution satisfies A * sol == rhs for every row and range constraints
        private static boolean validateSolution(java.math.BigInteger[][] A, int[] sol, int nVars, int minVal, int maxVal) {
            for (int k : sol) {
                if (k < minVal || k > maxVal) return false;
            }
            for (java.math.BigInteger[] bigIntegers : A) {
                java.math.BigInteger sum = java.math.BigInteger.ZERO;
                for (int j = 0; j < nVars; j++) {
                    if (!bigIntegers[j].equals(java.math.BigInteger.ZERO)) {
                        sum = sum.add(bigIntegers[j].multiply(java.math.BigInteger.valueOf(sol[j])));
                    }
                }
                if (!sum.equals(bigIntegers[nVars])) return false;
            }
            return true;
        }

        private static void gauss(int[][] matrix) {
            int rows = matrix.length;
            int cols = matrix[0].length;
            // convert to BigInteger
            java.math.BigInteger[][] A = new java.math.BigInteger[rows][cols];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    A[i][j] = java.math.BigInteger.valueOf(matrix[i][j]);
                }
            }

            java.math.BigInteger prevPivot = java.math.BigInteger.ONE;
            int limit = Math.min(rows, cols);

            for (int k = 0; k < limit; k++) {
                // find a non-zero pivot and swap if needed
                int pivotRow = k;
                while (pivotRow < rows && A[pivotRow][k].equals(java.math.BigInteger.ZERO)) {
                    pivotRow++;
                }
                if (pivotRow == rows) {
                    // entire column is zero -> move to next column
                    continue;
                }
                if (pivotRow != k) {
                    var tmp = A[k];
                    A[k] = A[pivotRow];
                    A[pivotRow] = tmp;
                }

                java.math.BigInteger pivot = A[k][k];
                // Bareiss fraction-free elimination
                for (int i = k + 1; i < rows; i++) {
                    for (int j = k + 1; j < cols; j++) {
                        // A[i][j] = (A[i][j]*pivot - A[i][k]*A[k][j]) / prevPivot
                        java.math.BigInteger num = A[i][j].multiply(pivot).subtract(A[i][k].multiply(A[k][j]));
                        if (!prevPivot.equals(java.math.BigInteger.ONE)) {
                            num = num.divide(prevPivot); // division is exact in Bareiss
                        }
                        A[i][j] = num;
                    }
                    A[i][k] = java.math.BigInteger.ZERO;
                }
                prevPivot = pivot;
            }

            // write back to int matrix (may overflow if values exceed int range)
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = A[i][j].intValue();
                }
            }
        }

        public static Machine parse(String line) {
            var parts = line.split("\\s");
            var indicators = parts[0];
            var started = 0;
            int index = 0;
            for (var c : indicators.toCharArray()) {
                if (c == ON || c == OFF) {
                    if (c == ON) {
                        started = setFlag(started, index);
                    }
                    index++;
                }
            }
            var buttons = new ArrayList<Button>();
            for (int i = 1; i < parts.length - 1; i++) {
                var lights = Numbers.asIntStream(parts[i]).toArray();
                int toggle = 0;
                for (var light : lights) {
                    toggle = setFlag(toggle, light);
                }
                buttons.add(new Button(toggle, lights));
            }
            buttons.sort(Comparator.comparingInt(b -> -b.indices().length));
            buttons.reversed();
            var counters = Numbers.asIntStream(parts[parts.length - 1]).toArray();
            return new Machine(started, buttons, counters);
        }
    }

    private static int setFlag(int flags, int position) {
        return flags | (1 << position);
    }

    record Button(int toggle, int[] indices) {
        @Override
        public String toString() {
            return Arrays.toString(this.indices);
        }
    }
}
