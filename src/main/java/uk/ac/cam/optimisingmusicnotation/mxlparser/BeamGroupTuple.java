package uk.ac.cam.optimisingmusicnotation.mxlparser;

import org.audiveris.proxymusic.Beam;
import org.audiveris.proxymusic.Note;

import java.util.*;

/**
 * Represents information for a group of beamed notes during parsing.
 */
class BeamGroupTuple {

    List<ChordTuple> chords;

    float startTime;
    float endTime;

    int voice = 1;
    int staff = 1;

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
        if (chord.notes.size() > 0) {
            String voice = chord.notes.get(0).getVoice();
            int voiceVal = Parser.getVoice(voice);
            var staff = chord.notes.get(0).getStaff();
            int staffVal = 1;
            if (staff != null) {
                staffVal = staff.intValue();
            }
            this.voice = voiceVal;
            this.staff = staffVal;
        }
    }

    boolean isRest() {
        return (this.chords.get(0).notes.get(0).getRest() != null);
    }

    void splitToInstantiatedBeamGroupTuple(TreeSet<Float> beamBreaks, TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices,
                                           TreeMap<Float, TempoChangeTuple> integratedTime, ParsingPartTuple part, List<LineTuple> target) {
        List<BeamTuple> beams = new ArrayList<>();

        Integer[] beaming = new Integer[10];
        Arrays.fill(beaming, -1);
        Integer[] beamStarts = new Integer[10];
        Arrays.fill(beamStarts, -1);
        for (int i = 0; i < chords.size(); ++i) {
            for (Note note : chords.get(i).notes) {
                for (Beam beam : note.getBeam()) {
                    if (beam.getNumber() != 1) {
                        switch (beam.getValue()) {
                            case FORWARD_HOOK:
                            case BACKWARD_HOOK:
                                if (beaming[beam.getNumber() - 2] == -1) {
                                    beams.add(new BeamTuple(i, i, beam.getNumber() - 1));
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
                                        beams.add(new BeamTuple(beamStarts[beam.getNumber() - 2], i, beam.getNumber() - 1));
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

        var split = new TreeMap<Integer, Float>();
        var lineStartTimes = new ArrayList<Float>();
        float lastSplit = -1f;

        for (int i = 0; i < chords.size(); ++i) {
            float normalisedTime = Parser.normaliseTime(chords.get(i).crotchets, integratedTime);
            float startTime = beamBreaks.floor(normalisedTime);
            float lineStartTime = newlines.floorKey(normalisedTime);
            lineStartTimes.add(lineStartTime);
            if (lastSplit != startTime) {
                lastSplit = startTime;
                split.put(i, startTime);
            }
        }

        var splitBeams = splitBeams(beams, split);

        TreeMap<Float, List<InstantiatedChordTuple>> iChords = new TreeMap<>();

        HashMap<Integer, TreeSet<Float>> emptyStaffCapitals = new HashMap<>(0);
        TreeSet<Float> emptyVoiceCapitals = new TreeSet<>();

        for (int i = 0; i < chords.size(); ++i) {
            InstantiatedChordTuple tuple = chords.get(i).toInstantiatedChordTuple(lineStartTimes.get(i), integratedTime);
            if (part.globalCapitalNotes.contains(chords.get(i).crotchets)
                    || part.capitalNotes.getOrDefault(staff, emptyStaffCapitals).getOrDefault(voice, emptyVoiceCapitals).contains(chords.get(i).crotchets)) {
                tuple.capital = true;
            }
            target.get(lineIndices.get(lineStartTimes.get(i))).addChord(staff, voice, tuple);
            Util.addToListInMap(iChords, split.floorEntry(i).getValue(), tuple);
        }

        for (var entry : iChords.entrySet()) {
            var tuple = new InstantiatedBeamGroupTuple(staff, voice);
            tuple.chords = entry.getValue();
            tuple.beams = splitBeams.getOrDefault(entry.getKey(), new ArrayList<>());
            target.get(lineIndices.get(newlines.floorKey(entry.getKey()))).addBeamGroup(tuple);
        }
    }

    static HashMap<Float, List<BeamTuple>> splitBeams(List<BeamTuple> beams, TreeMap<Integer, Float> split) {
        HashMap<Float, List<BeamTuple>> splitResult = new HashMap<>();

        for (var beam : beams) {
            int startIndex = beam.start;
            int endIndex = beam.end;
            if (startIndex == endIndex) {
                var newEndIndex = split.floorEntry(endIndex);
                Util.addToListInMap(splitResult, newEndIndex.getValue(), new BeamTuple(startIndex - newEndIndex.getKey(), endIndex - newEndIndex.getKey(), beam.number));
            }
            while (startIndex < endIndex) {
                var newEndIndex = split.floorEntry(endIndex);
                Util.addToListInMap(splitResult, newEndIndex.getValue(), new BeamTuple(Math.max(startIndex, newEndIndex.getKey()) - newEndIndex.getKey(), endIndex - newEndIndex.getKey(), beam.number));
                endIndex = newEndIndex.getKey() - 1;
            }
        }

        return splitResult;
    }

    void splitToInstantiatedRestTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, TreeMap<Float, TempoChangeTuple> integratedTime, List<LineTuple> target) {
        float endTime = Parser.normaliseTime(this.endTime, integratedTime);
        float startTime = Parser.normaliseTime(this.startTime, integratedTime);
        float lowestLineStart;
        Float receivedKey = newlines.floorKey(startTime);
        if (receivedKey != null) {
            lowestLineStart = receivedKey;
        } else {
            return;
        }
        while (endTime > startTime) {
            receivedKey = newlines.lowerKey(endTime);
            float newEndTime;
            if (receivedKey != null) {
                newEndTime = receivedKey;
            } else {
                break;
            }
            if (newEndTime < lowestLineStart) {
                break;
            }
            target.get(lineIndices.get(newEndTime)).addRest(new InstantiatedRestTuple(staff, voice, Math.max(newEndTime, startTime) - newEndTime, endTime - newEndTime));
            endTime = newEndTime;
        }
    }
}
