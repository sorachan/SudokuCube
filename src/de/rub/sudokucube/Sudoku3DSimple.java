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

import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;

/**
 *
 * @author Sora Steenvoort
 */
public class Sudoku3DSimple extends Sudoku {

    static final private Node[][] vert;

    @Override
    int TIME_FOR_RPFS() {
        return 4500;
    }

    @Override
    int TIME_FOR_RP() {
        return 500;
    }

    @Override
    public Path2D getGrid() {
        return grid;
    }

    @Override
    public Map<Node, Path2D> getNodeMap() {
        return nodeMap;
    }

    static final Map<Node, Path2D> nodeMap;
    static final Path2D grid;

    @Override
    public Node nodeByIndex(String index) {
        if (index.matches("\\([0-7],[0-7]\\)")) {
            String[] coord = index.split("[\\(,\\)]");
            return vert[Integer.parseInt(coord[1])][Integer.parseInt(coord[2])];
        } else {
            System.err.println(index + " does not specify a valid node!");
            return null;
        }
    }

    static private boolean defineAdjacencyA(int i, int j, int I, int J) {
        if (i < I) {
            return defineAdjacencyB(i, j, I, J);
        }
        if (I < i || J < j) {
            return defineAdjacencyB(I, J, i, j);
        }
        if (j < J) {
            return defineAdjacencyB(i, j, I, J);
        }
        return false;
    }

    static private boolean defineAdjacencyB(int i, int j, int I, int J) {
        // horizontal
        if (i == I) {
            return true;
        }
        // vertical
        if (j == J) {
            return true;
        }
        // 3d case
        if (I > 3 && j == I) {
            return true;
        }
        // subgrids
        if ((i / 2 == I / 2) && (j / 4 == J / 4)) {
            return true;
        }
        if ((i / 4 == I / 4) && (j / 2 == J / 2)) {
            return true;
        }
        return false;
    }

    @Override
    public char[] getPalette() {
        return palette;
    }

    @Override
    public Map<Node, HashSet<Node>> getAdjacency() {
        return adjacency;
    }

    @Override
    public Map<Integer, Character> getKeyBindings() {
        return keyBindings;
    }

    @Override
    public char getEmpty() {
        return EMPTY;
    }

    static final char[] palette;
    static final Map<Node, HashSet<Node>> adjacency; // map of sets
    static final Map<Integer, Character> keyBindings;
    static final char EMPTY;

    static {
        EMPTY = '.';
        vert = new Node[8][8];
        adjacency = new HashMap<>();
        palette = new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};

