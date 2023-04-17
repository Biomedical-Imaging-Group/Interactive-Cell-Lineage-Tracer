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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import additionaluserinterface.GridPanel;
import celllineagetracer.outline.Outline;
import celllineagetracer.pixelclass.PixelClass;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ByteProcessor;

public class Labelization implements ActionListener, Runnable {
	private JButton bnLabelize = new JButton("Labelize and show");
	private JButton bnLabelizeSave = new JButton("Labelize and store");
	private Thread thread = null;
	private JButton job;
	private JTextField txtLabelDir = new JTextField("", 30);
	private JTextField txtDataDir = new JTextField("", 30);
	private int nx;
	private int ny;
	private int nt;
	private JComboBox<String> cmbChannel = new JComboBox<String>();
	private JComboBox<String> cmbSlice = new JComboBox<String>();

	public Labelization() {
		this.nx = Supervisor.imp.getWidth();
		this.ny = Supervisor.imp.getHeight();
		this.nt = Supervisor.imp.getNFrames();
	}

	public JPanel getPanel() {
		int nz = Supervisor.imp.getNSlices();
		int nc = Supervisor.imp.getNChannels();
		for (int i = 1; i <= nz; i++) {
			this.cmbSlice.addItem("Slice " + i);
		}
		for (int i = 1; i <= nc; i++) {
			this.cmbChannel.addItem("Channel " + i);
		}
		GridPanel panel = new GridPanel(true);
		panel.place(1, 0, this.cmbSlice);
		panel.place(1, 1, this.cmbChannel);

		panel.place(10, 0, 2, 1, new JLabel("Label folder"));
		panel.place(11, 0, 2, 1, this.txtLabelDir);

		panel.place(12, 0, 2, 1, new JLabel("Data folder"));
		panel.place(13, 0, 2, 1, this.txtDataDir);
		panel.place(17, 0, this.bnLabelize);
		panel.place(17, 1, this.bnLabelizeSave);

		this.bnLabelize.addActionListener(this);
		this.bnLabelizeSave.addActionListener(this);

		String filename = stripExtension(Supervisor.imp.getTitle()) + ".csv";
		String dir = Supervisor.imp.getFileInfo().directory;
		this.txtDataDir.setText(dir + File.separator + filename + File.separator + "data");
		this.txtLabelDir.setText(dir + File.separator + filename + File.separator + "label");

		return panel;
	}

