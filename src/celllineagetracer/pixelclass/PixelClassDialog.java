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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import additionaluserinterface.GridPanel;
import additionaluserinterface.SpinnerInteger;
import celllineagetracer.Supervisor;
import ij.IJ;
import ij.gui.GUI;

public class PixelClassDialog extends JDialog implements ActionListener {
	private JButton bnCancel = new JButton("Cancel");
	private JButton bnCreate = new JButton("Create");
	private Supervisor supervisor;
	private JSlider sldHue;
	private JTextField txtName = new JTextField("", 10);
	private SpinnerInteger spnRegion = new SpinnerInteger(1, 0, 255, 1);
	private SpinnerInteger spnInner = new SpinnerInteger(1, 0, 255, 1);
	private SpinnerInteger spnOuter = new SpinnerInteger(1, 0, 255, 1);
	private SpinnerInteger szeInner = new SpinnerInteger(1, 0, 255, 1);
	private SpinnerInteger szeOuter = new SpinnerInteger(1, 0, 255, 1);
	private JCheckBox chkRegion = new JCheckBox("Region", true);
	private JCheckBox chkInner = new JCheckBox("Inner Contour", false);
	private JCheckBox chkOuter = new JCheckBox("Outer Contour", false);

	public PixelClassDialog(Supervisor supervisor, String name) {
		super(new JFrame(), "Pixel Classes");
		this.supervisor = supervisor;

		this.sldHue = initSlider();
		this.txtName.setEditable(true);

		GridPanel pn = new GridPanel();
		pn.place(0, 0, 2, 1, new JLabel("Name of the class"));
		pn.place(0, 2, 3, 1, this.txtName);
		pn.place(1, 0, 2, 1, new JLabel("Color Hue"));
		pn.place(1, 2, 3, 1, this.sldHue);

		pn.place(3, 0, this.chkRegion);
		pn.place(3, 1, new JLabel("Label"));
		pn.place(3, 2, this.spnRegion);

		pn.place(4, 0, this.chkInner);
		pn.place(4, 1, new JLabel("Label"));
		pn.place(4, 2, this.spnInner);
		pn.place(4, 3, new JLabel("Size Erosion"));
		pn.place(4, 4, this.szeInner);

		pn.place(6, 0, this.chkOuter);
		pn.place(6, 1, new JLabel("Label"));
		pn.place(6, 2, this.spnOuter);
		pn.place(6, 3, new JLabel("Size Dilation"));
		pn.place(6, 4, this.szeOuter);

		JPanel pnButtons = new JPanel(new GridLayout(1, 2));
		pnButtons.add(this.bnCancel);
		pnButtons.add(this.bnCreate);

		JPanel pn2 = new JPanel(new BorderLayout());
		pn2.add(pn, "North");
		pn2.add(pnButtons, "South");

		PixelClass klass = (PixelClass) Supervisor.classes.get(name);
		if ((klass != null) && (name != null)) {
			this.bnCreate.setText("Update");
			this.txtName.setText(name);
			this.txtName.setEnabled(false);
			this.spnRegion.set(klass.valueRegion);
			this.chkInner.setSelected(klass.sizeInner > 0);
			this.chkOuter.setSelected(klass.sizeOuter > 0);
			this.spnInner.set(klass.valueInner);
			this.spnOuter.set(klass.valueOuter);
			this.szeInner.set(klass.sizeInner);
			this.szeOuter.set(klass.sizeOuter);
			this.sldHue.setValue(klass.hue);
		}
		this.bnCancel.addActionListener(this);
		this.bnCreate.addActionListener(this);
		this.chkInner.addActionListener(this);
		this.chkOuter.addActionListener(this);
		this.chkRegion.addActionListener(this);

		add(pn2);
		pack();
		GUI.center(this);
		setModal(false);
		setVisible(true);
		update();
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
				int hue = this.sldHue.getValue();
				int label = this.spnRegion.get();

				PixelClass klass = (PixelClass) Supervisor.classes.get(name);
				if (klass == null) {
					klass = new PixelClass(hue, label);
				}
				klass.hue = hue;
				klass.valueRegion = label;
				if (this.chkInner.isSelected()) {
					klass.setInnerContour(this.szeInner.get(), this.spnInner.get());
				}
				if (this.chkOuter.isSelected()) {
					klass.setOuterContour(this.szeOuter.get(), this.spnOuter.get());
				}
				Supervisor.classes.put(name, klass);
				this.supervisor.updateAll(null, true);
			}
			dispose();
		}
		else if (event.getSource() == this.bnCancel) {
			dispose();
		}
		else if (event.getSource() == this.chkRegion) {
			update();
		}
		else if (event.getSource() == this.chkInner) {
			update();
		}
		else if (event.getSource() == this.chkOuter) {
			update();
		}
	}

	private void update() {
		this.chkRegion.setSelected(true);
		this.spnInner.setEnabled(this.chkInner.isSelected());
		this.szeInner.setEnabled(this.chkInner.isSelected());
		this.spnOuter.setEnabled(this.chkOuter.isSelected());
		this.szeOuter.setEnabled(this.chkOuter.isSelected());
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
