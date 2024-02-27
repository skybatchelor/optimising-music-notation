package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class StrongAccent extends ChordMarking {
    // TODO: rename it to Marcato
    protected float amendYOffset = .5f;
    @Override
    public float signedYOffset() {
        return RenderingConfiguration.upwardStems ? -absoluteYOffset-amendYOffset : absoluteYOffset+amendYOffset;
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float l = .4f;
        float h = .7f;
        float w = .1f;

        int sign = RenderingConfiguration.upwardStems ? 1 : -1;

        canvas.drawLine(anchor, -l, signedYOffset() + sign * h, 0, signedYOffset() - sign * h, w);
        canvas.drawLine(anchor, 0, signedYOffset() - sign * h, l, signedYOffset() + sign * h, w);
    }
}
