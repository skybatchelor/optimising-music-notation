package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

/**
 * A class for markings on a chord.
 */
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

    /**
     * Draws the chord marking.
     * @param canvas the canvas rendering the score
     * @param anchor the anchor of the notehead
     * @param <Anchor> the anchor type used by the score
     */
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {}
}
