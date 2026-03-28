package com.selfinterior.api.common.api;

public record ApiError(String code, String message, String requestId) {}
