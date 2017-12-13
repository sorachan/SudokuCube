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
public class StandardSudoku extends Sudoku {

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
        if (index.matches("\\([0-8],[0-8]\\)")) {
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
        // subgrids
        if (i/3==I/3&&j/3==J/3) {
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
        vert = new Node[9][9];
        adjacency = new HashMap<>();
        palette = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9'};

        // initialize cube structure
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Node2D n = new Node2D(i, j);
                vert[i][j] = n;
                adjacency.put(n, new HashSet<Node>());
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
        keyBindings.put(KeyEvent.VK_9, palette[8]);
        keyBindings.put(KeyEvent.VK_NUMPAD1, palette[0]);
        keyBindings.put(KeyEvent.VK_NUMPAD2, palette[1]);
        keyBindings.put(KeyEvent.VK_NUMPAD3, palette[2]);
        keyBindings.put(KeyEvent.VK_NUMPAD4, palette[3]);
        keyBindings.put(KeyEvent.VK_NUMPAD5, palette[4]);
        keyBindings.put(KeyEvent.VK_NUMPAD6, palette[5]);
        keyBindings.put(KeyEvent.VK_NUMPAD7, palette[6]);
        keyBindings.put(KeyEvent.VK_NUMPAD8, palette[7]);
        keyBindings.put(KeyEvent.VK_NUMPAD9, palette[8]);

        // define grid
        grid = new Path2D.Double();
        grid.moveTo(0,0);
        grid.lineTo(0,9);
        grid.lineTo(9,9);
        grid.lineTo(9,0);
        grid.closePath();
        for(int i=1;i<3;i++){
            grid.moveTo(3*i,0);
            grid.lineTo(3*i,9);
            grid.moveTo(0,3*i);
            grid.lineTo(9,3*i);
        }

        // define node map
        nodeMap = new HashMap<>();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Path2D.Double path = new Path2D.Double();
                path.moveTo(i,j);
                path.lineTo(i,j+1);
                path.lineTo(i+1,j+1);
                path.lineTo(i+1,j);
                path.closePath();
                nodeMap.put(vert[i][j], path);
            }
        }
    }

    public StandardSudoku() {
        values = new HashMap<>();
        given = new HashMap<>();

        // initialize cube
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                values.put(vert[i][j], EMPTY);
                given.put(vert[i][j], true);
            }
        }
    }

    private void setValue(int i, int j, Character v) {
        setValue(vert[i][j], v);
    }

    public void setValue(int i, int j, int v) {
        setValue(vert[i][j], (char) ('0' + v));
    }

    @Override
    public Sudoku copy() {
        return new StandardSudoku(this);
    }

    public StandardSudoku(StandardSudoku s3ds) {
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
            new StringBuilder("+-----+-----+-----+\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("+-----+-----+-----+\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("+-----+-----+-----+\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("|x x x|x x x|x x x|\n"),
            new StringBuilder("+-----+-----+-----+\n"),
        };
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                gStr[1+2*i].setCharAt(1+2*j, values.get(vert[i][j]));
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
        System.err.println("standard sudoku .ps not implemented yet");
//        StringBuilder givenClues;
//        try {
//            InputStream x = StandardSudoku.class.getResourceAsStream("StandardSudoku.psskel");
//            tempFile = File.createTempFile(this.getClass().getSimpleName(), ".psskel");
//            byte[] buffer = new byte[x.available()];
//            x.read(buffer);
//            try (OutputStream outStream = new FileOutputStream(tempFile)) {
//                outStream.write(buffer);
//            }
//            PrintStream tmpStream = new PrintStream(new FileOutputStream(tempFile, true));
//            tmpStream.println("/Helvetica-Bold\n20 selectfont");
//            for (int i = 0; i < 8; i++) {
//                for (int j = 0; j < 8; j++) {
//                    if (i < 4 && j < 4) {
//                        if (numberInPalette(getValue(vert[i][j])) != -1 && given.get(vert[i][j])) {
//                            tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
//                            tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
//                            tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
//                        }
//                    } else if (i < 4 && j > 3) {
//                        if (numberInPalette(getValue(vert[i][j])) != -1 && given.get(vert[i][j])) {
//                            tmpStream.println((5 + 10 * (j - 4)) + " mm 30 polar moveto");
//                            tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
//                            tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
//                        }
//                    } else if (i > 3 && j < 4) {
//                        if (numberInPalette(getValue(vert[i][j])) != -1 && given.get(vert[i][j])) {
//                            tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
//                            tmpStream.println((5 + 10 * (i - 4)) + " mm 30 polar rmoveto");
//                            tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
//                        }
//                    }
//                }
//            }
//            if (userDef) {
//                tmpStream.println("/Courier\n14 selectfont");
//                for (int i = 0; i < 8; i++) {
//                    for (int j = 0; j < 8; j++) {
//                        if (i < 4 && j < 4) {
//                            if (numberInPalette(getValue(vert[i][j])) != -1 && !given.get(vert[i][j])) {
//                                tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
//                                tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
//                                tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
//                            }
//                        } else if (i < 4 && j > 3) {
//                            if (numberInPalette(getValue(vert[i][j])) != -1 && !given.get(vert[i][j])) {
//                                tmpStream.println((5 + 10 * (j - 4)) + " mm 30 polar moveto");
//                                tmpStream.println("0 -" + (35 - 10 * i) + " mm rmoveto");
//                                tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
//                            }
//                        } else if (i > 3 && j < 4) {
//                            if (numberInPalette(getValue(vert[i][j])) != -1 && !given.get(vert[i][j])) {
//                                tmpStream.println((35 - 10 * j) + " mm 150 polar moveto");
//                                tmpStream.println((5 + 10 * (i - 4)) + " mm 30 polar rmoveto");
//                                tmpStream.println("(" + getValue(vert[i][j]) + ") center show");
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            System.err.println(e);
//        }
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
        List<Integer> numList = new ArrayList<>();
        for(int i=0;i<9;i++){
            numList.add(i);
        }
        Collections.shuffle(numList);
        values.put(vert[0][0],(char)('1'+numList.get(0)));
        values.put(vert[0][1], (char)('1'+numList.get(1)));
        values.put(vert[0][2], (char)('1'+numList.get(2)));
        values.put(vert[1][0], (char)('1'+numList.get(3)));
        values.put(vert[1][1], (char)('1'+numList.get(4)));
        values.put(vert[1][2], (char)('1'+numList.get(5)));
        values.put(vert[2][0], (char)('1'+numList.get(6)));
        values.put(vert[2][1], (char)('1'+numList.get(7)));
        values.put(vert[2][2], (char)('1'+numList.get(8)));
    }
}
