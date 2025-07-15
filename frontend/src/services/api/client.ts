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
    message?: string;
    errorCode?: string;
    timestamp?: string;
    path?: string;
    details?: FieldViolation[];
};
export type UpdateUserRequest = {
    firstName?: string;
    lastName?: string;
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
    createdAt?: string;
    updatedAt?: string;
};
export type RegisterUserRequest = {
    email?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
};
export type CreateBoardRequest = {
    title?: string;
    description?: string;
};
/**
 * 내 정보 조회
 */
export function getUser(opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: UserResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 500;
        data: ErrorResponse;
    }>("/api/users", {
        ...opts
    });
}
/**
 * 사용자 업데이트
 */
export function updateUser(updateUserRequest: UpdateUserRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 200;
        data: UserResponse;
    } | {
        status: 404;
        data: ErrorResponse;
    } | {
        status: 422;
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
 * 사용자 등록
 */
export function registerUser(registerUserRequest: RegisterUserRequest, opts?: Oazapfts.RequestOpts) {
    return oazapfts.fetchJson<{
        status: 201;
        data: UserResponse;
    } | {
        status: 409;
        data: ErrorResponse;
    } | {
        status: 422;
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
