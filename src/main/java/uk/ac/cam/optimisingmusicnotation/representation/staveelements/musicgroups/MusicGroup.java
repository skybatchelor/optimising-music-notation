package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents an annotation to a stave, such as dynamic markings and slurs.
 */
public abstract class MusicGroup implements StaveElement {
    protected final List<Chord> chords;
    public MusicGroup(){
        chords = new ArrayList<>();
    }
    public MusicGroup(List<Chord> chords){
        this.chords = chords;
    }

    /**
     * Draws the music group using the given canvas, and accounting for the anchors in the given anchor map.
     * @param canvas the canvas being used to render the score
     * @param chordAnchorsMap the generated anchors from drawing the chords
     * @param <Anchor> the anchor type used by the canvas
     */
    @Override
    abstract public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap);
}