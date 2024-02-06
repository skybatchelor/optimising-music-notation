package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

public class Note extends Chord {
    Pitch pitch;
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}