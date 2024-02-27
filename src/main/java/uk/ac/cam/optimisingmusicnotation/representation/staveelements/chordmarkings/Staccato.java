package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;

public class Staccato extends ChordMarking {
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float r = .06f;
        canvas.drawCircle(anchor, 0, signedYOffset, r);
    }
}
