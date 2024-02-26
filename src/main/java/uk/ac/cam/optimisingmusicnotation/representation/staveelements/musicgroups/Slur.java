package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Slur extends MusicGroup {
    private final Chord firstChord;
    private final Chord lastChord;

    public Slur(List<Chord> chords, Chord firstChord, Chord lastChord) {
        super(chords);
        this.firstChord = firstChord;
        this.lastChord = lastChord;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        // TODO: check if is on the same line
        float absoluteYOffset = .5f; // set vertical offset from Notehead here
        float signedYOffset = RenderingConfiguration.upwardStems ? -absoluteYOffset : absoluteYOffset;
        canvas.drawCurve(chordAnchorsMap.get(firstChord).notehead(), 0, signedYOffset, chordAnchorsMap.get(lastChord).notehead(), 0, signedYOffset, .2f, RenderingConfiguration.upwardStems);
    }
}
