package uk.ac.cam.optimisingmusicnotation.representation.staveelements.musicgroups;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.staveelements.StaveElement;

import java.util.ArrayList;
import java.util.List;

public abstract class MusicGroup implements StaveElement {
    protected final List<MusicGroup> groups;
    public MusicGroup(){
        groups = new ArrayList<>();
    }
    @Override
    public <Anchor> void draw(MusicCanvas<Anchor> canvas) {

    }
}