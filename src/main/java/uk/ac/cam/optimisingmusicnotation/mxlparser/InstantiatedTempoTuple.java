package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.*;

import java.util.TreeMap;

public class InstantiatedTempoTuple {
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
        this.rightItem = NoteType.MAXIMA;
        this.rightDots = 0;

        this.bpmValue = bpmValue;
    }

    MusicGroup toMusicGroup(Line line, TreeMap<Float, Chord> chords) {
        MusicalPosition position = new MusicalPosition(line, time);
        if (rightText != null) {
            return new TempoMarking(position, leftItem, leftDots, rightText);
        } else {
            return new TempoMarking(position, leftItem, leftDots, rightItem, rightDots);
        }
    }
}
