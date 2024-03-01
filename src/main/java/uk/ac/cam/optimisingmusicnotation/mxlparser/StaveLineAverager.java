package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

interface StaveLineAverager {
    void addChord(InstantiatedChordTuple chord);
    float getAverageStaveLine();
    void reset();
}
