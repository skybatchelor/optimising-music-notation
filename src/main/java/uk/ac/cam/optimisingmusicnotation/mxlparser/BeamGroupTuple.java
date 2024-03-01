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

    void splitToInstantiatedBeamGroupTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, TreeMap<Float, TempoChangeTuple> integratedTime, List<LineTuple> target) {
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

        var split = new TreeMap<Integer, Integer>();
        var lineStartTime = new ArrayList<Float>();
        int lastLine = -1;

        for (int i = 0; i < chords.size(); ++i) {
            float startTime = newlines.floorKey(Parser.normaliseTime(chords.get(i).crotchets, integratedTime));
            lineStartTime.add(startTime);
            int lineIndex = lineIndices.get(startTime);
            if (lastLine != lineIndex) {
                lastLine = lineIndex;
                split.put(i, lineIndex);
            }
        }

        var splitBeams = splitBeams(beams, split);

        TreeMap<Integer, List<InstantiatedChordTuple>> iChords = new TreeMap<>();

        for (int i = 0; i < chords.size(); ++i) {
            addToListInMap(iChords, split.floorEntry(i).getValue(), chords.get(i).toInstantiatedChordTuple(lineStartTime.get(i), integratedTime));
        }

        for (var entry : iChords.entrySet()) {
            var tuple = new InstantiatedBeamGroupTuple();
            tuple.chords = entry.getValue();
            tuple.beams = getListInMap(splitBeams, entry.getKey());
            target.get(entry.getKey()).notes.add(tuple);
        }
    }

    static <K, V> void addToListInMap(Map<K, List<V>> map, K key, V value) {
        if (map.containsKey(key)) {
            map.get(key).add(value);
        } else {
            map.put(key, new ArrayList<>() {{ add(value); }});
        }
    }

    static <K, V> List<V> getListInMap(Map<K, List<V>> map, K key) {
        if (map.containsKey(key)) {
            return map.get(key);
        } else {
            return new ArrayList<>();
        }
    }

    static HashMap<Integer, List<BeamTuple>> splitBeams(List<BeamTuple> beams, TreeMap<Integer, Integer> split) {
        HashMap<Integer, List<BeamTuple>> splitResult = new HashMap<>();

        for (var beam : beams) {
            int startIndex = beam.start;
            int endIndex = beam.end;
            if (startIndex == endIndex) {
                var newEndIndex = split.floorEntry(endIndex);
                addToListInMap(splitResult, newEndIndex.getValue(), new BeamTuple(startIndex - newEndIndex.getKey(), endIndex - newEndIndex.getKey(), beam.number));
            }
            while (startIndex < endIndex) {
                var newEndIndex = split.floorEntry(endIndex);
                addToListInMap(splitResult, newEndIndex.getValue(), new BeamTuple(Math.max(startIndex, newEndIndex.getKey()) - newEndIndex.getKey(), endIndex - newEndIndex.getKey(), beam.number));
                endIndex = newEndIndex.getKey() - 1;
            }
        }

        return splitResult;
    }

    void splitToInstantiatedRestTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, TreeMap<Float, TempoChangeTuple> integratedTime, List<LineTuple> target) {
        float endTime = Parser.normaliseTime(this.endTime, integratedTime);
        float startTime = Parser.normaliseTime(this.startTime, integratedTime);
        float lowestLineStart = newlines.floorKey(startTime);
        while (endTime > startTime) {
            float newEndTime = newlines.lowerKey(endTime);
            if (newEndTime < lowestLineStart) {
                break;
            }
            target.get(lineIndices.get(newEndTime)).rests.add(new InstantiatedRestTuple(Math.max(newEndTime, startTime) - newEndTime, endTime - newEndTime));
            endTime = newEndTime;
        }
    }
}
