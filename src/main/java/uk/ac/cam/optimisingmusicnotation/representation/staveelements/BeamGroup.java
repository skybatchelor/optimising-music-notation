package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;

import java.util.ArrayList;
import java.util.List;

public class BeamGroup extends MusicGroup {
    private final List<BeamGroup> contents;

    public BeamGroup() {
        contents = new ArrayList<>();
    }
    public BeamGroup(List<BeamGroup> contents) { this.contents = contents; }

    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}
