package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;

public abstract class LineElement extends MusicGroup {
    protected final MusicalPosition startPosition;
    protected final MusicalPosition endPosition;
    protected final Line line;

    protected LineElement(List<Chord> chords, Line line, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords);
        this.line = line;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
}
