package de.mayring.geoarena.view;

import java.io.Serializable;
import lombok.Data;

@Data
public class RouteView implements Serializable {

    private final String shape;
    private final String name;

}
