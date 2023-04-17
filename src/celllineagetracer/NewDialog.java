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

import additionaluserinterface.GridPanel;
import additionaluserinterface.SpinnerInteger;
import celllineagetracer.cell.Cell;
import celllineagetracer.cell.Cells;
import celllineagetracer.pixelclass.PixelClasses;
import ij.gui.GUI;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class NewDialog extends JDialog implements ActionListener {
	private JButton bnCancel = new JButton("Cancel");
	private JButton bnOK = new JButton("OK");
	private Supervisor supervisor;
	private JRadioButton rbFile = new JRadioButton("From file (CSV)", false);
	private JRadioButton rbBlank = new JRadioButton("Blank project", false);
	private JRadioButton rbBinary = new JRadioButton("Binary denomination", true);
	private SpinnerInteger spnCell = new SpinnerInteger(1, 1, 9, 1);
	private SpinnerInteger spnDivision = new SpinnerInteger(5, 1, 255, 1);
	private JTextField txtPath;
	private JComboBox<String> cmbClass = new JComboBox();

	public NewDialog(Supervisor supervisor, JTextField txtPath) {
		super(new JFrame(), "New Cell Lineage");
		this.supervisor = supervisor;
		this.txtPath = txtPath;
		for (String klass : Supervisor.classes.keySet()) {
			this.cmbClass.addItem(klass);
		}
		this.cmbClass.setEditable(true);

		ButtonGroup bn = new ButtonGroup();
		bn.add(this.rbFile);
		bn.add(this.rbBlank);
		bn.add(this.rbBinary);

		GridPanel pn = new GridPanel();
		pn.place(0, 0, 3, 1, this.rbFile);
		pn.place(1, 0, 3, 1, this.rbBlank);
		pn.place(2, 0, 3, 1, this.rbBinary);
		pn.place(3, 0, 1, 1, new JLabel("  "));
		pn.place(3, 1, 1, 1, new JLabel("Number of cells at begining"));
		pn.place(3, 2, 1, 1, this.spnCell);
		pn.place(4, 0, 1, 1, new JLabel("  "));
		pn.place(4, 1, 1, 1, new JLabel("Number of divisions"));
		pn.place(4, 2, 1, 1, this.spnDivision);
		pn.place(5, 1, 1, 1, new JLabel("Default class"));
		pn.place(5, 2, 1, 1, this.cmbClass);

		JPanel pnButtons = new JPanel(new GridLayout(1, 2));
		pnButtons.add(this.bnCancel);
		pnButtons.add(this.bnOK);

		JPanel pn2 = new JPanel(new BorderLayout());
		pn2.add(pn, "North");
		pn2.add(pnButtons, "South");

		this.bnCancel.addActionListener(this);
		this.bnOK.addActionListener(this);
		this.rbFile.addActionListener(this);
		this.rbBinary.addActionListener(this);
		this.rbBlank.addActionListener(this);

		add(pn2);
		pack();
		GUI.center(this);
		setModal(false);
		setVisible(true);
		update();
	}

	public synchronized void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.bnOK) {
			if (this.rbFile.isSelected()) {
				String path = ICLTFile.browseOpen(this.txtPath.getText());
				if (path != null) {
					ICLTFile.load(path);
					this.txtPath.setText(path);
				}
			}
			if (this.rbBlank.isSelected()) {
				Supervisor.clear();
			}
			if (this.rbBinary.isSelected()) {
				Supervisor.clear();
				int ncl = this.spnCell.get();
				int div = this.spnDivision.get();
				String klass = (String) this.cmbClass.getSelectedItem();
				for (int i = 1; i <= ncl; i++) {
					String cur = "" + i;
					division(cur, i, div, klass);
				}
			}
			this.supervisor.updateAll(null, true);
			dispose();
		}
		else if (event.getSource() == this.bnCancel) {
			dispose();
		}
		update();
	}

	private void division(String cur, int i, int div, String klass) {
		int hue = (int) (Math.random() * 359.0D);
		Supervisor.cells.put(cur, new Cell(klass, hue, true, true));
		if (cur.length() > div) {
			return;
		}
		division(cur + i, i, div, klass);
		division(cur + "0", i, div, klass);
	}

	private void update() {
		this.spnCell.setEnabled(this.rbBinary.isSelected());
		this.spnDivision.setEnabled(this.rbBinary.isSelected());
		this.cmbClass.setEnabled(this.rbBinary.isSelected());
	}
}
