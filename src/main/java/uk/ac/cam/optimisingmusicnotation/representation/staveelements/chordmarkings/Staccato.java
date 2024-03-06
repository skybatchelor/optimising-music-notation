package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;

/**
 * Represents a staccato on a chord.
 */
public class Staccato extends ChordMarking {
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float r = .15f;

        canvas.drawCircle(anchor, 0, signedYOffset(), r, true);
    }
}
