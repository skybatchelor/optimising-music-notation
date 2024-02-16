package uk.ac.cam.optimisingmusicnotation.representation.staveelements.annotations;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;

public abstract class Annotation implements StaveElement {
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {

    }
}
