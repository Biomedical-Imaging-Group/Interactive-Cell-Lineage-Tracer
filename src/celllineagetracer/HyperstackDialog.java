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

package celllineagetracer;

import additionaluserinterface.GridPanel;
import additionaluserinterface.Settings;
import additionaluserinterface.SpinnerInteger;
import ij.ImagePlus;
import ij.gui.GUI;
import ij.io.Opener;
import ij.plugin.HyperStackConverter;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class HyperstackDialog extends JDialog implements ActionListener, ChangeListener {
	private JButton bnCancel = new JButton("Cancel");
	private JButton bnOK = new JButton("OK");
	private SpinnerInteger spnSlices = new SpinnerInteger(1, 1, 99999, 1);
	private SpinnerInteger spnChannels = new SpinnerInteger(1, 1, 99999, 1);
	private JLabel lblTotal = new JLabel("images", 4);
	private JLabel lblFrames = new JLabel("frames", 4);
	private JLabel lblCheck = new JLabel("---", 4);
	private ImagePlus imp;
	private boolean canceled = false;
	private ImagePlus impHyperstack;
	private Settings settings;

	public static void main(String[] arg) {
		Opener file = new Opener();
		ImagePlus imp = file.openImage("/Users/dsage/Desktop/batch0.tif");
		imp.show();
		new HyperstackDialog(imp, null);
	}

	public HyperstackDialog(ImagePlus imp, Settings settings) {
		super(new JFrame(), "ICLT Hyperstack");

		this.imp = imp;
		this.settings = settings;
		int n = imp.getStackSize();
		int nt = imp.getNFrames();
		int nz = imp.getNSlices();
		int nc = imp.getNChannels();
		if ((nt == 1) && (nz > 1)) {
			int temp = nt;
			nt = nz;
			nz = temp;
		}
		this.spnSlices.set(nz);
		this.spnChannels.set(nc);
		this.lblTotal.setText("" + n);
		this.lblFrames.setText("" + nt);

		this.lblTotal.setBorder(BorderFactory.createEtchedBorder());
		this.lblFrames.setBorder(BorderFactory.createEtchedBorder());
		this.lblCheck.setBorder(BorderFactory.createEtchedBorder());

		GridPanel pnHyperstack = new GridPanel(true, 5);
		pnHyperstack.place(0, 0, new JLabel("Total number of images"));
		pnHyperstack.place(0, 1, this.lblTotal);
		pnHyperstack.place(1, 0, new JLabel("Number of slices (Z)"));
		pnHyperstack.place(1, 1, this.spnSlices);
		pnHyperstack.place(2, 0, new JLabel("Number of channels (C)"));
		pnHyperstack.place(2, 1, this.spnChannels);
		pnHyperstack.place(3, 0, new JLabel("Number of frames (T)"));
		pnHyperstack.place(3, 1, this.lblFrames);
		pnHyperstack.place(4, 0, new JLabel("Check out (Z x C x T)"));
		pnHyperstack.place(4, 1, this.lblCheck);

		JLabel lblInfo1 = new JLabel("The appliction allows at maximum 10 classes.");
		JLabel lblInfo2 = new JLabel("Leave 'Name' and 'Color Hue' empty if is not required.");
		Font font = lblInfo1.getFont();
		lblInfo1.setFont(new Font(font.getFamily(), font.getStyle(), font.getSize() - 2));
		lblInfo2.setFont(new Font(font.getFamily(), font.getStyle(), font.getSize() - 2));

		JPanel pnButton = new JPanel(new GridLayout(1, 2));
		pnButton.add(this.bnCancel);
		pnButton.add(this.bnOK);

		JPanel pn1 = new JPanel();
		pn1.setLayout(new BoxLayout(pn1, 3));
		pn1.add(pnHyperstack);
		pn1.add(pnButton);

		this.bnCancel.addActionListener(this);
		this.bnOK.addActionListener(this);
		this.spnSlices.addChangeListener(this);
		this.spnChannels.addChangeListener(this);
		if (settings != null) {
			settings.record("Hyperstack-spnSlices", this.spnSlices, "1");
			settings.record("Hyperstack-spnChannels", this.spnChannels, "1");
			settings.loadRecordedItems();
		}
		add(pn1);
		pack();
		GUI.center(this);
		setModal(true);
		setVisible(true);
		update();
	}

	public synchronized void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.bnOK) {
			int nc = this.spnChannels.get();
			int nz = this.spnSlices.get();
			int nt = this.imp.getStackSize() / (nc * nz);
			this.impHyperstack = HyperStackConverter.toHyperStack(this.imp, nc, nz, nt, "xyczt", "grayscale");
			this.canceled = false;
			if (this.settings != null) {
				this.settings.storeRecordedItems();
			}
			dispose();
		}
		else if (event.getSource() == this.bnCancel) {
			this.canceled = true;
			dispose();
		}
	}

	public ImagePlus getImagePlus() {
		return this.impHyperstack;
	}

	public boolean wasCanceled() {
		return this.canceled;
	}

	public void update() {
		int n = this.imp.getStackSize();
		int nc = this.spnChannels.get();
		int nz = this.spnSlices.get();
		int nt = n / (nc * nz);
		this.bnOK.setEnabled(n % (nc * nz) == 0);
		this.lblFrames.setText("" + nt);
		this.lblCheck.setText("" + (nt * nc * nz));

	}

	public void stateChanged(ChangeEvent e) {
		update();
	}
}
