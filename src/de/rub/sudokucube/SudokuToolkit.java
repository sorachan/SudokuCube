/*
 * Copyright (C) 2017 Sora Steenvoort
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.rub.sudokucube;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author Sora Steenvoort
 */
public class SudokuToolkit {

    private static int hamWt(boolean[] ba) {
        int hw = 0;
        for (boolean b : ba) {
            if (b) {
                hw++;
            }
        }
        return hw;
    }

    static public HashMap<Node, boolean[]> auxNumbers(Sudoku s) {
        int pl = s.getPalette().length;

        HashMap<Node, boolean[]> p = new HashMap<>();

        for (Node n : s.getNodes()) {
            boolean[] b = new boolean[pl];
            Arrays.fill(b, true);
            p.put(n, b);
        }

        for (Node n : s.getNodes()) {
            HashSet<Node> sn = s.getAdjacency().get(n);
            int index = s.numberInPalette(s.getValue(n));
            if (index != -1) {
                if (p.get(n)[index] == false) {
                    System.err.println("Invalid sudoku: " + n + " cannot be set to " + s.getPalette()[index]);
                    System.err.println(s.toPlaintext());
                }
                for (Node m : sn) {
                    p.get(m)[index] = false;
                }
                Arrays.fill(p.get(n), false);
                p.get(n)[index] = true;
            }
        }

        return p;
    }

    static public Set<Sudoku> solve(Sudoku S) {
        HashMap<Node, boolean[]> p = new HashMap<>();

        Sudoku s = S.copy();

        int pl = S.getPalette().length;

        for (Node n : S.getNodes()) {
            boolean[] b = new boolean[pl];
            Arrays.fill(b, true);
            p.put(n, b);
        }

        return solve(s, p);
    }

    static public Set<Sudoku> solve(Sudoku S, HashMap<Node, boolean[]> P) {
        Set<Sudoku> slns = Collections.synchronizedSet(new HashSet<Sudoku>());

        int pl = S.getPalette().length;

        HashMap<Node, boolean[]> p = new HashMap<>();
        for (Node n : P.keySet()) {
            boolean[] b = new boolean[pl];
            System.arraycopy(P.get(n), 0, b, 0, pl);
            p.put(n, b);
        }

        Sudoku s = S.copy();
        Sudoku X = null;
        int mn;

        while (s != null) {
            // step one
            for (Node n : s.getNodes()) {
                HashSet<Node> sn = s.getAdjacency().get(n);
                int index = s.numberInPalette(s.getValue(n));
                if (index != -1) {
                    if (p.get(n)[index] == false) {
                        System.out.println("Invalid sudoku: " + n + " cannot be set to " + s.getPalette()[index]);
                        System.out.println(s.toPlaintext());
                        return slns;
                    }
                    for (Node m : sn) {
                        p.get(m)[index] = false;
                    }
                    Arrays.fill(p.get(n), false);
                    p.get(n)[index] = true;
                }
            }

            // step two
            mn = pl;
            boolean solved = true;
            for (Node n : s.getNodes()) {
                int index = s.numberInPalette(s.getValue(n));
                if (index == -1) {
                    mn = Math.min(mn, hamWt(p.get(n)));
                    solved = false;
                }
            }
            if (solved) {
                slns.add(s);
                return slns;
            }

            HashSet<Node> minset = new HashSet<>();

            if (mn > 0) {
                for (Node n : s.getNodes()) {
                    if (hamWt(p.get(n)) == mn) {
                        minset.add(n);
                    }
                }
            }

            // step three
            switch (mn) {
                case 1:
                    for (Node n : minset) {
                        boolean[] b = p.get(n);
                        boolean problem = true;
                        for (int i = 0; i < pl; i++) {
                            if (b[i]) {
                                s.setValue(n, s.getPalette()[i]);
                                HashSet<Node> sn = s.getAdjacency().get(n);
                                for (Node m : sn) {
                                    p.get(m)[i] = false;
                                }
                                problem = false;
                                break;
                            }
                        }
                        if (problem) {
                            return slns;
                        }
                    }
                    break;
                case 0:
                    return slns;
                default:
                    for (Node n : minset) {
                        boolean[] b = p.get(n);
                        for (int i = 0; i < pl; i++) {
                            if (b[i]) {
                                s.setValue(n, s.getPalette()[i]);
                                slns.addAll(solve(s, p));
                            }
                        }
                        return slns;
                    }
            }
        }
        return slns;
    }

    static public int isSolvable(Sudoku S) {
        HashMap<Node, boolean[]> p = new HashMap<>();

        Sudoku s = S.copy();

        int pl = S.getPalette().length;

        for (Node n : S.getNodes()) {
            boolean[] b = new boolean[pl];
            Arrays.fill(b, true);
            p.put(n, b);
        }

        return isSolvable(s, p, 0);
    }

