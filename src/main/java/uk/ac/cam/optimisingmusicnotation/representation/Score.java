package uk.ac.cam.optimisingmusicnotation.representation;

import java.util.List;

/**
 * Represents a score of music.
 */
public class Score {
    /**
     * Gets the title of this score.
     * @return the title
     */
    public String getWorkTitle() {
        return workTitle;
    }

    private String workTitle;

    /**
     * Gets the composer of this score.
     * @return the composer
     */
    public String getComposer() {
        return composer;
    }

    private String composer;

    public List<Part> getParts() {
        return parts;
    }

    private List<Part> parts;

    /**
     * Generates a file name ending for a given part, ensuring that each part has a unique file name ending.
     * @param index the index of the part to get the file name ending for
     * @return the file name ending
     */
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
