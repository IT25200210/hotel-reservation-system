package com.hotel.reservation.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name="stock_movements")
public class StockMovement {
 public enum Type { RECEIVE, ISSUE }
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(nullable=false) private InventoryItem item;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Type type;
 @Column(nullable=false) private long quantity;
 @Column(nullable=false) private long balanceAfter;
 @Column(nullable=false,length=100) private String department;
 @Column(nullable=false,length=250) private String note;
 @Column(nullable=false,length=100) private String recordedBy;
 @Column(nullable=false) private Instant recordedAt=Instant.now();
 @Column(nullable=false,unique=true,length=36) private String requestKey;
 protected StockMovement(){}
 public StockMovement(InventoryItem item,Type type,long quantity,long balance,
 String department,String note,String actor,String key){
 this.item=item;this.type=type;this.quantity=quantity;balanceAfter=balance;
 this.department=department;this.note=note;recordedBy=actor;requestKey=key;}
 public Long getId(){return id;} public InventoryItem getItem(){return item;}
 public Type getType(){return type;} public long getQuantity(){return quantity;}
 public long getBalanceAfter(){return balanceAfter;} public String getDepartment(){return department;}
 public String getNote(){return note;} public String getRecordedBy(){return recordedBy;}
 public Instant getRecordedAt(){return recordedAt;} public String getRequestKey(){return requestKey;}
}
