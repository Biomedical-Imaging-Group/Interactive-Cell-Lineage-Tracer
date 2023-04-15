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

package celllineagetracer.polyline;

import ij.process.ImageProcessor;

public class PolylineMagnetic extends Polyline {
	private ImageProcessor ip;
	private int nx;
	private int ny;

	public PolylineMagnetic(ImageProcessor ip) {
		this.ip = ip;
		this.nx = (ip.getWidth() - 1);
		this.ny = (ip.getHeight() - 1);
	}

	public void add(double x, double y) {
		int xi = (int) Math.round(x);
		int yi = (int) Math.round(y);
		int w = DrawParameters.magAperture;
		int i1 = Math.max(0, xi - w);
		int i2 = Math.min(this.nx, xi + w);
		int j1 = Math.max(0, yi - w);
		int j2 = Math.min(this.ny, yi + w);
		double max = -1.7976931348623157E308D;
		int imax = xi;
		int jmax = yi;
		double ld = DrawParameters.magDataTerm;
		double lr = DrawParameters.magRegTerm;
		for (int i = i1; i <= i2; i++) {
			int id = i - xi;
			for (int j = j1; j <= j2; j++) {
				int jd = j - yi;
				double v = ld * value(this.ip, i, j) + lr * (w - Math.sqrt(id * id + jd * jd));
				if (v > max) {
					max = v;
					imax = i;
					jmax = j;
				}
			}
		}
		add(new Node(imax, jmax));
	}

	private double value(ImageProcessor ip, int i, int j) {
		if (DrawParameters.magCost == 1) {
			double gx = ip.getPixelValue(i - 1, j) - ip.getPixelValue(i + 1, j);
			double gy = ip.getPixelValue(i, j - 1) - ip.getPixelValue(i, j + 1);
			return gx * gx + gy * gy;
		}
		return ip.getPixelValue(i - 1, j);
	}
}
