import { RefinedResponse, ResponseType } from 'k6/http';
import { bodyId } from './utils';
//@ts-ignore
import * as grpc from 'k6/net/grpc';

export function isOk(response: RefinedResponse<ResponseType>): boolean {
    return response && (response.status === 200 || response.status === grpc.StatusOK);
}

export function isCreated(response: RefinedResponse<ResponseType>): boolean {
    return isGrpcOK(response) || response.status === 201;
}

export function isPartial(response: RefinedResponse<ResponseType>): boolean {
    return isGrpcOK(response) || response.status === 206;
}

export function isNotFound(response: RefinedResponse<ResponseType>): boolean {
    return response.status === grpc.StatusNotFound || response.status === 404;
}

export function isConflict(response: RefinedResponse<ResponseType>): boolean {
    return response.status === grpc.StatusAlreadyExists || response.status === 409;
}

export function body(response) {
    return response.message || response.json();
}

export function isArray(response) {
    return Array.isArray(body(response));
}

export function hasSize(response, expectedSize: number) {
    return isArray(response) && (body(response) as any).length === expectedSize;
}

export function hasAtLeastSize(response, expectedSize: number) {
    return isArray(response) && (body(response) as any).length >= expectedSize;
}

export function hasId(response): boolean {
    const responseId = body(response)?.id?.toString();
    return responseId && RegExp('^[0-9a-f]{24}\$').test(responseId);
}

export function sameId(response, expectedId: string): boolean {
    return hasId(response) && expectedId === bodyId(response);
}

function isGrpcOK(response) {
    return response.status === grpc.StatusOK
}
