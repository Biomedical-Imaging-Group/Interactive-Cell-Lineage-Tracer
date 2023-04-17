/*
 * Interactive Cell Lineage Tracer (ICLT)
 * 
 * Author: Daniel Sage and Chiara Toniolo, EPFL
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: Book chapter, 2023
 * Quantification of Mycobacterium tuberculosis growth in cell-based infection 
 * assays by time-lapse fluorescence microscopy
 * Chiara Toniolo, Daniel Sage, John D. McKinney, Neeraj Dhar
 */

/*
 * Copyright 2014-2023 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of Interactive Cell Lineage Tracer (ICLT).
 * 
 * ICLT is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * ICLT is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * ICLT. If not, see <http://www.gnu.org/licenses/>.
 */

package celllineagetracer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JFileChooser;

import celllineagetracer.cell.Cell;
import celllineagetracer.cell.Cells;
import celllineagetracer.outline.Outline;
import celllineagetracer.pixelclass.PixelClass;
import celllineagetracer.pixelclass.PixelClasses;
import celllineagetracer.polyline.Node;
import celllineagetracer.polyline.Polyline;
import ij.IJ;

public class ICLTFile {
	public static void save(String filename) {
		try {
			Cells cells = Supervisor.cells;
			PixelClasses classes = Supervisor.classes;
			BufferedWriter buffer = new BufferedWriter(new FileWriter(filename));
			for (String name : cells.keySet()) {
				Cell cell = (Cell) cells.get(name);
				String row = "CELL, " + name + ", " + cell.getDefaultClass() + ", " + cell.getHue() + ", "
						+ cell.isMobile() + ", " + cell.isMultiplePart();
				buffer.write(row + "\n");
			}
			for (String name : classes.keySet()) {
				PixelClass klass = (PixelClass) classes.get(name);
				String row = "CLASS, " + name + ", " + klass.hue + ", ";
				row = row + klass.valueRegion + ", ";
				row = row + klass.sizeInner + ", " + klass.valueInner + ", ";
				row = row + klass.sizeOuter + ", " + klass.valueOuter + ", ";
				buffer.write(row + "\n");
			}
			Iterator<Integer> localIterator2;

			for (String name : cells.keySet()) {
				Cell cell = (Cell) cells.get(name);
				localIterator2 = cell.getListOutlinesFrame().iterator();
				Integer frame = (Integer) localIterator2.next();
				Outline outline = cell.getOutline(frame);
				String row = "OUTLINE, " + outline.cell + ", " + outline.klass + "," + Tools.frame(frame.intValue())
						+ ", ";
				Polyline p = outline.getPolyline();
				row = row + p.size() + ",";
				for (Node node : p) {
					row = row + node.x + "," + node.y + ",";
				}
				buffer.write(row + "\n");
			}
			buffer.close();
		}
		catch (IOException ex) {
			IJ.error("Unable to save the cells into " + filename);
		}
	}

	public static void load(String filename) {
		Supervisor.clear();

		String line = "-";
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(filename));
			line = buffer.readLine();
			while (line != null) {
				StringTokenizer tokens = new StringTokenizer(line, ",");
				String type = tokens.nextToken().trim();
				if (type.equals("CLASS")) {
					int count = tokens.countTokens();
					String name = count > 0 ? tokens.nextToken().trim() : "noname";
					int hue = count > 1 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					int valueRegion = count > 1 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					int sizeInner = count > 3 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					int valueInner = count > 4 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					int sizeOuter = count > 5 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					int valueOuter = count > 6 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					PixelClass klass = new PixelClass(hue, valueRegion);
					klass.setInnerContour(sizeInner, valueInner);
					klass.setOuterContour(sizeOuter, valueOuter);
					Supervisor.classes.put(name, klass);
				}
				if (type.equals("CELL")) {
					int count = tokens.countTokens();
					String name = count > 0 ? tokens.nextToken().trim() : "noname";
					String klass = count > 1 ? tokens.nextToken().trim() : "";
					int hue = count > 1 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					boolean mo = count > 1 ? tokens.nextToken().trim().equals("true") : true;
					boolean mp = count > 1 ? tokens.nextToken().trim().equals("true") : true;
					Cell cell = new Cell(klass, hue, mo, mp);
					Supervisor.cells.put(name, cell);
				}
				if (type.equals("OUTLINE")) {
					int count = tokens.countTokens();
					String name = count > 0 ? tokens.nextToken().trim() : "noname";
					String klass = count > 1 ? tokens.nextToken().trim() : "";
					int frame = count > 2 ? Tools.convertToInt(tokens.nextToken(), 0) : 0;
					Cell cell = (Cell) Supervisor.cells.get(name);
					if (cell != null) {
						int npoints = Tools.convertToInt(tokens.nextToken(), 0);
						Polyline p = new Polyline();
						for (int i = 0; i < npoints; i++) {
							double x = Tools.convertToDouble(tokens.nextToken(), 0.0D);
							double y = Tools.convertToDouble(tokens.nextToken(), 0.0D);
							p.add(new Node(x, y));
						}
						cell.addOutline(frame, new Outline(p, name, klass, frame));
					}
				}
				line = buffer.readLine();
			}
			buffer.close();
		}
		catch (Exception ex) {
			IJ.error("Unable to read the line " + line);
		}
	}

	public static String browseOpen(String path) {
		if (path == null) {
			path = System.getProperty("user.home");
		}
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File(path));
		fc.setFileSelectionMode(0);
		int ret = fc.showOpenDialog(null);
		if (ret == 0) {
			return fc.getSelectedFile().getAbsolutePath();
		}
		return null;
	}

	public static String browseSave(String path) {
		if (path == null) {
			path = System.getProperty("user.home");
		}
		JFileChooser fc = new JFileChooser();
		fc.setSelectedFile(new File(path));
		fc.setFileSelectionMode(0);
		int ret = fc.showSaveDialog(null);
		if (ret == 0) {
			return fc.getSelectedFile().getAbsolutePath();
		}
		return null;
	}
}
