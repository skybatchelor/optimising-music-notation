package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.properties.TimeSignature;

class PulseLineTuple {
    float time;
    String name;
    int beatWeight;
    TimeSignature timeSig;

    PulseLineTuple(float time, String name, int beatWeight, TimeSignature timeSig) {
        this.time = time;
        this.name = name;
        this.beatWeight = beatWeight;
        this.timeSig = timeSig;
    }

    InstantiatedPulseLineTuple toInstantiatedPulseTuple(float lineTime, int lineNum) {
        return new InstantiatedPulseLineTuple(time - lineTime, name, beatWeight, timeSig);
    }
}
