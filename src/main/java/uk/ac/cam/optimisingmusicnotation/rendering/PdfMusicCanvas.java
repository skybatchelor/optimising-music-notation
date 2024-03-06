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
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BiFunction;

public class PdfMusicCanvas implements MusicCanvas<PdfMusicCanvas.Anchor> {

    // TODO: make these configurable
    private final float LINE_WIDTH = 0.8f;
    // distance between stave lines - all coordinates have this as the unit
    private final float STAVE_SPACING = 5f;
    private final float TOP_MARGIN = 8f;
    private final float BOTTOM_MARGIN = 1f;
    private final float DEFAULT_LINE_HEIGHT = 5f;
    private final float SPACE_ABOVE_LINE = 1.6f;
    private final float RESERVED_HEIGHT_BELOW_LOWEST = 0.8f;

    private final float crotchetsPerLine;
    private final float leftOffset;

    // starting height of the next stave - moved further down when things are drawn below the current stave
    private float reservedHeight = TOP_MARGIN + SPACE_ABOVE_LINE;
    // "true" height of the next stave, usually the lowest anything has been drawn
    // for figuring out where to connect pulse lines to when extending them upwards
    private float trueHeight = TOP_MARGIN + SPACE_ABOVE_LINE;

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
    private final List<Anchor> lineTrueBottomAnchors;
    private final Map<String, PdfXObject> images;
    private final Map<String, PdfFont> fonts;
    private final PdfDocument pdf;

    public PdfMusicCanvas(PdfDocument pdf, float crotchetsPerLine, float centreOffset) {
        lineAnchors = new ArrayList<>();
        leftLineAnchors = new ArrayList<>();
        lineTrueBottomAnchors = new ArrayList<>();
        images = new HashMap<>();
        fonts = new HashMap<>();
        this.pdf = pdf;
        this.crotchetsPerLine = crotchetsPerLine;
        this.leftOffset = centreOffset;
    }

