package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.List;

public class Stave {
    List<StaveElement> staveElements;
    List<Whitespace> whitespaces;

    Integer lineNumber;

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        // TODO draw bars (stave lines)

        for (Whitespace w : whitespaces) {
            w.draw(canvas, config);
        }
        for (StaveElement s : staveElements) {
            s.draw(canvas, config);

        }
    }
}
