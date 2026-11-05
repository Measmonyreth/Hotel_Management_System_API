package com.veth007.hotelmanagement.model.entity;

import org.hibernate.annotations.SQLDelete;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.veth007.hotelmanagement.constant.enums.Status;
import com.veth007.hotelmanagement.constant.enums.StatusFloor;
import com.veth007.hotelmanagement.infrastructure.model.entity.BaseSoftDeleted;
import com.veth007.hotelmanagement.model.entity.Room.RoomEntity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.List;

@Table(name = "floors")
@Entity(name = "floors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@SQLDelete(sql = "UPDATE floors SET deleted_at = NOW() WHERE id = ?")
public class FloorEntity extends BaseSoftDeleted<Long> {
    @Column(nullable = false, unique = true)
    private String floorName;

    @Column(nullable = false, unique = true)
    private String floorNumber;

    @Column(name = "description")
    private String  description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFloor statusFloor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status action;

    @OneToMany(mappedBy = "floor", fetch = FetchType.EAGER)
    private List<RoomEntity> rooms;
}
