package uk.ac.cam.optimisingmusicnotation.representation;

import java.util.List;

/**
 * Represents a score of music.
 */
public class Score {
    public String getWorkTitle() {
        return workTitle;
    }

    private String workTitle;

    public String getComposer() {
        return composer;
    }

    private String composer;

    public List<Part> getParts() {
        return parts;
    }

    private List<Part> parts;

    public String getPartFilename(int index) {
        String base = parts.get(index).getName();
        int count = 1;
        for (int i = 0; i < index; ++i) {
            if (parts.get(i).getName().equals(base)) {
                count++;
            }
        }
        return base + (count == 1 ? "" : "_" + count);
    }

    public Score(String workTitle, String composer, List<Part> parts) {
        this.workTitle = workTitle;
        this.composer = composer;
        this.parts = parts;
    }

    void setWorkTitle(String s) {
        workTitle = s;
    }


}
