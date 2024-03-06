package uk.ac.cam.optimisingmusicnotation.mxlparser;

/**
 * Represents secondary beams in a beam group
 */
class BeamTuple {
    int start;
    int end;
    int number;

    public BeamTuple(int start, int end, int number) {
        this.start = start; this.end = end; this.number = number;
    }
}
