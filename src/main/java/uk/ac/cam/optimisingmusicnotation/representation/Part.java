package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The music for a single part.
 */
public class Part {
    /**
     * The sections in this part
     * @return this part's sections
     */
    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    private List<Section> sections;

    /**
     * The name of the instrument this part is for.
     * @return the name of the instrument
     */
    public String getName() {
        return name;
    }

    private String name;
    private String abbreviation;

    /**
     * Whether this part's stems should go up or down
     * @return whether the stems should go up
     */
    public boolean getUpwardsStems() {
        return upwardsStems;
    }

    public void setUpwardsStems(boolean upwardsStems) {
        this.upwardsStems = upwardsStems;
    }

    private boolean upwardsStems;

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

    /**
     * Renders a part using the given canvas, drawing the given work title and composer.
     * @param canvas the canvas rendering the score
     * @param workTitle the work title
     * @param composer the composer
     * @param <Anchor> the anchor type used by the canvas
     */
    public <Anchor> void draw(MusicCanvas<Anchor> canvas, String workTitle, String composer) {
        RenderingConfiguration.upwardStems = upwardsStems;
        try {
            canvas.drawText(RenderingConfiguration.defaultFontFilePath,workTitle,24f,
                    TextAlignment.CENTRE, canvas.topCentreAnchor(), -60f,-3f,120f,20f, RenderingConfiguration.blackColor);
            canvas.drawText(RenderingConfiguration.defaultFontFilePath,name,16f,
                    TextAlignment.LEFT, canvas.topLeftAnchor(), 6f,-12,100f,20f, RenderingConfiguration.blackColor);
            canvas.drawText(RenderingConfiguration.defaultFontFilePath,composer,16f,
                    TextAlignment.RIGHT, canvas.topRightAnchor(), -106f,-12f,100f,20f, RenderingConfiguration.blackColor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (Section s: sections) {
            s.draw(canvas);
        }
    }

    public float getMinOffset() {
        return sections.stream().map(Section::getMinOffset).min(Float::compareTo).orElse(0f);
    }

    public float getMaxEnd() {
        return sections.stream().map(Section::getMaxEnd).max(Float::compareTo).orElse(0f);
    }

    public float getMaxWidth() {
        return getMaxEnd() - getMinOffset();
    }

    public float getMaxCrotchetsPerLine() {
        return sections.stream().map(Section::getMaxCrotchetsPerLine).max(Float::compareTo).orElse(0f);
    }
}
