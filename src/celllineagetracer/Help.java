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

import java.awt.Dimension;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Help extends JEditorPane {
	private String html = "";
	private String header = "";
	private String footer = "";
	private Dimension dim = new Dimension(Constants.widthTable, 200);
	private String font = "verdana";
	private String color = "#222222";
	private String background = "#f8f8f8";

	public Help() {
		create();
		append("h1", "Create manually a new outline");
		append("p", "1) Create a new cell (object), button Create");
		append("p", "2) Double click for the first node");
		append("p", "3) Single click to add a node");
		append("p", "Move mouse over the first node to validate the outline, or click on ENTER");
		append("p", "ESC to discard the manual creation of an outline");
		append("h1", "Create a new outline from a existing one");
		append("p", "Copy/Paste the selected one");
		append("p", "Propagate the selected one until the end");
		append("p", "Interpolate the selected on the next one");
		append("h1", "Delete a outline");
		append("p", "Cut the selected one");
		append("p", "Remove from the table");
		append("p", "Click on the 'Del' button");

		append("h1", "Action on selected node");
		append("p", "Drag to change the position");
		append("p", "CTRL+click to delete a node");
		append("p", "ALT+click to insert a node");
		append("p", "SHIFT+click to move all nodes");
	}

	public String getText() {
		Document doc = getDocument();
		try {
			return doc.getText(0, doc.getLength());
		}
		catch (BadLocationException e) {
			e.printStackTrace();
		}
		return getText();
	}

	public void clear() {
		this.html = "";
		append("");
	}

	private void create() {
		this.header += "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\n";
		this.header += "<html><head>\n";
		this.header = (this.header + "<style>body {background-color:" + this.background + "; color:" + this.color
				+ "; font-family: " + this.font + ";margin:4px}</style>\n");
		this.header += "<style>h1 {color:#555555; font-size:1.0em; font-weight:bold; padding:1px; margin:1px; padding-top:5px}</style>\n";
		this.header += "<style>h2 {color:#333333; font-size:0.9em; font-weight:bold; padding:1px; margin:1px;}</style>\n";
		this.header += "<style>h3 {color:#000000; font-size:0.9em; font-weight:italic; padding:1px; margin:1px;}</style>\n";
		this.header = (this.header + "<style>p  {color:" + this.color
				+ "; font-size:0.9em; padding:1px; margin:0px;}</style>\n");
		this.header += "</head>\n";
		this.header += "<body>\n";
		this.footer += "</body></html>\n";
		setEditable(false);
		setContentType("text/html; charset=ISO-8859-1");
	}

	public void append(String content) {
		this.html += content;
		setText(this.header + this.html + this.footer);
		if (this.dim != null) {
			setPreferredSize(this.dim);
		}
		setCaretPosition(0);
	}

	public void append(String tag, String content) {
		this.html = (this.html + "<" + tag + ">" + content + "</" + tag + ">");
		setText(this.header + this.html + this.footer);
		if (this.dim != null) {
			setPreferredSize(this.dim);
		}
		setCaretPosition(0);
	}

	public JScrollPane getPane() {
		JScrollPane scroll = new JScrollPane(this, 20, 30);
		scroll.setPreferredSize(this.dim);
		return scroll;
	}
}
