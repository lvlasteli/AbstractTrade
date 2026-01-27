package com.example.gateway.event.schema;

import com.example.gateway.constants.KafkaConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class IpBlockedEvent extends GatewayEvent {

    public static final String EVENT_TYPE = KafkaConstants.IP_BLOCKED;

    private String ipAddress;
    private String reason;
    private Integer blockDurationMinutes;
    private Long failedAttempts;
    private String userAgent;

    public static IpBlockedEvent create(String ipAddress, String reason, Integer blockDurationMinutes,
                                        Long failedAttempts, String userAgent) {
        IpBlockedEvent event = IpBlockedEvent.builder()
                .ipAddress(ipAddress)
                .reason(reason)
                .blockDurationMinutes(blockDurationMinutes)
                .failedAttempts(failedAttempts)
                .userAgent(userAgent)
                .build();
        event.initializeEvent(EVENT_TYPE, null);
        return event;
    }
}
