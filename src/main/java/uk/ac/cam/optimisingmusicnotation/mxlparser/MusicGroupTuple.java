package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

class MusicGroupTuple {
    float startTime;
    float endTime;
    MusicGroupType type;
    String text = "";
    boolean aboveStave;

    public MusicGroupTuple(float startTime, MusicGroupType type) {
        this.startTime = startTime;
        this.type = type;
    }

    void splitToInstantiatedMusicGroupTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, TreeMap<Float, TempoChangeTuple> integratedTime, List<LineTuple> target) {
        float startTime = Parser.normaliseTime(this.startTime, integratedTime);
        float endTime = Parser.normaliseTime(this.endTime, integratedTime);
        if (startTime == endTime) {
            float lineTime = newlines.floorKey(startTime);
            target.get(lineIndices.get(lineTime)).musicGroups.add(new InstantiatedMusicGroupTuple(startTime - lineTime, endTime - lineTime, type, text, aboveStave));
            return;
        }
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
            target.get(lineIndices.get(newEndTime)).musicGroups.add(new InstantiatedMusicGroupTuple(instantiatedStartTime, instantiatedEndTime, type, text, aboveStave));
            endTime = newEndTime;
            start = false;
        }
    }
}
