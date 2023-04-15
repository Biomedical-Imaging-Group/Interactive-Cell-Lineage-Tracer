/*
 * Interface Cell Lineage Tracer (ICLT)
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
 * This file is part of Interface Cell Lineage Tracer (ICLT).
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

import celllineagetracer.canvas.ICLTCanvas;
import celllineagetracer.canvas.ICLTWindow;
import celllineagetracer.cell.Cell;
import celllineagetracer.cell.Cells;
import celllineagetracer.cell.CellsTable;
import celllineagetracer.outline.Outline;
import celllineagetracer.outline.OutlinesTable;
import celllineagetracer.pixelclass.PixelClass;
import celllineagetracer.pixelclass.PixelClasses;
import celllineagetracer.pixelclass.PixelClassesTable;
import celllineagetracer.polyline.DrawParameters;
import celllineagetracer.polyline.Node;
import celllineagetracer.polyline.Polyline;
import ij.ImagePlus;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JOptionPane;

public class Supervisor implements MouseListener, KeyListener {
	public static Cells cells;
	public static PixelClasses classes;
	public static ImagePlus imp;
	private ICLTWindow window;
	private ICLTCanvas canvas;
	private CellsTable tableCells;
	private OutlinesTable tableOutlines;
	private PixelClassesTable tablePixelClasses;
	private Outline selected = null;

	public Supervisor(ImagePlus imp) {
		Supervisor.imp = imp;
		cells = new Cells();
		classes = new PixelClasses();
		this.tableCells = new CellsTable();
		this.tablePixelClasses = new PixelClassesTable();
		this.tableOutlines = new OutlinesTable();
		this.tableOutlines.addMouseListener(this);
		this.tableOutlines.addKeyListener(this);
		this.tablePixelClasses.addMouseListener(this);
		this.tableCells.addMouseListener(this);
	}

	public static void clear() {
		classes.clear();
		for (String cell : cells.keySet()) {
			((Cell) cells.get(cell)).removeAllOutlines();
		}
		cells.clear();
	}

	public void attachWindow(ICLTWindow window, ICLTCanvas canvas) {
		this.window = window;
		this.canvas = canvas;
	}

	public Outline createOutline(int frame, Polyline polyline) {
		String cellName = this.window.getCell();
		if (cellName == null) {
			return null;
		}
		Cell cell = (Cell) cells.get(cellName);
		if (cell == null) {
			return null;
		}
		String klassName = this.window.getPixelClass();

		Polyline p = new Polyline();
		for (Node node : polyline) {
			p.add(node);
		}
		Outline outline = new Outline(p, cellName, klassName, frame);
		cell.addOutline(frame, outline);
		updateAll(outline, true);
		return outline;
	}

	public void deleteOutline(Outline outline) {
		if (outline != null) {
			Cell cell = (Cell) cells.get(outline.cell);
			if (cell != null) {
				cell.removeOutline(new Integer(outline.getFrame()).intValue());
			}
		}
		updateAll(null, true);
	}

	public void interpolateOutline(Outline start) {
		if (start == null) {
			return;
		}
		Cell cell = (Cell) cells.get(start.cell);
		int next = start.getFrame() + 1;
		Outline connect = null;
		for (Integer frame : cell.getListOutlinesFrame()) {
			Outline outline = cell.getOutline(frame);
			if (outline.getFrame() > next) {
				next = outline.getFrame();
				connect = outline;
			}
		}
		if ((start != null) && (connect != null)) {
			int count = 0;
			for (int f = start.getFrame() + 1; f < connect.getFrame(); f++) {
				Outline outline = getOutline(f, start.cell);
				if (outline == null) {
					count++;
				}
			}
			if (count >= 1) {
				interpolatate(cell, start, connect);
			}
		}
		updateAll(null, true);
	}

	private void interpolatate(Cell cell, Outline start, Outline last) {
		Polyline a = start.getPolyline();
		Polyline b = last.getPolyline();
		Polyline ra = a.resample(100);
		Polyline rb = b.resample(100);
		ra.makeClockwise();
		rb.makeClockwise();
		double tol = DrawParameters.tolerance;
		int t1 = start.getFrame();
		int t2 = last.getFrame();
		int nf = t2 - t1;
		for (int t = 1; t < nf; t++) {
			Polyline d = new Polyline();
			for (int i = 0; i < Math.min(ra.size(), rb.size()); i++) {
				double x = ((nf - t) * ((Node) ra.get(i)).x + t * ((Node) rb.get(i)).x) / nf;
				double y = ((nf - t) * ((Node) ra.get(i)).y + t * ((Node) rb.get(i)).y) / nf;
				d.add(new Node(x, y));
			}
			Outline n = new Outline(d.simplify(tol), start.cell, start.klass, t + t1);
			cell.addOutline(t + t1, n);
		}
	}

	public void propagateOutline(Outline start) {
		if (start == null) {
			return;
		}
		Cell cell = (Cell) cells.get(start.cell);
		if (cell == null) {
			return;
		}
		for (int f = start.getFrame() + 1; f <= imp.getNFrames(); f++) {
			if (cell.getOutline(Integer.valueOf(f)) == null) {
				Outline outline = start.duplicate();
				outline.setFrame(f);
				cell.addOutline(f, outline);
			}
		}
		updateAll(null, true);
	}

	public void deleteCell(String name) {
		if (name == null) {
			return;
		}
		if (cells == null) {
			return;
		}
		Cell cell = (Cell) cells.get(name);
		if (cell == null) {
			return;
		}
		boolean flag = true;
		if (cell.getCountOutline() > 0) {
			int ret = JOptionPane.showConfirmDialog(null,
					"Do you want to delete this cell (" + cell.getCountOutline() + " outlines)", "Choose", 0);
			if (ret == 1) {
				flag = false;
			}
		}
		if (flag) {
			cells.remove(name);
		}
		updateAll(null, true);
	}

	public Cell getCell(String name) {
		if (name == null) {
			return null;
		}
		if (cells == null) {
			return null;
		}
		return (Cell) cells.get(name);
	}

	public void deleteClass(String name) {
		if (name == null) {
			return;
		}
		if (cells == null) {
			return;
		}
		PixelClass klass = (PixelClass) classes.get(name);
		if (klass == null) {
			return;
		}
		int count = 0;

		Iterator<Integer> localIterator2;
		for (Iterator<String> localIterator1 = cells.keySet().iterator(); localIterator1.hasNext(); localIterator2.hasNext()) {
			String cell = (String) localIterator1.next();
			Cell c = (Cell) cells.get(cell);
			localIterator2 = c.getListOutlinesFrame().iterator(); // continue;
			Integer frame = (Integer) localIterator2.next();
			if (c.getOutline(frame).klass.equals(name)) {
				count++;
			}
		}
		boolean flag = true;
		if (count > 0) {
			int ret = JOptionPane.showConfirmDialog(null, "Do you want to delete this class (" + count + " outlines)",
					"Choose", 0);
			if (ret == 1) {
				flag = false;
			}
		}
		if (flag) {
			classes.remove(name);
			Iterator<Integer> localIterator3;
			for (String ncell : cells.keySet()) {
				Cell c = (Cell) cells.get(ncell);
				localIterator3 = c.getListOutlinesFrame().iterator(); // continue;
				Integer frame = (Integer) localIterator3.next();
				if (c.getOutline(frame).klass.equals(name)) {
					c.getOutline(frame).klass = "";
				}
			}
		}
		updateAll(null, true);
	}

	public static ArrayList<Outline> getOutlinesAtFrame(int frame) {
		ArrayList<Outline> outlines = new ArrayList<Outline>();
		for (String name : cells.keySet()) {
			Cell cell = (Cell) cells.get(name);
			Set<Integer> oc = cell.getListOutlinesFrame();
			for (Integer f : oc) {
				if (f.intValue() == frame) {
					outlines.add(cell.getOutline(f));
				}
			}
		}
		return outlines;
	}

	/*
	 * public static ArrayList<Outline> getOutlinesAtFrame(int frame) {
	 * System.out.println("DEBUG Supervisor line 272 " + frame); ArrayList<Outline>
	 * outlines = new ArrayList<Outline>(); Iterator<Integer> localIterator2;
	 * System.out.println("DEBUG Supervisor line 272" + cells);
	 * System.out.println("DEBUG Supervisor line 272" + cells.keySet());
	 * 
	 * for (Iterator<String> localIterator1 = cells.keySet().iterator();
	 * localIterator1.hasNext(); localIterator2.hasNext()) { String name =
	 * (String)localIterator1.next(); Cell cell = (Cell)cells.get(name);
	 * localIterator2 = cell.getListOutlinesFrame().iterator(); //continue; Integer
	 * f = (Integer)localIterator2.next(); if (f.intValue() == frame) {
	 * outlines.add(cell.getOutline(f)); } } return outlines; }
	 */

	public static Outline getOutline(int frame, String cell) {
		Cell c = (Cell) cells.get(cell);
		if (cell != null) {
			return c.getOutline(Integer.valueOf(frame));
		}
		return null;
	}

	public ICLTCanvas getCanvas() {
		return this.canvas;
	}

	public ICLTWindow getWindow() {
		return this.window;
	}

	public Outline getSelected() {
		return this.selected;
	}

	public CellsTable getTableCells() {
		return this.tableCells;
	}

	public OutlinesTable getTableOutlines() {
		return this.tableOutlines;
	}

	public PixelClassesTable getTablePixelClasses() {
		return this.tablePixelClasses;
	}

	public void updateAll(Outline selected, boolean updateTable) {
		this.tableCells.update();
		this.tableOutlines.update();
		this.tablePixelClasses.update();

		updateTable = true;

		this.selected = selected;
		if (selected != null) {
			int channel = imp.getChannel();
			int slice = imp.getSlice();
			imp.setPosition(channel, slice, selected.getFrame());
		}
		if (this.window != null) {
			this.window.updateList();
			this.window.updateBar();
		}
		if ((selected != null) && (this.window != null)) {
			if (selected.cell != null) {
				this.window.setCell(selected.cell);
			}
			if (selected.klass != null) {
				this.window.setPixelClass(selected.klass);
			}
		}
		this.canvas.repaint();
		if ((updateTable) && (selected != null)) {
			for (int i = 0; i < this.tableOutlines.getRowCount(); i++) {
				int frame = Tools.convertToInt((String) this.tableOutlines.getValueAt(i, 2), -1);
				String cell = (String) this.tableOutlines.getValueAt(i, 0);
				if ((selected.getFrame() == frame) && (selected.cell.equals(cell))) {
					this.tableOutlines.setRowSelectionInterval(i, i);
					Rectangle rect = new Rectangle(this.tableOutlines.getCellRect(i, 0, true));
					this.tableOutlines.scrollRectToVisible(rect);
					return;
				}
			}
		}
	}

	public static void goFrame(int frame) {
		int channel = imp.getChannel();
		int slice = imp.getSlice();
		imp.setPosition(channel, slice, frame);
	}

	public static void incFrame(int inc) {
		int channel = imp.getChannel();
		int slice = imp.getSlice();
		int frame = imp.getFrame();
		imp.setPosition(channel, slice, frame + inc);
	}

	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == this.tableOutlines) {
			try {
				Outline outline = this.tableOutlines.getOutline();
				if (outline != null) {
					updateAll(outline, false);
				}
			}
			catch (Exception localException) {
			}
		}
	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (e.getSource() == this.tableOutlines) {
			try {
				Outline outline = this.tableOutlines.getOutline();
				if (outline != null) {
					updateAll(outline, false);
				}
			}
			catch (Exception localException) {
			}
		}
	}
}
