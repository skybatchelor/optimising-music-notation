package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.*;

import java.util.HashMap;
import java.util.TreeMap;

/**
 * Holds the tempo tuple adjusted to exist on a given line
 */
class InstantiatedTempoTuple {
    float time;
    NoteType leftItem;
    int leftDots;
    String rightText;
    NoteType rightItem;
    int rightDots;

    float bpmValue;

    public InstantiatedTempoTuple(NoteType leftItem, int leftDots, String rightText, NoteType rightItem, int rightDots, float time, float bpmValue) {
        this.time = time;
        this.leftItem = leftItem;
        this.leftDots = leftDots;
        this.rightText = rightText;
        this.rightItem = rightItem;
        this.rightDots = rightDots;

        this.bpmValue = bpmValue;
    }

    MusicGroup toMusicGroup(Stave stave, HashMap<Integer, HashMap<Integer, TreeMap<Float, Chord>>> chords) {
        MusicalPosition position = new MusicalPosition(stave.getLine(), stave, time);
        if (rightText != null) {
            return new TempoMarking(position, leftItem, leftDots, rightText);
        } else {
            return new TempoMarking(position, leftItem, leftDots, rightItem, rightDots);
        }
    }
}
