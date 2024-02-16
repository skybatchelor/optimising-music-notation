package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;

public interface StaveElement {
    <Anchor> void draw(MusicCanvas<Anchor> canvas);
}
