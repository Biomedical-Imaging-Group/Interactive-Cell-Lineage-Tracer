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

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MeasureTable extends JTable {
	public MeasureTable(String[] headers) {
		DefaultTableModel model = new DefaultTableModel(null, headers) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		setAutoCreateRowSorter(true);
		setModel(model);
		setSelectionMode(2);
	}

	public void setData(ArrayList<String[]> rows) {
		removeRows();
		if (rows.size() == 0) {
			return;
		}
		for (String[] row : rows) {
			add(row);
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

	public String getValue(int col) {
		int r = getSelectedRow();
		if (r < 0) {
			return "";
		}
		int row = convertRowIndexToModel(r);
		if (row < 0) {
			return "";
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		return (String) model.getValueAt(row, col);
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
