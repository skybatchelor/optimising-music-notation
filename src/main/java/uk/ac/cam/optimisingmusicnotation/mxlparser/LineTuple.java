package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class LineTuple {
    float startTime;

    void addRest(InstantiatedRestTuple rest) {
        Util.addToListInMapMap(rests, HashMap::new, rest.staff, rest.voice, rest);
    }

    HashMap<Integer, HashMap<Integer, List<InstantiatedRestTuple>>> rests;
    List<InstantiatedPulseLineTuple> pulses;

    void addBeamGroup(InstantiatedBeamGroupTuple beamGroup) {
        Util.addToListInMapMap(notes, HashMap::new, beamGroup.staff, beamGroup.voice, beamGroup);
    }

    HashMap<Integer, HashMap<Integer, List<InstantiatedBeamGroupTuple>>> notes;

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
