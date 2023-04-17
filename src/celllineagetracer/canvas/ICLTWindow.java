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

package celllineagetracer.canvas;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import celllineagetracer.Supervisor;
import celllineagetracer.cell.Cell;
import celllineagetracer.outline.Outline;
import ij.ImagePlus;
import ij.gui.StackWindow;

public class ICLTWindow extends StackWindow implements KeyListener, ActionListener, ItemListener {
	private JButton bnRemove = new JButton("del");
	private JButton bnControlFirst = new JButton("|<<");
	private JButton bnControlRewind = new JButton("<<");
	private JButton bnControlPrev = new JButton("<");
	private JButton bnControlNext = new JButton(">");
	private JButton bnControlAdv = new JButton(">>");
	private JButton bnControlLast = new JButton(">>|");
	private Choice cmbCells;
	private Choice cmbClass;
	private BarFrames bar;
	private JComboBox<String> cmbModeEdition;
	private GridBagConstraints c = new GridBagConstraints();
	private String[] modes = { "Free", "Magnetic", "Livewire", "Line" };
	private Supervisor supervisor;
	private ICLTCanvas canvas;

	public ICLTWindow(Supervisor supervisor, ImagePlus imp, ICLTCanvas canvas) {
		super(imp, canvas);
		this.canvas = canvas;
		this.supervisor = supervisor;
		addKeyListener(this);
	}

	public void setPath(ICLTCanvas.PathType path) {
		if (path == ICLTCanvas.PathType.FREE) {
			this.cmbModeEdition.setSelectedItem(this.modes[0]);
		}
		if (path == ICLTCanvas.PathType.MAGN) {
			this.cmbModeEdition.setSelectedItem(this.modes[1]);
		}
		if (path == ICLTCanvas.PathType.OPT) {
			this.cmbModeEdition.setSelectedItem(this.modes[2]);
		}
		if (path == ICLTCanvas.PathType.LINE) {
			this.cmbModeEdition.setSelectedItem(this.modes[3]);
		}
	}

	public void addPanel() {
		this.cmbModeEdition = new JComboBox<String>();
		String[] arrayOfString;
		int j = (arrayOfString = this.modes).length;
		for (int i = 0; i < j; i++) {
			String mode = arrayOfString[i];
			this.cmbModeEdition.addItem(mode);
		}
		this.cmbCells = new Choice();
		this.cmbClass = new Choice();
		int h = 20;
		int w = this.imp.getWidth() / 6;

		JLabel lblCells = new JLabel("<html><b>Cells</b></html>");
		lblCells.setBorder(BorderFactory.createEtchedBorder());
		lblCells.setAlignmentX(1.0F);
		JLabel lblClass = new JLabel("<html><b>Classes</b></html>");
		lblClass.setBorder(BorderFactory.createEtchedBorder());
		lblClass.setAlignmentX(1.0F);

		this.bar = new BarFrames();
		this.bar.setPreferredSize(new Dimension(this.imp.getWidth(), h));
		this.bnControlRewind.setPreferredSize(new Dimension(w, h));
		this.bnControlPrev.setPreferredSize(new Dimension(w, h));
		this.bnControlNext.setPreferredSize(new Dimension(w, h));
		this.bnControlFirst.setPreferredSize(new Dimension(w, h));
		this.bnControlAdv.setPreferredSize(new Dimension(w, h));
		this.bnControlFirst.setPreferredSize(new Dimension(w, h));
		this.bnControlLast.setPreferredSize(new Dimension(w, h));
		this.cmbCells.setPreferredSize(new Dimension(w, h));
		this.cmbClass.setPreferredSize(new Dimension(w, h));
		lblCells.setPreferredSize(new Dimension(w, h));
		lblClass.setPreferredSize(new Dimension(w, h));
		this.cmbModeEdition.setPreferredSize(new Dimension(w, h));

		JToolBar pnNavigation = new JToolBar("Navigation");
		pnNavigation.setLayout(new GridBagLayout());
		place(pnNavigation, 0, 1, this.bnControlFirst);
		place(pnNavigation, 0, 2, this.bnControlRewind);
		place(pnNavigation, 0, 3, this.bnControlPrev);
		place(pnNavigation, 0, 4, this.bnControlNext);
		place(pnNavigation, 0, 5, this.bnControlAdv);
		place(pnNavigation, 0, 6, this.bnControlLast);

		JToolBar pnControl = new JToolBar("Control");
		pnControl.setLayout(new GridBagLayout());

		place(pnControl, 0, 1, lblCells);
		place(pnControl, 0, 2, this.cmbCells);
		place(pnControl, 0, 3, lblClass);
		place(pnControl, 0, 4, this.cmbClass);
		place(pnControl, 0, 5, this.bnRemove);
		place(pnControl, 0, 6, this.cmbModeEdition);

		JToolBar pnFrames = new JToolBar("Frames");
		pnFrames.setLayout(new BorderLayout());
		pnFrames.add(this.bar, "Center");

		Panel panel = new Panel();
		panel.setLayout(new GridLayout(3, 1));
		panel.add(pnControl);
		panel.add(pnNavigation);
		panel.add(this.bar);
		add(panel);

		this.cmbModeEdition.addItemListener(this);
		this.bnControlRewind.addActionListener(this);
		this.bnControlPrev.addActionListener(this);
		this.bnControlFirst.addActionListener(this);
		this.bnControlNext.addActionListener(this);
		this.bnControlAdv.addActionListener(this);
		this.bnControlLast.addActionListener(this);
		this.bnRemove.addActionListener(this);
		this.cmbCells.addItemListener(this);
		this.cmbClass.addItemListener(this);

		pack();
	}

