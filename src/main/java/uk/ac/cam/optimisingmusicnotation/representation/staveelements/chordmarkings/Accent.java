package uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class Accent extends ChordMarking {
    protected float amendYOffset = .25f;
    @Override
    public float signedYOffset() {
        return RenderingConfiguration.upwardStems ? -absoluteYOffset-amendYOffset : absoluteYOffset+amendYOffset;
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float l = .7f;
        float h = .4f;
        float w = .1f;

        canvas.drawLine(anchor, -l, signedYOffset() + h, l, signedYOffset(), w);
        canvas.drawLine(anchor, -l, signedYOffset() - h, l, signedYOffset(), w);
    }
}
