package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;

import java.util.List;

class InstantiatedChordTuple {
    List<Pitch> pitches;
    List<Accidental> accidentals;
    List<Boolean> tiesFrom;
    List<Boolean> tiesTo;
    boolean capital = false;

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

    Chord toChord(Line line) {
        return new Chord(pitches, accidentals, tiesFrom, tiesTo, capital ? RenderingConfiguration.capitalScaleFactor : 1f,
                new MusicalPosition(line, crotchetsIntoLine), duration, noteType, dots, markings);
    }
}
