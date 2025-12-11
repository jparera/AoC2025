import util.Lines;
import util.Numbers;
import util.Solver;
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
            var solution = Solver.findMinSumIntegerSolution(matrix);
            if (solution == null) {
                throw new IllegalStateException("No solution found");
            }
            return Arrays.stream(solution).sum();
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
