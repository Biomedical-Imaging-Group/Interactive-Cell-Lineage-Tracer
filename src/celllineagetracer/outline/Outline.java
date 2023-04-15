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

package celllineagetracer.outline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import celllineagetracer.Supervisor;
import celllineagetracer.canvas.ICLTCanvas;
import celllineagetracer.cell.Cell;
import celllineagetracer.pixelclass.PixelClass;
import celllineagetracer.polyline.Node;
import celllineagetracer.polyline.Polyline;

public class Outline {
	public String cell;
	public String klass;
	private Polyline polyline;
	private int frame;

	public Outline(Polyline polyline, String cell, String klass, int frame) {
		this.polyline = polyline;
		this.cell = cell;
		this.klass = klass;
		this.frame = frame;
	}

	public Outline duplicate() {
		Polyline p = new Polyline();
		for (Node node : this.polyline) {
			p.add(new Node(node.x, node.y));
		}
		return new Outline(p, this.cell, this.klass, this.frame);
	}

	public int getFrame() {
		return this.frame;
	}

	public void setFrame(int frame) {
		this.frame = frame;
	}

	public boolean contains(int x, int y) {
		return getPolygon().contains(x, y);
	}

	public Color getCellColor() {
		if (this.cell == null) {
			return Color.BLACK;
		}
		Cell c = (Cell) Supervisor.cells.get(this.cell);
		if (c == null) {
			return Color.WHITE;
		}
		return c.getColor();
	}

	public Color getClassColor() {
		if (this.klass == null) {
			return Color.BLACK;
		}
		PixelClass c = (PixelClass) Supervisor.classes.get(this.klass);
		if (c == null) {
			return Color.WHITE;
		}
		return c.getColor();
	}

	public void drawArea(Graphics2D g, Color color, int areaOpacity, ICLTCanvas canvas) {
		Polygon polygon = getPolygon();
		Polygon p = new Polygon();
		for (int i = 0; i < polygon.npoints; i++) {
			p.addPoint(canvas.screenX(polygon.xpoints[i]), canvas.screenX(polygon.ypoints[i]));
		}
		g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), areaOpacity));
		g.fillPolygon(p);
	}

	public void drawCont(Graphics2D g, Color color, int contourStroke, ICLTCanvas canvas) {
		Polygon polygon = getPolygon();
		Polygon p = new Polygon();
		for (int i = 0; i < polygon.npoints; i++) {
			p.addPoint(canvas.screenX(polygon.xpoints[i]), canvas.screenX(polygon.ypoints[i]));
		}
		g.setColor(color);
		g.setStroke(new BasicStroke(contourStroke));
		g.drawPolygon(p);
		g.setStroke(new BasicStroke(1.0F));
	}

	public void drawCross(Graphics2D g, Color color, int centerSize, ICLTCanvas canvas) {
		Point2D.Double pt = this.polyline.computeCoG();
		g.setColor(color);
		double sz = canvas.getMagnification() * centerSize;
		int x = canvas.screenXD(pt.x);
		int y = canvas.screenXD(pt.y);
		int x1 = canvas.screenXD(pt.x - sz);
		int x2 = canvas.screenXD(pt.x + sz);
		int y1 = canvas.screenXD(pt.y - sz);
		int y2 = canvas.screenXD(pt.y + sz);
		g.drawLine(x, y1, x, y2);
		g.drawLine(x1, y, x2, y);
	}

	public void drawText(Graphics2D g, Color color, int textFont, int type, ICLTCanvas canvas) {
		Point2D.Double cog = this.polyline.computeCoG();
		String t = "" + canvas.imp.getFrame();

		g.setFont(new Font("Monospace", 2, textFont));
		FontMetrics fm = g.getFontMetrics();
		int w = fm.stringWidth(t) / 2;
		int x = canvas.screenXD(cog.x) - w;
		int y = canvas.screenXD(cog.y);
		g.setColor(Color.BLACK);
		g.drawString(t, x + 1, y + 1);
		g.setColor(color);
		g.drawString(t, x, y);
	}

	public Polygon getPolygon() {
		Polygon polygon = new Polygon();
		for (Node node : this.polyline) {
			polygon.addPoint((int) Math.round(node.x), (int) Math.round(node.y));
		}
		return polygon;
	}

	public void setPolyline(Polyline polyline) {
		this.polyline = polyline;
	}

	public Polyline getPolyline() {
		return this.polyline;
	}

	public String getInfo() {
		String info = this.cell == null ? "Undefined" : this.cell;
		info = info + " (" + (this.klass == null ? "" : this.klass) + ") ";
		if (this.polyline == null) {
			info = info + " no polyline";
		}
		else {
			info = info + " length: " + String.format("%4.1f", this.polyline.length());
			info = info + " nodes:" + this.polyline.size();
		}
		return info;
	}

	public String toString() {
		return "outline size=" + this.polyline.size();
	}
}
