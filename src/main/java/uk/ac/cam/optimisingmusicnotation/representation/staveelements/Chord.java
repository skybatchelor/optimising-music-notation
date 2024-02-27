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

    public float getCrotchetsIntoLine() {
        return musicalPosition.crotchetsIntoLine();
    }

    public float getEndCrotchetsIntoLine() {
        return musicalPosition.crotchetsIntoLine() + durationInCrotchets;
    }

    protected final MusicalPosition musicalPosition;

    public float getDurationInCrotchets() {
        return durationInCrotchets;
    }

    protected final float durationInCrotchets;

    public NoteType getNoteType() {
        return noteType;
    }

    protected final NoteType noteType;
    protected final int dots;

    public Chord() {
        notes = new ArrayList<>();
        markings = new ArrayList<>();
        musicalPosition = new MusicalPosition(null, 0);
        durationInCrotchets = 0;
        noteType = NoteType.BREVE;
        dots = 0;
    }

    public Chord(List<Pitch> pitches, List<Accidental> accidentals, List<Boolean> tiesFrom, List<Boolean> tiesTo, MusicalPosition musicalPosition, float durationInCrochets, NoteType noteType, int dots, List<ChordMarking> markings) {
        notes = new ArrayList<>(pitches.size());
        for (int i = 0; i < pitches.size(); ++i) {
            notes.add(new Note(pitches.get(i), accidentals.get(i), tiesFrom.get(i), tiesTo.get(i)));
        }
        this.musicalPosition = musicalPosition;
        this.durationInCrotchets = durationInCrochets;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
    }

    public void removeTiesTo() {
        for (Note note : notes) {
            note.hasTieTo = false;
        }
    }

    private boolean dotted() {
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
        Anchor stemBeginning;
        Anchor stemEnd;
        stemBeginning = canvas.offsetAnchor(sign == 1 ? highestNoteheadAnchor : lowestNoteheadAnchor, 0, sign * .5f);
        stemEnd = canvas.offsetAnchor(stemBeginning, 0, sign * 3f);
        chordAnchors = new ChordAnchors<>(lowestNoteheadAnchor, highestNoteheadAnchor, stemEnd, 0, 0);
        chordAnchorsMap.put(this, chordAnchors);
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        int lowestLine = Integer.MAX_VALUE;
        int highestLine = Integer.MIN_VALUE;
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
            drawAccidental(canvas, note, anchor, false); // I feel like changing the accidental placement based on ledger lines goes against his desire for consistency?
        }
        drawLedgerLines(canvas, lowestLine, highestLine);

        if (drawStem) {
            drawStem(canvas, chordAnchors, RenderingConfiguration.upwardStems ? 1 : -1);
        }

        if (!markings.isEmpty()) {
            drawChordMarkings(canvas, chordAnchors.notehead());
            chordAnchors = updateNoteheadOffset(chordAnchors);
        }

        chordAnchorsMap.put(this,chordAnchors);
    }

    private <Anchor> void drawChordMarkings(MusicCanvas<Anchor> canvas, Anchor anchor) {
        float cumulatedYOffsetIncrease = 0;
        for (ChordMarking marking: markings) {
            marking.increaseYOffset(cumulatedYOffsetIncrease);
            marking.draw(canvas, anchor);
            cumulatedYOffsetIncrease += .8f;
        }
    }

    private <Anchor> ChordAnchors<Anchor> updateNoteheadOffset(ChordAnchors<Anchor> chordAnchors) {
        return chordAnchors.withNoteheadOffset(markings.get(markings.size() - 1).signedYOffset());
    }

    private <Anchor> void drawNotehead(MusicCanvas<Anchor> canvas, Anchor anchor, boolean fillInCircle) {
        canvas.drawCircle(anchor, 0, 0, .5f, fillInCircle);
    }

    private <Anchor> void drawStem(MusicCanvas<Anchor> canvas, ChordAnchors<Anchor> chordAnchors, int sign) {
        Anchor stemEnd = chordAnchors.stemEnd();
        Anchor stemBeginning = canvas.offsetAnchor(sign == 1 ? chordAnchors.highestNotehead() : chordAnchors.lowestNotehead(), 0, sign * .5f);
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
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0, 0)), -RenderingConfiguration.ledgerLineWidth/2f, 0f, RenderingConfiguration.ledgerLineWidth/2f, 0f, RenderingConfiguration.staveLineWidth);
        }
        for (int i = 10; i <= highestLine; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0,0)), -RenderingConfiguration.ledgerLineWidth/2f, 0f, RenderingConfiguration.ledgerLineWidth/2f, 0f, RenderingConfiguration.staveLineWidth);
        }
    }

    private <Anchor> void drawDots(MusicCanvas<Anchor> canvas, Anchor anchor) {
        for (int i = 0; i < dots; i++) {
            canvas.drawCircle(anchor, 0.5f + RenderingConfiguration.dotSpacing * (i + 1) + RenderingConfiguration.dotRadius * (2 * i + 1), 0, RenderingConfiguration.dotRadius);
        }
    }

    private <Anchor> void drawTie(MusicCanvas<Anchor> canvas, Note note, Anchor anchor) {
        int sign = RenderingConfiguration.upwardStems ? -1 : 1;
        if (note.hasTieFrom) {
            MusicalPosition endMusicalPosition = new MusicalPosition(musicalPosition.line(), musicalPosition.crotchetsIntoLine() + durationInCrotchets);
            Anchor endAnchor = canvas.getAnchor(endMusicalPosition, note.pitch);
            float Xoffset = .7f;
            float absoluteYOffset = .4f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(anchor, Xoffset, signedYOffset, endAnchor, -Xoffset, signedYOffset, .15f, !RenderingConfiguration.upwardStems);
        }
        if (note.hasTieTo) {
            MusicalPosition startMusicalPosition = new MusicalPosition(musicalPosition.line(), 0);
            Anchor startAnchor = canvas.getAnchor(startMusicalPosition, note.pitch);
            float Xoffset = .7f;
            float absoluteYOffset = .4f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(startAnchor, Xoffset, signedYOffset, anchor, -Xoffset, signedYOffset, .15f, !RenderingConfiguration.upwardStems);
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
