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

public class Tools
{
  public static String frame(int frame)
  {
    return String.format("%04d", new Object[] { Integer.valueOf(frame) });
  }
  
  public static String changeExtension(String filename, String extension)
  {
    if (filename == null) {
      return null;
    }
    if (filename.contains(".")) {
      filename = filename.substring(0, filename.lastIndexOf('.'));
    }
    return filename + "." + extension;
  }
  
  public static double convertToDouble(String a, double def)
  {
    if (a == null) {
      return def;
    }
    String b = a.trim();
    double i = def;
    try
    {
      i = Double.parseDouble(b);
    }
    catch (Exception localException) {}
    return i;
  }
  
  public static int convertToInt(String a, int def)
  {
    if (a == null) {
      return def;
    }
    int i = def;
    String b = a.trim();
    try
    {
      i = Integer.parseInt(b);
    }
    catch (Exception localException) {}
    return i;
  }
}
