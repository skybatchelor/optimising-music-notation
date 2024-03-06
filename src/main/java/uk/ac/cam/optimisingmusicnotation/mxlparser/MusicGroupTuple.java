package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A tuple for a music group before it is placed on a line.
 */
class MusicGroupTuple {
    float startTime;
    float endTime;
    MusicGroupType type;
    String text = "";
    int num;
    boolean bool;
    boolean aboveStave;
    int staff;
    int voice = -1;

    public MusicGroupTuple(float startTime, MusicGroupType type, int staff) {
        this.startTime = startTime;
        this.type = type;
        this.staff = staff;
    }

    void splitToInstantiatedMusicGroupTuple(TreeMap<Float, Float> newlines, Map<Float, Integer> lineIndices, TreeMap<Float, TempoChangeTuple> integratedTime, List<LineTuple> target) {
        float startTime = Parser.normaliseTime(this.startTime, integratedTime);
        float endTime = Parser.normaliseTime(this.endTime, integratedTime);
        if (startTime == endTime) {
            float lineTime = newlines.floorKey(startTime);
            target.get(lineIndices.get(lineTime)).addMusicGroup(new InstantiatedMusicGroupTuple(startTime - lineTime, endTime - lineTime, staff, voice, type, text, num, bool, aboveStave));
            return;
        }
        boolean start = true;
        while (endTime > startTime) {
            float newEndTime = newlines.lowerKey(endTime);
            Float instantiatedStartTime = null;
            Float instantiatedEndTime = null;
            Float receivedKey = newlines.floorKey(startTime);
            if (receivedKey == null || newEndTime < receivedKey) {
                break;
            }
            if (newEndTime <= startTime) {
                instantiatedStartTime = startTime - newEndTime;
            }
            if (start) {
                instantiatedEndTime = endTime - newEndTime;
            }
            target.get(lineIndices.get(newEndTime)).addMusicGroup(new InstantiatedMusicGroupTuple(instantiatedStartTime, instantiatedEndTime, staff, voice, type, text, num, bool, aboveStave));
            endTime = newEndTime;
            start = false;
        }
    }
}
