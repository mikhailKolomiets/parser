import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Builder
public class Game {
    private String gameType;
    private String leagueName;
    private long id;
    private String firstTeam;
    private String secondTeam;
    private LocalDateTime startTime;

    //todo fix current year
    public void setStartTime(String date, String time) {
        startTime = LocalDateTime.parse(date + ".2021." + time, DateTimeFormatter.ofPattern("dd.MM.yyyy.HH:mm"));
    }

    @Override
    public String toString() {
        return gameType + ", " + leagueName + ", " + firstTeam + " - " + secondTeam + ", " +
                startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + ", " + id;
    }
}
