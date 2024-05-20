package dev.boarbot.entities.boaruser.data.collectibles;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CollectedBoar {
    private int num = 0;
    private List<Integer> editions = new ArrayList<>();
    private List<Long> editionDates = new ArrayList<>();
    private long firstObtained = 0;
    private long lastObtained = 0;
}
