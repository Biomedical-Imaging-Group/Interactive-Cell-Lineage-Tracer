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

import java.util.ArrayList;

import ij.process.ImageProcessor;

public class PolylineOptimized extends Polyline {
	private ImageProcessor ip;

	public PolylineOptimized(ImageProcessor ip) {
		this.ip = ip;
	}

	public void add(double x, double y) {
		if (size() < 3) {
			add(new Node(x, y));
			return;
		}
		Node a = (Node) get(0);
		Node b = new Node(x, y);
		double len = a.distance(b);
		if (len < 10.0D) {
			return;
		}
		double ld = DrawParameters.optDataTerm;
		double lr = DrawParameters.optRegTerm;

		int search = (int) (0.33D * len);
		clear();
		optimize(a, b, DrawParameters.optAperture, search, ld, lr);
	}

	private void optimize(Node a, Node b, int delta, int search, double wdata, double wreg) {
		int nsearch = 2 * search + 1;
		double dist = a.distance(b);
		double step = dist / (int) dist;
		int ndist = (int) Math.round(dist / step);
		float[][] space = new float[ndist][nsearch];
		double dx = (b.x - a.x) / dist;
		double dy = (b.y - a.y) / dist;
		for (int k = 0; k < ndist; k++) {
			double xc = a.x + k * dx;
			double yc = a.y + k * dy;
			for (int e = 0; e < nsearch; e++) {
				space[k][e] = ((float) this.ip.getInterpolatedValue(xc - (e - search) * dy, yc - (e - search) * dx));
			}
		}
		int[][] pos = new int[ndist][nsearch];

		double[] cost = new double[nsearch];
		double[] previousCost = new double[nsearch];
		int e1inf = search;
		int e1sup = search;
		for (int k = 1; k < ndist; k++) {
			int kprev = k - 1;
			e1inf = Math.max(e1inf - delta, 0);
			e1sup = Math.min(e1sup + delta, nsearch - 1);
			for (int ek = e1inf; ek <= e1sup; ek++) {
				double mini = Double.MAX_VALUE;
				int eprevinf = Math.max(ek - delta, 0);
				int eprevsup = Math.min(ek + delta, nsearch - 1);
				int emini = 0;
				for (int eprev = eprevinf; eprev <= eprevsup; eprev++) {
					double e = previousCost[eprev] - wdata * space[kprev][eprev] - wreg * (Math.abs(eprev) - search);
					if (e < mini) {
						mini = e;
						emini = eprev;
					}
				}
				cost[ek] = mini;
				pos[k][ek] = emini;
			}
			System.arraycopy(cost, 0, previousCost, 0, nsearch);
		}
		int e = search;
		ArrayList<Node> nodes = new ArrayList<Node>();
		for (int k = ndist - 1; k >= 0; k--) {
			double xc = a.x + k * dx;
			double yc = a.y + k * dy;
			double xo = xc - (e - search) * dy;
			double yo = yc - (e - search) * dx;
			nodes.add(new Node(xo, yo));
			e = pos[k][e];
		}
		clear();
		add(a);
		for (int i = nodes.size() - 1; i >= 0; i--) {
			add((Node) nodes.get(i));
		}
		add(b);
	}
}
