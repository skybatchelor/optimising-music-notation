package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.Beam;
import org.audiveris.proxymusic.Note;

import java.util.*;

class BeamGroupTuple {
    List<ChordTuple> chords;

    float startTime;
    float endTime;

    public BeamGroupTuple() {
        chords = new ArrayList<>();
        startTime = -1;
        endTime = -1;
    }

    public void addChord(ChordTuple chord) {
        chords.add(chord);
        if (startTime == -1 || chord.crotchets < startTime) {
            startTime = chord.crotchets;
        }
        if (endTime == -1 || chord.crotchets + chord.duration > endTime) {
            endTime = chord.crotchets + chord.duration;
        }
    }

    boolean isRest() {
        return (this.chords.get(0).notes.get(0).getRest() != null);
    }

    InstantiatedBeamGroupTuple toInstantiatedBeamTuple(float lineTime, TreeMap<Float, TempoChangeTuple> integratedTime) {
        InstantiatedBeamGroupTuple beamTuple = new InstantiatedBeamGroupTuple();
        for (ChordTuple chordTuple : chords) {
            beamTuple.chords.add(chordTuple.toInstantiatedChordTuple(lineTime, integratedTime));
        }

        Integer[] beaming = new Integer[10];
        Arrays.fill(beaming, -1);
        Integer[] beamStarts = new Integer[10];
        Arrays.fill(beamStarts, -1);
        for (int i = 0; i < beamTuple.chords.size(); ++i) {
            for (Note note : chords.get(i).notes) {
                for (Beam beam : note.getBeam()) {
                    if (beam.getNumber() != 1) {
                        switch (beam.getValue()) {
                            case FORWARD_HOOK:
                            case BACKWARD_HOOK:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beamTuple.beams.add(new BeamTuple(i, i, beam.getNumber() - 1));
                                } else {
                                    System.out.println("Started hook beaming when beam had already started");
                                }
                                break;
                            case BEGIN:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beamStarts[beam.getNumber() - 2] = i;
                                    beaming[beam.getNumber() - 2] = i;
                                } else {
                                    System.out.println("Started beam when beam had already started");
                                }
                                break;
                            case CONTINUE:
                                if (beaming[beam.getNumber() - 2] == i - 1) {
                                    beaming[beam.getNumber() - 2] = i;
                                } else {
                                    System.out.println("Continued invalid beam");
                                }
                                break;
                            case END:
                                if (beaming[beam.getNumber() - 2] == i - 1) {
                                    beaming[beam.getNumber() - 2] = -1;
                                    if (beamStarts[beam.getNumber() - 2] != -1) {
                                        beamTuple.beams.add(new BeamTuple(beamStarts[beam.getNumber() - 2], i, beam.getNumber() - 1));
                                    }
                                } else {
                                    System.out.println("Ended invalid beam");
                                }
                                break;
                        }
                    }
                }
            }
        }
        return beamTuple;
    }

    void splitToRestTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, TreeMap<Float, TempoChangeTuple> integratedTime, List<LineTuple> target) {
        float endTime = Parser.normaliseTime(this.endTime, integratedTime);
        float startTime = Parser.normaliseTime(this.startTime, integratedTime);
        while (endTime > startTime) {
            float newEndTime = newlines.lowerKey(endTime);
            if (newEndTime < newlines.floorKey(startTime)) {
                break;
            }
            target.get(lineIndices.get(newEndTime)).rests.add(new RestTuple(Math.max(newEndTime, startTime) - newEndTime, endTime - newEndTime));
            endTime = newEndTime;
        }
    }
}
