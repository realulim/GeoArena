package de.mayring.geoarena.view;

import java.io.Serializable;
import lombok.Data;

@Data
public class PlaceView implements Serializable {

    private final String shape;
    private final String title;
    private final String text;

}
