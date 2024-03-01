package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

class LineTuple {
    float startTime;

    void addRest(InstantiatedRestTuple rest) {
        Util.putInMapInMapMap(rests, HashMap::new, TreeMap::new, rest.staff, rest.voice, rest.startTime, rest);
    }

    HashMap<Integer, HashMap<Integer, TreeMap<Float, InstantiatedRestTuple>>> rests;
    List<InstantiatedPulseLineTuple> pulses;

    void addBeamGroup(InstantiatedBeamGroupTuple beamGroup) {
        Util.putInMapInMapMap(notes, HashMap::new, TreeMap::new, beamGroup.staff, beamGroup.voice, beamGroup.getStartTime(), beamGroup);
    }

    HashMap<Integer, HashMap<Integer, TreeMap<Float, InstantiatedBeamGroupTuple>>> notes;

    void addMusicGroup(InstantiatedMusicGroupTuple musicGroupTuple) {
        Util.addToListInMap(musicGroups, musicGroupTuple.staff, musicGroupTuple);
    }

    HashMap<Integer, List<InstantiatedMusicGroupTuple>> musicGroups;
    List<InstantiatedTempoTuple> tempoMarkings;

    LineTuple(float startTime) {
        this.startTime = startTime;
        rests = new HashMap<>();
        pulses = new ArrayList<>();
        notes = new HashMap<>();
        musicGroups = new HashMap<>();
        tempoMarkings = new ArrayList<>();
    }
}
