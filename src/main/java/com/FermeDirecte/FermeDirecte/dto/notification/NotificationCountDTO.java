package com.FermeDirecte.FermeDirecte.dto.notification;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class NotificationCountDTO {
    private long total;
    private long nonLues;
}
