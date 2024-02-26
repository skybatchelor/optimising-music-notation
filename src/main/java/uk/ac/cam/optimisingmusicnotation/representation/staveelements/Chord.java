package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Chord extends BeamGroup {
    protected final List<Note> notes;
    protected final List<ChordMarking> markings;
    protected final MusicalPosition musicalPosition;
    protected final float durationInCrochets;
    protected final NoteType noteType;
    protected final int dots;

    public Chord() {
        notes = new ArrayList<>();
        markings = new ArrayList<>();
        musicalPosition = new MusicalPosition(null, 0);
        durationInCrochets = 0;
        noteType = NoteType.BREVE;
        dots = 0;
    }

    public Chord(List<Pitch> pitches, List<Accidental> accidentals, MusicalPosition musicalPosition, float durationInCrochets, NoteType noteType, int dots, List<ChordMarking> markings) {
        notes = new ArrayList<>(pitches.size());
        for (int i = 0; i < pitches.size(); ++i) {
            notes.add(new Note(pitches.get(i), accidentals.get(i)));
        }
        this.musicalPosition = musicalPosition;
        this.durationInCrochets = durationInCrochets;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
    }

    public void addMarking(ChordMarking marking) {
        markings.add(marking);
    }

    private boolean dotted(){
        return dots > 0;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        ChordAnchors<Anchor> chordAnchors = null;
        for (Note note: notes) {
            Anchor anchor = canvas.getAnchor(musicalPosition, note.pitch);
            drawNotehead(canvas,note);
            drawStem(canvas,note,RenderingConfiguration.upwardStems ? 1 : -1);
            drawDots(canvas,anchor);
            drawAccidental(canvas,note,anchor);
            drawLedgerLines(canvas,note);
        }
        chordAnchorsMap.put(this,chordAnchors);
    }

    private <Anchor> void drawNotehead(MusicCanvas<Anchor> canvas, Note note){
        boolean fillInCircle = noteType.defaultLengthInCrotchets <= 1;
        canvas.drawCircle(canvas.getAnchor(musicalPosition, note.pitch), 0, 0, .5f, fillInCircle); // draw note head [!need to adjust on noteType]
    }
    private <Anchor> void drawStem(MusicCanvas<Anchor> canvas, Note note, int sign){
        if (noteType.defaultLengthInCrotchets <= 2) {
            Anchor bottomOfStem = canvas.offsetAnchor(canvas.getAnchor(musicalPosition, note.pitch), 0,
                    sign * .5f);
            Anchor topOfStem = canvas.offsetAnchor(bottomOfStem, 0, sign * 3f);
            canvas.drawLine(bottomOfStem, 0, 0, topOfStem, 0, 0, RenderingConfiguration.stemWidth);// draw stem
            // draw a bit of whitespace to separate from pulse line
            canvas.drawWhitespace(topOfStem, -RenderingConfiguration.stemWidth,
                    sign * RenderingConfiguration.gapHeight, 2 * RenderingConfiguration.stemWidth,
                    RenderingConfiguration.gapHeight);
        }
    }
    private <Anchor> void drawAccidental(MusicCanvas<Anchor> canvas, Note note, Anchor anchor){
        if (note.accidental != Accidental.NONE){
            String accidentalPath = "img/accidentals/" + note.accidental.toString().toLowerCase() + ".svg";
            try{
                canvas.drawImage(accidentalPath, anchor,-1.75f, 1f,0.75f, 2f);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private <Anchor> void drawLedgerLines(MusicCanvas<Anchor> canvas, Note note){
        int lowestLine = note.pitch.rootStaveLine();
        int highestLine = note.pitch.rootStaveLine();
        for (int i = lowestLine / 2; i < 0; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0)), -1f, 0f, 1f, 0f, .2f);
        }
        for (int i = 10; i <= highestLine; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0)), -1f, 0f, 1f, 0f, .2f);
        }}

    private <Anchor> void drawDots(MusicCanvas<Anchor> canvas, Anchor anchor){
        if (dotted()) {
            canvas.drawCircle(anchor, 1f, 0, .2f);
        }
    }


    private static class Note {
        Pitch pitch;
        Accidental accidental;

        public Note(Pitch pitch, Accidental accidental) {
            this.pitch = pitch;
            this.accidental = accidental;
        }
    }
}
