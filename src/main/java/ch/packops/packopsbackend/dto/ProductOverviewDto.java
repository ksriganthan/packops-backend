package ch.packops.packopsbackend.dto;

/**
 * @author David M.
 */
public class ProductOverviewDto {

    private Long id;
    private String name;

    public ProductOverviewDto() {
    }

    public ProductOverviewDto(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
