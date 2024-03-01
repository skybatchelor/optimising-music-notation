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
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;

public class PdfMusicCanvas implements MusicCanvas<PdfMusicCanvas.Anchor> {

    // TODO: make these configurable
    private final float LINE_WIDTH = 0.8f;
    private final float STAVE_SPACING = 5f;
    private final float MARGIN = 10f;
    private final float DEFAULT_LINE_HEIGHT = 15f;
    private final float SPACE_ABOVE_LINE = 5f;

    private final float crotchetsPerLine;
    private final float leftOffset;

    private float reservedHeight = MARGIN;

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
    private final List<Anchor> leftLineAnchors;
    private final Map<String, PdfXObject> images;
    private final Map<String, PdfFont> fonts;
    private final PdfDocument pdf;

    public PdfMusicCanvas(PdfDocument pdf, float crotchetsPerLine, float centreOffset) {
        lineAnchors = new ArrayList<>();
        leftLineAnchors = new ArrayList<>();
        images = new HashMap<>();
        fonts = new HashMap<>();
        this.pdf = pdf;
        this.crotchetsPerLine = crotchetsPerLine;
        this.leftOffset = centreOffset;
    }

    @Override
    public void addLine() {
        addLine(0f);
    }

    @Override
    public void addLine(float crotchetsOffset) {
        if (lineAnchors.isEmpty()) {
            addFirstLineOnPage(0, crotchetsOffset);
        }
        else {
            int pageNum = lineAnchors.get(lineAnchors.size() - 1).page;
            PdfPage page = pdf.getPage(pageNum + 1);

            float y = page.getPageSize().getTop() / STAVE_SPACING - reservedHeight - SPACE_ABOVE_LINE;
            if (y < page.getPageSize().getBottom() / STAVE_SPACING + MARGIN) {
                addFirstLineOnPage(pageNum + 1, crotchetsOffset);
            }
            else {
                float leftX = (page.getPageSize().getWidth() * (1f - LINE_WIDTH) * 0.5f) / STAVE_SPACING;
                leftLineAnchors.add(new Anchor(pageNum, leftX, y));

                float x = leftX + (page.getPageSize().getWidth() * (crotchetsOffset) * (LINE_WIDTH / crotchetsPerLine)) / STAVE_SPACING;
                lineAnchors.add(new Anchor(pageNum, x, y));
            }
        }

        reservedHeight += DEFAULT_LINE_HEIGHT;
    }

    private void addFirstLineOnPage(int pageNum, float crotchetsOffset) {
        while (pdf.getNumberOfPages() < pageNum + 1) {
            pdf.addNewPage();
            reservedHeight = MARGIN;
        }

        PdfPage page = pdf.getPage(pageNum + 1);
        float y = page.getPageSize().getTop() / STAVE_SPACING - reservedHeight - SPACE_ABOVE_LINE;

        float leftX = (page.getPageSize().getWidth() * (1f - LINE_WIDTH) * 0.5f) / STAVE_SPACING;
        leftLineAnchors.add(new Anchor(pageNum, leftX, y));

        float x = leftX + (page.getPageSize().getWidth() * (crotchetsOffset) * (LINE_WIDTH / crotchetsPerLine)) / STAVE_SPACING;
        lineAnchors.add(new Anchor(pageNum, x, y));
    }

    @Override
    public void reserveHeight(float height) {
        reservedHeight += height;
    }

    @Override
    public Anchor getAnchor(MusicalPosition musicalPosition) {
        return getAnchor(musicalPosition, new Pitch(8, 0, 0));
    }

