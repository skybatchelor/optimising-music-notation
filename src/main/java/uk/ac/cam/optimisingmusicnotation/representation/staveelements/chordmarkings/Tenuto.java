package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;

public class Tenuto extends ChordMarking {
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float l = .45f;
        float w = .15f;

        canvas.drawLine(anchor, -l, signedYOffset(), l, signedYOffset(), w);
    }
}
