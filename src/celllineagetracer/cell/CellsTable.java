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

package celllineagetracer.cell;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import celllineagetracer.Supervisor;
import ij.IJ;

public class CellsTable extends JTable {
	private String[] headers = { "Objects", "Hue", "Default Class", "Information" };

	public CellsTable() {
		DefaultTableModel model = new DefaultTableModel(null, this.headers) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		setModel(model);
		setAutoCreateRowSorter(true);
		getColumnModel().getColumn(1).setCellRenderer(new HueTableCellRenderer());
	}

	public String getSelectedCell() {
		int row = getSelectedRow();
		if (row >= 0) {
			return (String) getValueAt(row, 0);
		}
		return null;
	}

	public void add(String[] row) {
		if (row == null) {
			return;
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		model.addRow(row);
		model.fireTableDataChanged();
	}

	public void removeRow(int row) {
		if (row < 0) {
			return;
		}
		((DefaultTableModel) getModel()).removeRow(row);
	}

	public void removeAllRows() {
		while (getRowCount() > 0) {
			((DefaultTableModel) getModel()).removeRow(0);
		}
	}

	public String getValue(int col) {
		int row = convertRowIndexToModel(getSelectedRow());
		if (row < 0) {
			return "";
		}
		DefaultTableModel model = (DefaultTableModel) getModel();
		return (String) model.getValueAt(row, col);
	}

	public JScrollPane getScrollPane(int w, int h) {
		JScrollPane scroll = new JScrollPane(this);
		scroll.setMinimumSize(new Dimension(w, h));
		scroll.setPreferredSize(new Dimension(w, h));
		return scroll;
	}

	public void update() {
		removeAllRows();
		for (String name : Supervisor.cells.keySet()) {
			Cell cell = (Cell) Supervisor.cells.get(name);
			if (cell != null) {
				String[] row = { name, "" + cell.getHue(), "" + cell.getDefaultClass(), cell.getInformation() };
				add(row);
			}
		}
		repaint();
	}

	public void readTable() {
		Supervisor.cells.clear();
		for (int row = 0; row < getRowCount(); row++) {
			String name = ((String) getValueAt(row, 0)).trim();
			if (!name.equals("")) {
				int hue = convertValueAt(row, 1, 0);
				String klass = (String) getValueAt(row, 2);
				Cell cell = new Cell(klass, hue, true, true);
				Supervisor.cells.put(name, cell);
			}
		}
	}

	public void saveCSV(String filename) {
		if (filename == null) {
			return;
		}
		File file = new File(filename);
		try {
			BufferedWriter buffer = new BufferedWriter(new FileWriter(file));
			int nrows = getRowCount();
			int ncols = getColumnCount();
			String row = "";
			for (int c = 0; c < ncols; c++) {
				row = row + this.headers[c] + (c == ncols - 1 ? "" : ", ");
			}
			buffer.write(row + "\n");
			for (int r = 0; r < nrows; r++) {
				row = "";
				for (int c = 0; c < ncols; c++) {
					row = row + getValueAt(r, c) + (c == ncols - 1 ? "" : ", ");
				}
				buffer.write(row + "\n");
			}
			buffer.close();
		}
		catch (IOException localIOException) {
		}
	}

	public void loadCSV(String filename) {
		if (filename == null) {
			return;
		}
		removeAllRows();
		try {
			BufferedReader buffer = new BufferedReader(new FileReader(filename));
			buffer.readLine();
			String line = buffer.readLine();
			StringTokenizer tokens = new StringTokenizer(line, ",");
			String[] headers = new String[tokens.countTokens()];
			int count = 0;
			while (tokens.hasMoreTokens()) {
				headers[(count++)] = tokens.nextToken();
			}
			while (line != null) {
				tokens = new StringTokenizer(line, ",");
				String[] row = new String[tokens.countTokens()];
				count = 0;
				while (tokens.hasMoreTokens()) {
					row[(count++)] = tokens.nextToken();
				}
				add(row);
				line = buffer.readLine();
			}
			buffer.close();
			readTable();
		}
		catch (Exception ex) {
			IJ.error("Unable to read the table from " + filename);
		}
	}

	private int convertValueAt(int row, int col, int def) {
		String a = ((String) getValueAt(row, col)).trim();
		if (a == null) {
			return def;
		}
		int i = def;
		try {
			i = Integer.parseInt(a);
		}
		catch (Exception localException) {
		}
		return i;
	}

	public class HueTableCellRenderer extends DefaultTableCellRenderer {
		public HueTableCellRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (col <= 1) {
				Cell cell = (Cell) Supervisor.cells.get(table.getValueAt(row, 0));
				if (cell != null) {
					c.setForeground(Color.WHITE);
					Color color = Color.getHSBColor(cell.getHue() / 360.0F, 1.0F, 1.0F);
					c.setBackground(color);
				}
				else {
					c.setForeground(Color.BLACK);
					c.setBackground(Color.WHITE);
				}
			}
			return c;
		}
	}
}
