package com.bot.cinemabot.model.cgv;

import lombok.Data;
import java.util.List;

@Data
public class CgvApiData {
    private int startRow;
    private int listCount;
    private boolean skipCount;
    private int totalCount;
    private String orderBy;
    private List<CgvItem> list;
}