	private void place(JToolBar tool, int row, int col, Component comp) {
		this.c.gridx = col;
		this.c.gridy = row;
		tool.add(comp, this.c);
	}

	public void actionPerformed(ActionEvent e) {
		int frameIncrement = Math.max(1, this.imp.getNFrames() / 20);
		if (e.getSource() == this.bnRemove) {
			Outline s = this.supervisor.getSelected();
			if (s != null) {
				this.supervisor.deleteOutline(s);
			}
		}
		else if (e.getSource() == this.bnControlFirst) {
			Supervisor.goFrame(1);
		}
		else if (e.getSource() == this.bnControlRewind) {
			Supervisor.incFrame(-frameIncrement);
		}
		else if (e.getSource() == this.bnControlPrev) {
			Supervisor.incFrame(-1);
		}
		else if (e.getSource() == this.bnControlNext) {
			Supervisor.incFrame(1);
		}
		else if (e.getSource() == this.bnControlAdv) {
			Supervisor.incFrame(frameIncrement);
		}
		else if (e.getSource() == this.bnControlLast) {
			Supervisor.goFrame(this.imp.getNFrames());
		}
		this.supervisor.updateAll(null, true);
	}

	public String getCell() {
		String s = this.cmbCells.getSelectedItem();
		return s == null ? "" : s;
	}

	public void setCell(String cell) {
		this.cmbCells.select(cell);
	}

	public String getPixelClass() {
		String s = this.cmbClass.getSelectedItem();
		return s == null ? "" : s;
	}

	public void setPixelClass(String klass) {
		this.cmbClass.select(klass);
	}

	public void updateBar() {
		String name = this.cmbCells.getSelectedItem();
		if (name != null) {
			this.bar.updateCell(name);
		}
	}

	public void updateList() {
		this.cmbCells.removeItemListener(this);
		this.cmbClass.removeItemListener(this);

		String selectedCell = this.cmbCells.getSelectedItem();
		this.cmbCells.removeAll();
		for (String cell : Supervisor.cells.keySet()) {
			this.cmbCells.addItem(cell);
		}
		if (selectedCell != null) {
			this.cmbCells.select(selectedCell);
		}
		String selectedClass = this.cmbClass.getSelectedItem();
		this.cmbClass.removeAll();
		this.cmbClass.add("");
		for (String k : Supervisor.classes.keySet()) {
			this.cmbClass.addItem(k);
		}
		if (selectedClass != null) {
			this.cmbClass.select(selectedClass);
		}
		this.cmbCells.addItemListener(this);
		this.cmbClass.addItemListener(this);
	}

	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == this.cmbModeEdition) {
			ICLTCanvas canvas = (ICLTCanvas) getCanvas();
			if (this.cmbModeEdition.getSelectedIndex() == 0) {
				canvas.cancel();
				canvas.setPath(ICLTCanvas.PathType.FREE);
			}
			if (this.cmbModeEdition.getSelectedIndex() == 1) {
				canvas.cancel();
				canvas.setPath(ICLTCanvas.PathType.MAGN);
			}
			if (this.cmbModeEdition.getSelectedIndex() == 2) {
				canvas.cancel();
				canvas.setPath(ICLTCanvas.PathType.OPT);
			}
			if (this.cmbModeEdition.getSelectedIndex() == 3) {
				canvas.cancel();
				canvas.setPath(ICLTCanvas.PathType.LINE);
			}
		}
		else if (e.getSource() == this.cmbClass) {
			Outline s = this.supervisor.getSelected();
			if (s != null) {
				s.klass = this.cmbClass.getSelectedItem();
			}
			this.supervisor.updateAll(s, true);
		}
		if (e.getSource() == this.cmbCells) {
			String name = this.cmbCells.getSelectedItem();
			Cell cell = this.supervisor.getCell(name);
			Outline s = this.supervisor.getSelected();
			if (cell != null) {
				this.cmbClass.select(cell.getDefaultClass());
			}
			this.supervisor.updateAll(s, true);
		}
	}

	public Color getDrawingColor() {
		return new Color(220, 250, 250);
	}

	public void keyTyped(KeyEvent e) {
		if (e.getKeyCode() == 27) {
			this.canvas.cancel();
		}
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}
}
