package uk.ac.cam.optimisingmusicnotation.rendering;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;

import java.util.ArrayList;

public class PdfMusicCanvas implements MusicCanvas<PdfMusicCanvas.Anchor> {

    // TODO: make these configurable
    private final float LINE_WIDTH = 0.8f;
    private final int CROTCHETS_PER_LINE = 16;
    private final float CROTCHET_WIDTH = LINE_WIDTH / CROTCHETS_PER_LINE;
    private final float STAVE_SPACING = 5f;

    public static class Anchor {

        private final int page;
        private final float x, y;

        private Anchor(int page, float x, float y) {
            this.page = page;
            this.x = x;
            this.y = y;
        }
    }

    private final ArrayList<Anchor> lineAnchors;
    private final PdfDocument pdf;

    public PdfMusicCanvas(PdfDocument pdf) {
        PdfPage page = pdf.getPage(1);

        lineAnchors = new ArrayList<>();
        lineAnchors.add(new Anchor(0, (page.getPageSize().getWidth() * (1f - LINE_WIDTH) * 0.5f) / STAVE_SPACING,
                (page.getPageSize().getTop() - 30f) / STAVE_SPACING));
        this.pdf = pdf;
    }

    @Override
    public Anchor getAnchor(MusicalPosition musicalPosition) {
        // TODO: check how Pitch works
        return getAnchor(musicalPosition, new Pitch(0, 0));
    }

    @Override
    public Anchor getAnchor(MusicalPosition musicalPosition, Pitch pitch) {
        // TODO: ask about line numbers
        Anchor lineAnchor = lineAnchors.get(0);
        PdfPage page = pdf.getPage(lineAnchor.page + 1);

        return new Anchor(lineAnchor.page,
                lineAnchor.x + musicalPosition.crotchetsIntoLine() * CROTCHET_WIDTH * page.getPageSize().getWidth() / STAVE_SPACING,
                lineAnchor.y + (pitch.rootStaveLine() + 0.5f * pitch.semitonesAbove()));
    }

    @Override
    public void drawCircle(Anchor anchor, float x, float y, float r) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));
        canvas.circle((anchor.x + x) * STAVE_SPACING, (anchor.y + y) * STAVE_SPACING, r * STAVE_SPACING).fill();
    }

    @Override
    public void drawLine(Anchor anchor, float x1, float y1, float x2, float y2, float lineWidth) {
        PdfCanvas canvas = new PdfCanvas(pdf.getPage(anchor.page + 1));

        canvas.setLineWidth(lineWidth * STAVE_SPACING);
        canvas.moveTo((anchor.x + x1) * STAVE_SPACING, (anchor.y + y1) * STAVE_SPACING)
                .lineTo((anchor.x + x2) * STAVE_SPACING, (anchor.y + y2) * STAVE_SPACING)
                .stroke();
    }

    @Override
    public void drawLine(Anchor anchor1, float x1, float y1, Anchor anchor2, float x2, float y2, float lineWidth) {
        // TODO: check anchors are on same line
        drawLine(anchor1, x1, y1, anchor2.x + x2 - anchor1.x, anchor2.y + y2 - anchor1.y, lineWidth);
    }

    @Override
    public void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY, float width, float height) {

    }

    @Override
    public void drawImage(String fileName, Anchor topLeftAnchor, float topLeftX, float topLeftY,
                          Anchor bottomRightAnchor, float bottomRightX, float bottomRightY) {

    }
}