    @Override
    public Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch) {
        Anchor lineAnchor = lineAnchors.get(musicalPosition.line().getLineNumber());
        PdfPage page = pdf.getPage(lineAnchor.page + 1);

        return new Anchor(lineAnchor.page,
                lineAnchor.x + (musicalPosition.crotchetsIntoLine() - leftOffset)
                        * (LINE_WIDTH / crotchetsPerLine)
                        * page.getPageSize().getWidth() / STAVE_SPACING,
                lineAnchor.y + 0.5f * (pitch.rootStaveLine() - 8));
    }

    @Override
    public Anchor getLineStartAnchor(MusicalPosition musicalPosition) {
        return getLineStartAnchor(musicalPosition, new Pitch(8,0, 0));
    }

    @Override
    public Anchor getLineStartAnchor(MusicalPosition musicalPosition, Pitch pitch) {
        Anchor lineAnchor = leftLineAnchors.get(musicalPosition.line().getLineNumber());
        PdfPage page = pdf.getPage(lineAnchor.page + 1);

        return new Anchor(lineAnchor.page,
                lineAnchor.x,
                lineAnchor.y + 0.5f * (pitch.rootStaveLine() - 8));
    }

    @Override
    public Anchor getLowestStaveLineAnchor(MusicalPosition musicalPosition) {
        return getAnchor(musicalPosition, new Pitch(0, 0, 0));
    }

    @Override
    public Anchor getLowestStaveLineStartOfLineAnchor(Line line) {
        return getAnchor(new MusicalPosition(line, 0), new Pitch(0, 0, 0));
    }

    @Override
    public Anchor getStartOfLineAnchor(Line line) {
        return getAnchor(new MusicalPosition(line, 0), new Pitch(8, 0, 0));
    }

    @Override
    public Anchor getEndOfLineAnchor(Line line) {
        return getAnchor(new MusicalPosition(line, line.getLengthInCrotchets()), new Pitch(8, 0, 0));
    }

    @Override
    public Anchor offsetAnchor(Anchor anchor, float x, float y) {
        return new Anchor(anchor.page, anchor.x + x, anchor.y + y);
    }

    @Override
    public Anchor interpolateAnchors(Anchor anchor1, Anchor anchor2, float t) {
        return new Anchor(anchor1.page, (1 - t) * anchor1.x + t * anchor2.x, (1 - t) * anchor1.y + t * anchor2.y);
    }

    @Override
    public Anchor getTakeXTakeYAnchor(Anchor anchorX, Anchor anchorY) {
        return new Anchor(anchorX.page, anchorX.x, anchorY.y);
    }
    @Override
    public Anchor topLeftAnchor() {
        Rectangle pageSize = pdf.getPage(1).getPageSize();
        return new Anchor(0, pageSize.getLeft() / STAVE_SPACING,
                pageSize.getTop() / STAVE_SPACING);
    }

    @Override
    public Anchor topCentreAnchor() {
        Rectangle pageSize = pdf.getPage(1).getPageSize();
        return new Anchor(0, (pageSize.getLeft() + pageSize.getRight()) / (2f * STAVE_SPACING),
                pageSize.getTop() / STAVE_SPACING);
    }

    @Override
    public Anchor topRightAnchor() {
        Rectangle pageSize = pdf.getPage(1).getPageSize();
        return new Anchor(0, pageSize.getRight() / STAVE_SPACING,
                pageSize.getTop() / STAVE_SPACING);
    }

    @Override
    public boolean isAnchorBelow(Anchor anchor1, Anchor anchor2) {
        return anchor1.y < anchor2.y;
    }

    @Override
    public boolean isAnchorAbove(Anchor anchor1, Anchor anchor2) {
        return anchor1.y > anchor2.y;
    }

    @Override
    public Anchor getMinAnchor(List<Anchor> anchors, Anchor start, BiFunction<Anchor, Anchor, Boolean> lessThan) {
        for (Anchor anchor : anchors) {
            if (lessThan.apply(anchor, start)) {
                start = anchor;
            }
        }
        return start;
    }

    @Override
    public void drawCircle(Anchor anchor, float x, float y, float r) {
        drawCircle(anchor, x, y, r, true);
    }

    @Override
    public void drawCircle(Anchor anchor, float x, float y, float r, boolean fill) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));
        canvas.setStrokeColor(ColorConstants.BLACK);
        canvas.setFillColor(fill ? ColorConstants.BLACK : ColorConstants.WHITE);
        canvas.setLineWidth(0.15f * STAVE_SPACING);
        canvas.circle((anchor.x + x) * STAVE_SPACING, (anchor.y + y) * STAVE_SPACING, (r - 0.075f) * STAVE_SPACING);
        canvas.fillStroke();

        updateReservedHeight(anchor.page, anchor.y + y - r);
    }

    @Override
    public void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth) {
        drawLine(anchor, x1, y1, x2, y2, lineWidth, Color.BLACK, true);
    }

    @Override
    public void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color) {
        drawLine(anchor, x1, y1, x2, y2, lineWidth, color, true);
    }

    @Override
    public void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth, Color color, boolean reserveHeight) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));

        canvas.setLineWidth(lineWidth * STAVE_SPACING);
        canvas.setStrokeColor(new DeviceRgb(color.getRed(), color.getGreen(), color.getBlue()));
        canvas.moveTo((anchor.x + x1) * STAVE_SPACING, (anchor.y + y1) * STAVE_SPACING)
                .lineTo((anchor.x + x2) * STAVE_SPACING, (anchor.y + y2) * STAVE_SPACING)
                .stroke();

        if (reserveHeight) {
            updateReservedHeight(anchor.page, anchor.y + y1);
            updateReservedHeight(anchor.page, anchor.y + y2);
        }
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth) {
        drawLine(anchor1, x1, y1, anchor2, x2, y2, lineWidth, Color.BLACK, true);
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color) {
        drawLine(anchor1, x1, y1, anchor2.x + x2 - anchor1.x, anchor2.y + y2 - anchor1.y, lineWidth, color, true);
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color, boolean reserveHeight) {
        drawLine(anchor1, x1, y1, anchor2.x + x2 - anchor1.x, anchor2.y + y2 - anchor1.y, lineWidth, color, reserveHeight);
    }

    @Override
    public void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(topLeftAnchor.page + 1));

        canvas.rectangle((topLeftAnchor.x + topLeftX) * STAVE_SPACING, (topLeftAnchor.y + topLeftY - height) * STAVE_SPACING,
                width * STAVE_SPACING, height * STAVE_SPACING).setFillColor(ColorConstants.WHITE).fill();

        updateReservedHeight(topLeftAnchor.page, topLeftAnchor.y + topLeftY - height);
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
            try (InputStream in = getClass().getResourceAsStream(fileName)) {
                if (in != null) {
                    image = SvgConverter.convertToXObject(in, pdf);
                }
                else {
                    System.out.println(fileName);
                    FileInputStream imageFile = new FileInputStream(fileName);
                    image = SvgConverter.convertToXObject(imageFile, pdf);
                    imageFile.close();
                }
            }

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

        updateReservedHeight(topLeftAnchor.page, topLeftAnchor.y + topLeftY - height);
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
        canvas.setFillColor(fill ? ColorConstants.BLACK : ColorConstants.WHITE);
        canvas.setLineWidth(0.15f * STAVE_SPACING);

        canvas.ellipse(
                (centre.x + x - rx + 0.075f) * STAVE_SPACING,
                (centre.y + y - ry + 0.075f) * STAVE_SPACING,
                (centre.x + x + rx - 0.075f) * STAVE_SPACING,
                (centre.y + y + ry - 0.075f) * STAVE_SPACING
        );
        canvas.fillStroke();

        updateReservedHeight(centre.page, centre.y + y - ry);
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

        updateReservedHeight(anchor.page, anchor.y + y1 - height);
        updateReservedHeight(anchor.page, anchor.y + y2 - height);
    }

    @Override
    public void drawCurve(Anchor start, float startX, float startY, Anchor end, float endX, float endY, float lineWidth, boolean up) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(start.page + 1));
        canvas.setLineWidth(lineWidth * STAVE_SPACING);
        canvas.setStrokeColor(ColorConstants.BLACK);

        float startXPos = (start.x + startX) * STAVE_SPACING;
        float startYPos = (start.y + startY) * STAVE_SPACING;
        float endXPos = (end.x + endX) * STAVE_SPACING;
        float endYPos = (end.y + endY) * STAVE_SPACING;

        float width = Math.abs(startXPos - endXPos);
        float height = 0.6f * (float)Math.sqrt(width) * (up ? 1f : -1f);

        canvas.moveTo(startXPos, startYPos)
                .curveTo(startXPos, startYPos + height, endXPos, endYPos + height, endXPos, endYPos)
                .stroke();

        updateReservedHeight(start.page, start.y + startY + height);
        updateReservedHeight(end.page, end.y + endY + height);
    }

    @Override
    public void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
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
            canvas.setFontColor(ColorConstants.BLACK);
            switch (alignment) {
                case LEFT -> canvas.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT);
                case CENTRE -> canvas.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
                case RIGHT -> canvas.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT);
            }
            canvas.add(paragraph);
        }

        updateReservedHeight(topLeftAnchor.page, topLeftAnchor.y + topLeftY - height);
    }

    private void updateReservedHeight(int pageNum, float y) {
        Rectangle pageSize = pdf.getPage(pageNum + 1).getPageSize();
        float newHeight = pageSize.getTop() / STAVE_SPACING - (y - 3f);
        if (newHeight > reservedHeight) {
            reservedHeight = newHeight;
        }
    }
}
