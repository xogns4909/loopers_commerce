package com.loopers.application.ranking.dto;

import com.loopers.interfaces.api.ranking.RankingEntry;
import java.time.LocalDate;
import java.util.List;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankingResult {
    private LocalDate actualDate;
    private List<RankingEntry> entries;
    private String source; // "redis" | "db-weekly" | "db-monthly" | "empty"

    public boolean isEmpty() {
        return entries == null || entries.isEmpty();
    }
}
