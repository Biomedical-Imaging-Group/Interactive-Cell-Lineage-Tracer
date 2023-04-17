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
import additionaluserinterface.Settings;
import celllineagetracer.outline.Outline;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class Rendering implements ActionListener, Runnable {
	private JButton bnColorize = new JButton("Colorize");
	private JButton bnBinarize = new JButton("Binarize");
	private JRadioButton rbCells = new JRadioButton("Cells", true);
	private JRadioButton rbClass = new JRadioButton("Classes");
	private Settings settings;
	private Thread thread = null;
	private JButton job;

	public Rendering(Settings settings) {
		this.settings = settings;
	}

	public JPanel getPanel() {
		GridPanel pnRendering = new GridPanel("Rendering", 1);
		pnRendering.place(0, 2, this.bnBinarize);
		pnRendering.place(1, 2, this.bnColorize);
		pnRendering.place(1, 0, this.rbCells);
		pnRendering.place(1, 1, this.rbClass);

		this.bnColorize.addActionListener(this);
		this.bnBinarize.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		group.add(this.rbCells);
		group.add(this.rbClass);

		this.rbCells.addActionListener(this);
		this.rbClass.addActionListener(this);

		this.settings.record("rbClass", this.rbClass, true);
		this.settings.record("rbCells", this.rbCells, true);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, 3));
		panel.add(pnRendering);
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		if ((e.getSource() == this.bnBinarize) || (e.getSource() == this.bnColorize)) {
			this.job = ((JButton) e.getSource());
			if (this.thread == null) {
				Thread thread = new Thread(this);
				thread.setPriority(1);
				thread.start();
			}
		}
	}

	public void run() {
		this.bnColorize.setEnabled(false);
		this.bnBinarize.setEnabled(false);
		if (this.job == this.bnColorize) {
			colorize();
		}
		if (this.job == this.bnBinarize) {
			binarize();
		}
		this.bnBinarize.setEnabled(true);
		this.bnColorize.setEnabled(true);
	}

	public void colorize() {
		ImagePlus imp = Supervisor.imp;

		int f = imp.getFrame();
		int c = imp.getChannel();
		int s = imp.getSlice();
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nt = imp.getNFrames();
		ImageStack stack = new ImageStack(nx, ny);
		boolean cklass = this.rbClass.isSelected();
		for (int frame = 1; frame <= nt; frame++) {
			Supervisor.goFrame(frame);

			ColorProcessor ip = new ColorProcessor(nx, ny);
			ArrayList<Outline> outlines = Supervisor.getOutlinesAtFrame(frame);
			byte[] r = new byte[nx * ny];
			byte[] g = new byte[nx * ny];
			byte[] b = new byte[nx * ny];
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					for (Outline outline : outlines) {
						if (outline.contains(i, j)) {
							Color color = cklass ? outline.getClassColor() : outline.getCellColor();
							int index = j * nx + i;
							r[index] = ((byte) color.getRed());
							g[index] = ((byte) color.getGreen());
							b[index] = ((byte) color.getBlue());
						}
					}
				}
			}
			ip.setRGB(r, g, b);
			stack.addSlice(ip);
		}
		ImagePlus label = new ImagePlus("Color " + imp.getTitle(), stack);
		label.show();
		imp.setPosition(c, s, f);
	}

	public void binarize() {
		ImagePlus imp = Supervisor.imp;

		int f = imp.getFrame();
		int c = imp.getChannel();
		int s = imp.getSlice();
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nt = imp.getNFrames();
		ImageStack stack = new ImageStack(nx, ny);
		for (int frame = 1; frame <= nt; frame++) {
			Supervisor.goFrame(frame);
			ImageProcessor ip = new ByteProcessor(nx, ny);
			ArrayList<Outline> shapes = Supervisor.getOutlinesAtFrame(frame);
			for (int i = 0; i < nx; i++) {
				for (int j = 0; j < ny; j++) {
					for (Outline shape : shapes) {
						if (shape.contains(i, j)) {
							ip.putPixelValue(i, j, 255.0D);
						}
					}
				}
			}
			stack.addSlice(ip);
		}
		ImagePlus label = new ImagePlus("Label " + imp.getTitle(), stack);
		label.show();
		imp.setPosition(c, s, f);
	}
}
