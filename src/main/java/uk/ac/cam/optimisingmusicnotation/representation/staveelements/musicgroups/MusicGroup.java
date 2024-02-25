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
    protected final Chord firstChord;
    protected final Chord lastChord;
    public MusicGroup(){
        chords = new ArrayList<>();
        firstChord = new Chord();
        lastChord = new Chord();
    }
    public MusicGroup(List<Chord> chords){
        this.chords = chords;
        this.firstChord = chords.getFirst();
        this.lastChord = chords.getLast();
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {

    }
}