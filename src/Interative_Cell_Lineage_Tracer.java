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

import additionaluserinterface.Settings;
import celllineagetracer.HyperstackDialog;
import celllineagetracer.ICLTDialog;
import celllineagetracer.Supervisor;
import celllineagetracer.canvas.ICLTCanvas;
import celllineagetracer.canvas.ICLTWindow;
import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.StackWindow;
import ij.io.Opener;
import ij.plugin.PlugIn;

public class Interative_Cell_Lineage_Tracer implements PlugIn {
	private Settings settings = new Settings("Interactive Cell Lineage Tracer", IJ.getDirectory("plugins") + "Cell-Lineage-Tracer.txt");

	public static void main(String[] arg) {
		Opener file = new Opener();
		ImagePlus imp = file.openImage("/Users/dsage/Desktop/ICLT/batch0.tif");
		imp.show();
		new Interative_Cell_Lineage_Tracer().run("main");
	}

	public void run(String arg) {
		ImagePlus impSource = WindowManager.getCurrentImage();
		if (impSource == null) {
			IJ.error("No open image.");
			return;
		}
		if (!arg.equals("main")) {
			IJ.setTool("freehand");
		}
		HyperstackDialog dlg = new HyperstackDialog(impSource, this.settings);
		if (dlg.wasCanceled()) {
			return;
		}
		ImagePlus imp = dlg.getImagePlus();
		Supervisor supervisor = new Supervisor(imp);
		StackWindow windowOriginal = (StackWindow) imp.getWindow();
		ICLTCanvas canvas = new ICLTCanvas(supervisor, imp);
		ICLTWindow window = new ICLTWindow(supervisor, imp, canvas);
		window.addPanel();
		canvas.requestFocus();

		imp.killRoi();
		Overlay overlay = imp.getOverlay();
		if (overlay != null) {
			overlay.clear();
		}
		imp.setPosition(imp.getChannel(), imp.getSlice(), 1);

		supervisor.attachWindow(window, canvas);
		new ICLTDialog(supervisor, windowOriginal, this.settings);
	}
}
