package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

class InstantiatedMusicGroupTuple {
    Float startTime;
    Float endTime;
    MusicGroupType type;
    String text;
    int num;
    boolean bool;
    boolean aboveStave;

    int staff;
    int voice;

    public InstantiatedMusicGroupTuple(Float startTime, Float endTime, int staff, int voice, MusicGroupType type, String text, int num, boolean bool, boolean aboveStave) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.staff = staff;
        this.voice = voice;
        this.type = type;
        this.text = text;
        this.num = num;
        this.bool = bool;
        this.aboveStave = aboveStave;
    }

    static List<Chord> getAllChordsInInterval(HashMap<Integer, TreeMap<Float, Chord>> chordMap, Float startTime, Float endTime) {
        return chordMap.values()
                .stream()
                .map(floatChordTreeMap -> getVoiceChordsInInterval(floatChordTreeMap, startTime, endTime))
                .reduce(new ArrayList<>(), (a, b) -> { a.addAll(b); return a; });
    }

    static List<Chord> getVoiceChordsInInterval(TreeMap<Float, Chord> chordMap, Float startTime, Float endTime) {
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

    List<Chord> getChordsInInterval(HashMap<Integer, HashMap<Integer, TreeMap<Float, Chord>>> chordMap) {
        if (voice == -1) {
            return getAllChordsInInterval(chordMap.get(staff), startTime, endTime);
        } else {
            return getVoiceChordsInInterval(chordMap.get(staff).get(voice), startTime, endTime);
        }
    }

    MusicGroup toMusicGroup(Stave stave, HashMap<Integer, HashMap<Integer, TreeMap<Float, Chord>>> chords) {
        switch (type) {
            case DIM -> {
                MusicalPosition startPos = null;
                MusicalPosition endPos = null;
                if (startTime != null) {
                    startPos = new MusicalPosition(stave.getLine(), stave, startTime);
                }
                if (endTime != null) {
                    endPos = new MusicalPosition(stave.getLine(), stave, endTime);
                }
                return new Diminuendo(getChordsInInterval(chords), stave.getLine(), stave, startPos, endPos);
            }
            case CRESC -> {
                MusicalPosition startPos = null;
                MusicalPosition endPos = null;
                if (startTime != null) {
                    startPos = new MusicalPosition(stave.getLine(), stave, startTime);
                }
                if (endTime != null) {
                    endPos = new MusicalPosition(stave.getLine(), stave, endTime);
                }
                return new Crescendo(getChordsInInterval(chords), stave.getLine(), stave, startPos, endPos);
            }
            case SLUR -> {
                Chord startChord = null;
                Chord endChord = null;
                if (startTime != null && chords.get(staff).get(voice).containsKey(startTime)) {
                    startChord = chords.get(staff).get(voice).get(startTime);
                }
                if (endTime != null && chords.get(staff).get(voice).containsKey(endTime)) {
                    endChord = chords.get(staff).get(voice).get(endTime);
                }
                return new Slur(getChordsInInterval(chords), startChord, endChord, stave.getLine(), stave);
            }
            case DYNAMIC -> {
                return new Dynamic(getChordsInInterval(chords), text, new MusicalPosition(stave.getLine(), stave, startTime));
            }
            case TEXT -> {
                return new TextAnnotation(getChordsInInterval(chords), text, new MusicalPosition(stave.getLine(), stave, startTime), aboveStave);
            }
            case CODA -> {
                return new ImageAnnotation(getChordsInInterval(chords), RenderingConfiguration.imgFilePath + "/signs/coda.svg",
                        new MusicalPosition(stave.getLine(), stave, startTime), aboveStave, RenderingConfiguration.signWidth, RenderingConfiguration.signHeight, RenderingConfiguration.signOffset, true);
            }
            case SEGNO -> {
                return new ImageAnnotation(getChordsInInterval(chords), RenderingConfiguration.imgFilePath + "/signs/segno.svg",
                        new MusicalPosition(stave.getLine(), stave, startTime), aboveStave, RenderingConfiguration.signWidth, RenderingConfiguration.signHeight, RenderingConfiguration.signOffset, true);
            }
            case TUPLET -> {
                MusicalPosition startPos = null;
                MusicalPosition endPos = null;
                if (startTime != null) {
                    startPos = new MusicalPosition(stave.getLine(), stave, startTime);
                }
                if (endTime != null) {
                    endPos = new MusicalPosition(stave.getLine(), stave, endTime);
                }
                return new Tuplet(getChordsInInterval(chords), stave.getLine(), stave, startPos, endPos, num, bool);
            }
        }
        throw new IllegalArgumentException();
    }
}
