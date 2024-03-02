package uk.ac.cam.optimisingmusicnotation.representation;

import uk.ac.cam.optimisingmusicnotation.rendering.MusicCanvas;
import uk.ac.cam.optimisingmusicnotation.rendering.TextAlignment;
import uk.ac.cam.optimisingmusicnotation.representation.properties.RenderingConfiguration;

import java.io.IOException;
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

    public <Anchor> void draw(MusicCanvas<Anchor> canvas, String workTitle, String composer) {
        RenderingConfiguration.upwardStems = upwardsStems;
        canvas.reserveHeight(15f);
        try {
            canvas.drawText(RenderingConfiguration.defaultFontFilePath,workTitle,24f,
                    TextAlignment.CENTRE, canvas.topCentreAnchor(), -60f,-7f,120f,20f, RenderingConfiguration.blackColor);
            canvas.drawText(RenderingConfiguration.defaultFontFilePath,name,16f,
                    TextAlignment.LEFT, canvas.topLeftAnchor(), 6f,-15f,100f,20f, RenderingConfiguration.blackColor);
            canvas.drawText(RenderingConfiguration.defaultFontFilePath,composer,16f,
                    TextAlignment.RIGHT, canvas.topRightAnchor(), -106f,-15f,100f,20f, RenderingConfiguration.blackColor);
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
