package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;

/**
 * Holds the lines being held
 */
class InstantiatedLineTuple {
    float startTime;
    Line line;

    InstantiatedLineTuple(float startTime, Line line) {
        this.startTime = startTime;
        this.line = line;
    }
}