    static public int isSolvable(Sudoku S, HashMap<Node, boolean[]> P, int pcSlns) {
        if (pcSlns > 1) {
            return 0;
        }
        int cSlns = 0;

        int pl = S.getPalette().length;

        HashMap<Node, boolean[]> p = new HashMap<>();
        for (Node n : P.keySet()) {
            boolean[] b = new boolean[pl];
            System.arraycopy(P.get(n), 0, b, 0, pl);
            p.put(n, b);
        }

        Sudoku s = S.copy();
        Sudoku X = null;
        int mn;

        while (s != null) {
            // step one
            for (Node n : s.getNodes()) {
                HashSet<Node> sn = s.getAdjacency().get(n);
                int index = s.numberInPalette(s.getValue(n));
                if (index != -1) {
                    if (p.get(n)[index] == false) {
                        System.out.println("Invalid sudoku: " + n + " cannot be set to " + s.getPalette()[index]);
                        System.out.println(s.toPlaintext());
                        return cSlns;
                    }
                    for (Node m : sn) {
                        p.get(m)[index] = false;
                    }
                    Arrays.fill(p.get(n), false);
                    p.get(n)[index] = true;
                }
            }

            // step two
            mn = pl;
            boolean solved = true;
            for (Node n : s.getNodes()) {
                int index = s.numberInPalette(s.getValue(n));
                if (index == -1) {
                    mn = Math.min(mn, hamWt(p.get(n)));
                    solved = false;
                }
            }
            if (solved) {
                cSlns += 1;
                return cSlns;
            }

            HashSet<Node> minset = new HashSet<>();

            if (mn > 0) {
                for (Node n : s.getNodes()) {
                    if (hamWt(p.get(n)) == mn) {
                        minset.add(n);
                    }
                }
            }

            // step three
            switch (mn) {
                case 1:
                    for (Node n : minset) {
                        boolean[] b = p.get(n);
                        boolean problem = true;
                        for (int i = 0; i < pl; i++) {
                            if (b[i]) {
                                s.setValue(n, s.getPalette()[i]);
                                HashSet<Node> sn = s.getAdjacency().get(n);
                                for (Node m : sn) {
                                    p.get(m)[i] = false;
                                }
                                problem = false;
                                break;
                            }
                        }
                        if (problem) {
                            return cSlns;
                        }
                    }
                    break;
                case 0:
                    return cSlns;
                default:
                    for (Node n : minset) {
                        boolean[] b = p.get(n);
                        for (int i = 0; i < pl; i++) {
                            if (b[i]) {
                                s.setValue(n, s.getPalette()[i]);
                                cSlns += isSolvable(s, p, cSlns);
                            }
                        }
                        return cSlns;
                    }
            }
        }
        return cSlns;
    }

    static public Sudoku randomPuzzleFromSolution(Sudoku S) throws Exception {
        for (Node n : S.getNodes()) {
            if (S.numberInPalette(S.getValue(n)) == -1) {
                throw new Exception("Called randomPuzzleFromSolution on incomplete puzzle!");
            }
        }

        List<Node> nodeList = new ArrayList<>(S.getNodes());
        Collections.shuffle(nodeList);
        Node[] nodeArray = nodeList.toArray(new Node[0]);

        Set<Sudoku> candidates = new HashSet<>();

        randomPuzzleFromSolution(S, candidates, nodeArray, 0, new AtomicInteger(0), 0, System.currentTimeMillis());

        if (candidates.size() > 0) {
            List<Sudoku> candidatesList = new ArrayList<>(candidates);
            Collections.shuffle(candidatesList);
            return candidatesList.get(0);
        } else {
            throw new Exception("randomPuzzleFromSolution failed!");
        }
    }

    static public void randomPuzzleFromSolution(Sudoku S, Set<Sudoku> c, Node[] nArray, int depth, AtomicInteger maxdepth, int itemno, long tstart) {
        if (System.currentTimeMillis() - tstart > S.TIME_FOR_RPFS()) {
            return;
        }

        boolean minimum = true;
        int isSlvb;

        Sudoku s2 = S.copy();

        for (int j = itemno; j < nArray.length; j++) {
            Node n = nArray[j];
            if (s2.numberInPalette(s2.getValue(n)) != -1) {
                Sudoku s = s2.copy();
                s.setValue(n, s.getEmpty());
                isSlvb = isSolvable(s);
                if (isSlvb == 1) {
                    minimum = false;
                    if (System.currentTimeMillis() - tstart < S.TIME_FOR_RPFS()) {
                        randomPuzzleFromSolution(s, c, nArray, depth + 1, maxdepth, itemno + 1, tstart);
                    } else {
                        return;
                    }
                }
                if (minimum && depth >= maxdepth.intValue()) {
                    if (depth > maxdepth.intValue()) {
                        maxdepth.set(depth);
                        c.clear();
                    }
                    c.add(s2);
                }
            }
        }
    }
}
