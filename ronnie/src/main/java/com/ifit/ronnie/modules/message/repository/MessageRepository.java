package com.ifit.ronnie.modules.message.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.ifit.ronnie.modules.message.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    
    List<Message> findByMemoryId(String memoryId);
    void deleteByMemoryId(String memoryId);

    @Query("SELECT DISTINCT m.memoryId FROM Message m")
    List<String> findByDistinctMemoryIds();

    List<Message> findByUserIdAndCoachNameOrderByCreatedAtAsc(String userId, String coachName);
}
