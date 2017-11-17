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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.print.attribute.standard.MediaSizeName;

/**
 *
 * @author Sora Steenvoort
 */
public class PostScriptWorkshop {

    public static File printPuzzle(Sudoku puz, Sudoku sln, MediaSizeName format, boolean printSln, boolean userDef) {
        File tempFile = null;
        File puzPS = puz.toPostScript(userDef);
        File slnPS = sln.toPostScript(userDef);
        try {
            tempFile = File.createTempFile("PuzzleWithSolution", ".ps");
            InputStream x;
            OutputStream outStream;
            byte[] buffer;
            BufferedReader br;
            String line;
            PrintStream tmpStream;

            int h = 0, w = 0;

            if (format.equals(MediaSizeName.ISO_A4)) {
                w = 595;
                h = 842;
            } else if (format.equals(MediaSizeName.NA_LETTER)) {
                w = 612;
                h = 792;
            }

            SimpleDateFormat dateTime = new SimpleDateFormat("yyyyMMddHHmmssZZZ'ZZ'");

            outStream = new FileOutputStream(tempFile, true);
            tmpStream = new PrintStream(outStream);
            tmpStream.println(
                    "%!PS-Adobe-3.0\n"
                    + "%%BoundingBox: 0 0 " + w + " " + h + "\n"
                    + "%%HiResBoundingBox: 0 0 " + w + ".00 " + h + ".00\n"
                    + "%%Creator: handwritten PostScript\n"
                    + "%%LanguageLevel: 2\n"
                    + "%%CreationDate: D:" + dateTime.format(new Date()) + "\n"
                    + "%%DocumentData: Clean8Bit\n"
                    + "%%Pages: 1\n"
                    + "%%EndComments\n"
                    + "%%BeginProlog\n"
                    + "/pw {" + w + "} def\n"
                    + "/ph {" + h + "} def"
            );
            outStream.close();

            x = PostScriptWorkshop.class.getResourceAsStream("PuzzleWithSolution1.psskel");
            buffer = new byte[x.available()];
            x.read(buffer);
            outStream = new FileOutputStream(tempFile, true);
            outStream.write(buffer);
            outStream.close();

            outStream = new FileOutputStream(tempFile, true);
            tmpStream = new PrintStream(outStream);
            tmpStream.println("%%PageBoundingBox: 0 0 " + w + " " + h);
            outStream.close();

            x = PostScriptWorkshop.class.getResourceAsStream("PuzzleWithSolution2.psskel");
            buffer = new byte[x.available()];
            x.read(buffer);
            outStream = new FileOutputStream(tempFile, true);
            outStream.write(buffer);
            outStream.close();

            outStream = new FileOutputStream(tempFile, true);
            tmpStream = new PrintStream(outStream);
            if (puz.getClass().getSimpleName().equals("Sudoku3DSimple")) {
                tmpStream.println("2 2 scale");
            }
            br = new BufferedReader(new FileReader(puzPS));
            while ((line = br.readLine()) != null) {
                tmpStream.println(line);
            }
            outStream.close();

            if (printSln) {
                outStream = new FileOutputStream(tempFile, true);
                tmpStream = new PrintStream(outStream);
                tmpStream.println(
                        "restore\n"
                        + "\n"
                        + "save\n"
                        + "pw 2 div 25 mm translate\n"
                        + ".5 .5 scale\n"
                        + "180 rotate\n"
                        + "0 5 mm moveto\n"
                        + "/Helvetica-Bold\n"
                        + "20 selectfont\n"
                        + "(" + java.util.ResourceBundle.getBundle("de/rub/sudokucube/Bundle").getString("SOLUTION:") + ")" + " hcenter show\n"
                        + "/showpage {} bind def"
                );
                br = new BufferedReader(new FileReader(slnPS));
                while ((line = br.readLine()) != null) {
                    tmpStream.println(line);
                }
                outStream.close();
            }

            x = PostScriptWorkshop.class.getResourceAsStream("PuzzleWithSolution3.psskel");
            buffer = new byte[x.available()];
            x.read(buffer);
            outStream = new FileOutputStream(tempFile, true);
            outStream.write(buffer);
            outStream.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        puzPS.delete();
        slnPS.delete();
        return tempFile;
    }

    public static File printPuzzleWithSolution(Sudoku puz, Sudoku sln) {
        return printPuzzle(puz, sln, MediaSizeName.ISO_A4, true, false);
    }
}
