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

import additionaluserinterface.GridToolbar;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerDouble;
import additionaluserinterface.SpinnerInteger;
import celllineagetracer.Constants;
import celllineagetracer.Supervisor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DisplayControlPanel extends JPanel implements ActionListener, ChangeListener {
	private JCheckBox chkTrack = new JCheckBox("Trajectory", true);
	private JCheckBox chkArea = new JCheckBox("Area", true);
	private JCheckBox chkContour = new JCheckBox("Edge", true);
	private JCheckBox chkCenter = new JCheckBox("Center", true);
	private JCheckBox chkText = new JCheckBox("Text", true);
	private JCheckBox trkArea = new JCheckBox("\u29C9", true);
	private JCheckBox trkContour = new JCheckBox("\u29C9", true);
	private JCheckBox trkCenter = new JCheckBox("\u29C9", true);
	private JCheckBox trkText = new JCheckBox("\u29C9", true);
	private SpinnerDouble spnTrack = new SpinnerDouble(0.5D, 0.0D, 100.0D, 1.0D);
	private SpinnerInteger spnSizeFont = new SpinnerInteger(12, 2, 100, 1);
	private SpinnerInteger spnOpacity = new SpinnerInteger(50, 0, 100, 1);
	private SpinnerInteger spnStroke = new SpinnerInteger(1, 0, 100, 1);
	private SpinnerInteger spnCross = new SpinnerInteger(10, 0, 200, 1);
	private JComboBox<String> cmbColorTrack = new JComboBox<String>(Constants.colors);
	private JComboBox<String> cmbColorText = new JComboBox<String>(Constants.colors);
	private JComboBox<String> cmbColorArea = new JComboBox<String>(Constants.colors);
	private JComboBox<String> cmbColorContour = new JComboBox<String>(Constants.colors);
	private JComboBox<String> cmbColorCenter = new JComboBox<String>(Constants.colors);
	private JLabel lblTrace = new JLabel("Stroke");
	private JLabel lblArea = new JLabel("Opaque");
	private JLabel lblContour = new JLabel("Stroke");
	private JLabel lblCenter = new JLabel("Length");
	private JLabel lblText = new JLabel("Font");
	private JComboBox<String> cmbSelected = new JComboBox<String>(Constants.colors);
	private SpinnerInteger spnSelected = new SpinnerInteger(1, 0, 100, 1);
	private JComboBox<String> cmbText = new JComboBox<String>(
			new String[] { "Print object name", "Print class name", "Print frame" });
	private Supervisor supervisor;

	public DisplayControlPanel(Supervisor supervisor, Settings settings) {
		this.supervisor = supervisor;

		this.spnOpacity.setToolTipText("Opacity of the area");
		this.spnStroke.setToolTipText("Thickness of the contour");
		this.spnCross.setToolTipText("Size of the cross's arms");
		this.spnTrack.setToolTipText("Thickness of the track");

		GridToolbar pnDisplay = new GridToolbar("Display Outline", 1);
		pnDisplay.place(0, 0, 2, 1, this.chkTrack);
		pnDisplay.place(0, 2, this.cmbColorTrack);
		pnDisplay.place(0, 3, this.lblTrace);
		pnDisplay.place(0, 4, this.spnTrack);

		pnDisplay.place(2, 0, this.chkArea);
		pnDisplay.place(2, 1, this.trkArea);
		pnDisplay.place(2, 2, this.cmbColorArea);
		pnDisplay.place(2, 3, this.lblArea);
		pnDisplay.place(2, 4, this.spnOpacity);

		pnDisplay.place(4, 0, this.chkContour);
		pnDisplay.place(4, 1, this.trkContour);
		pnDisplay.place(4, 2, this.cmbColorContour);
		pnDisplay.place(4, 3, this.lblContour);
		pnDisplay.place(4, 4, this.spnStroke);

		pnDisplay.place(6, 0, this.chkCenter);
		pnDisplay.place(6, 1, this.trkCenter);
		pnDisplay.place(6, 2, this.cmbColorCenter);
		pnDisplay.place(6, 3, this.lblCenter);
		pnDisplay.place(6, 4, this.spnCross);

		pnDisplay.place(7, 0, this.chkText);
		pnDisplay.place(7, 1, this.trkText);
		pnDisplay.place(7, 2, this.cmbColorText);
		pnDisplay.place(7, 3, this.lblText);
		pnDisplay.place(7, 4, this.spnSizeFont);
		pnDisplay.place(9, 1, 2, 1, this.cmbText);

		pnDisplay.place(10, 0, 2, 1, new JLabel("Selected"));
		pnDisplay.place(10, 2, this.cmbSelected);
		pnDisplay.place(10, 3, new JLabel("Stroke"));
		pnDisplay.place(10, 4, this.spnSelected);

		setLayout(new BoxLayout(this, 3));
		add(pnDisplay);

		settings.record("chkTrack", this.chkTrack, true);
		settings.record("chkText", this.chkText, true);
		settings.record("chkArea", this.chkArea, true);
		settings.record("chkContour", this.chkContour, true);
		settings.record("chkCenter", this.chkCenter, false);

		settings.record("spnTrack", this.spnTrack, "0");
		settings.record("spnSizeFont", this.spnSizeFont, "12");
		settings.record("spnOpacity", this.spnOpacity, "33");
		settings.record("spnStroke", this.spnStroke, "1");
		settings.record("spnCross", this.spnCross, "10");

		settings.record("cmbColorText", this.cmbColorText, Constants.colors[6]);
		settings.record("cmbColorArea", this.cmbColorArea, Constants.colors[0]);
		settings.record("cmbColorContour", this.cmbColorContour, Constants.colors[1]);
		settings.record("cmbColorCenter", this.cmbColorCenter, Constants.colors[0]);
		settings.record("cmbText", this.cmbText, (String) this.cmbText.getItemAt(0));

		settings.record("trkArea", this.trkArea, false);
		settings.record("trkContour", this.trkContour, false);
		settings.record("trkCenter", this.trkCenter, false);
		settings.record("trkText", this.trkText, false);

		settings.record("cmbSelected", this.cmbSelected, "Red");
		settings.record("spnSelected", this.spnSelected, "2");

		this.spnSelected.addChangeListener(this);
		this.cmbSelected.addActionListener(this);

		this.chkTrack.addActionListener(this);
		this.chkText.addActionListener(this);
		this.chkArea.addActionListener(this);
		this.chkContour.addActionListener(this);
		this.chkCenter.addActionListener(this);

		this.trkText.addActionListener(this);
		this.trkArea.addActionListener(this);
		this.trkContour.addActionListener(this);
		this.trkCenter.addActionListener(this);

		this.spnTrack.addChangeListener(this);
		this.spnSizeFont.addChangeListener(this);
		this.spnOpacity.addChangeListener(this);
		this.spnStroke.addChangeListener(this);
		this.spnCross.addChangeListener(this);
		this.cmbText.addActionListener(this);

		this.cmbColorTrack.addActionListener(this);
		this.cmbColorText.addActionListener(this);
		this.cmbColorArea.addActionListener(this);
		this.cmbColorContour.addActionListener(this);
		this.cmbColorCenter.addActionListener(this);

		updateInterface();
	}

	public synchronized void actionPerformed(ActionEvent e) {
		display();
	}

	public void stateChanged(ChangeEvent e) {
		display();
	}

	public void display() {
		ICLTCanvas canvas = this.supervisor.getCanvas();

		canvas.trackShow = this.chkTrack.isSelected();
		canvas.areaShow = this.chkArea.isSelected();
		canvas.contourShow = this.chkContour.isSelected();
		canvas.centerShow = this.chkCenter.isSelected();
		canvas.textShow = this.chkText.isSelected();

		canvas.trackColorCode = this.cmbColorTrack.getSelectedIndex();
		canvas.areaColorCode = this.cmbColorArea.getSelectedIndex();
		canvas.contourColorCode = this.cmbColorContour.getSelectedIndex();
		canvas.centerColorCode = this.cmbColorCenter.getSelectedIndex();
		canvas.textColorCode = this.cmbColorText.getSelectedIndex();

		canvas.trackThickness = ((float) this.spnTrack.get());
		canvas.areaOpacity = this.spnOpacity.get();
		canvas.contourStroke = this.spnStroke.get();
		canvas.centerSize = this.spnCross.get();
		canvas.textFont = this.spnSizeFont.get();

		canvas.areaTrack = this.trkArea.isSelected();
		canvas.contourTrack = this.trkContour.isSelected();
		canvas.centerTrack = this.trkCenter.isSelected();
		canvas.textTrack = this.trkText.isSelected();

		canvas.selectedColor = this.cmbSelected.getSelectedIndex();
		canvas.selectedStroke = this.spnSelected.get();

		canvas.trackForward = 0;
		canvas.trackBackward = 0;

		canvas.trackForward = (this.chkTrack.isSelected() ? 100000 : 0);
		canvas.trackBackward = (this.chkTrack.isSelected() ? 100000 : 0);

		canvas.textType = this.cmbText.getSelectedIndex();
		canvas.repaint();
		updateInterface();
	}

	private void updateInterface() {
		boolean e = this.chkText.isSelected();
		this.spnSizeFont.setEnabled(e);
		this.cmbColorText.setEnabled(e);
		this.lblText.setEnabled(e);
		this.trkText.setEnabled(e);
		this.cmbText.setEnabled(e);

		boolean x = this.chkCenter.isSelected();
		this.spnCross.setEnabled(x);
		this.cmbColorCenter.setEnabled(x);
		this.lblCenter.setEnabled(x);
		this.trkCenter.setEnabled(x);

		boolean c = this.chkContour.isSelected();
		this.spnStroke.setEnabled(c);
		this.cmbColorContour.setEnabled(c);
		this.lblContour.setEnabled(c);
		this.trkContour.setEnabled(c);

		boolean t = this.chkTrack.isSelected();
		this.spnTrack.setEnabled(t);
		this.cmbColorTrack.setEnabled(t);
		this.lblTrace.setEnabled(t);

		boolean a = this.chkArea.isSelected();
		this.spnOpacity.setEnabled(a);
		this.cmbColorArea.setEnabled(a);
		this.lblArea.setEnabled(a);
		this.trkArea.setEnabled(a);
	}
}
