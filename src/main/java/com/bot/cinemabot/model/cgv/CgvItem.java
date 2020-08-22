package com.bot.cinemabot.model.cgv;

import lombok.Data;
import org.springframework.util.StringUtils;

@Data
public class CgvItem {
    private int idx;
    private String imageUrl;
    private String link;
    private String description;

    public String getDescription () {
        if (StringUtils.isEmpty(this.description)) {
            return this.description;
        }
        return this.description.replaceAll("\\n\\r?", " ");
    }
}
