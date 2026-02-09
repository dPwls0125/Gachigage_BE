package com.gachigage.chat.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.gachigage.member.Member;
import com.gachigage.product.domain.Product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@Entity
@Table(name = "chatroom")
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id")
	private Member seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "buyer_id")
	private Member buyer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id")
	private Product product;

	@Builder.Default
	@OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
	private List<ChatMessage> messages = new ArrayList<>();

	@Lob
	@Column(name = "last_message")
	private String lastMessage;

	@Column(name = "last_message_time")
	private LocalDateTime lastMessageTime;

	@Column(name = "buyer_last_read_message_id")
	private Long buyerLastReadMessageId;

	@Column(name = "seller_last_read_message_id")
	private Long sellerLastReadMessageId;

	@CreatedDate
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public void updateLastMessage(ChatMessage lastMessage) {
		this.lastMessage = lastMessage.getContent();
		this.lastMessageTime = lastMessage.getCreatedAt();
	}

	public void updateLastReadMessageId(Long memberOauthId, Long chatMessageId) {
		if (this.seller.getOauthId().equals(memberOauthId)) {
			this.sellerLastReadMessageId = chatMessageId;
		} else if (this.buyer.getOauthId().equals(memberOauthId)) {
			this.buyerLastReadMessageId = chatMessageId;
		}
	}
}
