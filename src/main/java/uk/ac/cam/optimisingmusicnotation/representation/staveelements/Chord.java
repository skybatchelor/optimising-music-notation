package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;

import java.util.ArrayList;
import java.util.List;

public abstract class Chord extends BeamGroup {
    protected final List<Note> notes;
    protected final List<ChordMarking> markings;

    public Chord(){
        notes = new ArrayList<>();
        markings = new ArrayList<>();
    };
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }

    private static abstract class Note {
        Pitch pitch;
        Accidental accidental;
    }
}
