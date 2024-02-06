package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.notemarkings.NoteMarking;

import java.util.List;

public class Chord extends NoteMarking {
    List<Note> notes;
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}
