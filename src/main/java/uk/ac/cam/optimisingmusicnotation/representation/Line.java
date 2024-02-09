package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.MusicalPosition;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;
import uk.ac.cam.optimisingmusicnotation.representation.whitespaces.Rest;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private final List<Stave> staves;

    public float getLengthInCrotchets() {
        return lengthInCrotchets;
    }

    private final float lengthInCrotchets;

    private final float offsetInCrochets;

    public Line() {
        staves = new ArrayList<>();
        lengthInCrotchets = 16;
        offsetInCrochets = 0;
        Stave stave = new Stave();
        stave.addWhiteSpace(new Rest(new MusicalPosition(this, 2), new MusicalPosition(this, 5)));// test white space
        staves.add(stave);
    }

    public Line(List<Stave> staves, float lengthInCrochets, float offsetInCrochets) {
        this.staves = staves;
        this.lengthInCrotchets = lengthInCrochets;
        this.offsetInCrochets = offsetInCrochets;
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        // TODO draw pulse lines
        for (Stave s: staves){
            s.draw(canvas,this, config);
        }
    }
}
