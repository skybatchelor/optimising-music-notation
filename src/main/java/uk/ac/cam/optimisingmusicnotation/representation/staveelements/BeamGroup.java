package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;

import java.util.ArrayList;
import java.util.List;

public class BeamGroup extends MusicGroup {
    private static class Beam {
        int startIndex;
        int endIndex;
        // Zero indexed beams
        int number;

        public Beam(int startIndex, int endIndex, int number) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.number = number;
        }
    }

    private final List<Chord> contents;
    // The list of secondary beams. Note that beam groups always have an implicit first level beam.
    private final List<Beam> beams;

    public BeamGroup() {
        contents = new ArrayList<>();
        beams = new ArrayList<>();
    }

    public BeamGroup(List<Chord> contents) {
        this.contents = contents;
        beams = new ArrayList<>();
    }

    void addBeam(int startIndex, int endIndex, int number) {
        beams.add(new Beam(startIndex, endIndex, number));
    }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}
