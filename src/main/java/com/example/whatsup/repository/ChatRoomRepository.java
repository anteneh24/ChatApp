
package com.example.whatsup.repository;

import com.example.whatsup.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("SELECT c FROM ChatRoom c JOIN c.users u WHERE u.id = :userId1 AND EXISTS (SELECT 1 FROM ChatRoom c2 JOIN c2.users u2 WHERE c2 = c AND u2.id = :userId2)")
    ChatRoom findChatroomByUsers(Long userId1, Long userId2);

    @Query("SELECT cr FROM ChatRoom cr JOIN cr.users u WHERE u.id = :userId")
    Page<ChatRoom> findChatRoomsByUsersContaining(Long userId, Pageable pageable);
}
