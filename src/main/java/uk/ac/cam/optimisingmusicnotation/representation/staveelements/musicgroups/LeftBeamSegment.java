package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.Line;
import uk.ac.cam.optimisingmusicnotation.representation.Stave;
import uk.ac.cam.optimisingmusicnotation.representation.properties.ChordAnchors;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.Chord;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.NoteType;

import java.util.ArrayList;
import java.util.Map;

/**
 * Represents a beam segment going to the left.
 */
public class LeftBeamSegment extends MusicGroup {
    private final Chord preChord;
    private final Chord chord;
    private final Line line;
    private final Stave stave;
    private final int maxBeam;
    private final boolean flag;

    public LeftBeamSegment(Chord preChord, Chord chord, Line line, Stave stave, int number, boolean flag) {
        super(new ArrayList<>(0));
        this.preChord = preChord;
        this.chord = chord;
        this.line = line;
        this.stave = stave;
        this.maxBeam = number;
        this.flag = flag;
    }

    public static <Anchor> void draw(MusicCanvas<Anchor> canvas, ChordAnchors<Anchor> chordAnchors, NoteType noteType, float timeScale, float scaleFactor) {
        int sign = RenderingConfiguration.upwardStems ? 1 : -1;
        for (int i = 0; i <= noteType.beamNumber(); ++i) {
            float beamOffset = -sign * (i * RenderingConfiguration.beamWidth
                    + RenderingConfiguration.gapBetweenBeams * i + RenderingConfiguration.beamOffset) * scaleFactor;
            canvas.drawBeam(
                    chordAnchors.stemEnd(),
                    -timeScale * noteType.defaultLengthInCrotchets * scaleFactor,
                    beamOffset,
                    chordAnchors.stemEnd(), 0,
                    beamOffset,
                    RenderingConfiguration.beamWidth * scaleFactor);
        }
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, Map<Chord, ChordAnchors<Anchor>> chordAnchorsMap) {
        if (preChord == null && !flag) return;
        Anchor startAnchor;
        if (preChord == null) {
            startAnchor = canvas.getTakeXTakeYAnchor(canvas.getAnchor(new MusicalPosition(line, stave,
                            chord.getMusicalPosition().crotchetsIntoLine()
                                    - chord.getDurationInCrotchets()
                                    * RenderingConfiguration.flagRatio)),
                    chordAnchorsMap.get(chord).stemEnd());
        } else {
            startAnchor = canvas.interpolateAnchors(
                chordAnchorsMap.get(chord).stemEnd(),
                chordAnchorsMap.get(preChord).stemEnd(),
                    chord.getDurationInCrotchets() * (flag ? RenderingConfiguration.flagRatio : RenderingConfiguration.beamletRatio) / preChord.getDurationInCrotchets()
            );
        }
        int sign = RenderingConfiguration.upwardStems ? 1 : -1;
        for (int i = 0; i <= maxBeam && (i <= RenderingConfiguration.beamletLimit || flag); ++i) {
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
