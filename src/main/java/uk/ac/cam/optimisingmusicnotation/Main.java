package uk.ac.cam.optimisingmusicnotation;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import uk.ac.cam.optimisingmusicnotation.rendering.PdfMusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Piece;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
        String outDir = "./out/"; // Output Directory
        Path outDirPath = Paths.get(outDir);
        if (!Files.exists(outDirPath)) {
            try {
                Files.createDirectory(outDirPath);
            } catch (IOException e) {
                System.err.println("Error while creating output directory path: ");
                e.printStackTrace();
            }
        }

        try (PdfWriter writer = new PdfWriter(outDir + "test.pdf")) {
            PdfDocument pdf = new PdfDocument(writer);
            PageSize ps = PageSize.A4;
            pdf.addNewPage(ps);

            PdfMusicCanvas canvas = new PdfMusicCanvas(pdf);
            Piece testPiece = new Piece();
            testPiece.draw(canvas,new RenderingConfiguration());
//
//            PdfMusicCanvas.Anchor anchor1 = canvas.getAnchor(new MusicalPosition(null, 0));
//            PdfMusicCanvas.Anchor anchor2 = canvas.getAnchor(new MusicalPosition(null, 16));
//
//            for (int i = 0; i < 5; i++) {
//                canvas.drawLine(anchor1, -2f, -i, anchor2, 2f, -i, 0.1f);
//            }
//
//            PdfMusicCanvas.Anchor anchor = canvas.getAnchor(new MusicalPosition(null, 0), new Pitch(0, 1));
//            canvas.drawCircle(anchor, 0f, 0f, 0.5f);
//            anchor = canvas.getAnchor(new MusicalPosition(null, 1), new Pitch(1, 1));
//            canvas.drawCircle(anchor, 0f, 0f, 0.5f);
//            anchor = canvas.getAnchor(new MusicalPosition(null, 1.5f), new Pitch(3, 1));
//            canvas.drawCircle(anchor, 0f, 0f, 0.5f);
//            anchor = canvas.getAnchor(new MusicalPosition(null, 2), new Pitch(4, 1));
//            canvas.drawCircle(anchor, 0f, 0f, 0.5f);
//
//            anchor1 = canvas.getAnchor(new MusicalPosition(null, 2.5f), new Pitch(10, 0));
//            anchor2 = canvas.getAnchor(new MusicalPosition(null, 4.0f), new Pitch(-1, 0));
//            canvas.drawWhitespace(anchor1, 0f, 0f, anchor2, 0f, 0f);

            pdf.close();
        }
        catch (IOException e) {
            System.err.println("Error while creating PDF: ");
            e.printStackTrace();
        }
    }
}
