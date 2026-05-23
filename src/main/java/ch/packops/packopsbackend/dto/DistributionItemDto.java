package ch.packops.packopsbackend.dto;

public class DistributionItemDto {
    private String label;
    private Integer count;
    private Boolean isTarget;

    public DistributionItemDto() {}

    public DistributionItemDto(String label, Integer count, Boolean isTarget) {
        this.label = label;
        this.count = count;
        this.isTarget = isTarget;
    }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
    public Boolean getIsTarget() { return isTarget; }
    public void setIsTarget(Boolean isTarget) { this.isTarget = isTarget; }
}
