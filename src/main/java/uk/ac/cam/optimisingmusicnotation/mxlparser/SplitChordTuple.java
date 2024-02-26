package uk.ac.cam.optimisingmusicnotation.mxlparser;

import java.util.ArrayList;
import java.util.List;

class SplitChordTuple {
    List<InstantiatedChordTuple> pre;
    List<InstantiatedChordTuple> post;

    public SplitChordTuple() {
        pre = new ArrayList<>();
        post = new ArrayList<>();
    }
}
