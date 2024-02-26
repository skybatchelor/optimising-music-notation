package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class InstantiatedBeamGroupTuple {

    static boolean isBeamed(uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType noteType) {
        switch (noteType) {
            case MAXIMA, BREVE, SEMIBREVE, MINIM, CROTCHET -> { return false; }
            case QUAVER, SQUAVER, DSQUAVER, HDSQUAVER -> { return true; }
        }
        return false;
    }

    List<InstantiatedChordTuple> chords;
    List<BeamTuple> beams;

    public InstantiatedBeamGroupTuple() { chords = new ArrayList<>(); beams = new ArrayList<>(); }

    BeamGroup toBeamGroup(Line line, TreeMap<Float, Chord> chordMap) {
        if (chords.size() == 1) {
            if (!isBeamed(chords.get(0).noteType)) {
                var chord = chords.get(0).toChord(line);
                chordMap.put(chord.getMusicalPosition().crotchetsIntoLine(), chord);
                return chord;
            }
        }
        List<Chord> chords = new ArrayList<>();
        for (InstantiatedChordTuple chordTuple : this.chords) {
            var chord = chordTuple.toChord(line);
            chords.add(chord);
            chordMap.put(chord.getMusicalPosition().crotchetsIntoLine(), chord);
        }
        BeamGroup group = new BeamGroup(chords);
        for (BeamTuple tuple : beams) {
            group.addBeam(tuple.start, tuple.end, tuple.number);
        }
        return group;
    }
}
