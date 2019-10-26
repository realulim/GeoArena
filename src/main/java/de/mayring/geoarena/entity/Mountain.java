package de.mayring.geoarena.entity;

import java.io.Serializable;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.Point;

@Entity
@Data
public class Mountain implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private int height;

    @JsonIgnore
    private Point shape;

    public Mountain() {}

    public Mountain(String name, int height, Point shape) {
	    this.name = name;
	    this.height = height;
	    this.shape = shape;
    }

}