    @Override
    public void addFirstStave(float crotchetsOffset, int staveNumber) {
        if (lineAnchors.isEmpty()) {
            addFirstLineOnPage(0, crotchetsOffset);
        }
        else {
            // save previous line's reserved height
            Anchor previousLineAnchor = lineAnchors.get(lineAnchors.size() - 1);
            lineTrueBottomAnchors.add(new Anchor(previousLineAnchor.page, previousLineAnchor.x,
                    pdf.getPage(previousLineAnchor.page + 1).getPageSize().getTop() / STAVE_SPACING - trueHeight));

            int pageNum = previousLineAnchor.page;
            PdfPage page = pdf.getPage(pageNum + 1);

            float y = page.getPageSize().getTop() / STAVE_SPACING - reservedHeight - SPACE_ABOVE_LINE;
            if (y - SPACE_ABOVE_LINE * (staveNumber - 1) -
                    RenderingConfiguration.postLineHeight * 2 < page.getPageSize().getBottom() / STAVE_SPACING + BOTTOM_MARGIN) {
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
        trueHeight += DEFAULT_LINE_HEIGHT;
    }


    @Override
    public void addStave() {
        addStave(0f);
    }

    @Override
    public void addStave(float crotchetsOffset) {
        if (lineAnchors.isEmpty()) {
            addFirstLineOnPage(0, crotchetsOffset);
        }
        else {
            // save previous line's reserved height
            Anchor previousLineAnchor = lineAnchors.get(lineAnchors.size() - 1);
            lineTrueBottomAnchors.add(new Anchor(previousLineAnchor.page, previousLineAnchor.x,
                    pdf.getPage(previousLineAnchor.page + 1).getPageSize().getTop() / STAVE_SPACING - trueHeight));

            int pageNum = previousLineAnchor.page;
            PdfPage page = pdf.getPage(pageNum + 1);

            // check if we need to start a new page if we don't have enough space on the current page
            float y = page.getPageSize().getTop() / STAVE_SPACING - reservedHeight - SPACE_ABOVE_LINE;
            if (y < page.getPageSize().getBottom() / STAVE_SPACING + BOTTOM_MARGIN) {
                addFirstLineOnPage(pageNum + 1, crotchetsOffset);
            }
            else {
                float leftX = (page.getPageSize().getWidth() * (1f - LINE_WIDTH) * 0.5f) / STAVE_SPACING;
                leftLineAnchors.add(new Anchor(pageNum, leftX, y));

                float x = leftX + (page.getPageSize().getWidth() * (crotchetsOffset) * (LINE_WIDTH / crotchetsPerLine)) / STAVE_SPACING;
                lineAnchors.add(new Anchor(pageNum, x, y));
            }
        }

        // move reserved and true heights down to below the default height for each line
        reservedHeight += DEFAULT_LINE_HEIGHT;
        trueHeight += DEFAULT_LINE_HEIGHT;
    }

    private void addFirstLineOnPage(int pageNum, float crotchetsOffset) {
        while (pdf.getNumberOfPages() < pageNum + 1) {
            pdf.addNewPage();
            reservedHeight = TOP_MARGIN + SPACE_ABOVE_LINE;
            trueHeight = TOP_MARGIN + SPACE_ABOVE_LINE;
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
        Anchor lineAnchor = lineAnchors.get(
                musicalPosition.getIndex());
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
        Anchor lineAnchor = leftLineAnchors.get(musicalPosition.getIndex());
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
    public Anchor getLowestStaveLineStartOfLineAnchor(Line line, Stave stave) {
        return getAnchor(new MusicalPosition(line, stave, 0), new Pitch(0, 0, 0));
    }

    @Override
    public Anchor getStartOfLineAnchor(Line line, Stave stave) {
        return getAnchor(new MusicalPosition(line, stave, 0), new Pitch(8, 0, 0));
    }

    @Override
    public Anchor getEndOfLineAnchor(Line line, Stave stave) {
        return getAnchor(new MusicalPosition(line, stave, line.getLengthInCrotchets()), new Pitch(8, 0, 0));
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
        return new Anchor(anchorY.page, anchorX.x, anchorY.y);
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
    public Anchor getTrueBottomAnchor(int line) {
        return lineTrueBottomAnchors.get(line);
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
    public boolean areAnchorsOnSamePage(Anchor anchor1, Anchor anchor2) {
        return anchor1.page == anchor2.page;
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

        updateReservedHeight(anchor.page, anchor.y + y - r, false);
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
        PdfPage page = pdf.getPage(anchor.page + 1);
        PdfCanvas canvas = new PdfCanvas(page);

        // probably want to do this check elsewhere and for everything instead of just lines
        // don't let endpoints go off the bottom of the page
        float bottom = page.getPageSize().getBottom() + BOTTOM_MARGIN * STAVE_SPACING;
        float y1Pos = (anchor.y + y1) * STAVE_SPACING;
        if (y1Pos < bottom) {
            y1Pos = bottom;
        }
        float y2Pos = (anchor.y + y2) * STAVE_SPACING;
        if (y2Pos < bottom) {
            y2Pos = bottom;
        }

        canvas.setLineWidth(lineWidth * STAVE_SPACING);
        canvas.setStrokeColor(new DeviceRgb(color.getRed(), color.getGreen(), color.getBlue()));
        canvas.moveTo((anchor.x + x1) * STAVE_SPACING, y1Pos)
                .lineTo((anchor.x + x2) * STAVE_SPACING, y2Pos)
                .stroke();

        updateReservedHeight(anchor.page, anchor.y + y1, !reserveHeight);
        updateReservedHeight(anchor.page, anchor.y + y2, !reserveHeight);
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth) {
        drawLine(anchor1, x1, y1, anchor2, x2, y2, lineWidth, Color.BLACK, true);
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color) {
        drawLine(anchor1, x1, y1, anchor2, x2, y2, lineWidth, color, true);
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth, Color color, boolean reserveHeight) {
        if (anchor1.page == anchor2.page) {
            drawLine(anchor1, x1, y1, anchor2.x + x2 - anchor1.x, anchor2.y + y2 - anchor1.y, lineWidth, color, reserveHeight);
        }
    }

    @Override
    public void drawWhitespace(Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(topLeftAnchor.page + 1));

        canvas.rectangle((topLeftAnchor.x + topLeftX) * STAVE_SPACING, (topLeftAnchor.y + topLeftY - height) * STAVE_SPACING,
                width * STAVE_SPACING, height * STAVE_SPACING).setFillColor(ColorConstants.WHITE).fill();

        updateReservedHeight(topLeftAnchor.page, topLeftAnchor.y + topLeftY - height, false);
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
                    //System.out.println(fileName);
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

        // calculate width/height to preserve aspect ratio from original image rather than streching, if set to 0
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

        updateReservedHeight(topLeftAnchor.page, topLeftAnchor.y + topLeftY - height, false);
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

        updateReservedHeight(centre.page, centre.y + y - ry, false);
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

        updateReservedHeight(anchor.page, anchor.y + y1 - height, false);
        updateReservedHeight(anchor.page, anchor.y + y2 - height, false);
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

        updateReservedHeight(start.page, start.y + startY + height, false);
        updateReservedHeight(end.page, end.y + endY + height, false);
    }

    @Override
    public void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                         Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height)
            throws IOException {
        drawText(fileName, text, fontSize, alignment, topLeftAnchor, topLeftX, topLeftY, width, height, Color.BLACK);

    }

    @Override
    public void drawText(String fileName, String text, float fontSize, TextAlignment alignment,
                         Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height, Color color)
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
            canvas.setFontColor(new DeviceRgb(color.getRed(), color.getGreen(), color.getBlue()));

            switch (alignment) {
                case LEFT -> canvas.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.LEFT);
                case CENTRE -> canvas.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
                case RIGHT -> canvas.setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT);
            }
            canvas.add(paragraph);
        }

        updateReservedHeight(topLeftAnchor.page, topLeftAnchor.y + topLeftY - height, false);
    }

    // call at the end of every drawing method to extend reserved height for low elements
    private void updateReservedHeight(int pageNum, float y, boolean onlyUpdateTrueHeight) {
        Rectangle pageSize = pdf.getPage(pageNum + 1).getPageSize();
        float newHeight = pageSize.getTop() / STAVE_SPACING - y;
        if (!onlyUpdateTrueHeight && newHeight + RESERVED_HEIGHT_BELOW_LOWEST > reservedHeight) {
            reservedHeight = newHeight + RESERVED_HEIGHT_BELOW_LOWEST;
        }
        if (newHeight > trueHeight) {
            trueHeight = newHeight;
        }
    }
}
