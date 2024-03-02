package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.BeamGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.*;

class InstantiatedBeamGroupTuple {

    List<InstantiatedChordTuple> chords;
    List<BeamTuple> beams;

    int staff;
    int voice;

    public float getStartTime() { return chords.stream().map(InstantiatedChordTuple::getCrotchetsIntoLine).min(Float::compareTo).orElse(0f); }

    public InstantiatedBeamGroupTuple(int staff, int voice) { this.staff = staff; this.voice = voice; chords = new ArrayList<>(); beams = new ArrayList<>(); }

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

    BeamGroup toBeamGroup(Stave stave,
                          HashMap<Integer, HashMap<Integer, TreeMap<Float, Chord>>> chordMap,
                          HashMap<Integer, HashMap<Integer, Map<Chord, BeamletInfo>>> needsFlag,
                          HashMap<Integer, HashMap<Integer, Map<Chord, BeamletInfo>>> needsBeamlet) {
        if (chords.size() == 1) {
            if (!chords.get(0).noteType.isBeamed()) {
                var chord = chords.get(0).toChord(stave);
                chordMap.get(staff).get(voice).put(chord.getCrotchetsIntoLine(), chord);
                return chord;
            } else {
                var chord = chords.get(0).toChord(stave);
                chordMap.get(staff).get(voice).put(chord.getCrotchetsIntoLine(), chord);
                if (RenderingConfiguration.singleFlaggedLeft) {
                    needsFlag.get(staff).get(voice).put(chord, new BeamletInfo(chord.getNoteType().beamNumber(), true));
                } else if (RenderingConfiguration.singleBeamletLeft) {
                    needsFlag.get(staff).get(voice).put(chord, new BeamletInfo(chord.getNoteType().beamNumber(), false));
                }
                if (RenderingConfiguration.singleFlaggedRight) {
                    needsBeamlet.get(staff).get(voice).put(chord, new BeamletInfo(chord.getNoteType().beamNumber(), true));
                } else if (RenderingConfiguration.singleBeamletRight) {
                    needsBeamlet.get(staff).get(voice).put(chord, new BeamletInfo(chord.getNoteType().beamNumber(), false));
                }
                return chord;
            }
        }
        List<Chord> chords = new ArrayList<>();
        Chord firstChord = this.chords.get(0).toChord(stave);
        chords.add(firstChord);
        chordMap.get(staff).get(voice).put(firstChord.getCrotchetsIntoLine(), firstChord);
        Chord lastChord = firstChord;
        float minTime = firstChord.getCrotchetsIntoLine();
        float maxTime = minTime;
        for (InstantiatedChordTuple chordTuple : this.chords.subList(1, this.chords.size())) {
            var chord = chordTuple.toChord(stave);
            chords.add(chord);
            chordMap.get(staff).get(voice).put(chord.getCrotchetsIntoLine(), chord);
            if (chord.getCrotchetsIntoLine() < minTime) {
                minTime = chord.getCrotchetsIntoLine();
                firstChord = chord;
            }
            if (chord.getCrotchetsIntoLine() > maxTime) {
                maxTime = chord.getCrotchetsIntoLine();
                lastChord = chord;
            }
        }
        if (RenderingConfiguration.allFlaggedLeft) {
            needsFlag.get(staff).get(voice).put(firstChord, new BeamletInfo(highestBeamNumber(chords.indexOf(firstChord)), true));
        } else if (RenderingConfiguration.beamletLeft) {
            needsFlag.get(staff).get(voice).put(firstChord, new BeamletInfo(highestBeamNumber(chords.indexOf(firstChord)), false));
        }
        if (RenderingConfiguration.allFlaggedRight) {
            needsBeamlet.get(staff).get(voice).put(lastChord, new BeamletInfo(highestBeamNumber(chords.indexOf(lastChord)), true));
        } else if (RenderingConfiguration.beamletRight) {
            needsBeamlet.get(staff).get(voice).put(lastChord, new BeamletInfo(highestBeamNumber(chords.indexOf(lastChord)), false));
        }
        BeamGroup group = new BeamGroup(chords);
        for (BeamTuple tuple : beams) {
            group.addBeam(tuple.start, tuple.end, tuple.number);
        }
        return group;
    }
}
