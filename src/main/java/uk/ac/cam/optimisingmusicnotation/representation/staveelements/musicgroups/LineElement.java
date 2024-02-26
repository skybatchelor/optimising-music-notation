package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;

public abstract class LineElement extends MusicGroup {
    protected final MusicalPosition startPosition;
    protected final MusicalPosition endPosition;

    protected LineElement(List<Chord> chords, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords);
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
}