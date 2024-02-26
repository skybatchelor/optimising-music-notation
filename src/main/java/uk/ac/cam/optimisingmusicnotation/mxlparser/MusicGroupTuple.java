package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class MusicGroupTuple {
    float startTime;
    float endTime;
    MusicGroupType type;

    public MusicGroupTuple(float startTime, MusicGroupType type) {
        this.startTime = startTime;
        this.type = type;
    }

    void splitToInstantiatedMusicGroupTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, List<LineTuple> target) {
        float endTime = this.endTime;
        boolean start = true;
        while (endTime > startTime) {
            float newEndTime = newlines.lowerKey(endTime);
            Float instantiatedStartTime = null;
            Float instantiatedEndTime = null;
            if (newEndTime < newlines.floorKey(startTime)) {
                break;
            }
            if (newEndTime <= startTime) {
                instantiatedStartTime = startTime - newEndTime;
            }
            if (start) {
                instantiatedEndTime = endTime - newEndTime;
            }
            target.get(lineIndices.get(newEndTime)).musicGroups.add(new InstantiatedMusicGroupTuple(instantiatedStartTime, instantiatedEndTime, type));
            endTime = newEndTime;
            start = false;
        }
    }
}
