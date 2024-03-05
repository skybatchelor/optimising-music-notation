package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.*;

class LineTuple {
    float startTime;
    float offset;
    float length;
    boolean extendUp = false;
    boolean extendDown = false;

    void addRest(InstantiatedRestTuple rest) {
        Util.putInMapInMapMap(rests, HashMap::new, TreeMap::new, rest.staff, rest.voice, rest.startTime, rest);
    }

    HashMap<Integer, HashMap<Integer, TreeMap<Float, InstantiatedRestTuple>>> rests;
    List<InstantiatedPulseLineTuple> pulses;

    void addBeamGroup(InstantiatedBeamGroupTuple beamGroup) {
        Util.putInMapInMapMap(beamGroups, HashMap::new, TreeMap::new, beamGroup.staff, beamGroup.voice, beamGroup.getStartTime(), beamGroup);
    }

    HashMap<Integer, HashMap<Integer, TreeMap<Float, InstantiatedBeamGroupTuple>>> beamGroups;

    void addChord(int staff, int voice, InstantiatedChordTuple chord) {
        Util.putInMapInMapMap(chordGroups, HashMap::new, TreeMap::new, staff, voice, chord.crotchetsIntoLine, chord);
    }

    HashMap<Integer, HashMap<Integer, TreeMap<Float, InstantiatedChordTuple>>> chordGroups;

    void addMusicGroup(InstantiatedMusicGroupTuple musicGroupTuple) {
        Util.addToListInMap(musicGroups, musicGroupTuple.staff, musicGroupTuple);
    }

    HashMap<Integer, List<InstantiatedMusicGroupTuple>> musicGroups;
    List<InstantiatedTempoTuple> tempoMarkings;

    LineTuple(float startTime, float offset, float length) {
        this.startTime = startTime;
        this.offset = offset;
        this.length = length;
        rests = new HashMap<>();
        pulses = new ArrayList<>();
        beamGroups = new HashMap<>();
        chordGroups = new HashMap<>();
        musicGroups = new HashMap<>();
        tempoMarkings = new ArrayList<>();
    }
}
