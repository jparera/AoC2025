package util;

import java.math.BigInteger;
import java.util.Arrays;

public class Solver {
    public static int[] findMinSumIntegerSolution(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        int nVars = cols - 1;

        // Derive implicit global upper bound from the original RHS magnitudes (keep finite search)
        int implicitMax = 1;
        for (int[] value : matrix) {
            int rhsAbs = Math.abs(value[nVars]);
            if (rhsAbs > implicitMax) implicitMax = rhsAbs;
        }

        // compute per-variable upper bounds from the original integer matrix (before elimination)
        int[] perVarMax = new int[nVars];
        Arrays.fill(perVarMax, implicitMax);
        for (int var = 0; var < nVars; var++) {
            // consider only free variables later; for now compute a safe bound for each var
            int best = implicitMax;
            boolean sawPositive = false;
            for (int[] ints : matrix) {
                int coeff = ints[var];
                if (coeff > 0) {
                    // only use this row to bound var if the row has no negative coefficients
                    boolean rowHasNegative = false;
                    for (int k = 0; k < nVars; k++) {
                        if (ints[k] < 0) {
                            rowHasNegative = true;
                            break;
                        }
                    }
                    if (rowHasNegative) {
                        // skip this row; negative coefficients could let var be larger
                        continue;
                    }
                    sawPositive = true;
                    int rhs = ints[nVars];
                    // if RHS negative while all coeffs non-negative, no non-negative solution exists
                    if (rhs < 0) {
                        return null;
                    }
                    int cand = rhs / coeff; // floor division (rhs and coeff are ints)
                    if (cand < best) best = cand;
                }
            }
            if (!sawPositive) {
                // no direct positive-only constraint found — keep global cap
                perVarMax[var] = implicitMax;
            } else {
                if (best < 0) best = 0;
                perVarMax[var] = Math.min(best, implicitMax);
            }
        }

        // Use BigInteger for exact arithmetic and perform Gaussian elimination
        var A = convertMatrixToBigInteger(matrix);
        gauss(A);

        // find pivot column for each row (first non-zero coefficient)
        int[] pivotCol = new int[rows];
        Arrays.fill(pivotCol, -1);
        int[] pivotRowForCol = new int[nVars];
        Arrays.fill(pivotRowForCol, -1);
        boolean[] isPivot = new boolean[nVars];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < nVars; j++) {
                if (!A[i][j].equals(BigInteger.ZERO)) {
                    pivotCol[i] = j;
                    isPivot[j] = true;
                    pivotRowForCol[j] = i;
                    break;
                }
            }
            // inconsistent row check: 0 ... 0 | rhs != 0
            if (pivotCol[i] == -1 && !A[i][nVars].equals(BigInteger.ZERO)) {
                return null;
            }
        }

        // collect free variable indices -> int[]
        int freeCount = 0;
        for (int j = 0; j < nVars; j++) {
            if (!isPivot[j]) freeCount++;
        }
        int[] freeVars = new int[freeCount];
        for (int j = 0, idx = 0; j < nVars; j++) {
            if (!isPivot[j]) freeVars[idx++] = j;
        }

        // collect pivot columns in descending order (higher index first) to back-substitute -> int[]
        int pivotCount = 0;
        for (int j = nVars - 1; j >= 0; j--) {
            if (pivotRowForCol[j] != -1) pivotCount++;
        }
        int[] pivotCols = new int[pivotCount];
        for (int j = nVars - 1, idx = 0; j >= 0; j--) {
            if (pivotRowForCol[j] != -1) pivotCols[idx++] = j;
        }

        int[] solution = new int[nVars];
        boolean[] assigned = new boolean[nVars];

        final int[] bestSolution = new int[nVars];
        final long[] bestSum = new long[]{Long.MAX_VALUE};
        final boolean[] found = new boolean[]{false};

        // Quick attempt: set all free variables to 0 and compute pivot variables by back-substitution
        boolean trivialOk = true;
        for (int col : pivotCols) {
            int row = pivotRowForCol[col];
            BigInteger rhs = A[row][nVars];
            BigInteger sum = BigInteger.ZERO;
            for (int j = col + 1; j < nVars; j++) {
                if (!A[row][j].equals(BigInteger.ZERO)) {
                    if (!assigned[j]) {
                        trivialOk = false;
                        break;
                    }
                    sum = sum.add(A[row][j].multiply(BigInteger.valueOf(solution[j])));
                }
            }
            if (!trivialOk) break;
            BigInteger lhs = rhs.subtract(sum);
            BigInteger pivotCoeff = A[row][col];
            BigInteger[] qr = lhs.divideAndRemainder(pivotCoeff);
            if (!qr[1].equals(BigInteger.ZERO)) {
                trivialOk = false;
                break;
            }
            BigInteger val = qr[0];
            if (val.compareTo(BigInteger.ZERO) < 0) {
                trivialOk = false;
                break;
            }
            try {
                int v = val.intValueExact();
                solution[col] = v;
                assigned[col] = true;
            } catch (ArithmeticException ex) {
                trivialOk = false;
                break;
            }
        }
        if (trivialOk) {
            if (validateSolution(A, solution, nVars)) {
                return Arrays.copyOf(solution, nVars);
            }
            // otherwise fallthrough to full search
            for (int col : pivotCols) {
                assigned[col] = false;
                solution[col] = 0;
            }
        }

        // start recursion with sumFree = 0; variables are required to be >= 0
        // pass perVarMax (computed from original matrix) into backtrack
        backtrack(A, nVars, pivotRowForCol, freeVars, freeCount, pivotCols, pivotCount, perVarMax, solution, assigned, 0, 0L, bestSolution, bestSum, found);

        if (!found[0]) {
            // Conservative bounds may have been too tight; relax to global implicit max and retry once
            int[] relaxed = new int[nVars];
            Arrays.fill(relaxed, implicitMax);
            backtrack(A, nVars, pivotRowForCol, freeVars, freeCount, pivotCols, pivotCount, relaxed, solution, assigned, 0, 0L, bestSolution, bestSum, found);
        }

        if (found[0]) {
            // final sanity check before returning
            if (validateSolution(A, bestSolution, nVars)) {
                return Arrays.copyOf(bestSolution, nVars);
            }
        }
        return null;
    }

    // recursive backtracking helper extracted from findMinSumIntegerSolution for clarity
    private static void backtrack(BigInteger[][] A,
                                  int nVars,
                                  int[] pivotRowForCol,
                                  int[] freeVars,
                                  int freeLen,
                                  int[] pivotCols,
                                  int pivotLen,
                                  int[] perVarMax,
                                  int[] solution,
                                  boolean[] assigned,
                                  int idx,
                                  long sumFree,
                                  int[] bestSolution,
                                  long[] bestSum,
                                  boolean[] found) {
        if (found[0] && sumFree >= bestSum[0]) return; // prune if already no better
        if (idx == freeLen) {
            // attempt to compute pivot variables by back-substitution
            boolean ok = true;
            // compute pivot variables into solution in the order of pivotCols (desc)
            for (int p = 0; p < pivotLen; p++) {
                int col = pivotCols[p];
                int row = pivotRowForCol[col];
                BigInteger rhs = A[row][nVars];
                BigInteger sum = BigInteger.ZERO;
                for (int j = col + 1; j < nVars; j++) {
                    if (!A[row][j].equals(BigInteger.ZERO)) {
                        if (!assigned[j]) {
                            ok = false;
                            break;
                        }
                        sum = sum.add(A[row][j].multiply(java.math.BigInteger.valueOf(solution[j])));
                    }
                }
                if (!ok) break;
                BigInteger lhs = rhs.subtract(sum);
                BigInteger pivotCoeff = A[row][col];
                BigInteger[] qr = lhs.divideAndRemainder(pivotCoeff);
                if (!qr[1].equals(BigInteger.ZERO)) {
                    ok = false;
                    break;
                } // non-integer
                BigInteger val = qr[0];
                if (val.compareTo(BigInteger.ZERO) < 0) {
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
                if (validateSolution(A, solution, nVars)) {
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
            for (int p = 0; p < pivotLen; p++) {
                int col = pivotCols[p];
                assigned[col] = false;
                solution[col] = 0;
            }
            return;
        }

        int var = freeVars[idx];
        int maxV = perVarMax[var];
        // iterate free variable values from 0 to per-variable max
        for (int v = 0; v <= maxV; v++) {
            if (found[0] && sumFree + v >= bestSum[0]) {
                break;
            }
            solution[var] = v;
            assigned[var] = true;
            backtrack(A, nVars, pivotRowForCol, freeVars, freeLen, pivotCols, pivotLen, perVarMax, solution, assigned, idx + 1, sumFree + v, bestSolution, bestSum, found);
            assigned[var] = false;
            solution[var] = 0;
        }
    }

    // helper: verify solution satisfies A * sol == rhs for every row and non-negativity
    private static boolean validateSolution(java.math.BigInteger[][] A, int[] sol, int nVars) {
        for (int k : sol) {
            if (k < 0) return false;
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

    @SuppressWarnings("unused")
    public static int gauss(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        // convert to BigInteger
        var A = convertMatrixToBigInteger(matrix);
        var rank = gauss(A);
        // write back to int matrix (may overflow if values exceed int range)
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                matrix[i][j] = A[i][j].intValue();
            }
        }
        return rank;
    }

    public static int gauss(BigInteger[][] A) {
        int rows = A.length;
        int cols = A[0].length;
        int limit = Math.min(rows, cols);
        var prevPivot = BigInteger.ONE;
        int rank = 0;
        for (int k = 0; k < limit; k++) {
            // find a non-zero pivot and swap if needed
            int pivotRow = k;
            while (pivotRow < rows && BigInteger.ZERO.equals(A[pivotRow][k])) {
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
            rank++;
            var pivot = A[k][k];
            // Bareiss fraction-free elimination
            for (int i = k + 1; i < rows; i++) {
                for (int j = k + 1; j < cols; j++) {
                    // A[i][j] = (A[i][j]*pivot - A[i][k]*A[k][j]) / prevPivot
                    var num = A[i][j].multiply(pivot).subtract(A[i][k].multiply(A[k][j]));
                    if (!BigInteger.ONE.equals(prevPivot)) {
                        num = num.divide(prevPivot); // division is exact in Bareiss
                    }
                    A[i][j] = num;
                }
                A[i][k] = BigInteger.ZERO;
            }
            prevPivot = pivot;
        }
        return rank;
    }

    private static BigInteger[][] convertMatrixToBigInteger(int[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        var A = new BigInteger[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                A[i][j] = BigInteger.valueOf(matrix[i][j]);
            }
        }
        return A;
    }
}
