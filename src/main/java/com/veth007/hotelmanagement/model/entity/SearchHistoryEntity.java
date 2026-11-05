package com.veth007.hotelmanagement.model.entity;

import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "search_histories")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SearchHistoryEntity extends BaseSoftDeleted<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id") // the searcher (null for anonymous)
    private CustomerEntity customer;

    @Column(nullable = false)
    private String keyword;
}
