package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;

import java.util.ArrayList;
import java.util.List;

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

    <Anchor> Anchor drawRetAnchor(MusicCanvas<Anchor> canvas) {
        int lowestLine = 10000000;
        int highestLine = -10000000;
        Anchor ret = null;
        for (Note note: notes) {
            int sign = RenderingConfiguration.upwardStems ? 1 : -1; // decide to draw the not stem upwards or downwards
            boolean fillInCircle = noteType.defaultLengthInCrotchets <= 1;
            boolean drawStem = noteType.defaultLengthInCrotchets <= 2;
            canvas.drawCircle(canvas.getAnchor(musicalPosition, note.pitch), 0, 0, .5f, fillInCircle); // draw note head [!need to adjust on noteType]
            if (note.pitch.rootStaveLine() < lowestLine) {
                lowestLine = note.pitch.rootStaveLine();
                ret = canvas.getAnchor(musicalPosition, note.pitch);
            }
            if (note.pitch.rootStaveLine() > highestLine) {
                highestLine = note.pitch.rootStaveLine();
            }
            if (drawStem) {
                Anchor bottomOfStem = canvas.offsetAnchor(canvas.getAnchor(musicalPosition, note.pitch), 0,
                        sign * .5f);
                Anchor topOfStem = canvas.offsetAnchor(bottomOfStem, 0, sign * 3f);
                canvas.drawLine(bottomOfStem, 0, 0, topOfStem, 0, 0, RenderingConfiguration.stemWidth);// draw stem
                // draw bit of whitespace to separate from pulse line
                canvas.drawWhitespace(topOfStem, -RenderingConfiguration.stemWidth,
                        sign * RenderingConfiguration.gapHeight, 2 * RenderingConfiguration.stemWidth,
                        RenderingConfiguration.gapHeight);
            }
            if (dotted()) {
                canvas.drawCircle(canvas.getAnchor(musicalPosition, note.pitch), 1f, 0, .2f);
            }
            if (note.accidental != Accidental.NONE) {
                Anchor anchor = canvas.getAnchor(musicalPosition, note.pitch);
                String accidentalPath = "img/accidentals/" + note.accidental.toString().toLowerCase() + ".svg";
                try{
                    canvas.drawImage(accidentalPath, anchor,-1.25f, 1f,0.75f, 2f);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }
            }
            for (int i = lowestLine / 2; i < 0; i += 2) {
                canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0)), -1f, 0f, 1f, 0f, .2f);
            }
            for (int i = 10; i <= highestLine; i += 2) {
                canvas.drawLine(canvas.getAnchor(musicalPosition, new Pitch(i, 0)), -1f, 0f, 1f, 0f, .2f);
            }
        }
        return ret;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {
        drawRetAnchor(canvas);
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
