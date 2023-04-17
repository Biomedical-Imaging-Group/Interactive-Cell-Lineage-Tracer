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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import celllineagetracer.Supervisor;
import celllineagetracer.cell.Cell;
import celllineagetracer.cell.CellDialog;
import celllineagetracer.cell.TrajectoryPoint;
import celllineagetracer.outline.Outline;
import celllineagetracer.polyline.DrawParameters;
import celllineagetracer.polyline.Node;
import celllineagetracer.polyline.Polyline;
import celllineagetracer.polyline.PolylineMagnetic;
import celllineagetracer.polyline.PolylineOptimized;
import celllineagetracer.polyline.PolylineStraight;
import ij.ImagePlus;
import ij.gui.ImageCanvas;

public class ICLTCanvas extends ImageCanvas implements KeyListener {
	private Outline clipboard;

	public static enum PathType {
		FREE, MAGN, OPT, LINE;
	}

	private Polyline free = null;
	private EditableShapePolyline shape = null;
	private int selectedNode = -1;
	private Point cursor = null;
	public ImagePlus imp;
	private PathType pathType = PathType.FREE;
	private Cursor blankCursor;
	private Color colorMouse = new Color(255, 255, 20, 200);
	private Supervisor supervisor;
	public boolean trackShow = true;
	public boolean areaShow = true;
	public boolean contourShow = true;
	public boolean centerShow = true;
	public boolean textShow = true;
	public int trackColorCode = 1;
	public int areaColorCode = 1;
	public int contourColorCode = 1;
	public int centerColorCode = 1;
	public int textColorCode = 1;
	public float trackThickness = 1.0F;
	public int areaOpacity = 50;
	public int contourStroke = 1;
	public int centerSize = 10;
	public int textFont = 12;
	public boolean areaTrack = true;
	public boolean contourTrack = true;
	public boolean centerTrack = true;
	public boolean textTrack = true;
	public int trackBackward = 0;
	public int trackForward = 0;
	public int selectedColor = 5;
	public int selectedStroke = 1;
	public int textType = 0;

	public ICLTCanvas(Supervisor supervisor, ImagePlus imp) {
		super(imp);
		this.supervisor = supervisor;
		this.imp = imp;
		addKeyListener(this);
		BufferedImage cursorImg = new BufferedImage(16, 16, 2);
		this.blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
	}

