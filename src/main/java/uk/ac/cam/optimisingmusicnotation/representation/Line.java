package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Rest;

import java.util.ArrayList;
import java.util.List;

public class Line {
    public List<Stave> getStaves() {
        return staves;
    }

    private final List<Stave> staves;

    private final Integer lineNumber;
    private final Integer lengthInCrotchets;

    public Line(Integer lineNumber){
        staves = new ArrayList<>();
        lengthInCrotchets = 16;
        this.lineNumber = lineNumber;
        Stave stave = new Stave();
        stave.addStaveElements(getTestChord()); // test note
        stave.addWhiteSpace(new Rest(new MusicalPosition(this, 2), new MusicalPosition(this, 5)));// test white space
        staves.add(stave);

    }

    /* A test function for getting chord */
    private Chord getTestChord() {
        List<Pitch> pitches = new ArrayList<>();
        List<Accidental> accidentals = new ArrayList<>();

        pitches.add(new Pitch(0, 0));
        accidentals.add(Accidental.NONE);

        MusicalPosition musicalPosition = new MusicalPosition(this, 1);
        float durationInCrochets = 1f;
        NoteType noteType = NoteType.CROTCHET;
        return new Chord(pitches, accidentals, musicalPosition, durationInCrochets, noteType);
    }
    /* A test function for getting chord */

    public Integer getLineNumber() {
        return lineNumber;
    }

    public Integer getLengthInCrotchets() {
        return lengthInCrotchets;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        // TODO draw pulse lines
        for (Stave s: staves){
            s.draw(canvas,this, config);
        }
    }
}
