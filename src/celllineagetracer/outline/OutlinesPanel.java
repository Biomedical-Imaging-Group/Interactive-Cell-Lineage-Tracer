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

package celllineagetracer.outline;

import celllineagetracer.Constants;
import celllineagetracer.Supervisor;
import ij.IJ;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

public class OutlinesPanel extends JPanel implements ActionListener {
	private Supervisor supervisor;
	private JButton bnRemove = new JButton("Remove");
	private JButton bnInterpolate = new JButton("Interpolate");
	private JButton bnPropagate = new JButton("Propagate");
	private OutlinesTable table;
	private JScrollPane scroll;

	public OutlinesPanel(Supervisor supervisor) {
		this.supervisor = supervisor;
		this.table = supervisor.getTableOutlines();

		JLabel lblOutline = new JLabel("<html><b>Outlines</b></html>");
		lblOutline.setBorder(BorderFactory.createEtchedBorder());
		JToolBar tool = new JToolBar();
		tool.setLayout(new GridLayout(1, 4));
		tool.setFloatable(false);
		tool.add(lblOutline);
		tool.add(this.bnInterpolate);
		tool.add(this.bnPropagate);
		tool.add(this.bnRemove);

		this.scroll = this.table.getPane(Constants.widthTable, 200);
		setLayout(new BorderLayout());
		add(tool, "North");
		add(this.scroll, "Center");

		this.bnPropagate.addActionListener(this);
		this.bnInterpolate.addActionListener(this);
		this.bnRemove.addActionListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		Outline outline = this.table.getOutline();
		if (e.getSource() == this.bnInterpolate) {
			if (outline == null) {
				IJ.error("Select a outline to interpolate");
				return;
			}
			this.supervisor.interpolateOutline(outline);
		}
		else if (e.getSource() == this.bnPropagate) {
			if (outline == null) {
				IJ.error("Select a outline to propagate");
				return;
			}
			this.supervisor.propagateOutline(outline);
		}
		else if (e.getSource() == this.bnRemove) {
			if (outline == null) {
				IJ.error("Select a outline to remove");
				return;
			}
			this.supervisor.deleteOutline(outline);
		}
		this.supervisor.updateAll(null, true);
	}

	public JScrollPane getPane(int w, int h) {
		JScrollPane scroll = new JScrollPane(this);
		scroll.setMinimumSize(new Dimension(w, h));
		scroll.setPreferredSize(new Dimension(w, h));
		return scroll;
	}

	public JScrollPane getScrollPane() {
		return this.scroll;
	}
}
