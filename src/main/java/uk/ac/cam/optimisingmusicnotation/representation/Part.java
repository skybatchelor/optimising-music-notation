package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.Section;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.lang.reflect.AnnotatedArrayType;
import java.util.ArrayList;
import java.util.List;

public class Part {
    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    private List<Section> sections;
    public String getName() {
        return name;
    }

    private String name;
    private String abbreviation;

    public Part() {
        this.sections = new ArrayList<>();
        name = "";
        abbreviation = "";
    }
    public Part(List<Section> sections) {
        this.sections = sections;
        name = "";
        abbreviation = "";
    }

    public void setName(String s) {
        name = s;
    }

    public void setAbbreviation(String s) {
        abbreviation = s;
    }

    public void addSection(Section s) {
        sections.add(s);
    }

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, String workTitle) {
        canvas.reserveHeight(15f);
        try {
            canvas.drawText("fonts/Roboto-Regular.ttf",workTitle,24f,
                    TextAlignment.CENTRE, canvas.topCentreAnchor(), -60f,-7f,120f,20f);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //canvas.reserveHeight(10f);
        for (Section s: sections) {
            s.draw(canvas);
        }
    }
}
