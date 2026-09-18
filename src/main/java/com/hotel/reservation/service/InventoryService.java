package com.hotel.reservation.service;
import com.hotel.reservation.entity.*;
import com.hotel.reservation.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.*;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.*;
import java.util.*;
@Service
@Transactional
@PreAuthorize("hasRole('INVENTORY')")
public class InventoryService {
 private final InventoryItemRepository items;
 private final StockMovementRepository movements;
 private final AuditLogService audit;
 public InventoryService(InventoryItemRepository i,StockMovementRepository m,AuditLogService a){items=i;movements=m;audit=a;}
 private String actor(){return SecurityContextHolder.getContext().getAuthentication().getName();}
 private void require(boolean ok,String message){if(!ok)throw new ResponseStatusException(CONFLICT,message);}
 private String text(String s,int max){require(s!=null&&!s.isBlank()&&s.trim().length()<=max,"Required text is missing or too long");return s.trim();}
 private InventoryItem lock(Long id){return items.lockById(id).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Item not found"));}
 @Transactional(readOnly=true)
 public List<InventoryItem> list(){return items.findAllByOrderByNameAsc();}
 @Transactional(readOnly=true)
 public InventoryItem get(Long id){return items.findById(id).orElseThrow(()->new ResponseStatusException(NOT_FOUND,"Item not found"));}
 public void save(Long id,String code,String name,String category,String unit,String location,long minimum){
 code=text(code,40).toUpperCase(Locale.ROOT);name=text(name,100);category=text(category,60);
 unit=text(unit,30);location=text(location,100);require(minimum>=0,"Minimum stock cannot be negative");
 InventoryItem i=id==null?new InventoryItem():lock(id);
 require(!i.isArchived(),"Archived items cannot be edited");
 require(id==null?!items.existsByCode(code):!items.existsByCodeAndIdNot(code,id),"Item code already exists");
 if(id!=null && movements.existsByItemId(id))require(unit.equals(i.getUnit()),"Unit cannot change after stock movements");
 i.setCode(code);i.setName(name);i.setCategory(category);i.setUnit(unit);i.setLocation(location);i.setMinimumStock(minimum);
 items.save(i);audit.log(actor(),id==null?"CREATE":"UPDATE","InventoryItem",i.getId(),"Inventory item details saved");
 }
 public void delete(Long id){InventoryItem i=lock(id);
 require(!movements.existsByItemId(id)&&i.getQuantity()==0,"Item has stock history; archive it after issuing remaining stock");
 items.delete(i);audit.log(actor(),"DELETE","InventoryItem",id,"Unused inventory item deleted");}
 public void archive(Long id){InventoryItem i=lock(id);require(i.getQuantity()==0,"Issue remaining stock before archiving");
 require(!i.isArchived(),"Item is already archived");i.setArchived(true);audit.log(actor(),"ARCHIVE","InventoryItem",id,"Inventory item archived");}
 public void move(Long id,StockMovement.Type type,long quantity,String department,String note,String key){
 require(type!=null&&quantity>0,"Choose a movement type and a positive whole quantity");
 department=text(department,100);note=note==null?"":note.trim();require(note.length()<=250,"Note is too long");
 try{key=UUID.fromString(key).toString();}catch(RuntimeException e){throw new ResponseStatusException(CONFLICT,"Invalid request key");}
 InventoryItem i=lock(id);var previous=movements.findByRequestKey(key);
 if(previous.isPresent()){StockMovement m=previous.get();require(m.getItem().getId().equals(id)&&m.getType()==type&&m.getQuantity()==quantity&&m.getDepartment().equals(department)&&m.getNote().equals(note),"Request key already used for another movement");return;}
 require(!i.isArchived(),"Archived item cannot receive or issue stock");
 long balance;
 if(type==StockMovement.Type.ISSUE){require(quantity<=i.getQuantity(),"Insufficient stock");balance=i.getQuantity()-quantity;}
 else{require(quantity<=Long.MAX_VALUE-i.getQuantity(),"Stock quantity exceeds allowed limit");balance=i.getQuantity()+quantity;}
 i.setQuantity(balance);StockMovement m=movements.save(new StockMovement(i,type,quantity,balance,department,note,actor(),key));
 audit.log(actor(),type.name(),"StockMovement",m.getId(),"Item "+id+"; quantity "+quantity+"; balance "+balance);
 }
 @Transactional(readOnly=true)
 public Page<StockMovement> history(Long id,int page){Pageable p=PageRequest.of(Math.max(0,page),25,Sort.by("id").descending());return id==null?movements.findAll(p):movements.findByItemId(id,p);}
}
