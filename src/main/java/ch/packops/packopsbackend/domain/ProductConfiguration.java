package ch.packops.packopsbackend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "product_configuration")
public class ProductConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private Integer defaultTargetWeight;
    private Integer defaultTolerance;
    private Integer packageUnits;
    private String icon;
    private String color;
    private Boolean active;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
