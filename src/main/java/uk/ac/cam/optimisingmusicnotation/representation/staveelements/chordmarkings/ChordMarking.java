package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public abstract class ChordMarking {
    protected float absoluteYOffset = 0;
    // signedYOffset is needed for updating the offset in chordAnchors
    public float signedYOffset() {
        return RenderingConfiguration.upwardStems ? -absoluteYOffset : absoluteYOffset;
    }
    public void increaseYOffset(float y) {
        absoluteYOffset += y;
    }
    // takes in the Notehead anchor. Only change the offset when drawing articulations
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {}
}
