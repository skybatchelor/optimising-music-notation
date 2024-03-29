package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;

import java.util.List;

/**
 * Holds information for chords adjusted to belong to a given line.
 */
class InstantiatedChordTuple {
    List<Pitch> pitches;
    List<Accidental> accidentals;
    List<Boolean> tiesFrom;
    List<Boolean> tiesTo;
    boolean capital;

    public float getCrotchetsIntoLine() {
        return crotchetsIntoLine;
    }

    float crotchetsIntoLine;
    float duration;
    NoteType noteType;
    int dots;
    List<ChordMarking> markings;

    public InstantiatedChordTuple(List<Pitch> pitches, List<Accidental> accidentals,
                                    List<Boolean> tiesFrom, List<Boolean> tiesTo,
                                    boolean capital,
                                    float crotchetsIntoLine, float duration, NoteType noteType, int dots, List<ChordMarking> markings) {
        this.pitches = pitches;
        this.accidentals = accidentals;
        this.tiesFrom = tiesFrom;
        this.tiesTo = tiesTo;
        this.capital = capital;
        this.crotchetsIntoLine = crotchetsIntoLine;
        this.duration = duration;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
    }

    Chord toChord(Stave stave) {
        return new Chord(pitches, accidentals, tiesFrom, tiesTo, capital ? RenderingConfiguration.capitalScaleFactor : 1f,
                new MusicalPosition(stave.getLine(), stave, crotchetsIntoLine), duration, noteType, dots, markings);
    }
}
