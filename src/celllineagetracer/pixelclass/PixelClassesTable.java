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

package celllineagetracer.pixelclass;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import celllineagetracer.Supervisor;

public class PixelClassesTable extends JTable {
	private String[] headers = { "Class", "Hue", "Label", "Size Inner", "Label Inner", "Size Outer", "Label Outer" };

	public PixelClassesTable() {
		DefaultTableModel model = new DefaultTableModel(null, this.headers) {
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};
		setModel(model);
		getColumnModel().getColumn(1).setCellRenderer(new HueTableCellRenderer());
		setRowSelectionAllowed(true);
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
		for (String name : Supervisor.classes.keySet()) {
			PixelClass pc = (PixelClass) Supervisor.classes.get(name);
			if (pc != null) {
				String sin = "" + (pc.sizeInner > 0 ? pc.sizeInner : "");
				String sout = "" + (pc.sizeOuter > 0 ? pc.sizeOuter : "");
				String vin = "" + (pc.sizeInner > 0 ? pc.valueInner : "");
				String vout = "" + (pc.sizeOuter > 0 ? pc.valueOuter : "");
				String[] row = { name, "" + pc.hue, "" + pc.valueRegion, sin, vin, sout, vout };
				add(row);
			}
		}
		repaint();
	}

	public class HueTableCellRenderer extends DefaultTableCellRenderer {
		public HueTableCellRenderer() {
		}

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int col) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
			if (col <= 1) {
				PixelClass klass = (PixelClass) Supervisor.classes.get(table.getValueAt(row, 0));
				if (klass != null) {
					c.setForeground(Color.WHITE);
					Color color = Color.getHSBColor(klass.hue / 360.0F, 1.0F, 1.0F);
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
