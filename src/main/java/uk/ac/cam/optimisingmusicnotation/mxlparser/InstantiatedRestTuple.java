package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Rest;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Whitespace;

import java.util.List;
import java.util.TreeMap;

class InstantiatedRestTuple {
    float startTime;
    float endTime;

    public InstantiatedRestTuple(float startTime, float endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
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
                    if (entry != null && entry.getValue().endTime >= currentRest.startTime) {
                        currentRest = new InstantiatedRestTuple(entry.getKey(), Math.max(currentRest.endTime, entry.getValue().endTime));
                        fusedRests.remove(entry.getKey());
                        changed = true;
                        continue;
                    }
                    entry = fusedRests.floorEntry(currentRest.endTime);
                    if (entry != null && entry.getValue().startTime >= currentRest.startTime) {
                        currentRest = new InstantiatedRestTuple(currentRest.startTime, Math.max(currentRest.endTime, entry.getValue().endTime));
                        fusedRests.remove(entry.getKey());
                        changed = true;
                    }
                }
                fusedRests.put(currentRest.startTime, currentRest);
            }
        }

        return fusedRests.values().stream().toList();
    }

    Whitespace toRest(Line line) {
        return new Rest(new MusicalPosition(line, startTime), new MusicalPosition(line, endTime));
    }
}