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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

/**
 *
 * @author Sora Steenvoort
 */
public class DrawPanel extends JPanel implements MouseListener {

    private boolean isSetUp = false;
    private boolean interactive = false;
    private boolean showHints = false;
    private Map<Node, Path2D> nodeMap = null;
    private Sudoku sud = null;
    private double minX, maxX, minY, maxY;
    private Node activeNode = null;
    private AffineTransform ats, att;
    private Set<Node> wrong = null;
    private boolean wrongAlert = false;
    private boolean wrongVis = true;

    public DrawPanel() {
        super();
        this.addMouseListener(this);
    }

    public void toggleHints() {
        showHints = !showHints;
        repaint();
    }

    public boolean setup(Sudoku Sud, boolean Interactive) {
        showHints = false;
        activeNode = null;
        sud = Sud;
        nodeMap = sud.getNodeMap();
        minX = maxX = minY = maxY = 0;
        for (Node n : nodeMap.keySet()) {
            Rectangle2D bBox = nodeMap.get(n).getBounds2D();
            minX = Math.min(minX, bBox.getMinX());
            maxX = Math.max(maxX, bBox.getMaxX());
            minY = Math.min(minY, bBox.getMinY());
            maxY = Math.max(maxY, bBox.getMaxY());
        }
        if (Interactive) {
            sud.lock();
            interactive = Interactive;
            InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            ActionMap am = getActionMap();
            for (final int kBind : sud.getKeyBindings().keySet()) {
                im.put(KeyStroke.getKeyStroke(kBind, 0), "KB" + kBind);
                am.put("KB" + kBind, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent ae) {
                        if (activeNode != null && !sud.getGiven().get(activeNode)) {
                            sud.setValue(activeNode, sud.getKeyBindings().get(kBind));
                            repaint();
                        }
                    }
                });
            }
        }
        isSetUp = true;
        return true;
    }

    public void drawCString(Graphics2D g, String text, Rectangle2D rect) {
        FontMetrics metrics = g.getFontMetrics();
        double x = rect.getCenterX() - metrics.stringWidth(text) / 2;
        double y = rect.getCenterY() - metrics.getHeight() / 2 + metrics.getAscent();
        g.drawString(text, (float) x, (float) y);
    }

    private void setActiveNode(MouseEvent me) {
        if (isSetUp) {
            AffineTransform atr = new AffineTransform();
            atr.setToTranslation(this.getWidth() / 2.0, this.getHeight() / 2.0);
            if (me.getButton() == MouseEvent.BUTTON1) {
                for (Node n : nodeMap.keySet()) {
                    Path2D p = (Path2D) (nodeMap.get(n).clone());
                    p.transform(att);
                    p.transform(ats);
                    p.transform(atr);
                    if (p.contains(me.getX(), me.getY())) {
                        activeNode = n;
                        break;
                    }
                }
                repaint();
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        if (isSetUp) {
            Path2D grid = (Path2D) (sud.getGrid().clone());
            g2.translate(this.getWidth() / 2.0, this.getHeight() / 2.0);
            Double sFactor = Math.min(this.getWidth() / (maxX - minX), this.getHeight() / (maxY - minY));
            ats = new AffineTransform();
            att = new AffineTransform();
            ats.setToScale(.95 * sFactor, .95 * sFactor);
            att.setToTranslation(-(minX + maxX) / 2.0, -(minY + maxY) / 2.0);
            Font mainFont = new Font(g2.getFont().getName(), Font.BOLD, (int) (.5 * sFactor));
            Font userFont = new Font(g2.getFont().getName(), Font.ITALIC, (int) (.4 * sFactor));
            Font hintFontBase = new Font(g2.getFont().getName(), Font.ITALIC, (int) (2 * sFactor / sud.getPalette().length));
            for (Node n : nodeMap.keySet()) {
                Path2D path = (Path2D) (nodeMap.get(n).clone());
                path.transform(att);
                path.transform(ats);
                if (n == activeNode) {
                    Paint p = g2.getPaint();
                    g2.setPaint(new Color(255, 255, 153));
                    g2.fill(path);
                    g2.setPaint(p);
                }
                g2.draw(path);
                if (sud.getValue(n) != sud.getEmpty()) {
                    Paint p = g2.getPaint();
                    if (sud.getGiven().get(n)) {
                        g2.setFont(mainFont);
                    } else {
                        g2.setFont(userFont);
                        if (wrongAlert && wrong.contains(n)) {
                            if (wrongVis) {
                                g2.setPaint(java.awt.Color.RED);
                            } else {
                                g2.setPaint(new Color(0, 0, 0, 0));
                            }
                        }
                    }
                    drawCString(g2, "" + sud.getValue(n), path.getBounds2D());
                    g2.setPaint(p);
                } else if (showHints) {
                    boolean[] hint = SudokuToolkit.auxNumbers(sud).get(n);
                    String hintStr = "";
                    for (int i = 0; i < sud.getPalette().length; i++) {
                        if (hint[i]) {
                            hintStr = hintStr.concat("" + sud.getPalette()[i]);
                        }
                    }
                    Font hintFont;
                    int width = g2.getFontMetrics(hintFontBase).stringWidth(hintStr);
                    Double limit = .7 * sFactor;
                    if (width > limit) {
                        hintFont = new Font(g2.getFont().getName(), Font.ITALIC, (int) (2 * (limit / width) * sFactor / sud.getPalette().length));
                    } else {
                        hintFont = hintFontBase;
                    }
                    Paint p = g2.getPaint();
                    g2.setFont(hintFont);
                    g2.setPaint(java.awt.Color.BLUE);
                    drawCString(g2, hintStr, path.getBounds2D());
                    g2.setPaint(p);
                }
            }
            grid.transform(att);
            grid.transform(ats);
            g2.setStroke(new BasicStroke((float) (sFactor / 10.0), BasicStroke.JOIN_MITER, BasicStroke.CAP_BUTT));
            g2.draw(grid);
        }
    }

    @Override
    public void mouseClicked(MouseEvent me) {
    }

    @Override
    public void mousePressed(MouseEvent me) {
        if (interactive) {
            setActiveNode(me);
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
    }

    @Override
    public void mouseEntered(MouseEvent me) {
    }

    @Override
    public void mouseExited(MouseEvent me) {
    }

    private void cleanUp() {
        for (Node n : wrong) {
            sud.setValue(n, sud.getEmpty());
        }
    }

    private Timer timer = new Timer(500, new ActionListener() {
        private int counter = 0;

        @Override
        public void actionPerformed(ActionEvent ae) {
            wrongVis = !wrongVis;
            repaint();
            if (counter++ > 6) {
                counter = 0;
                wrongAlert = false;
                cleanUp();
                timer.stop();
            }
        }
    });

    public void verify(Sudoku sln) {
        wrong = new HashSet<>();
        for (Node n : sud.getGiven().keySet()) {
            if (!sud.getGiven().get(n)) {
                if (sud.getValue(n).compareTo(sud.getEmpty()) != 0) {
                    if (sud.getValue(n).compareTo(sln.getValue(n)) != 0) {
                        wrong.add(n);
                    }
                }
            }
        }
        if(!wrong.isEmpty()){
            wrongAlert = true;
            timer.start();
        }else{
            boolean solved = true;
            for (Node n : sud.getGiven().keySet()) {        
                if (sud.getValue(n).compareTo(sln.getValue(n)) != 0) {
                    solved = false;
                }
            }
            if(solved){
                java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/rub/sudokucube/Bundle"); // NOI18N
                JOptionPane.showMessageDialog(this, bundle.getString("SOLVED"));
            }
        }
    }
}
