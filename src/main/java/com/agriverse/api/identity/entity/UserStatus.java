package com.agriverse.api.identity.entity;

/** Users.status per DB spec: active | suspended | pending_verification | deleted. */
public enum UserStatus {
    ACTIVE, SUSPENDED, PENDING_VERIFICATION, DELETED
}
