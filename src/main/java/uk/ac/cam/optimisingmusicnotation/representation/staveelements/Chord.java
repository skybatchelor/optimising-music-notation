package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.Flag;

import java.awt.*;
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
    protected float noteScale = 1f;

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

    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }
    
    public <Anchor> void computeAnchors(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        int lowestLine = 10000000;
        int highestLine = -10000000;
        ChordAnchors<Anchor> chordAnchors;
        Anchor lowestNoteheadAnchor = null;
        Accidental lowestAccidental = null;
        Anchor highestNoteheadAnchor = null;
        Accidental highestAccidental = null;
        for (Note note : notes) {
            // are notes sorted?
            Anchor noteheadAnchor = canvas.getAnchor(musicalPosition, note.pitch);
            // since I'm not sure, we'll find the lowest/highest anchor
            if (note.pitch.rootStaveLine() < lowestLine) {
                lowestLine = note.pitch.rootStaveLine();
                lowestNoteheadAnchor = noteheadAnchor;
                lowestAccidental = note.accidental;
            }
            if (note.pitch.rootStaveLine() > highestLine) {
                highestLine = note.pitch.rootStaveLine();
                highestNoteheadAnchor = noteheadAnchor;
                highestAccidental = note.accidental;
            }
        }
        int sign = RenderingConfiguration.upwardStems ? 1 : -1; // decide to draw the not stem upwards or downwards
        Anchor stemBeginning;
        Anchor stemEnd;
        stemBeginning = canvas.offsetAnchor(sign == 1 ? highestNoteheadAnchor : lowestNoteheadAnchor, 0, sign * RenderingConfiguration.noteheadRadius);
        stemEnd = canvas.offsetAnchor(stemBeginning, 0,
                sign * RenderingConfiguration.stemLength + 0.25f * (sign == 1 ? highestAccidental : lowestAccidental).getSemitoneOffset());
        chordAnchors = new ChordAnchors<>(lowestNoteheadAnchor, highestNoteheadAnchor, stemEnd, 0, 0);
        chordAnchorsMap.put(this, chordAnchors);
    }

    public static <Anchor> ChordAnchors<Anchor> computeAnchors(MusicCanvas<Anchor> canvas, Anchor anchor, float scaleFactor) {
        ChordAnchors<Anchor> chordAnchors;
        int sign = RenderingConfiguration.upwardStems ? 1 : -1; // decide to draw the not stem upwards or downwards
        Anchor stemBeginning = canvas.offsetAnchor(anchor, 0, sign * RenderingConfiguration.noteheadRadius * scaleFactor);
        Anchor stemEnd = canvas.offsetAnchor(stemBeginning, 0, sign * RenderingConfiguration.stemLength * scaleFactor);
        chordAnchors = new ChordAnchors<>(anchor, anchor, stemEnd, 0, 0);
        return chordAnchors;
    }

    public static <Anchor> void draw(MusicCanvas<Anchor> canvas, Anchor anchor, NoteType noteType, int dots, float timeScaleFactor, float scaleFactor) {
        ChordAnchors<Anchor> chordAnchors = computeAnchors(canvas, anchor, scaleFactor);

        boolean fillInCircle = noteType.defaultLengthInCrotchets <= 1;
        boolean drawStem = noteType.defaultLengthInCrotchets <= 2;

        if (drawStem) {
            drawStem(canvas, chordAnchors, RenderingConfiguration.upwardStems ? 1 : -1, scaleFactor);
        } else {
            drawNonStemWhiteSpace(canvas, chordAnchors, scaleFactor);
        }

        drawNotehead(canvas, anchor, noteType, fillInCircle, scaleFactor);
        drawDots(canvas, anchor, noteType, dots, false, scaleFactor);
        Flag.draw(canvas, chordAnchors, noteType, timeScaleFactor, scaleFactor);
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        int lowestLine = Integer.MAX_VALUE;
        int highestLine = Integer.MIN_VALUE;
        boolean fillInCircle = noteType.defaultLengthInCrotchets <= 1;
        boolean drawStem = noteType.defaultLengthInCrotchets <= 2;

        ChordAnchors<Anchor> chordAnchors;
        if (!chordAnchorsMap.containsKey(this)) {
            computeAnchors(canvas, chordAnchorsMap);
        }
        chordAnchors = chordAnchorsMap.get(this);

        if (drawStem) {
            drawStem(canvas, chordAnchors, RenderingConfiguration.upwardStems ? 1 : -1, noteScale);
        } else {
            drawNonStemWhiteSpace(canvas, chordAnchors, noteScale);
        }

        for (Note note : notes) {
            if (note.pitch.rootStaveLine() < lowestLine) {
                lowestLine = note.pitch.rootStaveLine();
            }
            if (note.pitch.rootStaveLine() > highestLine) {
                highestLine = note.pitch.rootStaveLine();
            }
            Anchor anchor = canvas.getAnchor(musicalPosition, note.pitch);
            drawTie(canvas, note, anchor);
            drawNotehead(canvas, anchor, noteType, fillInCircle, noteScale);
            drawDots(canvas, anchor, noteType, dots, note.pitch.rootStaveLine() % 2 == 0, noteScale);
            drawAccidental(canvas, note, anchor);
        }
        drawLedgerLines(canvas, lowestLine, highestLine);

        if (!markings.isEmpty()) {
            drawChordMarkings(canvas, chordAnchors.notehead());
            chordAnchors = updateNoteheadOffset(chordAnchors);
        }

        chordAnchorsMap.put(this, chordAnchors);
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

    private static float durationToStretch(NoteType noteType) {
        return switch (noteType) {
            case MAXIMA -> 1.55f;
            case BREVE -> 1.5f;
            case SEMIBREVE -> 1.4f;
            case MINIM -> 1.15f;
            case CROTCHET -> 1;
            case QUAVER -> .9f;
            case SQUAVER -> .85f;
            case DSQUAVER -> .825f;
            case HDSQUAVER -> .8125f;
        };
    }
    private static <Anchor> void drawNotehead(MusicCanvas<Anchor> canvas, Anchor anchor, NoteType noteType, boolean fillInCircle, float scaleFactor) {
//        canvas.drawCircle(anchor, 0, 0, .5f, fillInCircle);
        float r = RenderingConfiguration.noteheadRadius * scaleFactor;
        float k = durationToStretch(noteType);
        canvas.drawEllipse(anchor, 0,0, k * r, r, fillInCircle);

//        canvas.drawWhitespace(anchor, -RenderingConfiguration.stemWidth,
//                sign * RenderingConfiguration.gapHeight + sign * RenderingConfiguration.beamWidth / 2, 2 * RenderingConfiguration.stemWidth,
//                sign * RenderingConfiguration.gapHeight);
    }

    private static <Anchor> void drawNonStemWhiteSpace(MusicCanvas<Anchor> canvas, ChordAnchors<Anchor> chordAnchors, float scaleFactor) {
        canvas.drawLine(chordAnchors.lowestNotehead(), 0, (-RenderingConfiguration.noteheadRadius - RenderingConfiguration.gapHeight)  * scaleFactor,
                chordAnchors.highestNotehead(), 0, (RenderingConfiguration.noteheadRadius + RenderingConfiguration.gapHeight)  * scaleFactor,
                RenderingConfiguration.barLineWidth + 0.05f, Color.WHITE);// draw whitespace as white line to cover up pulse line
    }

    private static <Anchor> void drawStem(MusicCanvas<Anchor> canvas, ChordAnchors<Anchor> chordAnchors, int sign, float scaleFactor) {
        Anchor stemEnd = chordAnchors.stemEnd();
        Anchor stemBeginning = canvas.offsetAnchor(sign == -1 ? chordAnchors.highestNotehead() : chordAnchors.lowestNotehead(), 0, sign * RenderingConfiguration.noteheadRadius * scaleFactor);
        if (sign == 1) {
            if (canvas.isAnchorBelow(stemEnd, stemBeginning)) {
                stemBeginning = canvas.offsetAnchor(chordAnchors.lowestNotehead(), 0, -sign * RenderingConfiguration.noteheadRadius * scaleFactor);
                sign *= -1;
            }
        } else {
            if (canvas.isAnchorBelow(stemBeginning, stemEnd)) {
                stemBeginning = canvas.offsetAnchor(chordAnchors.highestNotehead(), 0, -sign * RenderingConfiguration.noteheadRadius * scaleFactor);
                sign *= -1;
            }
        }
        canvas.drawLine(chordAnchors.notehead(), 0, -sign * (RenderingConfiguration.noteheadRadius + RenderingConfiguration.gapHeight) * scaleFactor ,
                stemEnd, 0,  sign * (RenderingConfiguration.beamWidth / 2 + RenderingConfiguration.gapHeight) * scaleFactor,
                RenderingConfiguration.barLineWidth + 0.05f, Color.WHITE);// draw whitespace as white line to cover up pulse line
        canvas.drawLine(stemBeginning, 0, 0, stemEnd, 0,  sign * RenderingConfiguration.beamWidth * scaleFactor / 2,
                RenderingConfiguration.stemWidth * scaleFactor);// draw stem
    }
    private <Anchor> void drawAccidental(MusicCanvas<Anchor> canvas, Note note, Anchor anchor) {
        if (note.accidental != Accidental.NONE){
            String accidentalPath = RenderingConfiguration.imgFilePath + "/accidentals/" + note.accidental.toString().toLowerCase() + ".svg";
            float topLeftY = 1f + (note.accidental == Accidental.FLAT ? 0.5f : 0f);
            try{
                canvas.drawImage(accidentalPath, anchor,-1.65f, topLeftY,0.75f, 2f);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private <Anchor> void drawLedgerLines(MusicCanvas<Anchor> canvas, int lowestLine, int highestLine) {
        float width = RenderingConfiguration.ledgerLineWidth * durationToStretch(noteType);
        for (int i = (lowestLine / 2) * 2; i < 0; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0, 0)),
                    -width/2f, 0f, width/2f, 0f, RenderingConfiguration.staveLineWidth);
        }
        for (int i = 10; i <= highestLine; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0,0)),
                    -width/2f, 0f, width/2f, 0f, RenderingConfiguration.staveLineWidth);
        }
    }

    private static <Anchor> void drawDots(MusicCanvas<Anchor> canvas, Anchor anchor, NoteType noteType, int dots, boolean onLine, float scaleFactor) {
        float k = durationToStretch(noteType);
        for (int i = 0; i < dots; i++) {
            canvas.drawCircle(anchor, (RenderingConfiguration.noteheadRadius * k + RenderingConfiguration.dotSpacing
                    * (i + 1) + RenderingConfiguration.dotRadius * (2 * i + 1)) * scaleFactor, 0.5f * (onLine ? 1 : 0), RenderingConfiguration.dotRadius * scaleFactor);
        }
    }

    private <Anchor> void drawTie(MusicCanvas<Anchor> canvas, Note note, Anchor anchor) {
        int sign = RenderingConfiguration.upwardStems ? -1 : 1;
        if (note.hasTieFrom) {
            MusicalPosition endMusicalPosition = new MusicalPosition(musicalPosition.line(), musicalPosition.crotchetsIntoLine() + durationInCrotchets);
            Anchor endAnchor = canvas.getAnchor(endMusicalPosition, note.pitch);
            float xOffset = .7f;
            float absoluteYOffset = .4f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(anchor, xOffset, signedYOffset, endAnchor, -xOffset, signedYOffset, .15f, !RenderingConfiguration.upwardStems);
        }
        if (note.hasTieTo) {
            MusicalPosition startMusicalPosition = new MusicalPosition(musicalPosition.line(), 0);
            Anchor startAnchor = canvas.getAnchor(startMusicalPosition, note.pitch);
            float xOffset = .7f;
            float absoluteYOffset = .4f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(startAnchor, xOffset, signedYOffset, anchor, -xOffset, signedYOffset, .15f, !RenderingConfiguration.upwardStems);
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
