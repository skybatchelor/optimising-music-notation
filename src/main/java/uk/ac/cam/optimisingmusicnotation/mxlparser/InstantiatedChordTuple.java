package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.chordmarkings.ChordMarking;

import java.util.List;

class InstantiatedChordTuple {
    List<Pitch> pitches;
    List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals;
    float crotchetsIntoLine;
    float duration;
    uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType;
    int dots;
    List<ChordMarking> markings;

    public InstantiatedChordTuple(List<uk.ac.cam.optimisingmusicnotation.representation.properties.Pitch> pitches, List<uk.ac.cam.optimisingmusicnotation.representation.properties.Accidental> accidentals,
                                  float crotchetsIntoLine, float duration, uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType, int dots, List<ChordMarking> markings) {
        this.pitches = pitches;
        this.accidentals = accidentals;
        this.crotchetsIntoLine = crotchetsIntoLine;
        this.duration = duration;
        this.noteType = noteType;
        this.dots = dots;
        this.markings = markings;
    }

    Chord toChord(Line line) {
        return new Chord(pitches, accidentals, new MusicalPosition(line, crotchetsIntoLine), duration, noteType, dots, markings);
    }
}
