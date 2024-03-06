package uk.ac.cam.optimisingmusicnotation.mxlparser;

/**
 * Provides a class to get the mean line.
 */
class MeanAverager implements StaveLineAverager {
    private int total;
    private int count;

    @Override
    public void addChord(InstantiatedChordTuple chord) {
        for(var pitch : chord.pitches) {
            total += pitch.rootStaveLine();
            count++;
        }
    }

    @Override
    public float getAverageStaveLine() {
        return total != 0 ? (float)total / count : 0;
    }

    @Override
    public void reset() {
        total = 0;
        count = 0;
    }
}
