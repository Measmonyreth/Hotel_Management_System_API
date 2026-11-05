package com.veth007.hotelmanagement.model.entity;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.Type_vip;
import com.veth007.hotelmanagement.constant.enums.VipStatus;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Table(name = "vip_customers")
@Entity(name = "vipCustomer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class VipCustomerEntity extends BaseSoftDeleted<Long>{
    @Column(name = "code", nullable = false, unique = true)
    private String code;

   @Enumerated(EnumType.STRING)
@Column(name = "type_vip", nullable = false)
private Type_vip typeVip;

    
    @Column(nullable = false)
    private Double points;     // loyalty points

    @Column(name = "total_spent")
    private Double totalSpent;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private VipStatus status;

    @Column(nullable = false)
    private Double percentage_discount; 

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false, referencedColumnName = "id")
    private CustomerEntity customer;

    @OneToMany(mappedBy = "vip", fetch = FetchType.LAZY, cascade = jakarta.persistence.CascadeType.ALL)
    private List<DiscountEntity> discounts;
}
