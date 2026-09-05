package com.personal.lld.domain;

import lombok.Builder;

@Builder
public record IntersectionCreationPayload(int id, String name) {}