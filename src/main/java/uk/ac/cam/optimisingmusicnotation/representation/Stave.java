package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.ArrayList;
import java.util.List;

public class Stave {
    private final List<StaveElement> staveElements;
    private final List<Whitespace> whitespaces;
  
    public Stave() {
        staveElements = new ArrayList<>();
        whitespaces = new ArrayList<>();
    }

    public Stave(List<StaveElement> staveElements, List<Whitespace> whitespaces) {
        this.staveElements = staveElements;
        this.whitespaces = whitespaces;
    }
  
    public void addWhiteSpace(Whitespace whitespace) {
        whitespaces.add(whitespace);
    }
  
    public void addStaveElement(StaveElement staveElement) {
        staveElements.add(staveElement);
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Line line) {
        drawStaveLines(canvas, line);
        for (Whitespace w : whitespaces) {
            w.draw(canvas, line);
        }
        for (StaveElement s : staveElements) {
            s.draw(canvas);

        }
    }

    private <Anchor> void drawStaveLines(MusicCanvas<Anchor> canvas, Line line){
        for (int i = 0; i < 5; i++) {
            Pitch pitch = new Pitch(i*2, 0);
            Anchor anchor1 = canvas.getAnchor(new MusicalPosition(line, 0), pitch);
            Anchor anchor2 = canvas.getAnchor(new MusicalPosition(line, line.getLengthInCrotchets()), pitch);
            canvas.drawLine(anchor1, -1f, 0, anchor2, 2f, 0, 0.1f);
        }
    }

    public <Anchor> void drawPreStaveLines(MusicCanvas<Anchor> canvas, Line line){
        Anchor anchor1 = canvas.getAnchor(new MusicalPosition(line, 0));
        for (int i = 0; i < 5; i++) {
            canvas.drawLine(anchor1, -8f, -i, -2f, -i, 0.1f);
        }
    }
}
