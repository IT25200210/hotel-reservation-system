package com.hotel.reservation.dto;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

public class StockMovementForm {
    @NotNull
    private Long itemId;
    @NotNull @Pattern(regexp = "RECEIVE|ISSUE")
    private String type = "RECEIVE";
    @NotNull @Min(1) @Max(1000000000)
    private Integer quantity;
    @NotBlank @Size(max = 60)
    private String department;
    @Size(max = 200)
    private String note;
    @NotBlank @Pattern(regexp = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
    private String requestKey;
    public Long getItemId() { return itemId; }
    public void setItemId(Long value) { this.itemId = value; }
    public String getType() { return type; }
    public void setType(String value) { this.type = value; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer value) { this.quantity = value; }
    public String getDepartment() { return department; }
    public void setDepartment(String value) { this.department = value; }
    public String getNote() { return note; }
    public void setNote(String value) { this.note = value; }
    public String getRequestKey() { return requestKey; }
    public void setRequestKey(String value) { this.requestKey = value; }

}
