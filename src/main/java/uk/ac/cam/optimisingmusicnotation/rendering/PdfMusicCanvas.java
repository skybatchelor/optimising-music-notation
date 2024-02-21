package uk.ac.cam.optimisingmusicnotation.rendering;

import com.itextpdf.io.font.FontProgram;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.svg.converter.SvgConverter;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PdfMusicCanvas implements MusicCanvas<PdfMusicCanvas.Anchor> {

    // TODO: make these configurable
    private final float LINE_WIDTH = 0.8f;
    private final float STAVE_SPACING = 5f;
    private final int LINES_PER_PAGE = 10;

    public static class Anchor {

        private final int page;
        private final float x, y;

        private Anchor(int page, float x, float y) {
            this.page = page;
            this.x = x;
            this.y = y;
        }
    }

    private final List<Anchor> lineAnchors;
    private final Map<String, PdfXObject> images;
    private final Map<String, PdfFont> fonts;
    private final PdfDocument pdf;

    public PdfMusicCanvas(PdfDocument pdf) {
        lineAnchors = new ArrayList<>();
        images = new HashMap<>();
        fonts = new HashMap<>();
        this.pdf = pdf;
    }

    @Override
    public void addLine() {
        int pageNum = lineAnchors.size() / LINES_PER_PAGE;
        while (pdf.getNumberOfPages() < pageNum + 1) {
            pdf.addNewPage();
        }
        PdfPage page = pdf.getPage(pageNum + 1);

        float x = (page.getPageSize().getWidth() * (1f - LINE_WIDTH) * 0.5f) / STAVE_SPACING;
        float y;
        if (lineAnchors.isEmpty() || lineAnchors.get(lineAnchors.size() - 1).page != pageNum) {
            y = (page.getPageSize().getTop() - 50f) / STAVE_SPACING;
        }
        else {
            y = lineAnchors.get(lineAnchors.size() - 1).y - 15f;
        }

        Anchor newAnchor = new Anchor(pageNum, x, y);
        lineAnchors.add(newAnchor);
    }

    @Override
    public Anchor getAnchor(MusicalPosition musicalPosition) {
        return getAnchor(musicalPosition, new Pitch(8, 0));
    }

    @Override
    public Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch) {
        Anchor lineAnchor = lineAnchors.get(musicalPosition.line().getLineNumber());
        PdfPage page = pdf.getPage(lineAnchor.page + 1);

        return new Anchor(lineAnchor.page,
                lineAnchor.x + musicalPosition.crotchetsIntoLine()
                        * (LINE_WIDTH / musicalPosition.line().getLengthInCrotchets())
                        * page.getPageSize().getWidth() / STAVE_SPACING,
                lineAnchor.y + 0.5f * (pitch.rootStaveLine() - 8));
    }

    @Override
    public Anchor offsetAnchor(Anchor anchor, float x, float y) {
        return new Anchor(anchor.page, anchor.x + x, anchor.y + y);
    }

    @Override
    public void drawCircle(Anchor anchor, float x, float y, float r) {
        drawCircle(anchor, x, y, r, true);
    }

    @Override
    public void drawCircle(Anchor anchor, float x, float y, float r, boolean fill) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));
        canvas.setStrokeColor(ColorConstants.BLACK);
        canvas.setFillColor(ColorConstants.BLACK);
        canvas.setLineWidth(0.15f * STAVE_SPACING);
        canvas.circle((anchor.x + x) * STAVE_SPACING, (anchor.y + y) * STAVE_SPACING, r * STAVE_SPACING);
        if (fill) {
            canvas.fill();
        }
        else {
            canvas.stroke();
        }
    }

    @Override
    public void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth) {
        drawLine(anchor, x1, y1, x2, y2, lineWidth, Color.BLACK);
    }

    @Override
    public void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));

        canvas.setLineWidth(lineWidth * STAVE_SPACING);
        canvas.setStrokeColor(new DeviceRgb(color.getRed(), color.getGreen(), color.getBlue()));
        canvas.moveTo((anchor.x + x1) * STAVE_SPACING, (anchor.y + y1) * STAVE_SPACING)
                .lineTo((anchor.x + x2) * STAVE_SPACING, (anchor.y + y2) * STAVE_SPACING)
                .stroke();
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth) {
        drawLine(anchor1, x1, y1, anchor2, x2, y2, lineWidth, Color.BLACK);
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color) {
        // TODO: check anchors are on same line
        drawLine(anchor1, x1, y1, anchor2.x + x2 - anchor1.x, anchor2.y + y2 - anchor1.y, lineWidth, color);
    }

    @Override
    public void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(topLeftAnchor.page + 1));

        canvas.rectangle((topLeftAnchor.x + topLeftX) * STAVE_SPACING, (topLeftAnchor.y + topLeftY - height) * STAVE_SPACING,
                width * STAVE_SPACING, height * STAVE_SPACING).setFillColor(ColorConstants.WHITE).fill();
    }

    @Override
    public void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY,
                               Anchor bottomRightAnchor, float bottomRightX, float bottomRightY) {
        drawWhitespace(topLeftAnchor, topLeftX, topLeftY,
                (bottomRightAnchor.x + bottomRightX) - (topLeftAnchor.x + topLeftX),
                (topLeftAnchor.y + topLeftY) - (bottomRightAnchor.y + bottomRightY));
    }

    @Override
    public void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException {
        PdfXObject image;

        // cache image if it's the first time loading it, since images are usually reused
        if (!images.containsKey(fileName)) {
            FileInputStream imageFile = new FileInputStream(fileName);
            image = SvgConverter.convertToXObject(imageFile, pdf);
            imageFile.close();
            images.put(fileName, image);
        }
        else {
            image = images.get(fileName);
        }

        if (width == 0) {
            width = (height / image.getHeight()) * image.getWidth();
        }
        else if (height == 0) {
            height = (width / image.getWidth()) * image.getHeight();
        }

        PdfCanvas canvas = new PdfCanvas(pdf.getPage(topLeftAnchor.page + 1));
        canvas.addXObjectFittedIntoRectangle(image, new Rectangle(
                (topLeftAnchor.x + topLeftX) * STAVE_SPACING, (topLeftAnchor.y + topLeftY - height) * STAVE_SPACING,
                width * STAVE_SPACING, height * STAVE_SPACING
        ));
    }

    @Override
    public void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY,
                          Anchor bottomRightAnchor, float bottomRightX, float bottomRightY) throws IOException {
        drawImage(fileName, topLeftAnchor, topLeftX, topLeftY,
                (bottomRightAnchor.x + bottomRightX) - (topLeftAnchor.x + topLeftX),
                (topLeftAnchor.y + topLeftY) - (bottomRightAnchor.y + bottomRightY));
    }

    @Override
    public void drawEllipse(Anchor centre, float x, float y, float rx, float ry, boolean fill) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(centre.page + 1));
        canvas.setStrokeColor(ColorConstants.BLACK);
        canvas.setFillColor(ColorConstants.BLACK);
        canvas.setLineWidth(0.15f * STAVE_SPACING);

        canvas.ellipse(
                (centre.x + x - rx) * STAVE_SPACING,
                (centre.y + y - ry) * STAVE_SPACING,
                (centre.x + x + rx) * STAVE_SPACING,
                (centre.y + y + ry) * STAVE_SPACING
        );

        if (fill) {
            canvas.fill();
        }
        else {
            canvas.stroke();
        }
    }

    @Override
    public void drawBeam(Anchor left, float leftX, float leftY, Anchor right, float rightX, float rightY, float height) {
        drawBeam(left, leftX, leftY, right.x + rightX - left.x, right.y + rightY - left.y, height);
    }

    @Override
    public void drawBeam(Anchor anchor, float x1, float y1, float x2, float y2, float height) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));
        canvas.setFillColor(ColorConstants.BLACK);

        float leftXPos = (anchor.x + x1) * STAVE_SPACING;
        float rightXPos = (anchor.x + x2) * STAVE_SPACING;

        canvas.moveTo(leftXPos, (anchor.y + y1 - height / 2f) * STAVE_SPACING)
                .lineTo(leftXPos, (anchor.y + y1 + height / 2f) * STAVE_SPACING)
                .lineTo(rightXPos, (anchor.y + y2 + height / 2f) * STAVE_SPACING)
                .lineTo(rightXPos, (anchor.y + y2 - height / 2f) * STAVE_SPACING)
                .closePath().fill();
    }

    @Override
    public void drawArc(Anchor left, float leftX, float leftY, Anchor right, float rightX, float rightY, float height, float lineWidth) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(left.page + 1));
        canvas.setLineWidth(lineWidth * STAVE_SPACING);
        canvas.setStrokeColor(ColorConstants.BLACK);

        canvas.arc(
                (left.x + leftX) * STAVE_SPACING,
                (left.y + leftY) * STAVE_SPACING,
                (right.x + rightX) * STAVE_SPACING,
                (right.y + rightY - height) * STAVE_SPACING,
                50f, 80f
        ).stroke();
    }

    @Override
    public void drawText(String fileName, String text, float fontSize,
                         Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException {
        PdfFont font;

        // cache font if it's the first time loading it, since fonts are usually reused
        if (!fonts.containsKey(fileName)) {
            FontProgram program = FontProgramFactory.createFont(fileName);
            font = PdfFontFactory.createFont(program);
            fonts.put(fileName, font);
        }
        else {
            font = fonts.get(fileName);
        }

        PdfCanvas pdfCanvas = new PdfCanvas(pdf.getPage(topLeftAnchor.page + 1));
        Paragraph paragraph = new Paragraph(text);

        try (Canvas canvas = new Canvas(pdfCanvas, new Rectangle(
                (topLeftAnchor.x + topLeftX) * STAVE_SPACING,
                (topLeftAnchor.y + topLeftY - height) * STAVE_SPACING,
                width * STAVE_SPACING,
                height * STAVE_SPACING
        ))) {
            canvas.setFont(font);
            canvas.setFontSize(fontSize);
            canvas.add(paragraph);
        }
    }
}
