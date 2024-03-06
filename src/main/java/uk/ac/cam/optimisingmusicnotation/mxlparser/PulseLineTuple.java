package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.properties.TimeSignature;

import java.util.TreeMap;

/**
 * Holds information for pulse lines
 */
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

    InstantiatedPulseLineTuple toInstantiatedPulseTuple(float lineTime, TreeMap<Float, TempoChangeTuple> integratedTime) {
        return new InstantiatedPulseLineTuple(Parser.normaliseTime(time, integratedTime) - lineTime, name, beatWeight, timeSig);
    }
}
