package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Rest;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

/**
 * Holds information for a rest, adjusted to be on a given line.
 */
class InstantiatedRestTuple {
    float startTime;
    float endTime;

    int staff;
    int voice;

    public InstantiatedRestTuple(int staff, int voice, float startTime, float endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.staff = staff;
        this.voice = voice;
    }

    static List<InstantiatedRestTuple> fuseRestTuples(List<InstantiatedRestTuple> rests) {
        TreeMap<Float, InstantiatedRestTuple> fusedRests = new TreeMap<>();

        for (InstantiatedRestTuple rest : rests) {
            if (fusedRests.size() == 0) {
                fusedRests.put(rest.startTime, rest);
            } else {
                var currentRest = rest;
                boolean changed = true;
                while (changed) {
                    changed = false;
                    var entry = fusedRests.floorEntry(currentRest.startTime);
                    if (entry != null && entry.getValue().endTime + Parser.WHITESPACE_EPSILON >= currentRest.startTime) {
                        currentRest = new InstantiatedRestTuple(currentRest.staff, currentRest.voice, entry.getKey(), Math.max(currentRest.endTime, entry.getValue().endTime));
                        fusedRests.remove(entry.getKey());
                        changed = true;
                        continue;
                    }
                    entry = fusedRests.floorEntry(currentRest.endTime);
                    if (entry != null && entry.getValue().startTime + Parser.WHITESPACE_EPSILON >= currentRest.startTime) {
                        currentRest = new InstantiatedRestTuple(currentRest.staff, currentRest.voice, currentRest.startTime, Math.max(currentRest.endTime, entry.getValue().endTime));
                        fusedRests.remove(entry.getKey());
                        changed = true;
                    }
                }
                fusedRests.put(currentRest.startTime, currentRest);
            }
        }

        return fusedRests.values().stream().toList();
    }

    Whitespace toRest(Stave stave, HashMap<Integer, HashMap<Integer, TreeMap<Float, Whitespace>>> rests) {
        Whitespace whitespace = new Rest(new MusicalPosition(stave.getLine(), stave, startTime), new MusicalPosition(stave.getLine(), stave, endTime));
        rests.get(staff).get(voice).put(whitespace.getStartCrotchets(), whitespace);
        return whitespace;
    }
}