	public void paint(Graphics g) {
		super.paint(g);
		int frame = this.imp.getFrame();
		if (this.cursor != null) {
			setCursor(this.blankCursor);
			g.setColor(this.colorMouse);
			int w = DrawParameters.magAperture;
			double mag = this.magnification;
			g.drawRect(screenX(this.cursor.x - w), screenY(this.cursor.y - w), (int) (mag * (2 * w + 1)),
					(int) (mag * (2 * w + 1)));
		}
		Graphics2D g2 = (Graphics2D) g;
		if (this.free != null) {
			this.free.drawPolyline(g, Color.ORANGE, this);
		}
		if (this.shape != null) {
			this.shape.draw(g, Color.RED, this);
		}
		Outline selected = this.supervisor.getSelected();
		if ((this.shape != null) || (selected != null)) {
			g.setColor(new Color(100, 100, 100, 150));
			g.fillRect(0, 0, this.imp.getWidth(), 20);
			if (this.shape != null) {
				write(g, this.shape.getInfo("", ""), 12, 12);
			}
			if (selected != null) {
				write(g, selected.getInfo(), 12, 12);
			}
		}
		int minTrack = Math.max(0, frame - this.trackBackward);
		int maxTrack = Math.min(this.imp.getNFrames(), frame + this.trackForward);

		ArrayList<Outline> outlines = Supervisor.getOutlinesAtFrame(frame);
		for (Outline outline : outlines) {
			if (this.areaShow) {
				outline.drawArea(g2, getDisplayColor(outline, this.areaColorCode), this.areaOpacity, this);
			}
			if (this.contourShow) {
				outline.drawCont(g2, getDisplayColor(outline, this.contourColorCode), this.contourStroke, this);
			}
			if (this.centerShow) {
				outline.drawCross(g2, getDisplayColor(outline, this.centerColorCode), this.centerSize, this);
			}
			if (this.textShow) {
				outline.drawText(g2, getDisplayColor(outline, this.textColorCode), this.textFont, this.textType, this);
			}
		}
		if ((this.areaTrack) || (this.contourTrack) || (this.centerTrack) || (this.textTrack)) {
			for (int f = minTrack; f <= maxTrack; f++) {
				if (f != frame) {
					for (Outline outline : Supervisor.getOutlinesAtFrame(f)) {
						if (this.areaTrack) {
							outline.drawArea(g2, getDisplayColor(outline, this.areaColorCode), this.areaOpacity, this);
						}
						if (this.contourTrack) {
							outline.drawCont(g2, getDisplayColor(outline, this.contourColorCode), this.contourStroke,
									this);
						}
						if (this.centerTrack) {
							outline.drawCross(g2, getDisplayColor(outline, this.centerColorCode), this.centerSize,
									this);
						}
						if (this.textTrack) {
							outline.drawText(g2, getDisplayColor(outline, this.textColorCode), this.textFont,
									this.textType, this);
						}
					}
				}
			}
		}
		if (this.trackShow) {
			drawTrajectory(g2, minTrack, maxTrack);
		}
		if ((selected != null) && (selected.getFrame() == frame)) {
			Color cs = getDisplayColor(selected, this.selectedColor);
			int s = this.selectedStroke;
			Polyline polyline = selected.getPolyline();
			polyline.drawPolygon(g2, cs, s, this);
			for (Node node : polyline) {
				g2.drawOval(screenXD(node.x - 2 * s), screenYD(node.y - 2 * s), 4 * s + 1, 4 * s + 1);
			}
			if (this.selectedNode >= 0) {
				Node node = (Node) polyline.get(this.selectedNode);
				g2.fillOval(screenXD(node.x - 2 * s), screenYD(node.y - 2 * s), 4 * s + 1, 4 * s + 1);
			}
		}
	}

	private void drawTrajectory(Graphics2D g2, int minTrack, int maxTrack) {
		for (String name : Supervisor.cells.keySet()) {
			Cell cell = (Cell) Supervisor.cells.get(name);
			ArrayList<TrajectoryPoint> traj = cell.getTrajectory();
			if (traj == null) {
				break;
			}
			if (traj.size() < 2) {
				break;
			}
			g2.setStroke(new BasicStroke(this.trackThickness));
			TrajectoryPoint prev = null;
			for (TrajectoryPoint curr : traj) {
				if ((curr.frame >= minTrack) && (curr.frame <= maxTrack)) {
					if (prev == null) {
						g2.setColor(getDisplayColor(cell.getOutline(Integer.valueOf(curr.frame)), this.trackColorCode));
					}
					else {
						g2.drawLine(screenXD(prev.x), screenYD(prev.y), screenXD(curr.x), screenYD(curr.y));
					}
					prev = curr;
				}
			}
		}
	}

	public void unselect() {
		this.supervisor.updateAll(null, true);
		this.selectedNode = -1;
		cancel();
	}

	public void cancel() {
		this.cursor = null;
		this.free = null;
		this.shape = null;
		this.supervisor.updateAll(null, true);

		setCursor(new Cursor(0));
		repaint();
	}

	public void create() {
		this.shape.extend(this.free);
		this.shape.closeContour();
		this.shape.update();
		this.supervisor.createOutline(this.imp.getFrame(), this.shape);
		this.shape = null;
		cancel();
	}

