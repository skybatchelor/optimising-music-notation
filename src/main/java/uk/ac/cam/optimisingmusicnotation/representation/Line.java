package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BarLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BeatLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.PulseLine;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
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
    public List<PulseLine> getPulseLines() {
        return pulseLines;
    }


    public void addPulseLine(PulseLine pulseLine) {
        pulseLines.add(pulseLine);
    }

    private final List<PulseLine> pulseLines;

    public int getLineNumber() {
        return lineNumber;
    }

    private final int lineNumber;
  
    public float getLengthInCrotchets() {
        return lengthInCrotchets;
    }
  
    private final float lengthInCrotchets;
  
    private final float offsetInCrochets;


    public Line(int lineNumber, int testType){
        staves = new ArrayList<>();
        lengthInCrotchets = 16;
        offsetInCrochets = 0;
        this.lineNumber = lineNumber;
        Stave stave = new Stave();
        if (testType == 1){
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 0, 1)); // test note
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 1, 2));
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 2, 3));
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 3, 4));
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 4, 5));
                stave.addWhiteSpace(new Rest(new MusicalPosition(this, 4f), new MusicalPosition(this, 7f)));// test white space
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 8, 5)); // test note
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1, 9, 4));
                stave.addStaveElement(getTestChord(NoteType.CROTCHET, 1.5f, 10, 3));
                stave.addStaveElement(getTestChord(NoteType.MINIM, 2, 12, 2));
                stave.addStaveElement(getTestChord(NoteType.SEMIBREVE, 4, 14, 1));
            }
        staves.add(stave);
        pulseLines = new ArrayList<>();
        for (int i = 0; i < lengthInCrotchets + 1; i++) {
            pulseLines.add(new BeatLine(new MusicalPosition(this, i), 1));
        }
    }
  
    public Line(List<Stave> staves, float lengthInCrochets, float offsetInCrochets, int lineNumber) {
        this.staves = staves;
        this.lineNumber = lineNumber;
        this.lengthInCrotchets = lengthInCrochets;
        this.offsetInCrochets = offsetInCrochets;
        pulseLines = new ArrayList<>();
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
        return new Chord(pitches, accidentals, musicalPosition, durationInCrochets, noteType, 0, new ArrayList<>());
    }
  
    private Chord getTestChord(NoteType type, float durationInCrochets, float crochetsIntoLine, int rootStaveLine) {
        List<Pitch> pitches = new ArrayList<>();
        List<Accidental> accidentals = new ArrayList<>();

        pitches.add(new Pitch(rootStaveLine, 0));
        accidentals.add(Accidental.NONE);

        MusicalPosition musicalPosition = new MusicalPosition(this, crochetsIntoLine);
        NoteType noteType = type;
        return new Chord(pitches, accidentals, musicalPosition, durationInCrochets, noteType, 0, new ArrayList<>());
    }
    /* A test function for getting chord */

    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {
        // for (int i = 0; i <= lengthInCrotchets; i++) {
        //     MusicalPosition startPosition = new MusicalPosition(this, i);
        //     Anchor startAnchor = canvas.getAnchor(startPosition);
        //     canvas.drawLine(startAnchor,0f,2f,0f,0f,RenderingConfiguration.pulseLineWidth);
        // }
        for (PulseLine p: pulseLines) {
            p.drawAboveStave(canvas);
        }
        for (Stave s: staves){
            s.draw(canvas,this);
        }
        drawTimeSignatures(canvas);
    }

    private <Anchor> void drawTimeSignatures(MusicCanvas<Anchor> canvas){
        TimeSignature lastTimeSig = null;
        TimeSignature currentTimeSig;
        for (PulseLine p: pulseLines) {
            if (p instanceof BarLine){
                currentTimeSig = ((BarLine) p).getTimeSignature();
                if (currentTimeSig != null && !(currentTimeSig.equals(lastTimeSig))){
                    lastTimeSig = ((BarLine) p).getTimeSignature();
                    lastTimeSig.draw(canvas,p.getMusicalPosition());
                }
            }
        }
    }
}