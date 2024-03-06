package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.*;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.StrongAccent;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.LeftBeamSegment;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A collection of notes as a chord.
 */
public class Chord implements StaveElement {
    private final List<Note> notes;
    private final List<ChordMarking> markings;

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

    protected boolean render = true;

    public Chord() {
        notes = new ArrayList<>();
        markings = new ArrayList<>();
        musicalPosition = new MusicalPosition(null, null, 0);
        durationInCrotchets = 0;
        noteType = NoteType.BREVE;
        dots = 0;
    }

    public Chord(List<Pitch> pitches, List<Accidental> accidentals, List<Boolean> tiesFrom, List<Boolean> tiesTo, float noteScale,
                 MusicalPosition musicalPosition, float durationInCrotchets, NoteType noteType, int dots, List<ChordMarking> markings) {
        notes = new ArrayList<>(pitches.size());
        for (int i = 0; i < pitches.size(); ++i) {
            notes.add(new Note(pitches.get(i), accidentals.get(i), tiesFrom.get(i), tiesTo.get(i)));
        }
        this.noteScale = noteScale;
        this.musicalPosition = musicalPosition;
        this.durationInCrotchets = durationInCrotchets;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
    }

    private Chord(List<Note> notes, MusicalPosition musicalPosition, float durationInCrotchets, NoteType noteType, int dots, List<ChordMarking> markings, boolean render) {
        this.notes = notes;
        this.musicalPosition = musicalPosition;
        this.durationInCrotchets = durationInCrotchets;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
        this.render = render;
    }

    /**
     * Generates a copy of this chord which is positioned on the next line and is not drawn.
     * @param nextLine the next line
     * @return a chord on nextLine which will not be drawn
     */
    public Chord moveToNextLine(Line nextLine) {
        return new Chord(notes, new MusicalPosition(nextLine,
                musicalPosition.stave(),
                musicalPosition.crotchetsIntoLine() - musicalPosition.line().getLengthInCrotchets()),
                durationInCrotchets,
                noteType,
                dots,
                markings,
                false);
    }

    /**
     * Generates a copy of this chord which is positioned on the previous line and is not drawn.
     * @param prevLine the previous line
     * @return a chord on prevLine which will not be drawn
     */
    public Chord moveToPrevLine(Line prevLine) {
        return new Chord(notes, new MusicalPosition(prevLine,
                musicalPosition.stave(),
                musicalPosition.crotchetsIntoLine() + prevLine.getLengthInCrotchets()),
                durationInCrotchets,
                noteType,
                dots,
                markings,
                false);
    }

    /**
     * Removes all pre tie segments. Used to remove pre ties for notes which do not start lines.
     */
    public void removeTiesTo() {
        for (Note note : notes) {
            note.hasTieTo = false;
        }
    }

    /**
     * Returns the {@link MusicalPosition} of this chord.
     * @return the musical position of the chord
     */
    public MusicalPosition getMusicalPosition() {
        return musicalPosition;
    }

    /**
     * Computes the anchor placements for this chord, assuming that the stems are not affected by a beam.
     * @param canvas the canvas rendering the score
     * @param chordAnchorsMap the anchor map to put the anchors into
     * @param <Anchor> the anchor type the canvas uses
     */
    public <Anchor> void computeAnchors(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        int lowestLine = Integer.MAX_VALUE;
        int highestLine = Integer.MIN_VALUE;
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

    /**
     * Computes the anchors for a note, based on an anchor for the notehead.
     * @param canvas the canvas rendering the score
     * @param anchor the anchor for the notehead
     * @param scaleFactor the scale factor for the note
     * @return the anchors for the note
     * @param <Anchor> the anchor type used by the canvas
     */
    public static <Anchor> ChordAnchors<Anchor> computeAnchors(MusicCanvas<Anchor> canvas, Anchor anchor, float scaleFactor) {
        ChordAnchors<Anchor> chordAnchors;
        int sign = RenderingConfiguration.upwardStems ? 1 : -1; // decide to draw the not stem upwards or downwards
        Anchor stemBeginning = canvas.offsetAnchor(anchor, 0, sign * RenderingConfiguration.noteheadRadius * scaleFactor);
        Anchor stemEnd = canvas.offsetAnchor(stemBeginning, 0, sign * RenderingConfiguration.stemLength * scaleFactor);
        chordAnchors = new ChordAnchors<>(anchor, anchor, stemEnd, 0, 0);
        return chordAnchors;
    }

    /**
     * Statically draws a chord, with a notehead positioned at the given anchor.
     * Primarily used by {@link uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.TempoMarking}.
     * @param canvas the canvas rendering the score
     * @param anchor the anchor for the notehead
     * @param noteType the type of note being rendered
     * @param dots how many dots the note needs
     * @param timeScaleFactor the timescale factor, used for the flags
     * @param scaleFactor the scale factor of the note
     * @param <Anchor> the anchor type used by the canvas
     */
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
        LeftBeamSegment.draw(canvas, chordAnchors, noteType, timeScaleFactor, scaleFactor);
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

        if (!render) return;

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
        drawLedgerLines(canvas, lowestLine, highestLine, noteScale);

        if (!markings.isEmpty()) {
            int edgeLine = RenderingConfiguration.upwardStems ? lowestLine : highestLine;

            if (edgeLine % 2 == 1) {
                drawChordMarkings(canvas, chordAnchors.notehead(), 1);
            }
            else {
                drawChordMarkings(canvas, chordAnchors.notehead(),1.5f);
            }
            chordAnchors = updateNoteheadOffset(chordAnchors);
        }

        chordAnchorsMap.put(this, chordAnchors);
    }