	public void mousePressed(MouseEvent e) {
		int xm = offScreenX(e.getX());
		int ym = offScreenY(e.getY());
		this.selectedNode = -1;
		if (e.getClickCount() == 2) {
			if (Supervisor.cells.size() == 0) {
				new CellDialog(this.supervisor, null);
				return;
			}
			this.shape = new EditableShapePolyline();
			this.cursor = new Point(xm, ym);
		}
		Outline selected = this.supervisor.getSelected();
		if (this.cursor == null) {
			if (selected != null) {
				for (int i = 0; i < selected.getPolyline().size(); i++) {
					if (((Node) selected.getPolyline().get(i)).distance(xm, ym) < 14.0D) {
						this.selectedNode = i;
					}
				}
			}
			Node curr;
			if (this.selectedNode >= 0) {
				Polyline p = selected.getPolyline();
				int modifiers = e.getModifiers();
				if ((modifiers & 0x2) == 2) {
					if (p.size() > 3) {
						p.remove(this.selectedNode);
					}
					this.selectedNode = -1;
				}
				if ((modifiers & 0x8) == 8) {
					int np = p.size();
					curr = (Node) p.get(this.selectedNode);
					Node prev = (Node) p.get(this.selectedNode == 0 ? np - 1 : this.selectedNode - 1);
					if (curr.distance(prev) > 5.0D) {
						p.add(this.selectedNode, new Node(0.5D * (curr.x + prev.x), 0.5D * (curr.y + prev.y)));
					}
					this.selectedNode = -1;
				}
				repaint();
				return;
			}
			ArrayList<Outline> outlines = Supervisor.getOutlinesAtFrame(this.imp.getFrame());
			Outline inside = null;
			for (Outline outline : outlines) {
				if (outline.contains(xm, ym)) {
					inside = outline;
				}
			}
			if (inside != null) {
				this.supervisor.updateAll(inside, true);
				repaint();
				return;
			}
			if (selected != null) {
				this.supervisor.updateAll(null, true);
				repaint();
				return;
			}
		}
		if (this.cursor != null) {
			this.shape.extend(this.free);
			if (this.pathType == PathType.FREE) {
				this.free = new Polyline();
			}
			if (this.pathType == PathType.OPT) {
				this.free = new PolylineOptimized(this.imp.getProcessor());
			}
			if (this.pathType == PathType.MAGN) {
				this.free = new PolylineMagnetic(this.imp.getProcessor());
			}
			if (this.pathType == PathType.LINE) {
				this.free = new PolylineStraight();
			}
			setCursor(this.blankCursor);

			this.free.add(xm, ym);
			this.shape.setDraw(this.free);

			this.shape.update();
			if (this.shape.isClose(this.cursor)) {
				create();
			}
			repaint();
			return;
		}
		super.mousePressed(e);
	}

	public void mouseMoved(MouseEvent e) {
		int xm = offScreenX(e.getX());
		int ym = offScreenY(e.getY());
		if (this.cursor != null) {
			this.cursor.x = xm;
			this.cursor.y = ym;
		}
		if (this.shape != null) {
			this.shape.update();
			if (this.shape.isClose(this.cursor)) {
				Node first = (Node) this.shape.get(0);
				if (this.free != null) {
					this.free.add(first.x, first.y);
				}
				create();
			}
		}
		if (this.free != null) {
			this.free.add(xm, ym);
		}
		repaint();

		super.mouseMoved(e);
		if (this.cursor != null) {
			setCursor(this.blankCursor);
		}
	}

	public void mouseDragged(MouseEvent e) {
		Outline selected = this.supervisor.getSelected();
		if ((selected != null) && (this.selectedNode >= 0)) {
			int xm = offScreenX(e.getX());
			int ym = offScreenY(e.getY());
			Polyline contour = selected.getPolyline();
			int modifiers = e.getModifiers();
			if ((modifiers & 0x1) == 1) {
				double dx = xm - ((Node) contour.get(this.selectedNode)).x;
				double dy = ym - ((Node) contour.get(this.selectedNode)).y;
				contour.translate(dx, dy);
				((Cell) Supervisor.cells.get(selected.cell)).computeTrajectory();
			}
			else {
				int ns = contour.size() - 1;
				if ((ns > 1) && ((this.selectedNode == 0) || (this.selectedNode == ns))) {
					double d = ((Node) contour.get(0)).distance((Point2D) contour.get(ns));
					if (d < 1.0D) {
						((Node) contour.get(0)).x = xm;
						((Node) contour.get(0)).y = ym;
						((Node) contour.get(ns)).x = xm;
						((Node) contour.get(ns)).y = ym;
					}
				}
				else {
					((Node) contour.get(this.selectedNode)).x = xm;
					((Node) contour.get(this.selectedNode)).y = ym;
				}
				((Cell) Supervisor.cells.get(selected.cell)).computeTrajectory();
			}
			repaint();
			return;
		}
		super.mouseDragged(e);
	}

