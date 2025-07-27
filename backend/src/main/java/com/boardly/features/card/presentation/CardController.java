package com.boardly.features.card.presentation;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import com.boardly.features.card.application.port.input.CreateCardCommand;
import com.boardly.features.card.application.port.input.UpdateCardCommand;
import com.boardly.features.card.application.port.input.MoveCardCommand;
import com.boardly.features.card.application.port.input.CloneCardCommand;
import com.boardly.features.card.application.port.input.DeleteCardCommand;
import com.boardly.features.card.application.port.input.AssignCardMemberCommand;
import com.boardly.features.card.application.port.input.UnassignCardMemberCommand;
import com.boardly.features.card.application.port.input.AddCardLabelCommand;
import com.boardly.features.card.application.port.input.RemoveCardLabelCommand;
import com.boardly.features.card.application.usecase.CreateCardUseCase;
import com.boardly.features.card.application.usecase.CardQueryUseCase;
import com.boardly.features.card.application.usecase.UpdateCardUseCase;
import com.boardly.features.card.application.usecase.MoveCardUseCase;
import com.boardly.features.card.application.usecase.CloneCardUseCase;
import com.boardly.features.card.application.usecase.DeleteCardUseCase;
import com.boardly.features.card.application.usecase.ManageCardMemberUseCase;
import com.boardly.features.card.application.usecase.ManageCardLabelUseCase;
import com.boardly.features.card.domain.model.Card;
import com.boardly.features.card.domain.valueobject.CardMember;
import com.boardly.features.card.presentation.request.CreateCardRequest;
import com.boardly.features.card.presentation.request.UpdateCardRequest;
import com.boardly.features.card.presentation.request.MoveCardRequest;
import com.boardly.features.card.presentation.request.CloneCardRequest;
import com.boardly.features.card.presentation.request.AssignCardMemberRequest;
import com.boardly.features.card.presentation.request.UnassignCardMemberRequest;
import com.boardly.features.card.presentation.request.AddCardLabelRequest;
import com.boardly.features.card.presentation.request.RemoveCardLabelRequest;
import com.boardly.features.card.presentation.response.CardResponse;
import com.boardly.features.boardlist.domain.model.ListId;
import com.boardly.features.user.domain.model.UserId;
import com.boardly.features.card.domain.model.CardId;
import com.boardly.features.label.domain.model.LabelId;
import com.boardly.shared.domain.common.Failure;
import com.boardly.shared.presentation.ApiFailureHandler;
import com.boardly.shared.presentation.Path;
import com.boardly.shared.presentation.response.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.vavr.control.Either;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Tag(name = "Cards", description = "카드 관련 API")
@Slf4j
@RestController
@RequestMapping(Path.CARDS)
@RequiredArgsConstructor
public class CardController {

    private final CreateCardUseCase createCardUseCase;
    private final CardQueryUseCase cardQueryUseCase;
    private final UpdateCardUseCase updateCardUseCase;
    private final MoveCardUseCase moveCardUseCase;
    private final CloneCardUseCase cloneCardUseCase;
    private final DeleteCardUseCase deleteCardUseCase;
    private final ManageCardMemberUseCase manageCardMemberUseCase;
    private final ManageCardLabelUseCase manageCardLabelUseCase;
    private final ApiFailureHandler failureHandler;

