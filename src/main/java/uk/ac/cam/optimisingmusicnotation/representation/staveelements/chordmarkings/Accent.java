package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;

/**
 * Represents an accent on a chord.
 */
public class Accent extends ChordMarking {
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float l = .7f;
        float h = .4f;
        float w = .1f;

        canvas.drawLine(anchor, -l, signedYOffset() + h, l, signedYOffset(), w);
        canvas.drawLine(anchor, -l, signedYOffset() - h, l, signedYOffset(), w);
    }
}
