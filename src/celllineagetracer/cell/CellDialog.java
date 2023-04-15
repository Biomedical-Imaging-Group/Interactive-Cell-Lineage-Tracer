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

package celllineagetracer.cell;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import additionaluserinterface.GridPanel;
import celllineagetracer.Supervisor;
import ij.IJ;
import ij.gui.GUI;

public class CellDialog extends JDialog implements ActionListener {
	private JButton bnCancel = new JButton("Cancel");
	private JButton bnCreate = new JButton("Create");
	private JSlider sldHue;
	private Supervisor supervisor;
	private JComboBox<String> cmbMotility = new JComboBox<String>(new String[] { "Mobile object", "Fixed object" });
	private JComboBox<String> cmbMultiplePart = new JComboBox<String>(
			new String[] { "Multiple parts blod in a frame", "Unique blob in a frame" });
	private JTextField txtName = new JTextField();
	private JComboBox<String> cmbClass = new JComboBox<String>();

	public CellDialog(Supervisor supervisor, String name) {
		super(new JFrame(), "Cell / Object");
		this.supervisor = supervisor;

		this.sldHue = initSlider();
		for (String cla : Supervisor.classes.keySet()) {
			this.cmbClass.addItem(cla);
		}
		this.txtName.setEditable(true);

		GridPanel pn = new GridPanel();
		pn.place(0, 0, new JLabel("Name"));
		pn.place(0, 1, this.txtName);
		pn.place(1, 0, new JLabel("Default class"));
		pn.place(1, 1, this.cmbClass);
		pn.place(4, 0, new JLabel("Color Hue"));
		pn.place(4, 1, this.sldHue);

		JPanel pnButtons = new JPanel(new GridLayout(1, 2));
		pnButtons.add(this.bnCancel);
		pnButtons.add(this.bnCreate);

		JPanel pn2 = new JPanel(new BorderLayout());
		pn2.add(pn, "North");
		pn2.add(pnButtons, "South");

		Cell cell = (Cell) Supervisor.cells.get(name);
		if ((cell != null) && (name != null)) {
			this.bnCreate.setText("Update");
			this.txtName.setEnabled(false);
			this.txtName.setText(name);
			this.cmbClass.setSelectedItem(cell.getDefaultClass());
			this.cmbMotility.setSelectedIndex(cell.isMobile() ? 0 : 1);
			this.cmbMultiplePart.setSelectedIndex(cell.isMultiplePart() ? 0 : 1);
			this.sldHue.setValue(cell.getHue());
		}
		this.bnCancel.addActionListener(this);
		this.bnCreate.addActionListener(this);
		add(pn2);
		pack();
		GUI.center(this);
		setModal(false);
		setVisible(true);
	}

	public synchronized void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.bnCreate) {
			String name = this.txtName.getText();
			if (name != null) {
				name = name.trim();
				if (name.equals("")) {
					IJ.error("No specified name.");
					dispose();
					return;
				}
				String defaultClass = (String) this.cmbClass.getSelectedItem();
				int hue = this.sldHue.getValue();
				Cell cell = (Cell) Supervisor.cells.get(name);
				if (cell == null) {
					cell = new Cell(defaultClass, hue, true, true);
				}
				cell.setHue(hue);
				cell.setDefaultClass(defaultClass);
				Supervisor.cells.put(name, cell);
				this.supervisor.updateAll(null, true);
				if (cell != null) {
					this.supervisor.getWindow().setCell(name);
				}
			}
		}
		dispose();
	}

	private JSlider initSlider() {
		JSlider sld = new JSlider();
		sld.setMinimum(0);
		sld.setMaximum(360);
		sld.setPreferredSize(new Dimension(300, 40));

		Hashtable<Integer, JLabel> labels = new Hashtable<Integer, JLabel>();
		JLabel lbl1 = new JLabel("Red");
		JLabel lbl2 = new JLabel("Yellow");
		JLabel lbl3 = new JLabel("Green");
		JLabel lbl4 = new JLabel("Cyan");
		JLabel lbl5 = new JLabel("Blue");
		JLabel lbl6 = new JLabel("Magenta");
		Font font = lbl1.getFont();
		Font small = new Font(font.getFamily(), font.getStyle(), font.getSize() - 3);
		lbl1.setFont(small);
		lbl2.setFont(small);
		lbl3.setFont(small);
		lbl4.setFont(small);
		lbl5.setFont(small);
		lbl6.setFont(small);
		labels.put(Integer.valueOf(0), lbl1);
		labels.put(Integer.valueOf(60), lbl2);
		labels.put(Integer.valueOf(120), lbl3);
		labels.put(Integer.valueOf(180), lbl4);
		labels.put(Integer.valueOf(240), lbl5);
		labels.put(Integer.valueOf(300), lbl6);
		sld.setMinorTickSpacing(10);
		sld.setPaintTicks(true);
		sld.setPaintLabels(true);
		sld.setMajorTickSpacing(60);
		sld.setLabelTable(labels);
		sld.setSnapToTicks(true);
		return sld;
	}
}
