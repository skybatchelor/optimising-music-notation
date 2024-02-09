package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Section;

import javax.sound.sampled.Line;
import java.lang.reflect.AnnotatedArrayType;
import java.util.ArrayList;
import java.util.List;

public class Part {
    List<Section> sections;
    String name;
    String abbreviation;

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
}