	public static String stripExtension(String s) {
		return (s != null) && (s.lastIndexOf(".") > 0) ? s.substring(0, s.lastIndexOf(".")) : s;
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.bnLabelize) || (e.getSource() == this.bnLabelizeSave)) {
			this.job = ((JButton) e.getSource());
			if (this.thread == null) {
				Thread thread = new Thread(this);
				thread.setPriority(1);
				thread.start();
			}
		}
	}

	public void run() {
		int c = this.cmbChannel.getSelectedIndex() + 1;
		int z = this.cmbSlice.getSelectedIndex() + 1;
		this.bnLabelize.setEnabled(false);
		if (this.job == this.bnLabelize) {
			showLabel(c, z);
		}
		if (this.job == this.bnLabelizeSave) {
			String dirData = this.txtDataDir.getText() + File.separator;
			String dirLabel = this.txtLabelDir.getText() + File.separator;
			saveLabel(c, z, dirData, dirLabel);
		}
		this.bnLabelize.setEnabled(true);
	}

	public void showLabel(int channel, int slice) {
		ImageStack stack = new ImageStack(this.nx, this.ny);
		for (String klass : Supervisor.classes.keySet()) {
			PixelClass pc = (PixelClass) Supervisor.classes.get(klass);
			IJ.log("Class: " + klass + " hue: " + pc.hue + " value: " + pc.valueRegion);
		}
		for (int frame = 1; frame <= this.nt; frame++) {
			Supervisor.imp.setPosition(channel, slice, frame);
			Object outlines = Supervisor.getOutlinesAtFrame(frame);
			ByteProcessor lab = computeFrame((ArrayList) outlines);
			stack.addSlice(lab);
		}
		new ImagePlus("lab", stack).show();
	}

	public void saveLabel(int channel, int slice, String dirData, String dirLabel) {
		int nc = Supervisor.imp.getNChannels();
		int nz = Supervisor.imp.getNSlices();
		try {
			File dir1 = new File(dirData).getParentFile();
			if (!dir1.isDirectory()) {
				new File(dir1.getAbsolutePath()).mkdir();
			}
			File dir2 = new File(dirLabel).getParentFile();
			if (!dir2.isDirectory()) {
				new File(dir2.getAbsolutePath()).mkdir();
			}
			File dird = new File(dirData);
			dird.mkdir();
			if (!dird.exists()) {
				IJ.error("No directory " + dirData);
				return;
			}
			File dirl = new File(dirLabel);
			dirl.mkdir();
			if (!dirl.exists()) {
				IJ.error("No directory " + dirLabel);
				return;
			}
		}
		catch (Exception localException) {
			File dir2;
			for (String klass : Supervisor.classes.keySet()) {
				PixelClass pc = (PixelClass) Supervisor.classes.get(klass);
				IJ.log("Class: " + klass + " hue: " + pc.hue + " value: " + pc.valueRegion + " (" + pc.sizeInner + ","
						+ pc.sizeOuter + ")");
			}
			for (int frame = 1; frame <= this.nt; frame++) {
				Supervisor.imp.setPosition(channel, slice, frame);
				ArrayList<Outline> outlines = Supervisor.getOutlinesAtFrame(frame);
				ByteProcessor lab = computeFrame(outlines);
				try {
					int n = (frame - 1) * nc * nz + (slice - 1) * nc + channel;
					String la = Supervisor.imp.getStack().getShortSliceLabel(n) + ".tif";
					IJ.log("Save Label: " + la);
					new FileSaver(new ImagePlus("", lab)).saveAsTiff(dirLabel + la);
					new FileSaver(new ImagePlus("", Supervisor.imp.getProcessor())).saveAsTiff(dirData + la);
				}
				catch (Exception localException1) {
				}
			}
		}
	}

	private ByteProcessor computeFrame(ArrayList<Outline> outlines) {
		ByteProcessor lab = new ByteProcessor(this.nx, this.ny);
		for (Outline outline : outlines) {
			int frame = outline.getFrame();
			String name = outline.klass;
			PixelClass item = (PixelClass) Supervisor.classes.get(name);
			if (item != null) {
				IJ.log("Labelisation Frame " + frame + "outlines of cell:" + name);

				ByteProcessor base = fill(outline, item.valueRegion, frame);
				if (item.sizeInner >= 1) {
					boolean[][] be = disc(2 * item.sizeInner + 1);
					increment(lab, erosion(base, be, item.valueRegion, item.valueInner));
				}
				if (item.sizeOuter >= 1) {
					boolean[][] bd = disc(2 * item.sizeOuter + 1);
					increment(lab, dilation(base, bd, item.valueRegion, item.valueOuter));
				}
				increment(lab, base);
			}
		}
		return lab;
	}

	private void increment(ByteProcessor a, ByteProcessor b) {
		byte[] ba = (byte[]) a.getPixels();
		byte[] bb = (byte[]) b.getPixels();
		for (int i = 0; i < ba.length; i++) {
			if ((ba[i] == 0) && (bb[i] != 0)) {
				ba[i] = bb[i];
			}
		}
	}

	private ByteProcessor fill(Outline outline, double valueRegion, int frame) {
		ByteProcessor base = new ByteProcessor(this.nx, this.ny);
		int count = 0;
		for (int i = 0; i < this.nx; i++) {
			for (int j = 0; j < this.ny; j++) {
				if (outline.contains(i, j)) {
					base.putPixelValue(i, j, valueRegion);
					count++;
				}
			}
		}
		IJ.log("Frame " + frame + " " + outline.klass + " pixels " + count + " (" + valueRegion + ")");
		return base;
	}

	private boolean[][] disc(int n) {
		if (n <= 1) {
			return new boolean[][] { { true } };
		}
		boolean[][] se = new boolean[n][n];
		double a = n / 2.0D * (n / 2.0D);
		double b = n / 2.0D * n;
		for (int k = 0; k < n; k++) {
			for (int l = 0; l < n; l++) {
				int kk = k - n / 2;
				int ll = l - n / 2;
				if (kk * kk / a + ll * ll / b < 1.0D) {
					se[k][l] = true;
				}
				else {
					se[k][l] = false;
				}
			}
		}
		return se;
	}

	private ByteProcessor erosion(ByteProcessor input, boolean[][] b, double value, double valueContour) {
		int nx = input.getWidth();
		int ny = input.getHeight();
		int m = b.length;
		int h = m / 2;
		ByteProcessor output = new ByteProcessor(nx, ny);
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (input.getPixelValue(x, y) == value) {
					double min = Double.MAX_VALUE;
					for (int i = 0; i < m; i++) {
						for (int j = 0; j < m; j++) {
							if (b[i][j] != false) {
								int k = Math.max(0, Math.min(nx - 1, x + i - h));
								int l = Math.max(0, Math.min(ny - 1, y + j - h));
								if (input.getPixelValue(k, l) < min) {
									min = input.getPixelValue(k, l);
								}
							}
						}
					}
					output.putPixelValue(x, y, min);
				}
			}
		}
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (input.getPixelValue(x, y) == value) {
					if (output.getPixel(x, y) == 0) {
						output.putPixelValue(x, y, valueContour);
					}
					else {
						output.putPixelValue(x, y, 0.0D);
					}
				}
			}
		}
		return output;
	}

	public ByteProcessor dilation(ByteProcessor input, boolean[][] b, double value, double valueContour) {
		int nx = input.getWidth();
		int ny = input.getHeight();
		int m = b.length;
		int h = m / 2;
		ByteProcessor output = (ByteProcessor) input.duplicate();
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (input.getPixelValue(x, y) == 0.0F) {
					double max = -1.7976931348623157E308D;
					for (int i = 0; i < m; i++) {
						for (int j = 0; j < m; j++) {
							if (b[i][j] != false) {
								int k = Math.max(0, Math.min(nx - 1, x + i - h));
								int l = Math.max(0, Math.min(ny - 1, y + j - h));
								if (input.getPixelValue(k, l) > max) {
									max = input.getPixelValue(k, l);
								}
							}
						}
					}
					output.putPixelValue(x, y, max);
				}
			}
		}
		for (int x = 0; x < nx; x++) {
			for (int y = 0; y < ny; y++) {
				if (output.getPixelValue(x, y) == value) {
					if (input.getPixelValue(x, y) == 0.0F) {
						output.putPixelValue(x, y, valueContour);
					}
					else {
						output.putPixelValue(x, y, 0.0D);
					}
				}
			}
		}
		return output;
	}
}
