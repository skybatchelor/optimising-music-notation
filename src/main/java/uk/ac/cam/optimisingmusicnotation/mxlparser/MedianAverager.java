package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.TreeMap;

class MedianAverager implements StaveLineAverager {
    private TreeMap<Integer, Integer> counts = new TreeMap<>();
    private int count = 0;

    @Override
    public void addChord(InstantiatedChordTuple chord) {
        for(var pitch : chord.pitches) {
            if (counts.containsKey(pitch.rootStaveLine())) {
                counts.put(pitch.rootStaveLine(), counts.get(pitch.rootStaveLine()));
            } else {
                counts.put(pitch.rootStaveLine(), 0);
            }
        }
    }

    @Override
    public float getAverageStaveLine() {
        if (counts.size() == 0) {
            return 0;
        } else {
            int lastPitch = counts.firstKey();
            int currentPitch = lastPitch;
            int currentCount = 0;
            while (currentCount < count / 2) {
                currentCount += counts.get(currentPitch);
                lastPitch = currentPitch;
                currentPitch = counts.higherKey(currentPitch);
            }
            return lastPitch;
        }
    }

    @Override
    public void reset() {
        counts.clear();
        count = 0;
    }
}
