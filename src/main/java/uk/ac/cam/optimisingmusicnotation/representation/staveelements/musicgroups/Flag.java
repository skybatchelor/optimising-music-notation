package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.ArrayList;
import java.util.Map;

public class Flag extends MusicGroup {
    private final Chord preChord;
    private final Chord chord;
    private final Line line;
    private final int maxBeam;

    public Flag(Chord preChord, Chord chord, Line line, int number) {
        super(new ArrayList<>(0));
        this.preChord = preChord;
        this.chord = chord;
        this.line = line;
        this.maxBeam = number;
    }

    public static <Anchor> void drawFlag() {

    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        Anchor startAnchor;
        if (preChord == null) {
            startAnchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(new MusicalPosition(line, chord.getMusicalPosition().crotchetsIntoLine() -
                    chord.getDurationInCrotchets() * RenderingConfiguration.flagRatio)), chordAnchorsMap.get(chord).stemEnd());
        } else {
            startAnchor = canvas.interpolateAnchors(
                chordAnchorsMap.get(chord).stemEnd(),
                chordAnchorsMap.get(preChord).stemEnd(),
                    chord.getDurationInCrotchets() * RenderingConfiguration.flagRatio / preChord.getDurationInCrotchets()
            );
        }
        int sign = RenderingConfiguration.upwardStems ? 1 : -1;
        for (int i = 0; i <= maxBeam; ++i) {
            float beamOffset = -(sign * i * RenderingConfiguration.beamWidth
                    + sign * RenderingConfiguration.gapBetweenBeams * i + sign * RenderingConfiguration.beamOffset);
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
