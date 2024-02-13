package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Clef;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.ArrayList;
import java.util.List;

public class Stave {
    private final List<StaveElement> staveElements;
    private final List<Whitespace> whitespaces;
    public Stave(){
        staveElements = new ArrayList<>();
        whitespaces = new ArrayList<>();
    }
    public void addStaveElements(StaveElement staveElement) {
        staveElements.add(staveElement);
    }
    public void addWhiteSpace(Whitespace whitespace) {
        whitespaces.add(whitespace);
    }
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line, RenderingConfiguration config) {
        drawStaveLines(canvas, line);
        for (Whitespace w : whitespaces) {
            w.draw(canvas, line, config);
        }
        for (StaveElement s : staveElements) {
            s.draw(canvas, config);

        }
    }

    private <Anchor> void drawStaveLines(MusicCanvas<Anchor> canvas, Line line){
        Anchor anchor1 = canvas.getAnchor(new MusicalPosition(line, 0));
        Anchor anchor2 = canvas.getAnchor(new MusicalPosition(line, line.getLengthInCrotchets()));

        for (int i = 0; i < 5; i++) {
            canvas.drawLine(anchor1, -1f, -i, anchor2, 2f, -i, 0.1f);
        }
    }

    public <Anchor> void drawPreStaveLines(MusicCanvas<Anchor> canvas, Line line){
        Anchor anchor1 = canvas.getAnchor(new MusicalPosition(line, 0));
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(anchor1, -8f, -i, -2f, -i, 0.1f);
        }
    }
}
