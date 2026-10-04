package com.orderline.inventory.repository;

import com.orderline.inventory.entity.Reservation;
import com.orderline.inventory.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);
}
