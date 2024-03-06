package uk.ac.cam.optimisingmusicnotation.mxlparser;

/**
 * An interface for getting the average line of an item.
 */
interface StaveLineAverager {
    void addChord(InstantiatedChordTuple chord);
    float getAverageStaveLine();
    void reset();
}
