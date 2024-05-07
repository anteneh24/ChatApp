
package com.example.whatsup.repository;

import com.example.whatsup.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
 Page<Message> findByChatroomIdOrderByCreatedAtDesc(Long chatroomId, Pageable pageable);

}
