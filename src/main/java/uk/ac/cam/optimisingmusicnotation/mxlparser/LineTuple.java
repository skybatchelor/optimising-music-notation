package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.List;

class LineTuple {
    float startTime;
    List<InstantiatedRestTuple> rests;
    List<InstantiatedPulseLineTuple> pulses;
    List<InstantiatedBeamGroupTuple> notes;
    List<InstantiatedMusicGroupTuple> musicGroups;
    List<InstantiatedTempoTuple> tempoMarkings;

    LineTuple(float startTime) {
        this.startTime = startTime;
        rests = new ArrayList<>();
        pulses = new ArrayList<>();
        notes = new ArrayList<>();
        musicGroups = new ArrayList<>();
        tempoMarkings = new ArrayList<>();
    }
}
