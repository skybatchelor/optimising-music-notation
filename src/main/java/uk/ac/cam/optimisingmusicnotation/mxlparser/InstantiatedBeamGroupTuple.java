package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class InstantiatedBeamGroupTuple {

    List<InstantiatedChordTuple> chords;
    List<BeamTuple> beams;

    public InstantiatedBeamGroupTuple() { chords = new ArrayList<>(); beams = new ArrayList<>(); }

    int highestBeamNumber() {
        int highestBeamNumber = 0;
        for (var tuple : beams) {
            highestBeamNumber = Math.max(highestBeamNumber, tuple.number);
        }
        return highestBeamNumber;
    }

    int highestBeamNumber(int index) {
        int highestBeamNumber = 0;
        for (var tuple : beams) {
            if (tuple.start <= index && index <= tuple.end) {
                highestBeamNumber = Math.max(highestBeamNumber, tuple.number);
            }
        }
        return highestBeamNumber;
    }

    void addToAverager(StaveLineAverager averager) {
        for (var tuple : chords) {
            averager.addChord(tuple);
        }
    }

    BeamGroup toBeamGroup(Line line, TreeMap<Float, Chord> chordMap, Map<Chord, Integer> needsFlag, Map<Chord, Integer> needsBeamlet) {
        if (chords.size() == 1) {
            if (!chords.get(0).noteType.isBeamed()) {
                var chord = chords.get(0).toChord(line);
                chordMap.put(chord.getMusicalPosition().crotchetsIntoLine(), chord);
                return chord;
            } else {
                var chord = chords.get(0).toChord(line);
                chordMap.put(chord.getMusicalPosition().crotchetsIntoLine(), chord);
                needsFlag.put(chord, chord.getNoteType().beamNumber());
                return chord;
            }
        }
        List<Chord> chords = new ArrayList<>();
        Chord firstChord = this.chords.get(0).toChord(line);
        chords.add(firstChord);
        chordMap.put(firstChord.getCrotchetsIntoLine(), firstChord);
        Chord lastChord = firstChord;
        float minTime = firstChord.getCrotchetsIntoLine();
        float maxTime = minTime;
        for (InstantiatedChordTuple chordTuple : this.chords.subList(1, this.chords.size())) {
            var chord = chordTuple.toChord(line);
            chords.add(chord);
            chordMap.put(chord.getCrotchetsIntoLine(), chord);
            if (chord.getCrotchetsIntoLine() < minTime) {
                minTime = chord.getCrotchetsIntoLine();
                firstChord = chord;
            }
            if (chord.getCrotchetsIntoLine() > maxTime) {
                maxTime = chord.getCrotchetsIntoLine();
                lastChord = chord;
            }
        }
        if (RenderingConfiguration.allFlagged) {
            needsFlag.put(firstChord, highestBeamNumber(chords.indexOf(firstChord)));
        }
        if (RenderingConfiguration.beamlets) {
            needsBeamlet.put(lastChord, highestBeamNumber(chords.indexOf(lastChord)));
        }
        BeamGroup group = new BeamGroup(chords);
        for (BeamTuple tuple : beams) {
            group.addBeam(tuple.start, tuple.end, tuple.number);
        }
        return group;
    }
}
