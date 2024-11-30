package dev.boarbot.entities.boaruser.data;

import dev.boarbot.util.time.TimeUtil;

public record AdventData(int adventBits, int adventYear) {
    public AdventData() {
        this(0, TimeUtil.getYear());
    }
}
