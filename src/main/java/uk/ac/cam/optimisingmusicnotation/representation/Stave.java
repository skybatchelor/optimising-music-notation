package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.Canvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.List;

public class Stave implements Drawable {
    List<StaveElement> staveElements;
    List<Whitespace> whitespaces;
    @Override
    public void Draw(Canvas canvas, RenderingConfiguration config) {

    }
}
