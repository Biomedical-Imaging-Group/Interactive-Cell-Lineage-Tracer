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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import additionaluserinterface.GridPanel;
import celllineagetracer.cell.Cell;
import celllineagetracer.outline.MeasureTable;
import celllineagetracer.outline.Outline;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.PolygonRoi;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;

public class Measure implements ActionListener, Runnable {
	private JButton bnMeasure = new JButton("Measure");
	private JButton bnSetMeasurements = new JButton("Set Measurements (ImageJ) ...");
	private Thread thread = null;
	private JButton job;
	private JComboBox<String> cmbChannel = new JComboBox<String>();
	private JComboBox<String> cmbSlice = new JComboBox<String>();

	public JPanel getPanel() {
		int nz = Supervisor.imp.getNSlices();
		int nc = Supervisor.imp.getNChannels();
		this.cmbChannel.addItem("All Channels");
		this.cmbSlice.addItem("All Slices");
		for (int i = 1; i <= nz; i++) {
			this.cmbSlice.addItem("Slice " + i);
		}
		for (int i = 1; i <= nc; i++) {
			this.cmbChannel.addItem("Channel " + i);
		}
		GridPanel pnMeasure = new GridPanel("Measurement", 4);
		pnMeasure.place(10, 0, 2, 1, new JLabel("ImageJ bug: do not check 'Display Label'"));
		pnMeasure.place(11, 0, 2, 1, this.bnSetMeasurements);
		pnMeasure.place(12, 0, new JLabel("Channel"));
		pnMeasure.place(12, 1, this.cmbChannel);
		pnMeasure.place(13, 0, new JLabel("Slice Z"));
		pnMeasure.place(13, 1, this.cmbSlice);
		pnMeasure.place(14, 1, this.bnMeasure);

		this.bnMeasure.addActionListener(this);
		this.bnSetMeasurements.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, 3));
		panel.add(pnMeasure);
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.bnMeasure) {
			this.job = ((JButton) e.getSource());
			if (this.thread == null) {
				Thread thread = new Thread(this);
				thread.setPriority(1);
				thread.start();
			}
		}
		else if (e.getSource() == this.bnSetMeasurements) {
			IJ.run("Set Measurements...");
		}
	}

	public void run() {
		this.bnMeasure.setEnabled(false);
		if (this.job == this.bnMeasure) {
			measure();
		}
		this.bnMeasure.setEnabled(true);
	}

	public void measure() {
		IJ.run("Clear Results");
		ImagePlus imp = Supervisor.imp;
		int frameStart = imp.getFrame();
		int sliceStart = imp.getSlice();
		int channelStart = imp.getChannel();
		int channel = this.cmbChannel.getSelectedIndex();
		int slice = this.cmbSlice.getSelectedIndex();
		int m = Analyzer.getMeasurements();
		Analyzer.setMeasurements(m);

		int minChannel = channel == 0 ? 1 : channel;
		int maxChannel = channel == 0 ? imp.getNChannels() : channel;
		int minSlice = slice == 0 ? 1 : slice;
		int maxSlice = slice == 0 ? imp.getNSlices() : slice;
		IJ.log(" maxChannel " + minChannel + " /" + maxChannel);
		IJ.log(" maxSlice " + minSlice + " /" + maxSlice);
		ArrayList<String[]> rows = new ArrayList();
		imp.setPosition(channelStart, sliceStart, frameStart);
		String[] headersTable = null;
		ResultsTable rt = Analyzer.getResultsTable();
		if (rt != null) {
			while (rt.getCounter() > 0) {
				rt.deleteRow(0);
			}
		}
		Analyzer analyzer = new Analyzer(imp);
		Iterator<Integer> localIterator2;
		for (Iterator<String> localIterator1 = Supervisor.cells.keySet().iterator(); localIterator1.hasNext(); localIterator2.hasNext()) {
			String name = (String) localIterator1.next();
			Cell cell = (Cell) Supervisor.cells.get(name);
			localIterator2 = cell.getListOutlinesFrame().iterator();
			Integer frame = (Integer) localIterator2.next();
			Outline outline = cell.getOutline(frame);
			for (int z = minSlice; z <= maxSlice; z++) {
				for (int c = minChannel; c <= maxChannel; c++) {
					imp.setPosition(c, z, frame.intValue());
					imp.updateAndDraw();
					Point2D.Double cog = outline.getPolyline().computeCoG();

					PolygonRoi roi = new PolygonRoi(outline.getPolygon(), 2);
					imp.setRoi(roi);

					analyzer.run(imp.getProcessor());
					analyzer.displayResults();

					headersTable = rt.getHeadings();
					int ncol = rt.getHeadings().length;
					String[] row = new String[10 + ncol];
					row[0] = outline.cell;
					row[1] = outline.klass;
					row[2] = "" + outline.getFrame();
					row[3] = "" + outline.getPolyline().size();
					row[4] = String.format("%3.2f", outline.getPolyline().length());
					row[5] = String.format("%3.2f", cog.x);
					row[6] = String.format("%3.2f", cog.y);
					row[7] = "" + c;
					row[8] = "" + z;
					row[9] = frame.toString();
					for (int col = 0; col < ncol; col++) {
						row[(10 + col)] = rt.getStringValue(headersTable[col], rt.getCounter() - 1);
					}
					rows.add(row);

				}
			}
		}
		if (rows.size() > 0) {
			int ncol = headersTable.length;
			String[] headers = new String[10 + ncol];
			headers[0] = "Cell";
			headers[1] = "Class";
			headers[2] = "ID";
			headers[3] = "Nodes";
			headers[4] = "Length";
			headers[5] = "XG";
			headers[6] = "YG";
			headers[7] = "Channel";
			headers[8] = "Slice";
			headers[9] = "Frame";
			for (int i = 0; i < headersTable.length; i++) {
				headers[(10 + i)] = headersTable[i];
			}
			MeasureTable table = new MeasureTable(headers);
			try {
				table.setData(rows);
			}
			catch (Exception ex) {
				IJ.error(ex.toString());
			}
			SimpleDateFormat formatter = new SimpleDateFormat(" yyyy-MM-dd-HH-mm-ss");
			String formattedDate = formatter.format(new Date());

			String title = "measure ";
			title = title
					+ (channel == 0 ? " all-channels " : new StringBuilder(" channel").append(channel).toString());
			title = title + (slice == 0 ? " all-slices  " : new StringBuilder(" slice").append(slice).toString());
			title = title + formattedDate;
			table.show(title, 800, 200);
		}
		imp.killRoi();
		imp.setPosition(channelStart, sliceStart, frameStart);
	}
}