    @Operation(summary = "카드 생성", description = "새로운 카드를 생성합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카드 생성 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 생성 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping
    public ResponseEntity<?> createCard(
            @Parameter(description = "카드 생성 요청 정보", required = true) @RequestBody CreateCardRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 생성 요청: userId={}, title={}, listId={}", userId, request.title(), request.listId());

        CreateCardCommand command = CreateCardCommand.of(
                request.title(),
                request.description(),
                new ListId(request.listId()),
                new UserId(userId));

        Either<Failure, Card> result = createCardUseCase.createCard(command);

        return result.fold(
                failureHandler::handleFailure,
                card -> {
                    log.info("카드 생성 성공: cardId={}, title={}", card.getCardId().getId(),
                            card.getTitle());
                    return ResponseEntity.status(HttpStatus.CREATED).body(CardResponse.from(card));
                });
    }

    @Operation(summary = "카드 조회", description = "특정 카드의 상세 정보를 조회합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/{cardId}")
    public ResponseEntity<?> getCard(
            @Parameter(description = "조회할 카드 ID", required = true) @PathVariable String cardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 조회 요청: userId={}, cardId={}", userId, cardId);

        Either<Failure, Card> result = cardQueryUseCase.getCard(
                new CardId(cardId),
                new UserId(userId));

        return result.fold(
                failureHandler::handleFailure,
                card -> {
                    log.info("카드 조회 성공: cardId={}, title={}", card.getCardId().getId(),
                            card.getTitle());
                    return ResponseEntity.ok(CardResponse.from(card));
                });
    }

    @Operation(summary = "리스트별 카드 목록 조회", description = "특정 리스트에 속한 모든 카드를 위치 순서대로 조회합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CardResponse.class)))),
            @ApiResponse(responseCode = "403", description = "카드 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/lists/{listId}")
    public ResponseEntity<?> getCardsByListId(
            @Parameter(description = "조회할 리스트 ID", required = true) @PathVariable String listId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("리스트별 카드 목록 조회 요청: userId={}, listId={}", userId, listId);

        Either<Failure, List<Card>> result = cardQueryUseCase.getCardsByListId(
                new ListId(listId),
                new UserId(userId));

        return result.fold(
                failureHandler::handleFailure,
                cards -> {
                    log.info("리스트별 카드 목록 조회 성공: listId={}, 카드 개수={}", listId, cards.size());
                    List<CardResponse> responses = cards.stream()
                            .map(CardResponse::from)
                            .toList();
                    return ResponseEntity.ok(responses);
                });
    }

    @Operation(summary = "카드 수정", description = "카드의 제목과 설명을 수정합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 수정 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 수정 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "아카이브된 보드의 카드는 수정 불가", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PutMapping("/{cardId}")
    public ResponseEntity<?> updateCard(
            @Parameter(description = "수정할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 수정 요청 정보", required = true) @RequestBody UpdateCardRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 수정 요청: userId={}, cardId={}, title={}", userId, cardId, request.title());

        UpdateCardCommand command = UpdateCardCommand.of(
                new CardId(cardId),
                request.title(),
                request.description(),
                new UserId(userId));

        Either<Failure, Card> result = updateCardUseCase.updateCard(command);

        return result.fold(
                failureHandler::handleFailure,
                card -> {
                    log.info("카드 수정 성공: cardId={}, title={}", card.getCardId().getId(),
                            card.getTitle());
                    return ResponseEntity.ok(CardResponse.from(card));
                });
    }

    @Operation(summary = "카드 이동", description = "카드를 같은 리스트 내에서 이동하거나 다른 리스트로 이동합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 이동 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 이동 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드 또는 리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "아카이브된 보드의 카드는 이동 불가", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PutMapping("/{cardId}/move")
    public ResponseEntity<?> moveCard(
            @Parameter(description = "이동할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 이동 요청 정보", required = true) @RequestBody MoveCardRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 이동 요청: userId={}, cardId={}, targetListId={}, newPosition={}",
                userId, cardId, request.targetListId(), request.newPosition());

        MoveCardCommand command = MoveCardCommand.of(
                new CardId(cardId),
                request.targetListId() != null ? new ListId(request.targetListId()) : null,
                request.newPosition(),
                new UserId(userId));

        Either<Failure, Card> result = moveCardUseCase.moveCard(command);

        return result.fold(
                failureHandler::handleFailure,
                card -> {
                    log.info("카드 이동 성공: cardId={}, newListId={}, newPosition={}",
                            card.getCardId().getId(), card.getListId().getId(),
                            card.getPosition());
                    return ResponseEntity.ok(CardResponse.from(card));
                });
    }

    @Operation(summary = "카드 복제", description = "기존 카드의 내용을 복사하여 새로운 카드를 생성합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "카드 복제 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 복제 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드 또는 리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "아카이브된 보드의 카드는 복제 불가 또는 리스트 카드 개수 제한 초과", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{cardId}/clone")
    public ResponseEntity<?> cloneCard(
            @Parameter(description = "복제할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 복제 요청 정보", required = true) @RequestBody CloneCardRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 복제 요청: userId={}, cardId={}, newTitle={}, targetListId={}",
                userId, cardId, request.newTitle(), request.targetListId());

        CloneCardCommand command = CloneCardCommand.of(
                new CardId(cardId),
                request.newTitle(),
                request.targetListId() != null ? new ListId(request.targetListId()) : null,
                new UserId(userId));

        Either<Failure, Card> result = cloneCardUseCase.cloneCard(command);

        return result.fold(
                failureHandler::handleFailure,
                card -> {
                    log.info("카드 복제 성공: originalCardId={}, clonedCardId={}, title={}, listId={}",
                            cardId, card.getCardId().getId(), card.getTitle(),
                            card.getListId().getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(CardResponse.from(card));
                });
    }

    @Operation(summary = "카드 삭제", description = "카드를 영구적으로 삭제합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "카드 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 삭제 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "아카이브된 보드의 카드는 삭제 불가", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCard(
            @Parameter(description = "삭제할 카드 ID", required = true) @PathVariable String cardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 삭제 요청: userId={}, cardId={}", userId, cardId);

        DeleteCardCommand command = DeleteCardCommand.of(
                new CardId(cardId),
                new UserId(userId));

        Either<Failure, Void> result = deleteCardUseCase.deleteCard(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("카드 삭제 성공: cardId={}", cardId);
                    return ResponseEntity.noContent().build();
                });
    }

    @Operation(summary = "카드 검색", description = "특정 리스트에서 카드 제목으로 검색합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 검색 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CardResponse.class)))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 검색 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "리스트를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/lists/{listId}/search")
    public ResponseEntity<?> searchCards(
            @Parameter(description = "검색할 리스트 ID", required = true) @PathVariable String listId,
            @Parameter(description = "검색어", required = true) @RequestParam String searchTerm,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 검색 요청: userId={}, listId={}, searchTerm={}", userId, listId, searchTerm);

        Either<Failure, List<Card>> result = cardQueryUseCase.searchCards(
                new ListId(listId),
                searchTerm,
                new UserId(userId));

        return result.fold(
                failureHandler::handleFailure,
                cards -> {
                    log.info("카드 검색 성공: listId={}, searchTerm={}, 검색 결과 개수={}", listId, searchTerm,
                            cards.size());
                    List<CardResponse> responses = cards.stream()
                            .map(CardResponse::from)
                            .toList();
                    return ResponseEntity.ok(responses);
                });
    }

    // =================================================================
    // 카드 멤버 관리 API
    // =================================================================

    @Operation(summary = "카드 멤버 할당", description = "카드에 멤버를 할당합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 멤버 할당 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 멤버 할당 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드 또는 멤버를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "멤버가 이미 할당되어 있음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{cardId}/members")
    public ResponseEntity<?> assignCardMember(
            @Parameter(description = "멤버를 할당할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 멤버 할당 요청 정보", required = true) @RequestBody AssignCardMemberRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 멤버 할당 요청: userId={}, cardId={}, memberId={}", userId, cardId, request.memberId());

        AssignCardMemberCommand command = new AssignCardMemberCommand(
                new CardId(cardId),
                new UserId(request.memberId()),
                new UserId(userId));

        Either<Failure, Void> result = manageCardMemberUseCase.assignMember(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("카드 멤버 할당 성공: cardId={}, memberId={}", cardId, request.memberId());
                    return ResponseEntity.ok().build();
                });
    }

    @Operation(summary = "카드 멤버 해제", description = "카드에서 멤버를 해제합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 멤버 해제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 멤버 해제 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드 또는 멤버를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @DeleteMapping("/{cardId}/members")
    public ResponseEntity<?> unassignCardMember(
            @Parameter(description = "멤버를 해제할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 멤버 해제 요청 정보", required = true) @RequestBody UnassignCardMemberRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 멤버 해제 요청: userId={}, cardId={}, memberId={}", userId, cardId, request.memberId());

        UnassignCardMemberCommand command = new UnassignCardMemberCommand(
                new CardId(cardId),
                new UserId(request.memberId()),
                new UserId(userId));

        Either<Failure, Void> result = manageCardMemberUseCase.unassignMember(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("카드 멤버 해제 성공: cardId={}, memberId={}", cardId, request.memberId());
                    return ResponseEntity.ok().build();
                });
    }

    @Operation(summary = "카드 멤버 목록 조회", description = "카드에 할당된 멤버 목록을 조회합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 멤버 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = CardMember.class)))),
            @ApiResponse(responseCode = "403", description = "카드 멤버 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/{cardId}/members")
    public ResponseEntity<?> getCardMembers(
            @Parameter(description = "멤버를 조회할 카드 ID", required = true) @PathVariable String cardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 멤버 목록 조회 요청: userId={}, cardId={}", userId, cardId);

        List<CardMember> members = manageCardMemberUseCase.getCardMembers(
                new CardId(cardId),
                new UserId(userId));

        log.info("카드 멤버 목록 조회 성공: cardId={}, 멤버 개수={}", cardId, members.size());
        return ResponseEntity.ok(members);
    }

    // =================================================================
    // 카드 라벨 관리 API
    // =================================================================

    @Operation(summary = "카드 라벨 추가", description = "카드에 라벨을 추가합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 라벨 추가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 라벨 추가 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드 또는 라벨을 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "라벨이 이미 추가되어 있음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @PostMapping("/{cardId}/labels")
    public ResponseEntity<?> addCardLabel(
            @Parameter(description = "라벨을 추가할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 라벨 추가 요청 정보", required = true) @RequestBody AddCardLabelRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 라벨 추가 요청: userId={}, cardId={}, labelId={}", userId, cardId, request.labelId());

        AddCardLabelCommand command = new AddCardLabelCommand(
                new CardId(cardId),
                new LabelId(request.labelId()),
                new UserId(userId));

        Either<Failure, Void> result = manageCardLabelUseCase.addLabel(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("카드 라벨 추가 성공: cardId={}, labelId={}", cardId, request.labelId());
                    return ResponseEntity.ok().build();
                });
    }

    @Operation(summary = "카드 라벨 제거", description = "카드에서 라벨을 제거합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "write", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 라벨 제거 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "카드 라벨 제거 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드 또는 라벨을 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_write') and hasAuthority('SCOPE_openid')")
    @DeleteMapping("/{cardId}/labels")
    public ResponseEntity<?> removeCardLabel(
            @Parameter(description = "라벨을 제거할 카드 ID", required = true) @PathVariable String cardId,
            @Parameter(description = "카드 라벨 제거 요청 정보", required = true) @RequestBody RemoveCardLabelRequest request,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 라벨 제거 요청: userId={}, cardId={}, labelId={}", userId, cardId, request.labelId());

        RemoveCardLabelCommand command = new RemoveCardLabelCommand(
                new CardId(cardId),
                new LabelId(request.labelId()),
                new UserId(userId));

        Either<Failure, Void> result = manageCardLabelUseCase.removeLabel(command);

        return result.fold(
                failureHandler::handleFailure,
                success -> {
                    log.info("카드 라벨 제거 성공: cardId={}, labelId={}", cardId, request.labelId());
                    return ResponseEntity.ok().build();
                });
    }

    @Operation(summary = "카드 라벨 목록 조회", description = "카드에 추가된 라벨 목록을 조회합니다.", tags = {
            "Cards" }, security = @SecurityRequirement(name = "oauth2", scopes = { "read", "openid" }))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "카드 라벨 목록 조회 성공", content = @Content(mediaType = APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = LabelId.class)))),
            @ApiResponse(responseCode = "403", description = "카드 라벨 조회 권한 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "카드를 찾을 수 없음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "입력 값이 유효하지 않음", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(mediaType = APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PreAuthorize("hasAuthority('SCOPE_read') and hasAuthority('SCOPE_openid')")
    @GetMapping("/{cardId}/labels")
    public ResponseEntity<?> getCardLabels(
            @Parameter(description = "라벨을 조회할 카드 ID", required = true) @PathVariable String cardId,
            HttpServletRequest httpRequest,
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getSubject();
        log.info("카드 라벨 목록 조회 요청: userId={}, cardId={}", userId, cardId);

        List<LabelId> labels = manageCardLabelUseCase.getCardLabels(
                new CardId(cardId),
                new UserId(userId));

        log.info("카드 라벨 목록 조회 성공: cardId={}, 라벨 개수={}", cardId, labels.size());
        return ResponseEntity.ok(labels);
    }

}
