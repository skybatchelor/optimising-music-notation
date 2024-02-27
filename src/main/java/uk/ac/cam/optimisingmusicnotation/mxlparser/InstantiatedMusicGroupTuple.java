package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class InstantiatedMusicGroupTuple {
    Float startTime;
    Float endTime;
    MusicGroupType type;
    String text;

    public InstantiatedMusicGroupTuple(Float startTime, Float endTime, MusicGroupType type, String text) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
        this.text = text;
    }

    static List<Chord> getChordsInInterval(TreeMap<Float, Chord> chordMap, Float startTime, Float endTime) {
        if (chordMap.size() == 0) {
            return new ArrayList<>();
        }
        float currentTime;
        List<Chord> chords = new ArrayList<>();
        if (endTime == null) {
            currentTime = chordMap.lastKey();
        } else {
            currentTime = endTime;
        }
        if (chordMap.containsKey(currentTime)) {
            chords.add(chordMap.get(currentTime));
        }
        float newStartTime = 0;
        if (startTime != null) {
            newStartTime = startTime;
        }
        while (currentTime > newStartTime) {
            Float nextTime = chordMap.lowerKey(currentTime);
            if (nextTime == null) {
                break;
            }
            chords.add(chordMap.get(nextTime));
            currentTime = nextTime;
        }
        return chords;
    }

    List<Chord> getChordsInInterval(TreeMap<Float, Chord> chordMap) {
        return getChordsInInterval(chordMap, startTime, endTime);
    }

    MusicGroup toMusicGroup(Line line, TreeMap<Float, Chord> chords) {
        switch (type) {
            case DIM -> {
                MusicalPosition startPos = null;
                MusicalPosition endPos = null;
                if (startTime != null) {
                    startPos = new MusicalPosition(line, startTime);
                }
                if (endTime != null) {
                    endPos = new MusicalPosition(line, endTime);
                }
                return new Diminuendo(getChordsInInterval(chords), line, startPos, endPos);
            }
            case CRESC -> {
                MusicalPosition startPos = null;
                MusicalPosition endPos = null;
                if (startTime != null) {
                    startPos = new MusicalPosition(line, startTime);
                }
                if (endTime != null) {
                    endPos = new MusicalPosition(line, endTime);
                }
                return new Crescendo(getChordsInInterval(chords), line, startPos, endPos);
            }
            case SLUR -> {
                Chord startChord = null;
                Chord endChord = null;
                if (startTime != null && chords.containsKey(startTime)) {
                    startChord = chords.get(startTime);
                }
                if (endTime != null && chords.containsKey(endTime)) {
                    endChord = chords.get(endTime);
                }
                return new Slur(getChordsInInterval(chords), startChord, endChord, line);
            }
            case DYNAMIC -> {
                return new Dynamic(getChordsInInterval(chords), text, new MusicalPosition(line, startTime));
            }
        }
        throw new IllegalArgumentException();
    }
}
