/**
 * Boardly API
 * 1.0.0
 * DO NOT MODIFY - This file has been generated using oazapfts.
 * See https://www.npmjs.com/package/oazapfts
 */
import * as Oazapfts from "@oazapfts/runtime";
import * as QS from "@oazapfts/runtime/query";
export const defaults: Oazapfts.Defaults<Oazapfts.CustomHeaders> = {
    headers: {},
    baseUrl: "http://localhost:8080",
};
const oazapfts = Oazapfts.runtime(defaults);
export const servers = {
    server1: "http://localhost:8080"
};
export type UpdateUserRequest = {
    firstName?: string;
    lastName?: string;
};
export type UserResponse = {
    userId?: string;
    email?: string;
    firstName?: string;
    lastName?: string;
    isActive?: boolean;
};
export type FieldViolation = {
    field?: string;
    message?: string;
    rejectedValue?: unknown;
};
export type ErrorResponse = {
    code?: string;
    message?: string;
    timestamp?: string;
    path?: string;
    details?: FieldViolation[];
    context?: unknown;
};
export type LabelResponse = {
    /** 라벨 ID */
    labelId?: string;
    /** 보드 ID */
    boardId?: string;
    /** 라벨 이름 */
    name?: string;
    /** 라벨 색상 */
    color?: string;
    /** 생성 시간 */
    createdAt?: string;
    /** 수정 시간 */
    updatedAt?: string;
};
export type UpdateLabelRequest = {
    /** 라벨 이름 */
    name: string;
    /** 라벨 색상 (HEX 코드) */
    color: string;
};
export type CardResponse = {
    cardId?: string;
    title?: string;
    description?: string;
    position?: number;
    listId?: string;
    createdAt?: string;
    updatedAt?: string;
};
export type UpdateCardRequest = {
    title?: string;
    description?: string;
};
export type MoveCardRequest = {
    targetListId?: string;
    newPosition?: number;
};
export type CardLabelResponse = {
    id?: string;
    name?: string;
    color?: string;
};
export type CardAssigneeResponse = {
    userId?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
};
export type CardUserResponse = {
    userId?: string;
    firstName?: string;
    lastName?: string;
};
export type BoardCardResponse = {
    cardId?: string;
    title?: string;
    description?: string;
    position?: number;
    priority?: string;
    isCompleted?: boolean;
    isArchived?: boolean;
    dueDate?: string;
    startDate?: string;
    labels?: CardLabelResponse[];
    assignees?: CardAssigneeResponse[];
    attachmentCount?: number;
    commentCount?: number;
    lastCommentAt?: string;
    createdBy?: CardUserResponse;
    createdAt?: string;
    updatedAt?: string;
    completedAt?: string;
    completedBy?: CardUserResponse;
};
export type BoardColumnResponse = {
    columnId?: string;
    columnName?: string;
    columnColor?: string;
    position?: number;
    cardCount?: number;
    cards?: BoardCardResponse[];
};
export type BoardMemberResponse = {
    userId?: string;
    firstName?: string;
    lastName?: string;
    email?: string;
    role?: string;
    permissions?: string[];
    joinedAt?: string;
    lastActiveAt?: string;
    isActive?: boolean;
};
export type BoardLabelResponse = {
    id?: string;
    name?: string;
    color?: string;
    description?: string;
};
export type BoardDetailResponse = {
    boardId?: string;
    boardName?: string;
    boardDescription?: string;
    isStarred?: boolean;
    boardColor?: string;
    columns?: BoardColumnResponse[];
    boardMembers?: BoardMemberResponse[];
    labels?: BoardLabelResponse[];
    createdAt?: string;
    updatedAt?: string;
};
export type UpdateBoardRequest = {
    title?: string;
    description?: string;
};
export type BoardResponse = {
    boardId?: string;
    title?: string;
    description?: string;
    isArchived?: boolean;
    ownerId?: string;
    isStarred?: boolean;
    createdAt?: string;
    updatedAt?: string;
};
export type ListColor = {
    color: string;
};
export type UpdateBoardListRequest = {
    /** 리스트 제목 */
    title: string;
    /** 리스트 설명 */
    description?: string;
    /** 리스트 색상 */
    color?: string;
    listColor?: ListColor;
};
export type BoardListResponse = {
    listId?: string;
    title?: string;
    description?: string;
    position?: number;
    color?: string;
    boardId?: string;
    createdAt?: string;
    updatedAt?: string;
};
export type UpdateBoardListPositionRequest = {
    /** 새로운 위치 (0부터 시작) */
    position: number;
};
export type RegisterUserRequest = {
    email?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
};
export type CreateLabelRequest = {
    /** 라벨을 생성할 보드 ID */
    boardId: string;
    /** 라벨 이름 */
    name: string;
    /** 라벨 색상 (HEX 코드) */
    color: string;
};
export type CreateCardRequest = {
    title?: string;
    description?: string;
    listId?: string;
};
export type UserId = {
    id?: string;
};
export type CardMember = {
    userId?: UserId;
    assignedAt?: string;
};
export type AssignCardMemberRequest = {
    memberId: string;
};
export type UnassignCardMemberRequest = {
    memberId: string;
};
export type LabelId = {
    id?: string;
};
export type AddCardLabelRequest = {
    labelId: string;
};
export type RemoveCardLabelRequest = {
    labelId: string;
};
export type CloneCardRequest = {
    newTitle?: string;
    targetListId?: string;
};
export type CreateBoardRequest = {
    title?: string;
    description?: string;
};
export type CreateBoardListRequest = {
    /** 리스트 제목 */
    title: string;
    /** 리스트 설명 */
    description?: string;
    /** 리스트 색상 */
    color?: string;
    listColor?: ListColor;
};
export type BoardSummaryDto = {
    id?: string;
    title?: string;
    description?: string;
    createdAt?: string;
    listCount?: number;
    cardCount?: number;
    isStarred?: boolean;
    color?: string;
    role?: string;
};
export type ActorResponse = {
    id?: string;
    firstName?: string;
    lastName?: string;
    profileImageUrl?: string;
};
export type ActivityResponse = {
    id?: string;
    "type"?: "CARD_CREATE" | "CARD_MOVE" | "CARD_RENAME" | "CARD_ARCHIVE" | "CARD_UNARCHIVE" | "CARD_DELETE" | "CARD_ASSIGN_MEMBER" | "CARD_UNASSIGN_MEMBER" | "CARD_SET_DUE_DATE" | "CARD_REMOVE_DUE_DATE" | "CARD_ADD_COMMENT" | "CARD_ADD_ATTACHMENT" | "CARD_REMOVE_ATTACHMENT" | "CARD_ADD_CHECKLIST" | "CARD_ADD_LABEL" | "CARD_REMOVE_LABEL" | "CARD_DUPLICATE" | "CARD_UPDATE_DESCRIPTION" | "LIST_CREATE" | "LIST_RENAME" | "LIST_ARCHIVE" | "LIST_UNARCHIVE" | "LIST_MOVE" | "LIST_CHANGE_COLOR" | "LIST_DELETE" | "BOARD_CREATE" | "BOARD_RENAME" | "BOARD_ARCHIVE" | "BOARD_UNARCHIVE" | "BOARD_MOVE" | "BOARD_DELETE" | "BOARD_UPDATE_DESCRIPTION" | "BOARD_ADD_MEMBER" | "BOARD_REMOVE_MEMBER" | "BOARD_UPDATE_MEMBER_ROLE" | "BOARD_DUPLICATE" | "USER_UPDATE_PROFILE" | "USER_CHANGE_LANGUAGE" | "USER_CHANGE_PASSWORD" | "USER_DELETE_ACCOUNT";
    actor?: ActorResponse;
    timestamp?: string;
    payload?: {
        [key: string]: unknown;
    };
    boardName?: string;
    boardId?: string;
};
export type DashboardStatisticsDto = {
    totalBoards?: number;
    totalCards?: number;
    starredBoards?: number;
    archivedBoards?: number;
};
export type DashboardResponse = {
    boards?: BoardSummaryDto[];
    recentActivity?: ActivityResponse[];
    statistics?: DashboardStatisticsDto;
};
export type ActivityListResponse = {
    activities?: ActivityResponse[];
    totalCount?: number;
    currentPage?: number;
    totalPages?: number;
    hasNextPage?: boolean;
    hasPreviousPage?: boolean;
};
/**
 * 사용자 업데이트
 */
