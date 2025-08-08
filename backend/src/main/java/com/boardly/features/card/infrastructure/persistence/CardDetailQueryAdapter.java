package com.boardly.features.card.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.boardly.features.card.application.port.output.CardDetailQueryPort;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.model.CardDetail;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.card.domain.repository.CardLabelRepository;
import com.boardly.features.card.domain.repository.CardRepository;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.label.domain.model.Label;
import com.boardly.features.label.domain.repository.LabelRepository;
import com.boardly.features.user.domain.model.User;
import com.boardly.features.user.domain.model.UserId;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 카드 상세 정보 조회 어댑터
 * 
 * <p>
 * 실제 구현에서는 여러 테이블을 조인하여 카드의 모든 상세 정보를 조회합니다.
 * 각 조회 부분을 메서드로 분리하여 체이닝 방식으로 구성합니다.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CardDetailQueryAdapter implements CardDetailQueryPort {

    private final CardRepository cardRepository;
    private final LabelRepository labelRepository;
    private final CardLabelRepository cardLabelRepository;

    @Override
    public Optional<CardDetail> findCardDetailById(CardId cardId, UserId userId) {
        log.info("카드 상세 정보 조회: cardId={}, userId={}", cardId.getId(), userId.getId());

        return findCardById(cardId)
                .flatMap(this::enrichWithLabelInfo)
                .flatMap(this::enrichWithMemberInfo)
                .flatMap(this::enrichWithAttachmentInfo)
                .flatMap(this::enrichWithCommentInfo)
                .flatMap(this::enrichWithBoardMemberInfo)
                .flatMap(this::enrichWithBoardLabelInfo)
                .flatMap(this::enrichWithActivityInfo)
                .flatMap(this::enrichWithCreatorInfo)
                .map(this::buildCardDetail);
    }

    /**
     * 카드 기본 정보 조회
     */
    private Optional<Card> findCardById(CardId cardId) {
        return cardRepository.findById(cardId)
                .map(card -> {
                    log.debug("카드 기본 정보 조회 완료: cardId={}", cardId.getId());
                    return card;
                });
    }

    /**
     * 라벨 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithLabelInfo(Card card) {
        List<Label> labels = findLabels(card.getCardId()).orElse(List.of());
        return Optional.of(new CardDetailData(card, labels, List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), null));
    }

    /**
     * 멤버 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithMemberInfo(CardDetailData data) {
        List<CardMember> assignees = findAssignees(data.card().getCardId()).orElse(List.of());
        return Optional.of(data.withAssignees(assignees));
    }

    /**
     * 첨부파일 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithAttachmentInfo(CardDetailData data) {
        List<Object> attachments = findAttachments(data.card().getCardId()).orElse(List.of());
        return Optional.of(data.withAttachments(attachments));
    }

    /**
     * 댓글 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithCommentInfo(CardDetailData data) {
        List<Object> comments = findComments(data.card().getCardId()).orElse(List.of());
        return Optional.of(data.withComments(comments));
    }

    /**
     * 보드 멤버 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithBoardMemberInfo(CardDetailData data) {
        List<Object> boardMembers = findBoardMembers(data.card().getListId().getId()).orElse(List.of());
        return Optional.of(data.withBoardMembers(boardMembers));
    }

    /**
     * 보드 라벨 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithBoardLabelInfo(CardDetailData data) {
        List<Label> boardLabels = findBoardLabels(data.card().getListId().getId()).orElse(List.of());
        return Optional.of(data.withBoardLabels(boardLabels));
    }

    /**
     * 활동 내역 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithActivityInfo(CardDetailData data) {
        List<Object> activities = findActivities(data.card().getCardId()).orElse(List.of());
        return Optional.of(data.withActivities(activities));
    }

    /**
     * 생성자 정보로 카드 정보 보강
     */
    private Optional<CardDetailData> enrichWithCreatorInfo(CardDetailData data) {
        User createdBy = findCreatedBy(data.card().getCreatedBy()).orElse(null);
        return Optional.of(data.withCreatedBy(createdBy));
    }

    /**
     * CardDetail 객체 생성
     */
    private CardDetail buildCardDetail(CardDetailData data) {
        log.debug("CardDetail 객체 생성: cardId={}", data.card().getCardId().getId());

        return CardDetail.of(
                data.card(),
                data.labels(),
                data.assignees(),
                List.of(), // attachments는 CardAttachment 타입으로 변환 필요
                List.of(), // comments는 CardComment 타입으로 변환 필요
                List.of(), // boardMembers는 BoardMember 타입으로 변환 필요
                data.boardLabels(),
                List.of(), // activities는 CardActivity 타입으로 변환 필요
                data.createdBy());
    }

    // 조회 메서드들 (임시 구현)
    private Optional<List<Label>> findLabels(CardId cardId) {
        log.debug("카드 라벨 정보 조회: cardId={}", cardId.getId());
        return Optional.of(List.of());
    }

    private Optional<List<CardMember>> findAssignees(CardId cardId) {
        log.debug("카드 멤버 정보 조회: cardId={}", cardId.getId());
        return Optional.of(List.of());
    }

    private Optional<List<Object>> findAttachments(CardId cardId) {
        log.debug("첨부파일 정보 조회: cardId={}", cardId.getId());
        return Optional.of(List.of());
    }

    private Optional<List<Object>> findComments(CardId cardId) {
        log.debug("댓글 정보 조회: cardId={}", cardId.getId());
        return Optional.of(List.of());
    }

    private Optional<List<Object>> findBoardMembers(String listId) {
        log.debug("보드 멤버 정보 조회: listId={}", listId);
        return Optional.of(List.of());
    }

    private Optional<List<Label>> findBoardLabels(String listId) {
        log.debug("보드 라벨 정보 조회: listId={}", listId);
        return Optional.of(List.of());
    }

    private Optional<List<Object>> findActivities(CardId cardId) {
        log.debug("활동 내역 조회: cardId={}", cardId.getId());
        return Optional.of(List.of());
    }

    private Optional<User> findCreatedBy(UserId createdBy) {
        log.debug("생성자 정보 조회: createdBy={}", createdBy.getId());
        return Optional.empty();
    }

    /**
     * 카드 상세 정보 데이터를 담는 레코드
     */
    private record CardDetailData(
            Card card,
            List<Label> labels,
            List<CardMember> assignees,
            List<Object> attachments,
            List<Object> comments,
            List<Object> boardMembers,
            List<Label> boardLabels,
            List<Object> activities,
            User createdBy) {

        public CardDetailData withAssignees(List<CardMember> assignees) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }

        public CardDetailData withAttachments(List<Object> attachments) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }

        public CardDetailData withComments(List<Object> comments) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }

        public CardDetailData withBoardMembers(List<Object> boardMembers) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }

        public CardDetailData withBoardLabels(List<Label> boardLabels) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }

        public CardDetailData withActivities(List<Object> activities) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }

        public CardDetailData withCreatedBy(User createdBy) {
            return new CardDetailData(card, labels, assignees, attachments, comments,
                    boardMembers, boardLabels, activities, createdBy);
        }
    }
}
