package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public abstract class ChordMarking {
    float absoluteYOffset = 1.35f;
    float signedYOffset = RenderingConfiguration.upwardStems ? absoluteYOffset : -absoluteYOffset;
    <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {}
}