        // initialize cube structure
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 4 || j < 4) {
                    Node2D n = new Node2D(i, j);
                    vert[i][j] = n;
                    adjacency.put(n, new HashSet<Node>());
                }
            }
        }

        // define adjacency relation
        Node2D N, M;
        for (Node n : adjacency.keySet()) {
            for (Node m : adjacency.keySet()) {
                N = (Node2D) n;
                M = (Node2D) m;
                if (defineAdjacencyA(N.getI(), N.getJ(), M.getI(), M.getJ())) {
                    adjacency.get(n).add(m);
                }
            }
        }

        // define key bindings
        keyBindings = new HashMap<>();
        keyBindings.put(KeyEvent.VK_DELETE, EMPTY);
        keyBindings.put(KeyEvent.VK_BACK_SPACE, EMPTY);
        keyBindings.put(KeyEvent.VK_1, palette[0]);
        keyBindings.put(KeyEvent.VK_2, palette[1]);
        keyBindings.put(KeyEvent.VK_3, palette[2]);
        keyBindings.put(KeyEvent.VK_4, palette[3]);
        keyBindings.put(KeyEvent.VK_5, palette[4]);
        keyBindings.put(KeyEvent.VK_6, palette[5]);
        keyBindings.put(KeyEvent.VK_7, palette[6]);
        keyBindings.put(KeyEvent.VK_8, palette[7]);
        keyBindings.put(KeyEvent.VK_NUMPAD1, palette[0]);
        keyBindings.put(KeyEvent.VK_NUMPAD2, palette[1]);
        keyBindings.put(KeyEvent.VK_NUMPAD3, palette[2]);
        keyBindings.put(KeyEvent.VK_NUMPAD4, palette[3]);
        keyBindings.put(KeyEvent.VK_NUMPAD5, palette[4]);
        keyBindings.put(KeyEvent.VK_NUMPAD6, palette[5]);
        keyBindings.put(KeyEvent.VK_NUMPAD7, palette[6]);
        keyBindings.put(KeyEvent.VK_NUMPAD8, palette[7]);

        // define grid
        grid = new Path2D.Double();
        grid.moveTo(Sudoku.polx(4, 150), Sudoku.poly(4, 150));
        grid.lineTo(Sudoku.polx(4, 150), Sudoku.poly(4, 150) + 4);
        grid.lineTo(0, 4);
        grid.lineTo(Sudoku.polx(4, 30), Sudoku.poly(4, 30) + 4);
        grid.lineTo(Sudoku.polx(4, 30), Sudoku.poly(4, 30));
        grid.lineTo(0, Sudoku.poly(4, 30) + Sudoku.poly(4, 150));
        grid.closePath();
        grid.moveTo(0, 2);
        grid.lineTo(Sudoku.polx(4, 150), Sudoku.poly(4, 150) + 2);
        grid.moveTo(Sudoku.polx(2, 30), Sudoku.poly(2, 30));
        grid.lineTo(Sudoku.polx(2, 30), Sudoku.poly(2, 30) + 4);
        grid.moveTo(Sudoku.polx(2, 150), Sudoku.poly(2, 150));
        grid.lineTo(Sudoku.polx(2, 150) + Sudoku.polx(4, 30), Sudoku.poly(2, 150) + Sudoku.poly(4, 30));
        grid.moveTo(Sudoku.polx(4, 150), Sudoku.poly(4, 150));
        grid.lineTo(0, 0);
        grid.lineTo(Sudoku.polx(4, 30), Sudoku.poly(4, 30));
        grid.moveTo(0, 0);
        grid.lineTo(0, 4);

        // define node map
        nodeMap = new HashMap<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Path2D.Double path = new Path2D.Double();
                if (i < 4 && j < 4) {
                    path.moveTo(Sudoku.polx(4 - j, 150), Sudoku.poly(4 - j, 150) + (4 - i));
                    path.lineTo(Sudoku.polx(3 - j, 150), Sudoku.poly(3 - j, 150) + (4 - i));
                    path.lineTo(Sudoku.polx(3 - j, 150), Sudoku.poly(3 - j, 150) + (3 - i));
                    path.lineTo(Sudoku.polx(4 - j, 150), Sudoku.poly(4 - j, 150) + (3 - i));
                    path.closePath();
                    nodeMap.put(vert[i][j], path);
                } else if (i < 4 && j > 3) {
                    path.moveTo(Sudoku.polx(j - 4, 30), Sudoku.poly(j - 4, 30) + (4 - i));
                    path.lineTo(Sudoku.polx(j - 3, 30), Sudoku.poly(j - 3, 30) + (4 - i));
                    path.lineTo(Sudoku.polx(j - 3, 30), Sudoku.poly(j - 3, 30) + (3 - i));
                    path.lineTo(Sudoku.polx(j - 4, 30), Sudoku.poly(j - 4, 30) + (3 - i));
                    path.closePath();
                    nodeMap.put(vert[i][j], path);
                } else if (i > 3 && j < 4) {
                    path.moveTo(Sudoku.polx(4 - j, 150) + Sudoku.polx(i - 4, 30), Sudoku.poly(4 - j, 150) + Sudoku.poly(i - 4, 30));
                    path.lineTo(Sudoku.polx(3 - j, 150) + Sudoku.polx(i - 4, 30), Sudoku.poly(3 - j, 150) + Sudoku.poly(i - 4, 30));
                    path.lineTo(Sudoku.polx(3 - j, 150) + Sudoku.polx(i - 3, 30), Sudoku.poly(3 - j, 150) + Sudoku.poly(i - 3, 30));
                    path.lineTo(Sudoku.polx(4 - j, 150) + Sudoku.polx(i - 3, 30), Sudoku.poly(4 - j, 150) + Sudoku.poly(i - 3, 30));
                    path.closePath();
                    nodeMap.put(vert[i][j], path);
                }
            }
        }
    }

    public Sudoku3DSimple() {
        values = new HashMap<>();
        given = new HashMap<>();

        // initialize cube
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 4 || j < 4) {
                    values.put(vert[i][j], EMPTY);
                    given.put(vert[i][j], true);
                }
            }
        }

