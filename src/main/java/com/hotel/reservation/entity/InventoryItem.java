package com.hotel.reservation.entity;
import jakarta.persistence.*;
@Entity
@Table(name="inventory_items")
public class InventoryItem {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=40) private String code;
 @Column(nullable=false,length=100) private String name;
 @Column(nullable=false,length=60) private String category;
 @Column(nullable=false,length=30) private String unit;
 @Column(nullable=false,length=100) private String location;
 @Column(nullable=false) private long quantity;
 @Column(nullable=false) private long minimumStock;
 @Column(nullable=false) private boolean archived;
 public Long getId(){return id;} public String getCode(){return code;}
 public String getName(){return name;} public String getCategory(){return category;}
 public String getUnit(){return unit;} public String getLocation(){return location;}
 public long getQuantity(){return quantity;} public long getMinimumStock(){return minimumStock;}
 public boolean isArchived(){return archived;}
 public boolean isLowStock(){return !archived && quantity<=minimumStock;}
 public void setCode(String v){code=v;} public void setName(String v){name=v;}
 public void setCategory(String v){category=v;} public void setUnit(String v){unit=v;}
 public void setLocation(String v){location=v;} public void setQuantity(long v){quantity=v;}
 public void setMinimumStock(long v){minimumStock=v;} public void setArchived(boolean v){archived=v;}
}
