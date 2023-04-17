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

package celllineagetracer.pixelclass;

import celllineagetracer.Constants;
import celllineagetracer.Supervisor;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class PixelClassesPanel extends JPanel implements ActionListener {
	private Supervisor supervisor;
	private JButton bnCreate = new JButton("Create");
	private JButton bnRemove = new JButton("Remove");
	private JButton bnEdit = new JButton("Edit");
	private PixelClassesTable table;

	public PixelClassesPanel(Supervisor supervisor) {
		this.supervisor = supervisor;
		this.table = supervisor.getTablePixelClasses();

		JLabel lbl = new JLabel("<html><b>Classes</b></html>");
		lbl.setBorder(BorderFactory.createEtchedBorder());

		JToolBar tool = new JToolBar();
		tool.setLayout(new GridLayout(1, 4));
		tool.setFloatable(false);
		tool.add(lbl);
		tool.add(this.bnCreate);
		tool.add(this.bnEdit);
		tool.add(this.bnRemove);

		JScrollPane scroll = this.table.getScrollPane(Constants.widthTable, 200);
		setLayout(new BorderLayout());
		add(tool, "North");
		add(scroll, "Center");

		this.bnCreate.addActionListener(this);
		this.bnEdit.addActionListener(this);
		this.bnRemove.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.bnCreate) {
			new PixelClassDialog(this.supervisor, null);
		}
		else if (e.getSource() == this.bnEdit) {
			String name = this.table.getSelectedCell();
			new PixelClassDialog(this.supervisor, name);
		}
		else if (e.getSource() == this.bnRemove) {
			int row = this.table.getSelectedRow();
			if (row >= 0) {
				String name = (String) this.table.getValueAt(row, 0);
				this.supervisor.deleteClass(name);
			}
		}
		this.supervisor.updateAll(null, true);
	}
}
