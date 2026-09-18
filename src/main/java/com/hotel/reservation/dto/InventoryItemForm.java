package com.hotel.reservation.dto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

public class InventoryItemForm {

    private Long version;
    @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{1,30}", message = "Use 1-30 letters, numbers, underscores or hyphens")
    private String code;
    @NotBlank @Size(max = 100)
    private String name;
    @NotBlank @Size(max = 60)
    private String category;
    @NotBlank @Size(max = 30)
    private String unit;
    @NotBlank @Size(max = 100)
    private String location;
    @NotNull @Min(0) @Max(1000000000)
    private Integer openingQuantity = 0;
    @NotNull @Min(0) @Max(1000000000)
    private Integer minimumStock = 0;
    public Long getVersion() { return version; }
    public void setVersion(Long value) { this.version = value; }
    public String getCode() { return code; }
    public void setCode(String value) { this.code = value; }
    public String getName() { return name; }
    public void setName(String value) { this.name = value; }
    public String getCategory() { return category; }
    public void setCategory(String value) { this.category = value; }
    public String getUnit() { return unit; }
    public void setUnit(String value) { this.unit = value; }
    public String getLocation() { return location; }
    public void setLocation(String value) { this.location = value; }
    public Integer getOpeningQuantity() { return openingQuantity; }
    public void setOpeningQuantity(Integer value) { this.openingQuantity = value; }
    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer value) { this.minimumStock = value; }

}
