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

    public Chord(List<Pitch> pitches, List<Accidental> accidentals, List<Boolean> tiesFrom, List<Boolean> tiesTo, MusicalPosition musicalPosition, float durationInCrochets, NoteType noteType, int dots, List<ChordMarking> markings) {
        notes = new ArrayList<>(pitches.size());
        for (int i = 0; i < pitches.size(); ++i) {
            notes.add(new Note(pitches.get(i), accidentals.get(i), tiesFrom.get(i), tiesTo.get(i)));
        }
        this.musicalPosition = musicalPosition;
        this.durationInCrochets = durationInCrochets;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
    }

    public void removeTiesTo() {
        for (Note note : notes) {
            note.hasTieTo = false;
        }
    }

    private boolean dotted(){
        return dots > 0;
    }

    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }
    
    public <Anchor> void computeAnchors(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        int lowestLine = 10000000;
        int highestLine = -10000000;
        ChordAnchors<Anchor> chordAnchors;
        Anchor lowestNoteheadAnchor = null;
        Anchor highestNoteheadAnchor = null;
        for (Note note : notes) {
            // are notes sorted?
            Anchor noteheadAnchor = canvas.getAnchor(musicalPosition, note.pitch);
            // since i'm not sure, we'll find the lowest/highest anchor
            if (note.pitch.rootStaveLine() < lowestLine) {
                lowestLine = note.pitch.rootStaveLine();
                lowestNoteheadAnchor = noteheadAnchor;
            }
            if (note.pitch.rootStaveLine() > highestLine) {
                highestLine = note.pitch.rootStaveLine();
                highestNoteheadAnchor = noteheadAnchor;
            }
        }
        int sign = RenderingConfiguration.upwardStems ? 1 : -1; // decide to draw the not stem upwards or downwards
        boolean drawStem = noteType.defaultLengthInCrotchets <= 2;
        if (drawStem) {
            Anchor stemBeginning;
            Anchor stemEnd;
            if (sign == 1) {
                stemBeginning = canvas.offsetAnchor(highestNoteheadAnchor, 0,
                        sign * .5f);
                stemEnd = canvas.offsetAnchor(stemBeginning, 0, sign * 3f);
            } else {
                stemBeginning = canvas.offsetAnchor(lowestNoteheadAnchor, 0, sign * .5f);
                stemEnd = canvas.offsetAnchor(stemBeginning, 0, sign * 3f);
            }
            chordAnchors = new ChordAnchors<>(lowestNoteheadAnchor, stemEnd, 0, 0);
        } else {
            chordAnchors = new ChordAnchors<>(lowestNoteheadAnchor, null, 0, 0);
        }
        chordAnchorsMap.put(this, chordAnchors);
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        int lowestLine = 10000000;
        int highestLine = -10000000;
        int sign = RenderingConfiguration.upwardStems ? 1 : -1; // decide to draw the not stem upwards or downwards
        boolean fillInCircle = noteType.defaultLengthInCrotchets <= 1;
        boolean drawStem = noteType.defaultLengthInCrotchets <= 2;

        boolean hasLedgerLines;

        ChordAnchors<Anchor> chordAnchors;
        if (!chordAnchorsMap.containsKey(this)) {
            computeAnchors(canvas, chordAnchorsMap);
        }
        chordAnchors = chordAnchorsMap.get(this);
        for (Note note : notes) {
            if (note.pitch.rootStaveLine() < lowestLine) {
                lowestLine = note.pitch.rootStaveLine();
            }
            if (note.pitch.rootStaveLine() > highestLine) {
                highestLine = note.pitch.rootStaveLine();
            }
            Anchor anchor = canvas.getAnchor(musicalPosition, note.pitch);
            drawTie(canvas, note, anchor);
            drawNotehead(canvas, anchor, fillInCircle);
            drawDots(canvas, anchor);
            hasLedgerLines = hasLedgerLines(note);
            drawAccidental(canvas, note, anchor, true); // I feel like changing the accidental placement based on ledger lines goes against his desire for consistency?
        }
        drawLedgerLines(canvas, lowestLine, highestLine);

        if (drawStem) {
            drawStem(canvas, chordAnchors, RenderingConfiguration.upwardStems ? 1 : -1);
        }
        chordAnchorsMap.put(this,chordAnchors);
    }

    private <Anchor> void drawNotehead(MusicCanvas<Anchor> canvas, Anchor anchor, boolean fillInCircle) {
        canvas.drawCircle(anchor, 0, 0, .5f, fillInCircle); // draw note head [!need to adjust on noteType]
    }
    private <Anchor> void drawStem(MusicCanvas<Anchor> canvas, ChordAnchors<Anchor> chordAnchors, int sign) {
        Anchor stemEnd = chordAnchors.stemEnd();
        Anchor stemBeginning = canvas.offsetAnchor(stemEnd, 0, -sign * 3f);
        canvas.drawLine(stemBeginning, 0, 0, stemEnd, 0, 0, RenderingConfiguration.stemWidth);// draw stem
        // draw bit of whitespace to separate from pulse line
        canvas.drawWhitespace(stemEnd, -RenderingConfiguration.stemWidth,
                sign * RenderingConfiguration.gapHeight, 2 * RenderingConfiguration.stemWidth,
                RenderingConfiguration.gapHeight);
    }
    private <Anchor> void drawAccidental(MusicCanvas<Anchor> canvas, Note note, Anchor anchor, boolean hasLedgerLines) {
        if (note.accidental != Accidental.NONE){
            String accidentalPath = RenderingConfiguration.imgFilePath + "/accidentals/" + note.accidental.toString().toLowerCase() + ".svg";
            float topLeftY = 1f + (note.accidental == Accidental.FLAT ? 0.5f : 0f);
            try{
                canvas.drawImage(accidentalPath, anchor,-(1.65f + (hasLedgerLines ? 0.25f : 0f)), topLeftY,0.75f, 2f);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private boolean hasLedgerLines(Note note) {
        return note.pitch.rootStaveLine() < 0 || note.pitch.rootStaveLine() >= 10;
    }
    private <Anchor> void drawLedgerLines(MusicCanvas<Anchor> canvas, int lowestLine, int highestLine) {
        for (int i = (lowestLine / 2) * 2; i < 0; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0)), -RenderingConfiguration.ledgerLineWidth/2f, 0f, RenderingConfiguration.ledgerLineWidth/2f, 0f, .2f);
        }
        for (int i = 10; i <= highestLine; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0)), -RenderingConfiguration.ledgerLineWidth/2f, 0f, RenderingConfiguration.ledgerLineWidth/2f, 0f, .2f);
        }
    }

    private <Anchor> void drawDots(MusicCanvas<Anchor> canvas, Anchor anchor){
        if (dotted()) {
            canvas.drawCircle(anchor, 1f, 0, .2f);
        }
    }

    private <Anchor> void drawTie(MusicCanvas<Anchor> canvas, Note note, Anchor anchor) {
        if (note.hasTieFrom) {
            MusicalPosition endMusicalPosition = new MusicalPosition(musicalPosition.line(), musicalPosition.crotchetsIntoLine() + durationInCrochets);
            Anchor endAnchor = canvas.getAnchor(endMusicalPosition, note.pitch);
            int sign = RenderingConfiguration.upwardStems ? 1 : -1;
            float Xoffset = .2f;
            float absoluteYOffset = .1f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(anchor, Xoffset, signedYOffset, endAnchor, -Xoffset, signedYOffset, .15f, RenderingConfiguration.upwardStems);
        }
        if (note.hasTieTo) {
            MusicalPosition startMusicalPosition = new MusicalPosition(musicalPosition.line(), 0);
            Anchor startAnchor = canvas.getAnchor(startMusicalPosition, note.pitch);
            int sign = RenderingConfiguration.upwardStems ? 1 : -1;
            float Xoffset = .2f;
            float absoluteYOffset = .1f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(startAnchor, Xoffset, signedYOffset, anchor, -Xoffset, signedYOffset, .15f, RenderingConfiguration.upwardStems);
        }
    }


    private static class Note {
        Pitch pitch;
        Accidental accidental;

        boolean hasTieFrom;

        boolean hasTieTo;

        public Note(Pitch pitch, Accidental accidental, boolean hasTieFrom, boolean hasTieTo) {
            this.pitch = pitch;
            this.accidental = accidental;
            this.hasTieFrom = hasTieFrom;
            this.hasTieTo = hasTieTo;
        }
    }
}