export function updateUser(updateUserRequest: UpdateUserRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: UserResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/users", oazapfts.json({
        ...opts,
        method: "PUT",
        body: updateUserRequest
    }));
}
/**
 * 라벨 조회
 */
export function getLabel(labelId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: LabelResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/labels/${encodeURIComponent(labelId)}`, {
        ...opts
    });
}
/**
 * 라벨 수정
 */
export function updateLabel(labelId: string, updateLabelRequest: UpdateLabelRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: LabelResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/labels/${encodeURIComponent(labelId)}`, oazapfts.json({
        ...opts,
        method: "PUT",
        body: updateLabelRequest
    }));
}
/**
 * 라벨 삭제
 */
export function deleteLabel(labelId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 204;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/labels/${encodeURIComponent(labelId)}`, {
        ...opts,
        method: "DELETE"
    });
}
/**
 * 카드 조회
 */
export function getCard(cardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: CardResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}`, {
        ...opts
    });
}
/**
 * 카드 수정
 */
export function updateCard(cardId: string, updateCardRequest: UpdateCardRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: CardResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}`, oazapfts.json({
        ...opts,
        method: "PUT",
        body: updateCardRequest
    }));
}
/**
 * 카드 삭제
 */
export function deleteCard(cardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 204;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}`, {
        ...opts,
        method: "DELETE"
    });
}
/**
 * 카드 이동
 */
