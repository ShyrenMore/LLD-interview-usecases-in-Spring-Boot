package com.personal.lld.core;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LogConfiguration {

    private LogLevel rootLevel = LogLevel.INFO; // Default level
}
