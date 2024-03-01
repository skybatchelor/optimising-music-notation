package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

class LineTuple {
    float startTime;

    void addRest(InstantiatedRestTuple rest) {
        Util.addToListInMapMap(rests, TreeMap::new, rest.staff, rest.voice, rest);
    }

    TreeMap<Integer, TreeMap<Integer, List<InstantiatedRestTuple>>> rests;
    List<InstantiatedPulseLineTuple> pulses;

    void addBeamGroup(InstantiatedBeamGroupTuple beamGroup) {
        Util.addToListInMapMap(notes, TreeMap::new, beamGroup.staff, beamGroup.voice, beamGroup);
    }

    TreeMap<Integer, TreeMap<Integer, List<InstantiatedBeamGroupTuple>>> notes;

    void addMusicGroup(InstantiatedMusicGroupTuple musicGroupTuple) {
        Util.addToListInMap(musicGroups, musicGroupTuple.staff, musicGroupTuple);
    }

    TreeMap<Integer, List<InstantiatedMusicGroupTuple>> musicGroups;
    List<InstantiatedTempoTuple> tempoMarkings;

    LineTuple(float startTime) {
        this.startTime = startTime;
        rests = new TreeMap<>();
        pulses = new ArrayList<>();
        notes = new TreeMap<>();
        musicGroups = new TreeMap<>();
        tempoMarkings = new ArrayList<>();
    }
}
