package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BarLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.BeatLine;
import uk.ac.cam.optimisingmusicnotation.representation.beatlines.PulseLine;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.TimeSignature;

class InstantiatedPulseLineTuple {
    float timeInLine;
    String name;
    int beatWeight;
    TimeSignature timeSig;

    InstantiatedPulseLineTuple(float timeInLine, String name, int beatWeight, TimeSignature timeSig) {
        this.timeInLine = timeInLine;
        this.name = name;
        this.beatWeight = beatWeight;
        this.timeSig = timeSig;
    }

    PulseLine toPulseLine(Line line) {
        return switch (beatWeight) {
            case 0 -> new BarLine(new MusicalPosition(line, timeInLine), name, timeSig);
            default -> new BeatLine(new MusicalPosition(line, timeInLine), beatWeight);
        };
    }
}
