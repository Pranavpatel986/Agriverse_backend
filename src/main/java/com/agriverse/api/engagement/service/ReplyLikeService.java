package com.agriverse.api.engagement.service;

import com.agriverse.api.common.exception.ResourceNotFoundException;
import com.agriverse.api.engagement.dto.LikeCountResponse;
import com.agriverse.api.engagement.entity.Like;
import com.agriverse.api.engagement.entity.LikeableType;
import com.agriverse.api.engagement.entity.Reply;
import com.agriverse.api.engagement.repository.LikeRepository;
import com.agriverse.api.engagement.repository.ReplyRepository;
import com.agriverse.api.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Liking a Reply mirrors CommentService's comment-liking logic, per the API spec's "equivalent endpoint" note. */
@Service
@RequiredArgsConstructor
public class ReplyLikeService {

    private final ReplyRepository replyRepository;
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeCountResponse like(Long userId, UUID replyId) {
        Reply reply = replyRepository.findByPublicIdAndDeletedAtIsNull(replyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reply", replyId));
        if (likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, LikeableType.REPLY, reply.getId()).isEmpty()) {
            Like like = new Like();
            like.setUser(userRepository.getReferenceById(userId));
            like.setEntityType(LikeableType.REPLY);
            like.setEntityId(reply.getId());
            likeRepository.save(like);
            reply.setLikeCount(reply.getLikeCount() + 1);
            replyRepository.save(reply);
        }
        return new LikeCountResponse(reply.getLikeCount());
    }

    @Transactional
    public LikeCountResponse unlike(Long userId, UUID replyId) {
        Reply reply = replyRepository.findByPublicIdAndDeletedAtIsNull(replyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Reply", replyId));
        likeRepository.findByUserIdAndEntityTypeAndEntityId(userId, LikeableType.REPLY, reply.getId())
                .ifPresent(like -> {
                    likeRepository.delete(like);
                    reply.setLikeCount(Math.max(0, reply.getLikeCount() - 1));
                    replyRepository.save(reply);
                });
        return new LikeCountResponse(reply.getLikeCount());
    }
}
