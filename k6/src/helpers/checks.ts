import { hasId, isConflict, isCreated, isNotFound, isOk, sameId } from './assertions';

export const createResponseChecks = {
    'http code is created or grpc ok': isCreated,
    'response includes an id': hasId
};

export const notFoundResponseChecks = {
    'http code is not found': isNotFound
}

export const conflictResponseChecks = {
    'http code is conflict': isConflict
}

export function okResponseChecks(expectedId: string) {
    return {
        'http code is ok': isOk,
        'response has same id': r => sameId(r, expectedId)
    };
}
