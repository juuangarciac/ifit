package com.ifit.ronnie.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ifit.ronnie.model.MessageType;

public interface MessageTypeRepository extends JpaRepository<MessageType, Long> {
    List<MessageType> findAll();
}
