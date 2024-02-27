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
        float absoluteYOffset = 1.5f; // set vertical offset from Notehead here
        float signedYOffset = RenderingConfiguration.upwardStems ? -absoluteYOffset : absoluteYOffset;
        float startNoteheadOffset = 0;
        float endNoteheadOffset = 0;

        Anchor startAnchor = canvas.getAnchor(new MusicalPosition(line, 0));
        Anchor endAnchor = canvas.getAnchor(new MusicalPosition(line, 0));


        ChordAnchors<Anchor> firstChordAnchors = chordAnchorsMap.get(firstChord);
        if (firstChordAnchors != null) {
            startAnchor = firstChordAnchors.notehead();
            startNoteheadOffset = firstChordAnchors.noteheadOffset();
        }

        ChordAnchors<Anchor> lastChordAnchors = chordAnchorsMap.get(lastChord);
        if (lastChordAnchors != null) {
            endAnchor = lastChordAnchors.notehead();
            endNoteheadOffset = lastChordAnchors.noteheadOffset();
        }

        canvas.drawCurve(startAnchor, 0, signedYOffset + startNoteheadOffset, endAnchor, 0, signedYOffset + endNoteheadOffset, .2f, !RenderingConfiguration.upwardStems);
    }
}
