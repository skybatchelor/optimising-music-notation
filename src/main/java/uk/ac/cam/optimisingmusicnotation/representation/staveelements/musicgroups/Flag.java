package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.List;
import java.util.Map;

public class Flag extends MusicGroup {
    private final Chord preChord;
    private final Chord chord;
    private final Line line;
    private final int maxBeam;

    public Flag(List<Chord> chords, Chord preChord, Chord chord, Line line, int number) {
        super(chords);
        this.preChord = preChord;
        this.chord = chord;
        this.line = line;
        this.maxBeam = number;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        Anchor startAnchor;
        if (preChord == null) {
            startAnchor = canvas.getAnchor(new MusicalPosition(line, chord.getMusicalPosition().crotchetsIntoLine() -
                    chord.getDurationInCrochets() * RenderingConfiguration.flagRatio));
        } else {
            startAnchor = canvas.interpolateAnchors(
                chordAnchorsMap.get(chord).stemEnd(),
                chordAnchorsMap.get(preChord).stemEnd(),
                    preChord.getDurationInCrochets() / chord.getDurationInCrochets() * RenderingConfiguration.flagRatio
            );
        }
        int sign = RenderingConfiguration.upwardStems ? 1 : -1;
        for (int i = 0; i <= maxBeam; ++i) {
            float beamOffset = -(sign * i * RenderingConfiguration.beamWidth
                    + sign * RenderingConfiguration.gapBetweenBeams * i);
            canvas.drawBeam(
                    startAnchor,
                    0,
                    beamOffset,
                    chordAnchorsMap.get(chord).stemEnd(), 0,
                    beamOffset,
                    RenderingConfiguration.beamWidth);
        }
    }
}
