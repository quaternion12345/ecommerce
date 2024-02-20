package com.example.catalogservice.jpa;

import com.example.catalogservice.dto.CatalogDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.Date;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicUpdate
@Table(name = "ecommerce_catalogs")
public class MySQLCatalogEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120, unique = true)
    private String productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer unitPrice;

    @Column(nullable = false, updatable = false, insertable = false)
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private Date createdAt;

    // Use Dirty Checking
    public void updateCatalog(CatalogDto catalogDto){
        if(catalogDto.getProductName() != null) this.productName = catalogDto.getProductName();
        if(catalogDto.getStock() != null) this.stock = catalogDto.getStock();
        if(catalogDto.getUnitPrice() != null) this.unitPrice = catalogDto.getUnitPrice();
    }
}
