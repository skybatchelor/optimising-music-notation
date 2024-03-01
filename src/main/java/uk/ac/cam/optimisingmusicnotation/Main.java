package uk.ac.cam.optimisingmusicnotation;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import uk.ac.cam.optimisingmusicnotation.mxlparser.Parser;
import uk.ac.cam.optimisingmusicnotation.rendering.PdfMusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Part;
import uk.ac.cam.optimisingmusicnotation.representation.Score;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        //arg 0: path of source mxl
        //arg 1: path of target (pdf)
        if (args.length != 2){
            System.err.println("Needs 2 arguments");
            throw new RuntimeException();
        }
        String target = args[0];
        Object mxl;
        try {
            mxl = Parser.openMXL(target);
        } catch (IOException e) {
            System.err.println("Source file not found");
            throw new RuntimeException();
        }
        Score score = Parser.parseToScore(mxl);
        if (score == null) {
            System.err.println("xml parsing failed");
        } else {
            String outTarget = !args[1].equals("") ? args[1] : score.getWorkTitle();
            for (int i = 0; i < score.getParts().size(); ++i) {
                drawPartToPDF(outTarget, i, score);
            }
        }
    }

    private static void drawPartToPDF(String outTarget, int partIndex, Score score) {
        Part part = score.getParts().get(partIndex);
        try (PdfWriter writer = new PdfWriter(outTarget + "_" + score.getPartFilename(partIndex) + ".pdf")) {
            PdfDocument pdf = new PdfDocument(writer);
            PageSize ps = PageSize.A4;
            pdf.addNewPage(ps);
            PdfMusicCanvas canvas = new PdfMusicCanvas(pdf, part.getMaxWidth(), part.getMinOffset());
            part.draw(canvas, score.getWorkTitle(), score.getComposer());
            pdf.close();
        }
        catch (FileNotFoundException e) {
            System.err.println("Target directory does not exist");
            throw new RuntimeException();

        } catch (IOException e) {
            System.err.println("Error while creating PDF for part \"" + part.getName() + "\". Close all output files before converting to PDF.");
        }
    }
}

