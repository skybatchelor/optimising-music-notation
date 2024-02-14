package uk.ac.cam.optimisingmusicnotation.representation.beatlines;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public interface PulseLine {
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config);
}