    private <Anchor> void drawChordMarkings(MusicCanvas<Anchor> canvas, Anchor noteAnchor, float cumulatedYOffsetIncrease) {

        for (ChordMarking marking: markings) {
            if (!(marking instanceof StrongAccent)) {
                marking.increaseYOffset(cumulatedYOffsetIncrease);
                marking.draw(canvas, noteAnchor);
                cumulatedYOffsetIncrease += 1; // increase by one for each articulation
            }
        }
        // high anchor and low anchor for drawing strong accent (Marcato)
        Anchor highAnchor = canvas.getAnchor(musicalPosition, new Pitch(9, 0, 0));
        Anchor lowAnchor = canvas.getAnchor(musicalPosition, new Pitch(-1, 0, 0));

        float signedYOffsetIncrease = RenderingConfiguration.upwardStems ? -cumulatedYOffsetIncrease : cumulatedYOffsetIncrease;
        Anchor offsetNoteAnchor = canvas.offsetAnchor(noteAnchor, 0, signedYOffsetIncrease);

        highAnchor = canvas.isAnchorAbove(highAnchor, offsetNoteAnchor) ? highAnchor : offsetNoteAnchor;
        lowAnchor = canvas.isAnchorBelow(lowAnchor, offsetNoteAnchor) ? lowAnchor : offsetNoteAnchor;

        Anchor edgeAnchor = RenderingConfiguration.upwardStems ? lowAnchor : highAnchor;

        for (ChordMarking marking: markings) {

            if (marking instanceof StrongAccent) {
                marking.draw(canvas, edgeAnchor);
            }
        }
    }

    // update the NoteheadOffset after all articulations have been drawn
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
        if (note.accidental != Accidental.NONE) {
            String accidentalPath = RenderingConfiguration.imgFilePath + "/accidentals/" + note.accidental.toString().toLowerCase() + ".svg";
            float topLeftY = 1.1f + (note.accidental == Accidental.FLAT ? 0.2f : 0f);
            float topLeftX = -1.3f + (note.accidental == Accidental.FLAT ? 0.1f : 0f);
            try{
                canvas.drawImage(accidentalPath, anchor,topLeftX, topLeftY,0.6f, 1.9f);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private <Anchor> void drawLedgerLines(MusicCanvas<Anchor> canvas, int lowestLine, int highestLine, float scale) {
        float width = RenderingConfiguration.ledgerLineWidth * durationToStretch(noteType);
        for (int i = (lowestLine / 2) * 2; i < 0; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0, 0)),
                    -width * scale/2f, 0f, width * scale/2f, 0f, RenderingConfiguration.staveLineWidth);
        }
        for (int i = 10; i <= highestLine; i += 2) {
            canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0,0)),
                    -width * scale/2f, 0f, width * scale/2f, 0f, RenderingConfiguration.staveLineWidth);
        }
    }

    private static <Anchor> void drawDots(MusicCanvas<Anchor> canvas, Anchor anchor, NoteType noteType, int dots, boolean onLine, float scaleFactor) {
        float k = durationToStretch(noteType);
        for (int i = 0; i < dots; i++) {
            canvas.drawCircle(anchor, (RenderingConfiguration.noteheadRadius * k + RenderingConfiguration.dotSpacing
                    * (i + 1) + RenderingConfiguration.dotRadius * (2 * i + 1)) * scaleFactor, 0.5f * (onLine ? 1 : 0), RenderingConfiguration.dotRadius * scaleFactor);
        }
    }

    // drawing ties based on the length of the note
    private <Anchor> void drawTie(MusicCanvas<Anchor> canvas, Note note, Anchor anchor) {
        int sign = RenderingConfiguration.upwardStems ? -1 : 1;
        if (note.hasTieFrom) {
            MusicalPosition endMusicalPosition = new MusicalPosition(musicalPosition.line(), musicalPosition.stave(), musicalPosition.crotchetsIntoLine() + durationInCrotchets);
            Anchor endAnchor = canvas.getAnchor(endMusicalPosition, note.pitch);
            float xOffset = .7f;
            float absoluteYOffset = .4f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(anchor, xOffset, signedYOffset, endAnchor, -xOffset, signedYOffset, .15f, !RenderingConfiguration.upwardStems);
        }
        if (note.hasTieTo) {
            MusicalPosition startMusicalPosition = new MusicalPosition(musicalPosition.line(), musicalPosition.stave(), 0);
            Anchor startAnchor = canvas.getAnchor(startMusicalPosition, note.pitch);
            float xOffset = .7f;
            float absoluteYOffset = .4f;
            float signedYOffset = sign * absoluteYOffset;
            canvas.drawCurve(startAnchor, xOffset, signedYOffset, anchor, -xOffset, signedYOffset, .15f, !RenderingConfiguration.upwardStems);
        }
    }


    /**
     * Represents a given notehead in a chord.
     * Pitch is the pitch of the note.
     * Accidental marks any accidentals to be drawn on the note.
     * Ties are represented as flags in Note.
     * Both flags are false by default.
     * note.hasTieFrom is true if and only if there is a tie that starts from the node
     * note.hasTieTo is true if and only if there is a tie that ends at the node AND that the node where the tie starts is on the previous line.
     * Note that when there is a tie in a line, the note that the tie ends at should have hasTieTo to be false.
     */
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
