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

    public Chord() {
        notes = new ArrayList<>();
        markings = new ArrayList<>();
        musicalPosition = new MusicalPosition(null, 0);
        durationInCrochets = 0;
        noteType = NoteType.BREVE;
    };

    public Chord(List<Pitch> pitches, List<Accidental> accidentals, MusicalPosition musicalPosition, float durationInCrochets, NoteType noteType) {
        notes = new ArrayList<>(pitches.size());
        for (int i = 0; i < pitches.size(); ++i) {
            notes.add(new Note(pitches.get(i), accidentals.get(i)));
        }
        markings = new ArrayList<>();
        this.musicalPosition = musicalPosition;
        this.durationInCrochets = durationInCrochets;
        this.noteType = noteType;
    }

    public void addMarking(ChordMarking marking) {
        markings.add(marking);
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        for (Note note: notes) {
            int noteVerticalPosition = note.pitch.rootStaveLine() + note.pitch.semitonesAbove();
            // int sign = config.noteStemDirection() ? 1 : -1; // decide to draw the not stem upwards or downwards
            int sign = 1;
            canvas.drawCircle(canvas.getAnchor(musicalPosition), 0, noteVerticalPosition, .5f); // draw note head [!need to adjust on noteType]
            canvas.drawLine(canvas.getAnchor(musicalPosition), 0, noteVerticalPosition + sign * .5f, 0, noteVerticalPosition + sign * 3.5f, .15f);// draw stem
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
