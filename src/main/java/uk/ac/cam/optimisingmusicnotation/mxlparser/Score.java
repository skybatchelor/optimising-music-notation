package uk.ac.cam.optimisingmusicnotation.mxlparser;

import uk.ac.cam.optimisingmusicnotation.representation.Section;

import java.util.List;

public class Score {
    String workTitle;

    List<Part> parts;

    public Score(String workTitle, List<Part> parts) {
        this.workTitle = workTitle;
        this.parts = parts;
    }

    void setWorkTitle(String s) {
        workTitle = s;
    }


}
