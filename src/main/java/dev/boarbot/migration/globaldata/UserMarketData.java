package dev.boarbot.migration.globaldata;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class UserMarketData {
    private int bucks = 0;
    private List<String> itemIDs = new ArrayList<>();
    private List<Long> editionDates = new ArrayList<>();
}