export function moveCard(cardId: string, moveCardRequest: MoveCardRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: CardResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/move`, oazapfts.json({
        ...opts,
        method: "PUT",
        body: moveCardRequest
    }));
}
/**
 * 보드 상세 조회
 */
export function getBoardDetail(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardDetailResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}`, {
        ...opts
    });
}
/**
 * 보드 업데이트
 */
export function updateBoard(boardId: string, updateBoardRequest: UpdateBoardRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}`, oazapfts.json({
        ...opts,
        method: "PUT",
        body: updateBoardRequest
    }));
}
/**
 * 보드 삭제
 */
export function deleteBoard(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 204;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}`, {
        ...opts,
        method: "DELETE"
    });
}
/**
 * 보드 리스트 수정 (Update)
 */
export function updateBoardList(listId: string, updateBoardListRequest: UpdateBoardListRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardListResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/board-lists/${encodeURIComponent(listId)}`, oazapfts.json({
        ...opts,
        method: "PUT",
        body: updateBoardListRequest
    }));
}
/**
 * 보드 리스트 삭제 (Delete)
 */
export function deleteBoardList(listId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 204;
        data: object;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/board-lists/${encodeURIComponent(listId)}`, {
        ...opts,
        method: "DELETE"
    });
}
/**
 * 보드 리스트 위치 변경 (Update Position)
 */
export function updateBoardListPosition(listId: string, updateBoardListPositionRequest: UpdateBoardListPositionRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardListResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/board-lists/${encodeURIComponent(listId)}/position`, oazapfts.json({
        ...opts,
        method: "PUT",
        body: updateBoardListPositionRequest
    }));
}
/**
 * 사용자 등록
 */
export function registerUser(registerUserRequest: RegisterUserRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: UserResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/users/register", oazapfts.json({
        ...opts,
        method: "POST",
        body: registerUserRequest
    }));
}
/**
 * 라벨 생성
 */
export function createLabel(createLabelRequest: CreateLabelRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: LabelResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/labels", oazapfts.json({
        ...opts,
        method: "POST",
        body: createLabelRequest
    }));
}
/**
 * 카드 생성
 */
export function createCard(createCardRequest: CreateCardRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: CardResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/cards", oazapfts.json({
        ...opts,
        method: "POST",
        body: createCardRequest
    }));
}
/**
 * 카드 멤버 목록 조회
 */
export function getCardMembers(cardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: CardMember[];
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/members`, {
        ...opts
    });
}
/**
 * 카드 멤버 할당
 */
export function assignCardMember(cardId: string, assignCardMemberRequest: AssignCardMemberRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/members`, oazapfts.json({
        ...opts,
        method: "POST",
        body: assignCardMemberRequest
    }));
}
/**
 * 카드 멤버 해제
 */
export function unassignCardMember(cardId: string, unassignCardMemberRequest: UnassignCardMemberRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/members`, oazapfts.json({
        ...opts,
        method: "DELETE",
        body: unassignCardMemberRequest
    }));
}
/**
 * 카드 라벨 목록 조회
 */