	public void mouseReleased(MouseEvent e) {
		this.selectedNode = -1;
		repaint();
		super.mouseReleased(e);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		int modifiers = e.getModifiers();
		boolean m = ((modifiers & 0x2) == 2) || ((modifiers & 0x4) == 4);
		if (code == 32) {
			Supervisor.incFrame(1);
			this.supervisor.updateAll(null, true);
		}
		if (code == 10) {
			create();
		}
		if (code == 27) {
			cancel();
		}
		if ((m) && (code == 67)) {
			Outline selected = this.supervisor.getSelected();
			if (selected != null) {
				this.clipboard = selected.duplicate();
			}
			this.cursor = null;
		}
		if ((m) && (code == 86)) {
			Outline selected = this.supervisor.getSelected();
			Point c = getCursorLoc();
			if ((this.clipboard != null) && (selected == null)) {
				Point2D.Double cog = this.clipboard.getPolyline().computeCoG();
				double dx = offScreenX(c.x) - cog.x;
				double dy = offScreenY(c.y) - cog.y;
				this.clipboard.getPolyline().translate(dx, dy);
				selected = this.supervisor.createOutline(this.imp.getFrame(), this.clipboard.getPolyline());
				this.clipboard = this.clipboard.duplicate();
			}
			this.cursor = null;
		}
		if ((m) && (code == 88)) {
			Outline selected = this.supervisor.getSelected();
			if (selected != null) {
				this.clipboard = selected.duplicate();
				this.supervisor.deleteOutline(selected);
			}
			this.cursor = null;
		}
		if (code == 8) {
			Outline selected = this.supervisor.getSelected();
			if (selected != null) {
				this.supervisor.deleteOutline(selected);
			}
			this.cursor = null;
		}
		repaint();
	}

	public void keyReleased(KeyEvent e) {
		repaint();
	}

	public void setPath(PathType pathType) {
		this.pathType = pathType;
		repaint();
	}

	public void setToleranceFactor(double toleranceFactor) {
		Outline selected = this.supervisor.getSelected();
		if (selected != null) {
			Polyline s = selected.getPolyline();
			Polyline r = s.resample(1000);
			Polyline p = r.simplify(DrawParameters.tolerance);
			selected.setPolyline(p);
		}
		repaint();
	}

	public void setSmoothFactor(int smoothFactor) {
		Outline selected = this.supervisor.getSelected();
		if (selected != null) {
			Polyline s = selected.getPolyline();
			Polyline r = s.resample(1000);
			Polyline p = r.smooth(DrawParameters.smooth);
			selected.setPolyline(p);
		}
		cancel();
	}

	private void write(Graphics g, String text, int x, int y) {
		g.setColor(Color.BLACK);
		g.drawString(text, x, y);
		g.setColor(Color.WHITE);
		g.drawString(text, x - 1, y - 1);
	}

	private Color getDisplayColor(Outline outline, int code) {
		if (outline == null) {
			return Color.BLACK;
		}
		if (code == 0) {
			return outline.getCellColor();
		}
		if (code == 1) {
			return outline.getClassColor();
		}
		if (code == 2) {
			return Color.getHSBColor(outline.getFrame() / this.imp.getNFrames(), 1.0F, 1.0F);
		}
		if (code == 3) {
			return Color.RED;
		}
		if (code == 4) {
			return Color.YELLOW;
		}
		if (code == 5) {
			return Color.GREEN;
		}
		if (code == 6) {
			return Color.WHITE;
		}
		return Color.BLACK;
	}
}
