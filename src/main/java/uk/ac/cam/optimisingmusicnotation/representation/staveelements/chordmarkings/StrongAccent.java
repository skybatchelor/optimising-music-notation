package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;

public class StrongAccent extends ChordMarking {
    // TODO: rename it to Marcato
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float l = .45f;
        float h = .25f;
        float w = .1f;
        float k = 1.1f;

        canvas.drawLine(anchor, -l, signedYOffset - h, 0, signedYOffset + h, w);
        canvas.drawLine(anchor, 0, signedYOffset + h, l, signedYOffset - h, k*w);
    }
}
