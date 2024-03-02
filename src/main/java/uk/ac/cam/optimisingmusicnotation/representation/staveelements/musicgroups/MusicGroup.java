package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class MusicGroup implements StaveElement {
    protected final List<Chord> chords;
    public MusicGroup(){
        chords = new ArrayList<>();
    }
    public MusicGroup(List<Chord> chords){
        this.chords = chords;
    }
    @Override
    abstract public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap);
}