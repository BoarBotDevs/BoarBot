package dev.boarbot.migration.userdata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class NewBoarData implements Comparable<NewBoarData> {
    private String userID;
    private long obtainedTime;

    public NewBoarData(String userID, long obtainedTime) {
        this.userID = userID;
        this.obtainedTime = obtainedTime;
    }

    public int compareTo(NewBoarData otherObj) {
        return Long.compare(this.obtainedTime, otherObj.obtainedTime);
    }
}
