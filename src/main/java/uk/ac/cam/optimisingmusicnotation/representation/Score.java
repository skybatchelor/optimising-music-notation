package uk.ac.cam.optimisingmusicnotation.representation;

import java.util.List;

public class Score {
    private String workTitle;

    public List<Part> getParts() {
        return parts;
    }

    private List<Part> parts;

    public Score(String workTitle, List<Part> parts) {
        this.workTitle = workTitle;
        this.parts = parts;
    }

    void setWorkTitle(String s) {
        workTitle = s;
    }


}