export function getCardLabels(cardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: LabelId[];
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/labels`, {
        ...opts
    });
}
/**
 * 카드 라벨 추가
 */
export function addCardLabel(cardId: string, addCardLabelRequest: AddCardLabelRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/labels`, oazapfts.json({
        ...opts,
        method: "POST",
        body: addCardLabelRequest
    }));
}
/**
 * 카드 라벨 제거
 */
export function removeCardLabel(cardId: string, removeCardLabelRequest: RemoveCardLabelRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/labels`, oazapfts.json({
        ...opts,
        method: "DELETE",
        body: removeCardLabelRequest
    }));
}
/**
 * 카드 복제
 */
export function cloneCard(cardId: string, cloneCardRequest: CloneCardRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: CardResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/${encodeURIComponent(cardId)}/clone`, oazapfts.json({
        ...opts,
        method: "POST",
        body: cloneCardRequest
    }));
}
/**
 * 내 보드 목록 조회
 */
export function getMyBoards({ includeArchived }: {
    includeArchived?: boolean;
} = {}, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardResponse[];
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards${QS.query(QS.explode({
        includeArchived
    }))}`, {
        ...opts
    });
}
/**
 * 보드 생성
 */
export function createBoard(createBoardRequest: CreateBoardRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: BoardResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/boards", oazapfts.json({
        ...opts,
        method: "POST",
        body: createBoardRequest
    }));
}
/**
 * 보드 즐겨찾기 제거
 */
export function unstarBoard(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}/unstar`, {
        ...opts,
        method: "POST"
    });
}
/**
 * 보드 언아카이브
 */
export function unarchiveBoard(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}/unarchive`, {
        ...opts,
        method: "POST"
    });
}
/**
 * 보드 즐겨찾기 추가
 */
export function starBoard(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}/star`, {
        ...opts,
        method: "POST"
    });
}
/**
 * 보드 아카이브
 */
export function archiveBoard(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}/archive`, {
        ...opts,
        method: "POST"
    });
}
/**
 * 보드 리스트 목록 조회 (Read)
 */
export function getBoardLists(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: BoardListResponse[];
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/board-lists/${encodeURIComponent(boardId)}`, {
        ...opts
    });
}
/**
 * 보드 리스트 생성 (Create)
 */
export function createBoardList(boardId: string, createBoardListRequest: CreateBoardListRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: BoardListResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/board-lists/${encodeURIComponent(boardId)}`, oazapfts.json({
        ...opts,
        method: "POST",
        body: createBoardListRequest
    }));
}
/**
 * 보드 라벨 목록 조회
 */
export function getBoardLabels(boardId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: LabelResponse[];
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/labels/board/${encodeURIComponent(boardId)}`, {
        ...opts
    });
}
/**
 * 대시보드 조회
 */
export function getDashboard(opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: DashboardResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/dashboard", {
        ...opts
    });
}
/**
 * 리스트별 카드 목록 조회
 */
export function getCardsByListId(listId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: CardResponse[];
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/lists/${encodeURIComponent(listId)}`, {
        ...opts
    });
}
/**
 * 카드 검색
 */
export function searchCards(listId: string, searchTerm: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: CardResponse[];
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/cards/lists/${encodeURIComponent(listId)}/search${QS.query(QS.explode({
        searchTerm
    }))}`, {
        ...opts
    });
}
/**
 * 내 활동 목록 조회
 */
export function getMyActivities({ page, size }: {
    page?: number;
    size?: number;
} = {}, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: ActivityListResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/activities/me${QS.query(QS.explode({
        page,
        size
    }))}`, {
        ...opts
    });
}
/**
 * 보드 활동 목록 조회
 */
export function getBoardActivities(boardId: string, { page, size, since }: {
    page?: number;
    size?: number;
    since?: string;
} = {}, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: ActivityListResponse;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/activities/boards/${encodeURIComponent(boardId)}${QS.query(QS.explode({
        page,
        size,
        since
    }))}`, {
        ...opts
    });
}
/**
 * 보드 멤버 삭제
 */
export function removeBoardMember(boardId: string, targetUserId: string, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: object;
    } | {
        status: 400;
        data: ErrorResponse;
    } | {
        status: 403;
        data: ErrorResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>(`/api/boards/${encodeURIComponent(boardId)}/members/${encodeURIComponent(targetUserId)}`, {
        ...opts,
        method: "DELETE"
    });
}
