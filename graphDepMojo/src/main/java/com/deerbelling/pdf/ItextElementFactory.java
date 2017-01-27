package com.deerbelling.pdf;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

public class ItextElementFactory {
	public static JFreeChart generatePieChart(String title, Map<String, Integer> valueMap) {
		DefaultPieDataset dataSet = new DefaultPieDataset();
		for (String libKey : valueMap.keySet()) {
			int countRef = valueMap.get(libKey);
			if (countRef != 0) {
				String libLabel = libKey.substring(libKey.lastIndexOf('/') + 1);
				if (!"classes".equals(libLabel) && !"test-classes".equals(libLabel)) {
					dataSet.setValue(libLabel + " nb:" + countRef, countRef);
				}
			}
		}
		JFreeChart chart = org.jfree.chart.ChartFactory.createPieChart(title, dataSet, true, true, false);
		return chart;
	}

	public static JFreeChart generateBarChart(String title, Map<String, Integer> valueMap) {
		DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
		for (String libKey : valueMap.keySet()) {
			int countRef = valueMap.get(libKey);
			if (countRef != 0) {
				String libLabel = libKey;
				if (libLabel.contains("/")){
					libLabel = libLabel.substring(libLabel.lastIndexOf('/') + 1);
				}
				if (libLabel.contains(".jar")){
					libLabel = libLabel.substring(0, libLabel.lastIndexOf(".jar"));
				}
				if (!"classes".equals(libLabel) && !"test-classes".equals(libLabel)) {
					dataSet.setValue(countRef, "References", libLabel);
				}
			}
		}
		JFreeChart chart = org.jfree.chart.ChartFactory.createBarChart(title, "Dependencies", "References", dataSet,
				PlotOrientation.HORIZONTAL, false, false, false);
		
		CategoryPlot plot = chart.getCategoryPlot();
		Font labelFont = new Font(Font.SANS_SERIF, Font.BOLD, 8); 
		plot.getDomainAxis().setTickLabelFont(labelFont);
		
		BarRenderer renderer = (BarRenderer) plot.getRenderer();

		// set the color (r,g,b) or (r,g,b,a)
		renderer.setSeriesPaint(0, new Color(230, 130, 0));
		
		return chart;
	}

	public static PdfPTable generateList(String title, Set<String> list) {
		PdfPTable phraseTable = new PdfPTable(1);

		phraseTable.setWidthPercentage(90);
		phraseTable.setHorizontalAlignment(Element.ALIGN_LEFT);

		PdfPCell cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
		Paragraph paragraph = new Paragraph(title);
		paragraph.setAlignment(Element.ALIGN_CENTER);
		paragraph.setFont(FontFactory.getFont("SansSerif", 22, Font.BOLD));
		cell.addElement(paragraph);
		phraseTable.addCell(cell);

		cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
		paragraph = new Paragraph("\n");
		paragraph.setFont(FontFactory.getFont("SansSerif", 8, Font.BOLD));
		cell.addElement(paragraph);
		phraseTable.addCell(cell);

		List itextList = new List();
		cell = new PdfPCell();
		cell.setBorder(PdfPCell.NO_BORDER);
		for (String value : list) {
			if (value != null) {
				String libLabel = value;
				if (libLabel.contains("/")){
					libLabel = libLabel.substring(libLabel.lastIndexOf('/') + 1);
				}
				if (libLabel.contains(".jar")){
					libLabel = libLabel.substring(0, libLabel.lastIndexOf(".jar"));
				}
				ListItem item = new ListItem(libLabel);
				item.setAlignment(Element.ALIGN_JUSTIFIED);
				itextList.add(item);
			}
		}
		cell.addElement(itextList);
		phraseTable.addCell(cell);
		return phraseTable;
	}
}
