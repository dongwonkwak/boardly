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
export type CreateCardRequest = {
    title?: string;
    description?: string;
    listId?: string;
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
 * 보드 리스트 수정
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
 * 보드 리스트 삭제
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
 * 보드 리스트 위치 변경
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
 * 보드 리스트 목록 조회
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
 * 보드 리스트 생성
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
