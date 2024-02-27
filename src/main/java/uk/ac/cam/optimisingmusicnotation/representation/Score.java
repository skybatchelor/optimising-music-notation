package uk.ac.cam.optimisingmusicnotation.representation;

import java.util.List;

public class Score {
    public String getWorkTitle() {
        return workTitle;
    }

    private String workTitle;
    private String composer;

    public List<Part> getParts() {
        return parts;
    }

    private List<Part> parts;

    public Score(String workTitle, String composer, List<Part> parts) {
        this.workTitle = workTitle;
        this.composer = composer;
        this.parts = parts;
    }

    void setWorkTitle(String s) {
        workTitle = s;
    }


}
