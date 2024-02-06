package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;

import java.util.List;

public class BeamGroup extends MusicGroup {
    List<BeamGroup> contents;
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {

    }
}
