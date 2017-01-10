package com.deerbelling.pdf;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jfree.chart.JFreeChart;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

public class DocWriter implements Closeable {

	private Document document;
	private PdfWriter writer;

	public DocWriter(String fileName) throws FileNotFoundException, DocumentException {
		document = new Document();
		writer = PdfWriter.getInstance(document, new FileOutputStream(fileName));
		document.open();
	}

	public void writeChartsToPdf(int width, int height, JFreeChart... charts) {
		try {
			PdfContentByte contentByte = writer.getDirectContent();

			for (JFreeChart chart : charts) {
				PdfTemplate template = contentByte.createTemplate(width, height);
				Graphics2D graphics2d = new PdfGraphics2D(template, width, height, new DefaultFontMapper());
				Rectangle2D rectangle2d = new Rectangle2D.Double(0, 0, width, height);

				chart.draw(graphics2d, rectangle2d);

				graphics2d.dispose();
				Image chartImage = Image.getInstance(template);

				document.add(chartImage);

				// FIXME update with the good syntax.
				document.add(new Paragraph("\n"));

			}

		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	public void writeCellToPdf(PdfPTable cell) throws DocumentException {
		document.newPage();
		document.add(cell);
	}

	@Override
	public void close() throws IOException {
		document.close();
	}
}
