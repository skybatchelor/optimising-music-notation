package uk.ac.cam.optimisingmusicnotation.representation.properties;

public class Clef {
    ClefSign sign;
    int line;
    int octaveChange;

    public Clef(ClefSign sign){
        this.sign = sign;
        switch (sign){
            case F -> this.line = 4;
            case G -> this.line = 2;
            default -> this.line = 3;
        }
        this.octaveChange = 0;
    }

    public Clef(ClefSign sign, int line){
        this.sign = sign;
        this.line = line;
        this.octaveChange = 0;
    }

    public Clef(ClefSign sign, int line, int octaveChange){
        this.sign = sign;
        this.line = line;
        this.octaveChange = octaveChange;
    }

    @Override
    public String toString() {
        return sign.toString();
    }
}
