package uk.ac.cam.optimisingmusicnotation.rendering;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfXObject;
import com.itextpdf.svg.converter.SvgConverter;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.awt.*;
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
    private final int LINES_PER_PAGE = 8;
    private final float[] STAVE_POS = { 0f, 1f, 2.33f, 3.33f, 4.67f };

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
    private final PdfDocument pdf;

    public PdfMusicCanvas(PdfDocument pdf) {
        lineAnchors = new ArrayList<>();
        images = new HashMap<>();
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
            y = lineAnchors.get(lineAnchors.size() - 1).y - 18f;
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

        // TODO: make variable stave line spacing more flexible
        float stavePos = STAVE_POS[(pitch.rootStaveLine() / 2) % 5];
        float staveSpacing = STAVE_POS[(pitch.rootStaveLine() / 2 + 4) % 5] - stavePos;

        return new Anchor(lineAnchor.page,
                lineAnchor.x + musicalPosition.crotchetsIntoLine()
                        * (LINE_WIDTH / musicalPosition.line().getLengthInCrotchets())
                        * page.getPageSize().getWidth() / STAVE_SPACING,
                lineAnchor.y - 6f + stavePos + staveSpacing * 0.5f * (pitch.rootStaveLine() % 2)
                        + (float)(pitch.rootStaveLine() / 10) * 6f);
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
}
