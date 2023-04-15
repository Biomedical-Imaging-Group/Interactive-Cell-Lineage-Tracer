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

package celllineagetracer.outline;

import celllineagetracer.Supervisor;
import celllineagetracer.Tools;
import celllineagetracer.cell.Cell;
import celllineagetracer.cell.Cells;
import java.awt.Dimension;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class OutlinesTable extends JTable {
	public OutlinesTable() {
		String[] headers = { "Object", "Class", "Frame" };
		DefaultTableModel model = new DefaultTableModel(null, headers) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		setAutoCreateRowSorter(true);
		setModel(model);
		setSelectionMode(0);
	}

	public int getRowFromFirstColumns(String col0, String col1, int col2) {
		int n = getRowCount();
		DefaultTableModel model = (DefaultTableModel) getModel();
		for (int i = 0; i < n; i++) {
			int frame = Integer.parseInt((String) model.getValueAt(i, 2));
			String v0 = (String) model.getValueAt(i, 0);
			v0 = v0 == null ? "" : v0;
			String v1 = (String) model.getValueAt(i, 1);
			v1 = v1 == null ? "" : v1;
			if ((v0.equals(col0.trim())) && (v1.equals(col1.trim())) && (frame == col2)) {
				return i;
			}
		}
		return -1;
	}

	public void update() {
		Cells cells = Supervisor.cells;
		removeRows();
		for (String name : cells.keySet()) {
			Cell cell = (Cell) cells.get(name);
			if (cell != null) {
				for (Integer frame : cell.getListOutlinesFrame()) {
					Outline outline = cell.getOutline(frame);
					add(new String[] { outline.cell, outline.klass, Tools.frame(frame.intValue()) });
				}
			}
		}
	}

	public void add(String[] row) {
		String[] rowdel = new String[row.length + 1];
		for (int i = 0; i < row.length; i++) {
			rowdel[i] = row[i];
		}
		rowdel[row.length] = "���";
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.addRow(rowdel);
	}

	public void removeRows() {
		while (getRowCount() > 0) {
			((DefaultTableModel) getModel()).removeRow(0);
		}
	}

	public Outline getOutline() {
		DefaultTableModel model = (DefaultTableModel) getModel();
		int r = getSelectedRow();
		if (r < 0) {
			return null;
		}
		int row = convertRowIndexToModel(r);
		if (row < 0) {
			return null;
		}
		String cellSel = ((String) model.getValueAt(row, 0)).trim();
		int frameSel = Tools.convertToInt(((String) model.getValueAt(row, 2)).trim(), -1);
		for (String name : Supervisor.cells.keySet()) {
			Cell cell = (Cell) Supervisor.cells.get(name);
			if (name.equals(cellSel)) {
				for (Integer frame : cell.getListOutlinesFrame()) {
					if (frame.intValue() == frameSel) {
						return cell.getOutline(frame);
					}
				}
			}
		}
		return null;
	}

	public JScrollPane getPane(int w, int h) {
		JScrollPane scroll = new JScrollPane(this);
		scroll.setMinimumSize(new Dimension(w, h));
		scroll.setPreferredSize(new Dimension(w, h));
		return scroll;
	}

	public void show(String title, int w, int h) {
		JFrame frame = new JFrame(title);
		frame.add(getPane(w, h));
		frame.pack();
		frame.setVisible(true);
	}
}
