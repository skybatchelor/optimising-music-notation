package uk.ac.cam.optimisingmusicnotation.representation.staveelements.notemarkings;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;

public class NoteMarking extends BeamGroup {
    NoteMarking noteToMark;

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}
