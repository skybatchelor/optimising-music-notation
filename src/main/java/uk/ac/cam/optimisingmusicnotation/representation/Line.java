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
  
    public Integer getLineNumber() {
        return lineNumber;
    }

    private final Integer lineNumber;
  
    public float getLengthInCrotchets() {
        return lengthInCrotchets;
    }
  
    private final float lengthInCrotchets;
  
    private final float offsetInCrochets;

    public Line(Integer lineNumber, int testType){
        staves = new ArrayList<>();
        lengthInCrotchets = 16;
        offsetInCrochets = 0;
        this.lineNumber = lineNumber;
        Stave stave = new Stave();
        if (testType == 1){
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 0, 1)); // test note
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 1, 2));
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 2, 3));
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 3, 4));
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 4, 5));
                stave.addWhiteSpace(new Rest(new MusicalPosition(this, 4f), new MusicalPosition(this, 7f)));// test white space
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 8, 5)); // test note
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 9, 4));
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 10, 3));
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 1, 11, 2));
                stave.addStaveElements(getTestChord(NoteType.CROTCHET, 4, 12, 1));
            }
        staves.add(stave);
    }
  
    public Line(List<Stave> staves, float lengthInCrochets, float offsetInCrochets) {
        this.staves = staves;
        this.lengthInCrotchets = lengthInCrochets;
        this.offsetInCrochets = offsetInCrochets;
    }
  

    /* A test function for getting chord */
    private Chord getTestChord() {
        List<Pitch> pitches = new ArrayList<>();
        List<Accidental> accidentals = new ArrayList<>();

        pitches.add(new Pitch(0, 0));
        accidentals.add(Accidental.NONE);

        MusicalPosition musicalPosition = new MusicalPosition(this, 0);
        float durationInCrochets = 1f;
        NoteType noteType = NoteType.CROTCHET;
        return new Chord(pitches, accidentals, musicalPosition, durationInCrochets, noteType);
    }
  
    private Chord getTestChord(NoteType type, float durationInCrochets, float crochetsIntoLine, int rootStaveLine) {
        List<Pitch> pitches = new ArrayList<>();
        List<Accidental> accidentals = new ArrayList<>();

        pitches.add(new Pitch(rootStaveLine, 0));
        accidentals.add(Accidental.NONE);

        MusicalPosition musicalPosition = new MusicalPosition(this, crochetsIntoLine);
        NoteType noteType = type;
        return new Chord(pitches, accidentals, musicalPosition, durationInCrochets, noteType);
    }
    /* A test function for getting chord */

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (int i = 0; i <= lengthInCrotchets; i++) {
            MusicalPosition startPosition = new MusicalPosition(this, i);
            Anchor startAnchor = canvas.getAnchor(startPosition);
            canvas.drawLine(startAnchor,0f,2f,0f,0f,RenderingConfiguration.pulseLineWidth);
        }
        for (Stave s: staves){
            s.draw(canvas,this, config);
        }
    }
}
