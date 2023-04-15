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

package celllineagetracer.canvas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;

import celllineagetracer.Supervisor;
import celllineagetracer.cell.Cell;
import celllineagetracer.outline.Outline;
import ij.ImagePlus;

public class BarFrames extends JButton implements MouseListener {
	private String cell;

	public BarFrames() {
		super("frames");
		setBorder(BorderFactory.createEtchedBorder());
		setPreferredSize(new Dimension(180, 15));
		setMinimumSize(new Dimension(180, 15));
		setBackground(Color.GRAY);
		addMouseListener(this);
	}

	public void updateCell(String cell) {
		this.cell = cell;
		repaint();
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int frames = Supervisor.imp.getNFrames();
		int w = getWidth();
		int h = getHeight();
		double step = w / frames;

		g.setColor(Color.GRAY);
		g.fillRect(0, 0, w, h);
		if (this.cell == null) {
			return;
		}
		double left = 0.0D;
		double right = step;
		for (int frame = 1; frame <= frames; frame++) {
			int l = (int) Math.round(left);
			int r = (int) Math.round(right);
			g.setColor(Color.GRAY);
			g.drawRect(l, 0, r, h);
			Color color = ((Cell) Supervisor.cells.get(this.cell)).getColor();
			Outline outline = Supervisor.getOutline(frame, this.cell);
			if (outline == null) {
				g.setColor(Color.WHITE);
				g.fillRect(l, 0, r, h);
			}
			else {
				g.setColor(color);
				g.fillRect(l, 0, r, h);
			}
			left += step;
			right += step;
		}
	}

	public void mouseClicked(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		int frames = Supervisor.imp.getNFrames();
		ImagePlus imp = Supervisor.imp;
		int w = getWidth();
		double step = w / frames;

		int frame = (int) Math.floor(e.getX() / step);
		imp.setPosition(imp.getChannel(), imp.getSlice(), frame + 1);
		ICLTCanvas canvas = (ICLTCanvas) imp.getCanvas();
		canvas.unselect();
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
	}
}
