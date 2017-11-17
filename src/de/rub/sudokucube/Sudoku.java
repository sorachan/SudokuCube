package de.rub.sudokucube;

import java.awt.geom.Path2D;
import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Sora Steenvoort
 */
public abstract class Sudoku {

    abstract int TIME_FOR_RP();

    abstract int TIME_FOR_RPFS();

    int TIME() {
        return TIME_FOR_RP() + TIME_FOR_RPFS();
    }

    public abstract Map<Node, Path2D> getNodeMap();

    public abstract Path2D getGrid();

    static double polx(double r, double theta) {
        return r * Math.cos(theta * 2 * Math.PI / 360.0);
    }

    static double poly(double r, double theta) {
        if (theta == 30 || theta == 150) {
            return -r * 0.5; // negative to make up for Java's crappy coordinate system
        }
        if (theta == -30 || theta == -150) {
            return r * 0.5; // positive to make up for Java's crappy coordinate system
        }
        return -r * Math.sin(theta * 2 * Math.PI / 360.0);
    }

    abstract char[] getPalette();

    abstract Map<Node, HashSet<Node>> getAdjacency(); // map of sets

    abstract Map<Integer, Character> getKeyBindings();

    abstract public char getEmpty();
    
    Map<Node, Character> values;
    Map<Node, Boolean> given;

    public static Map<Integer, Character> getDefaultKeyBindings() {
        Map<Integer, Character> defKeyBind = new HashMap<>();
        return defKeyBind;
    }

    public Sudoku sanitize() {
        Sudoku s = copy();
        for (Node n : given.keySet()) {
            if (!given.get(n)) {
                s.setValue(n, getEmpty());
                s.getGiven().put(n, true);
            }
        }
        return s;
    }

    public void lock() {
        for (Node n : getNodes()) {
            if (values.get(n).compareTo(getEmpty()) == 0) {
                given.put(n, false);
            }
        }
    }

    public void toXML(PrintStream os) {
        os.println(this.getClass().getCanonicalName());
        for (Node n : getNodes()) {
            char c = values.get(n);
            if (c != getEmpty()) {
                os.println(n + ";" + c + ";" + (given.get(n) ? 1 : 0));
            }
        }
    }

    static public Sudoku fromXML(InputStream is) throws Exception {
        String inLine = "";
        BufferedReader br = null;
        Sudoku s = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            inLine = br.readLine();
            s = (Sudoku) (Class.forName(inLine).newInstance());
        } catch (ClassNotFoundException cnfe) {
            throw (new ClassNotFoundException(
                    "The class specified in the file header is invalid!\n"
                    + "Offending class name: " + inLine
            ));
        } catch (ClassCastException cce) {
            throw (new ClassNotFoundException(
                    "The class specified in the file header is not a subclass of Sudoku!\n"
                    + "Offending class name: " + inLine
            ));
        }
        while ((inLine = br.readLine()) != null) {
            if (inLine.length() > 0) {
                String[] fields = inLine.split(";");
                Node n = s.nodeByIndex(fields[0]);
                s.getValues().put(n, fields[1].charAt(0));
                s.getGiven().put(n, fields[2].charAt(0) == '1');
            }
        }
        return s;
    }

    abstract public Node nodeByIndex(String index);

    static public Sudoku randomPuzzle(String className) throws Exception {
        Class cls = Class.forName(className);
        Sudoku s = (Sudoku) (cls.newInstance());
        char[] palette = (char[]) (cls.getDeclaredField("palette").get(null));
        char empty = (char) (cls.getDeclaredField("EMPTY").get(null));
        s.randomSeed();
        List<Node> nodesls = new ArrayList<>(s.getNodes());
        Set<Node> nodesrm = new HashSet<>();
        for (Node n : nodesls) {
            if (s.getValue(n) != s.getEmpty()) {
                nodesrm.add(n);
            }
        }
        nodesls.removeAll(nodesrm);
        Node n = null;
        int slnCt = SudokuToolkit.isSolvable(s);
        while (slnCt == 0) {
            s.randomSeed();
        }
        while (slnCt != 1) {
            if (slnCt > 1) {
                HashMap<Node, boolean[]> p = SudokuToolkit.auxNumbers(s);
                Collections.shuffle(nodesls);
                n = nodesls.get(0);
                List<Integer> candidates = new ArrayList<>();
                for (int i = 0; i < palette.length; i++) {
                    if (p.get(n)[i] == true) {
                        candidates.add(i);
                    }
                }
                Collections.shuffle(candidates);
                int index = candidates.get(0);
                s.setValue(n, palette[index]);
            } else {
                s.setValue(n, empty);
            }
            slnCt = SudokuToolkit.isSolvable(s);
        }
        List<Sudoku> sln = new ArrayList<>(SudokuToolkit.solve(s));
        return sln.get(0);
    }

    public abstract void randomSeed();

    public abstract Sudoku copy();

    public abstract String toPlaintext();

    public abstract File toPostScript(boolean userDef);

    public void setValue(Node n, Character v) {
        values.put(n, v);
    }

    public Map<Node, Character> getValues() {
        return values;
    }

    public Map<Node, Boolean> getGiven() {
        return given;
    }

    public Character getValue(Node n) {
        return values.get(n);
    }

    public Set<Node> getNodes() {
        return getAdjacency().keySet();
    }

    public abstract Node getNodeByNumber(int i, int j);

    public abstract int numberInPalette(char x);
}
