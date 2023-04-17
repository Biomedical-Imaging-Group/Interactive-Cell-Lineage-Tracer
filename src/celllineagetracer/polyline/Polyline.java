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

package celllineagetracer.polyline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;

import ij.gui.ImageCanvas;

public class Polyline
  extends ArrayList<Node>
{
  public void add(double x, double y)
  {
    add(new Node(x, y));
  }
  
  public void drawDash(Graphics g, Color color, ImageCanvas canvas)
  {
    g.setColor(color);
    Graphics2D g2 = (Graphics2D)g;
    Stroke dashed = new BasicStroke(1.0F, 0, 2, 0.0F, new float[] { 5.0F }, 0.0F);
    g2.setStroke(dashed);
    int n = size();
    if (n <= 1) {
      return;
    }
    Path2D polygon = new Path2D.Double();
    polygon.moveTo(canvas.screenXD(((Node)get(0)).x), canvas.screenYD(((Node)get(0)).y));
    for (int i = 1; i < n - 1; i++) {
      polygon.lineTo(canvas.screenXD(((Node)get(i)).x), canvas.screenYD(((Node)get(i)).y));
    }
    g2.draw(polygon);
    g2.setStroke(new BasicStroke());
  }
  
  public void drawPolygon(Graphics g, double stroke, ImageCanvas canvas)
  {
    g.setColor(Color.WHITE);
    Graphics2D g2 = (Graphics2D)g;
    g2.setStroke(new BasicStroke(2.0F));
    int n = size();
    if (n <= 1) {
      return;
    }
    for (int i = 0; i < n - 1; i++)
    {
      Node pt1 = (Node)get(i);
      Node pt2 = (Node)get(i + 1);
      g.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
    }
    g2.setStroke(new BasicStroke());
  }
  
  public void drawPolyline(Graphics g, Color color, ImageCanvas canvas)
  {
    g.setColor(color);
    int n = size();
    if (n <= 1) {
      return;
    }
    for (int i = 0; i < n - 1; i++)
    {
      Node pt1 = (Node)get(i);
      Node pt2 = (Node)get(i + 1);
      g.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
    }
  }
  
  public void drawPolygon(Graphics2D g2, Color color, int stroke, ImageCanvas canvas)
  {
    g2.setColor(color);
    g2.setStroke(new BasicStroke(stroke));
    int n = size();
    if (n <= 1) {
      return;
    }
    for (int i = 0; i < n - 1; i++)
    {
      Node pt1 = (Node)get(i);
      Node pt2 = (Node)get(i + 1);
      g2.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
    }
    Node pt1 = (Node)get(0);
    Node pt2 = (Node)get(n - 1);
    g2.drawLine(canvas.screenXD(pt1.x), canvas.screenYD(pt1.y), canvas.screenXD(pt2.x), canvas.screenYD(pt2.y));
  }
  
  public void drawNodes(Graphics g, Color color, ImageCanvas canvas)
  {
    g.setColor(color);
    int n = size();
    for (int i = 0; i < n; i++)
    {
      Point2D.Double pt1 = (Point2D.Double)get(i);
      g.drawOval(canvas.screenXD(pt1.x - 2.0D), canvas.screenYD(pt1.y - 2.0D), 5, 5);
    }
  }
  
  public Point2D.Double computeCoG()
  {
    double xg = 0.0D;
    double yg = 0.0D;
    for (Node node : this)
    {
      xg += node.x;
      yg += node.y;
    }
    return new Point2D.Double(xg / size(), yg / size());
  }
  
  public void translate(double dx, double dy)
  {
    for (Node node : this)
    {
      node.x += dx;
      node.y += dy;
    }
  }
  
  public Polyline resample(int nsamples)
  {
    double fineSampling = 2.0D;
    Polyline u = new Polyline();
    int nseg = size();
    for (int i = 0; i < nseg - 1; i++)
    {
      double dist = ((Node)get(i + 1)).distance((Point2D)get(i));
      int np = (int)(fineSampling * dist);
      if (np > 0)
      {
        double dx = (((Node)get(i + 1)).x - ((Node)get(i)).x) / np;
        double dy = (((Node)get(i + 1)).y - ((Node)get(i)).y) / np;
        for (int k = 0; k < np; k++) {
          u.add(new Node(((Node)get(i)).x + k * dx, ((Node)get(i)).y + k * dy));
        }
      }
      else
      {
        u.add((Node)get(i));
      }
    }
    Point2D.Double cog = u.computeCoG();
    int nu = u.size();
    double min = 1e300;
    int imin = -1;
    for (int i = 0; i < nu; i++) {
      if (cog.x - ((Node)u.get(i)).x > 1.0D)
      {
        double dx = Math.abs(cog.y - ((Node)u.get(i)).y);
        if (dx < min)
        {
          min = dx;
          imin = i;
        }
      }
    }
    Polyline uo = new Polyline();
    if (imin >= 0)
    {
      for (int i = imin; i < nu; i++) {
        uo.add((Node)u.get(i));
      }
      for (int i = 0; i < imin; i++) {
        uo.add((Node)u.get(i));
      }
    }
    double step = uo.size() / nsamples;
    Polyline p = new Polyline();
    int nuo = uo.size();
    for (double i = 0.0D; i < nuo; i += step) {
      p.add((Node)uo.get((int)Math.floor(i)));
    }
    return p;
  }
  
  public double length()
  {
    int n = size();
    if (n == 0) {
      return 0.0D;
    }
    double d = 0.0D;
    for (int i = 1; i < n; i++) {
      d += ((Node)get(i - 1)).distance((Point2D)get(i));
    }
    return d;
  }
  
  public GeneralPath getPath()
  {
    GeneralPath path = new GeneralPath(1, 4);
    if (size() <= 1) {
      return path;
    }
    Point2D.Double pt = (Point2D.Double)get(0);
    path.moveTo(pt.x, pt.y);
    for (int i = 1; i < size(); i++) {
      path.lineTo(((Node)get(i)).x, ((Node)get(i)).y);
    }
    return path;
  }
  
  public Polyline simplify(double tolerance)
  {
    int n = size();
    if (n == 0) {
      return new Polyline();
    }
    Point2D.Double[] list = new Point2D.Double[n];
    for (int i = 0; i < n; i++)
    {
      Point2D.Double pt = (Point2D.Double)get(i);
      list[i] = new Point2D.Double(pt.x, pt.y);
    }
    CurveSimplify simplify = new CurveSimplify(new Point2D.Double[n]);
    
    Polyline simplified = simplify.simplify(list, tolerance, true);
    return simplified;
  }
  
  public Polyline smooth(int win)
  {
    int n = size();
    if (n == 0) {
      return this;
    }
    int size = 2 * win + 1;
    Polyline s = new Polyline();
    s.add(((Node)get(0)).x, ((Node)get(0)).y);
    if (size > (n - 1) / 2)
    {
      s.add((Node)get(n - 1));
      return s;
    }
    for (int i = 1; i < n - 1; i++)
    {
      int norm = size;
      Point2D.Double pt = (Point2D.Double)get(i);
      double x = size * pt.x;
      double y = size * pt.y;
      for (int k = 1; k < size; k++)
      {
        int kk = i - size + k;
        if (kk >= 0)
        {
          Point2D.Double pk = (Point2D.Double)get(kk);
          x += k * pk.x;
          y += k * pk.y;
          norm += k;
        }
        kk = i + size - k;
        if (kk < n)
        {
          Point2D.Double pk = (Point2D.Double)get(kk);
          x += k * pk.x;
          y += k * pk.y;
          norm += k;
        }
      }
      s.add(x / norm, y / norm);
    }
    s.add((Node)get(n - 1));
    return s;
  }
  
  public void makeClockwise()
  {
    if (isClockwise()) {
      return;
    }
    Collections.reverse(this);
  }
  
  private boolean isClockwise()
  {
    double sum = 0.0D;
    int n = size();
    for (int i = 0; i < n; i++)
    {
      Node v1 = (Node)get(i);
      Node v2 = (Node)get((i + 1) % n);
      sum += (v2.x - v1.x) * (v2.y + v1.y);
    }
    return sum > 0.0D;
  }
}
