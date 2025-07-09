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
    message?: string;
    errorCode?: string;
    timestamp?: string;
    path?: string;
    details?: FieldViolation[];
};
export type RegisterUserRequest = {
    email?: string;
    password?: string;
    firstName?: string;
    lastName?: string;
};
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
