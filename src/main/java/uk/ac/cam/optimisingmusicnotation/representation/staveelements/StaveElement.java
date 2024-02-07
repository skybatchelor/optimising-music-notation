package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public interface StaveElement {
    <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config);
}
