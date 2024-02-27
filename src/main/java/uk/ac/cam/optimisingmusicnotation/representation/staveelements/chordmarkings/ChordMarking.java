package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public abstract class ChordMarking {
    protected float absoluteYOffset = 1.3f;
    public float signedYOffset() {
        return RenderingConfiguration.upwardStems ? -absoluteYOffset : absoluteYOffset;
    }
    public void increaseYOffset(float y) {
        absoluteYOffset += y;
    }
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {}
}