//            for(Node n : adjacency.keySet()){
//                if(adjacency.get(vert[0][0]).contains(n))
//                    System.out.println(n);
//                //System.out.println("(0,0) is adjacent to "+n+": "+adjacency.get(vert[0][0]).contains(n));
//            }
    }

    private void setValue(int i, int j, Character v) {
        setValue(vert[i][j], v);
    }

    public void setValue(int i, int j, int v) {
        setValue(vert[i][j], (char) ('0' + v));
    }

    @Override
    public Sudoku copy() {
        return new Sudoku3DSimple(this);
    }

    public Sudoku3DSimple(Sudoku3DSimple s3ds) {
        values = new HashMap<>();
        given = new HashMap<>();

        for (Node n : adjacency.keySet()) {
            values.put(n, s3ds.getValues().get(n));
            given.put(n, s3ds.getGiven().get(n));
        }
    }

    @Override
    public Node getNodeByNumber(int i, int j) {
        return vert[i][j];
    }

    @Override
    public String toPlaintext() {
        StringBuilder[] gStr = {
            new StringBuilder("                _\n"),
            new StringBuilder("            _.~´x`~._\n"),
            new StringBuilder("        _.~´x       x`~._\n"),
            new StringBuilder("    _.~´x       x   _.~´x`~._\n"),
            new StringBuilder(" .~´x       x   _.~´x       x`~.\n"),
            new StringBuilder("|`~._   x   _.~´x       x   _.~´|\n"),
            new StringBuilder("| x  `~._.~´x       x   _.~´  x |\n"),
            new StringBuilder("|     x  `~._   x   _.~´| x     |\n"),
            new StringBuilder("| x       x  `~. .~´  x |     x |\n"),
            new StringBuilder("|`~._ x       x | x     | x     |\n"),
            new StringBuilder("| x  `~._ x     |     x |     x |\n"),
            new StringBuilder("|     x  `~._ x | x     | x     |\n"),
            new StringBuilder("| x       x  `~.|     x |     x |\n"),
            new StringBuilder(" `~._ x       x | x     | x _.~´\n"),
            new StringBuilder("     `~._ x     |     x |.~´\n"),
            new StringBuilder("         `~._ x | x _.~´\n"),
            new StringBuilder("             `~.|.~´\n")
        };
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 4) {
                    if (j < 4) {
                        gStr[6 + 2 * (3 - i) + j].setCharAt(2 + 4 * j, values.get(vert[i][j]));
                    } else {
                        gStr[6 + 2 * (3 - i) + (7 - j)].setCharAt(2 + 4 * j, values.get(vert[i][j]));
                    }
                } else {
                    if (j < 4) {
                        gStr[4 - (i - 4) + j].setCharAt(4 + 4 * j + 4 * (i - 4), values.get(vert[i][j]));
                    }
                }
            }
        }
        StringBuilder pText = new StringBuilder();
        for (StringBuilder g : gStr) {
            pText.append(g);
        }
        return (pText.toString());
    }

    @Override
    public File toPostScript(boolean userDef) {
        File tempFile = null;
        StringBuilder givenClues;
        try {
            InputStream x = Sudoku3DSimple.class.getResourceAsStream("Sudoku3DSimple.psskel");
            tempFile = File.createTempFile(this.getClass().getSimpleName(), ".psskel");
            byte[] buffer = new byte[x.available()];
            x.read(buffer);
            try (OutputStream outStream = new FileOutputStream(tempFile)) {
                outStream.write(buffer);
            }
            PrintStream tmpStream = new PrintStream(new FileOutputStream(tempFile, true));
            tmpStream.println("/Helvetica-Bold\n20 selectfont");
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (i < 4 && j < 4) {
                        if (numberInPalette(getValue(vert[i][j])) != -1 && given.get(vert[i][j])) {
                            tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
                            tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
                            tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
                        }
                    } else if (i < 4 && j > 3) {
                        if (numberInPalette(getValue(vert[i][j])) != -1 && given.get(vert[i][j])) {
                            tmpStream.println((5 + 10 * (j - 4)) + " mm 30 polar moveto");
                            tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
                            tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
                        }
                    } else if (i > 3 && j < 4) {
                        if (numberInPalette(getValue(vert[i][j])) != -1 && given.get(vert[i][j])) {
                            tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
                            tmpStream.println((5 + 10 * (i - 4)) + " mm 30 polar rmoveto");
                            tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
                        }
                    }
                }
            }
            if (userDef) {
                tmpStream.println("/Courier\n14 selectfont");
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (i < 4 && j < 4) {
                            if (numberInPalette(getValue(vert[i][j])) != -1 && !given.get(vert[i][j])) {
                                tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
                                tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
                                tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
                            }
                        } else if (i < 4 && j > 3) {
                            if (numberInPalette(getValue(vert[i][j])) != -1 && !given.get(vert[i][j])) {
                                tmpStream.println((5 + 10 * (j - 4)) + " mm 30 polar moveto");
                                tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
                                tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
                            }
                        } else if (i > 3 && j < 4) {
                            if (numberInPalette(getValue(vert[i][j])) != -1 && !given.get(vert[i][j])) {
                                tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
                                tmpStream.println((5 + 10 * (i - 4)) + " mm 30 polar rmoveto");
                                tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        return (tempFile);
    }

    @Override
    public int numberInPalette(char x) {
        if ('0' < x && x <= '9') {
            return x - '0' - 1;
        }
        return -1;
    }

    @Override
    public void randomSeed() {
        char[][] seed = new char[8][8];
        seed[0][0] = 'a';
        seed[1][0] = 'b';
        seed[2][0] = 'c';
        seed[3][0] = 'd';
        seed[0][1] = 'e';
        seed[1][1] = 'f';
        seed[2][1] = 'g';
        seed[3][1] = 'h';
        List<Integer[]> perm4u = new ArrayList<>();
        perm4u.add(new Integer[]{0, 1, 2, 3});
        perm4u.add(new Integer[]{0, 2, 1, 3});
        perm4u.add(new Integer[]{0, 3, 1, 2});
        Collections.shuffle(perm4u);
        Integer[] p = perm4u.get(0);
        seed[4 + p[0]][0] = 'e';
        seed[4 + p[1]][0] = 'f';
        seed[4 + p[2]][0] = 'g';
        seed[4 + p[3]][0] = 'h';
        List<Character[]> x0 = new ArrayList<>();
        x0.add(new Character[]{'c', 'd'});
        x0.add(new Character[]{'c', 'g'});
        x0.add(new Character[]{'c', 'h'});
        x0.add(new Character[]{'d', 'g'});
        x0.add(new Character[]{'d', 'h'});
        x0.add(new Character[]{'g', 'h'});
        Collections.shuffle(x0);
        Character[] x1 = x0.get(0);
        List<Character> x2 = new ArrayList<>(Arrays.asList(new Character[]{'b', 'c', 'd', 'f', 'g', 'h'}));
        x2.removeAll(Arrays.asList(x1));
        List<Character> x3 = new ArrayList<>(x2);
        x3.removeAll(Arrays.asList(new Character[]{'b', 'f'}));
        seed[0][2] = x1[0];
        seed[0][3] = x1[1];
        boolean valid = false;
        while (!valid) {
            valid = true;
            List<Integer> perm4 = new ArrayList<>(Arrays.asList(new Integer[]{0, 1, 2, 3}));
            Collections.shuffle(perm4);
            Integer[] P = perm4.toArray(new Integer[0]);
            if (x2.get(P[0]).compareTo(seed[4][0])
                    * x2.get(P[1]).compareTo(seed[5][0])
                    * x2.get(P[2]).compareTo(seed[6][0])
                    * x2.get(P[3]).compareTo(seed[7][0]) == 0) {
                valid = false;
                continue;
            }
            seed[0][4] = x2.get(P[0]);
            seed[0][5] = x2.get(P[1]);
            seed[0][6] = x2.get(P[2]);
            seed[0][7] = x2.get(P[3]);
        }
        int I = Math.random() < 0.5 ? 0 : 1;
        seed[1][2] = x3.get(1 - I);
        seed[1][3] = x3.get(I);
        List<Integer[]> perm4i = new ArrayList<>();
        perm4i.add(new Integer[]{0, 1, 2, 3});
        perm4i.add(new Integer[]{3, 2, 0, 1});
        perm4i.add(new Integer[]{1, 0, 3, 2});
        perm4i.add(new Integer[]{2, 3, 1, 0});
        perm4i.add(new Integer[]{1, 0, 2, 3});
        perm4i.add(new Integer[]{2, 3, 0, 1});
        perm4i.add(new Integer[]{0, 1, 3, 2});
        perm4i.add(new Integer[]{3, 2, 1, 0});
        Collections.shuffle(perm4i);
        Integer[] P1 = perm4i.get(0);
        int P2 = Math.random() < 0.5 ? 0 : 1;
        char[][] seed2 = new char[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 4) {
                    if (j < 2) {
                        seed2[i][8 * 0 + j] = seed[i][j];
                    } else if (j < 4) {
                        seed2[i][8 * 0 + j] = seed[i][j + (j % 2 == 0 ? 1 : -1) * P2];
                    } else {
                        seed2[i][8 * 0 + j] = seed[i][4 + P1[j - 4]];
                    }
                } else if (j < 2) {
                    seed2[i][8 * 0 + j] = seed[4 + P1[i - 4]][j];
                } else if (j < 4) {
                    seed2[i][8 * 0 + j] = seed[4 + P1[i - 4]][j + (j % 2 == 0 ? 1 : -1) * P2];
                }
            }
        }
        List<Integer> indices = new ArrayList<>(Arrays.asList(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7}));
        Collections.shuffle(indices);
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i < 4 || j < 4) {
                    char c = seed2[i][j];
                    if (c > 0) {
                        values.put(vert[i][j], palette[indices.get('h' - seed2[i][j])]);
                    } else {
                        values.put(vert[i][j], EMPTY);
                    }
                }
            }
        }
    }
}
