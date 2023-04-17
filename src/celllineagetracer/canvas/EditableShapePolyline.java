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

import celllineagetracer.polyline.DrawParameters;
import celllineagetracer.polyline.Node;
import celllineagetracer.polyline.Polyline;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

public class EditableShapePolyline extends Polyline {
	private ArrayList<Polyline> polylines;
	private Polyline draw;
	private int indexStable;
	private int indexConnect;

	public EditableShapePolyline() {
		this.polylines = new ArrayList<Polyline>();
	}

	public EditableShapePolyline(Polyline contour) {
		this.polylines = new ArrayList<Polyline>();
		this.polylines.add(contour);
	}

	public void closeContour() {
		int n = size();
		if (n <= 1) {
			return;
		}
		remove(n - 1);
	}

	public boolean isClose(Point p) {
		if (p == null) {
			return false;
		}
		if (size() <= 0) {
			return false;
		}
		if (length() <= 20.0D) {
			return false;
		}
		return ((Node) get(0)).distance(p.x, p.y) < 10.0D;
	}

	public void update() {
		clear();
		int smooth = DrawParameters.smooth;
		double tolerance = DrawParameters.tolerance;

		for (Polyline polyline : this.polylines) {
			if (polyline.size() > 1) {
				Polyline p = polyline.smooth(smooth).simplify(tolerance);
				int count = 0;
				for (Node pt : p) {
					pt.setStart(count++ == 0);
					add(pt);
				}
			}
		}
		this.indexStable = size();
		if ((this.draw != null) && (this.draw.size() > 1)) {
			Polyline p = this.draw.simplify(tolerance);
			int count = 0;
			for (Node pt : p) {
				pt.setStart(count++ == 0);
				add(pt);
			}
		}
		this.indexConnect = size();
		int n = size();
		if (n <= 0) {
		}
	}

	public void extend(Polyline p) {
		if (p == null) {
			return;
		}
		this.polylines.add(p);
		this.draw = null;
	}

	public void setDraw(Polyline draw) {
		this.draw = draw;
		if (draw == null) {
		}
	}

	public void draw(Graphics g, Color color, ICLTCanvas canvas) {
		for (Node node : this) {
			if (node.starts()) {
				g.drawOval(canvas.screenXD(node.x - 3.0D), canvas.screenYD(node.y - 3.0D), 7, 7);
			}
		}
		int n = size();
		if (n <= 1) {
			return;
		}
		g.setColor(color);

		int e = Math.min(this.indexStable, n - 2);
		for (int i = 0; i < e; i++) {
			Node pt1 = (Node) get(i);
			Node pt2 = (Node) get(i + 1);
			g.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
		}
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1.0F, 0, 2, 0.0F, new float[] { 5.0F }, 0.0F));

		int f = Math.min(this.indexConnect, n - 2);
		for (int i = e; i < f; i++) {
			Node pt1 = (Node) get(i);
			Node pt2 = (Node) get(i + 1);
			g.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
		}
		g2.setStroke(new BasicStroke(1.0F, 0, 2, 0.0F, new float[] { 5.0F, 2.0F, 2.0F, 2.0F }, 0.0F));
		Node pt1 = (Node) get(n - 2);
		Node pt2 = (Node) get(n - 1);
		g.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
		g2.setStroke(new BasicStroke());
	}

	public String getInfo(String cell, String klass) {
		String info = cell == null ? "Undefined" : cell;
		info = info + "(" + (klass == null ? "" : klass) + ") ";
		info = info + " length: " + String.format("%4.1f", new Object[] { Double.valueOf(length()) });
		info = info + " nodes:" + size();

		return info;
	}
}
