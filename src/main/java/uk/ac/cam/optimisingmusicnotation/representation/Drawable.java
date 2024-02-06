package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public interface Drawable {
    <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config);
}
