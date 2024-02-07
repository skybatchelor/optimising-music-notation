package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private final List<Stave> staves;

    public Integer getLengthInCrotchets() {
        return lengthInCrotchets;
    }

    private final Integer lengthInCrotchets;

    public Line(){
        staves = new ArrayList<>();
        lengthInCrotchets = 16;
        staves.add(new Stave());
    }
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, RenderingConfiguration config) {
        // TODO draw pulse lines
        for (Stave s: staves){
            s.draw(canvas,this, config);
        }
    }
}
