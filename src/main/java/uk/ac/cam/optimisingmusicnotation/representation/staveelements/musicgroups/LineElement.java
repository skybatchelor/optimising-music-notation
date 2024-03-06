package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;

/**
 * An element which exists as a line across multiple notes. Extends {@link MusicGroup}.
 */
public abstract class LineElement extends MusicGroup {
    protected final MusicalPosition startPosition;
    protected final MusicalPosition endPosition;
    protected final Line line;
    protected final Stave stave;

    /**
     * Creates a new line element with the given information.
     * @param chords the chords which might affect the line's placement
     * @param line the line the element is on
     * @param stave the stave the element is on
     * @param startPosition the start position of the element, which may be null for an element split by a newline
     * @param endPosition the end position of the element, which may be null for an element split by a newline
     */
    protected LineElement(List<Chord> chords, Line line, Stave stave, MusicalPosition startPosition, MusicalPosition endPosition) {
        super(chords);
        this.line = line;
        this.stave = stave;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }
}
