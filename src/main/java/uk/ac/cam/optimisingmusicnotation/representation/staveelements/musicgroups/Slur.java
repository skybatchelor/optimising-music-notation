package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Slur extends MusicGroup {
    private final Chord firstChord;
    private final Chord lastChord;
    private final Line line;

    public Slur(List<Chord> chords, Chord firstChord, Chord lastChord, Line line) {
        super(chords);
        this.firstChord = firstChord;
        this.lastChord = lastChord;
        this.line = line;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        float absoluteYOffset = .5f; // set vertical offset from Notehead here
        float signedYOffset = RenderingConfiguration.upwardStems ? -absoluteYOffset : absoluteYOffset;
        // TODO: deal with null first and last chords
        Anchor startAnchor = canvas.getAnchor(new MusicalPosition(line, 0));
        Anchor endAnchor = canvas.getAnchor(new MusicalPosition(line, 0));
        if (chordAnchorsMap.get(firstChord) != null) {
            startAnchor = chordAnchorsMap.get(firstChord).notehead();
        }
        if (chordAnchorsMap.get(lastChord) != null) {
            endAnchor = chordAnchorsMap.get(lastChord).notehead();
        }
        canvas.drawCurve(startAnchor, 0, signedYOffset, endAnchor, 0, signedYOffset, .2f, RenderingConfiguration.upwardStems);
    }
}
