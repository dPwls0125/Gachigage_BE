package com.gachigage.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.gachigage.chat.domain.ChatRoom;
import com.gachigage.member.Member;
import com.gachigage.product.domain.Product;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
	@Query("select count(c) > 0 "
		+ "from ChatRoom c "
		+ "where c.id = :chatRoomId "
		+ "and (c.seller.oauthId = :oauthId or c.buyer.oauthId = :oauthId)")
	boolean existsMemberInRoom(@Param("chatRoomId") Long chatRoomId, @Param("oauthId") Long memberOauthId);

	Optional<ChatRoom> findByProductAndBuyer(Product product, Member buyer);

	@Query("SELECT cr FROM ChatRoom cr " + "JOIN FETCH cr.product p " + "JOIN FETCH cr.buyer b "
		+ "JOIN FETCH cr.seller s " + "WHERE b.oauthId = :userOauthId OR s.oauthId = :userOauthId "
		+ "ORDER BY cr.lastMessageTime DESC")
	List<ChatRoom> findMyChatRooms(@Param("userOauthId") Long userOauthId);
}
