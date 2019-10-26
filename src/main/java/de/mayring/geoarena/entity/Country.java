package de.mayring.geoarena.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vividsolutions.jts.geom.MultiPolygon;
import java.io.Serializable;

@Entity
@Data
@EqualsAndHashCode(exclude = { "shape" } )
public class Country implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonIgnore
    private MultiPolygon shape;

    private String isoa3Code;

    public Country() {}

    public Country(String name, MultiPolygon shape, String isoa3Code) {
	    this.name = name;
	    this.shape = shape;
	    this.isoa3Code = isoa3Code;
    }

}
