package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;

import java.util.ArrayList;
import java.util.Map;

public class Beamlet extends MusicGroup {
    private final Chord postChord;
    private final Chord chord;
    private final Line line;
    private final int maxBeam;
    private final boolean flag;

    public Beamlet(Chord preChord, Chord chord, Line line, int number, boolean flag) {
        super(new ArrayList<>(0));
        this.postChord = preChord;
        this.chord = chord;
        this.line = line;
        this.maxBeam = number;
        this.flag = flag;
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        if (postChord == null && !flag) return;
        Anchor startAnchor;
        if (postChord == null) {
            startAnchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(new MusicalPosition(line, chord.getMusicalPosition().crotchetsIntoLine() +
                    chord.getDurationInCrotchets() * (flag ? RenderingConfiguration.flagRatio : RenderingConfiguration.beamletRatio))), chordAnchorsMap.get(chord).stemEnd());
        } else {
            startAnchor = canvas.interpolateAnchors(
                    chordAnchorsMap.get(chord).stemEnd(),
                    chordAnchorsMap.get(postChord).stemEnd(),
                    flag ? RenderingConfiguration.flagRatio : RenderingConfiguration.beamletRatio
            );
        }
        int sign = RenderingConfiguration.upwardStems ? 1 : -1;
        for (int i = 0; i <= maxBeam && i <= RenderingConfiguration.beamletLimit; ++i) {
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
