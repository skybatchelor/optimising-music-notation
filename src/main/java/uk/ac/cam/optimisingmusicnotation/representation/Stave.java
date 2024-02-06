package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.List;

public class Stave implements Drawable {
    List<StaveElement> staveElements;
    List<Whitespace> whitespaces;
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}
