package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.Crescendo;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.Diminuendo;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.Slur;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class InstantiatedMusicGroupTuple {
    Float startTime;
    Float endTime;
    MusicGroupType type;

    public InstantiatedMusicGroupTuple(Float startTime, Float endTime, MusicGroupType type) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }

    List<Chord> getChordsInInterval(TreeMap<Float, Chord> chordMap) {
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
        float startTime = 0;
        if (this.startTime != null) {
            startTime = this.startTime;
        }
        while (currentTime > startTime) {
            Float nextTime = chordMap.lowerKey(currentTime);
            if (nextTime == null) {
                break;
            }
            chords.add(chordMap.get(nextTime));
            currentTime = nextTime;
        }
        return chords;
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
                return new Diminuendo(getChordsInInterval(chords), startPos, endPos);
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
                return new Crescendo(getChordsInInterval(chords), startPos, endPos);
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
        }
        throw new IllegalArgumentException();
    }
}
