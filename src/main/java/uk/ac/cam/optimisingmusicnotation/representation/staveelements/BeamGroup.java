package uk.ac.cam.optimisingmusicnotation.representation.staveelements;

import uk.ac.cam.optimisingmusicnotation.Canvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups.MusicGroup;

import java.util.List;

public class BeamGroup extends MusicGroup {
    List<BeamGroup> contents;
    @Override
    public void Draw(Canvas canvas, RenderingConfiguration config) {

    }
}
