package com.ivotasevski.google_sheet_parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Data
@Slf4j
public class ParsingConfig {

    private String sheetId;
    private Boolean includeNullValuesInOutput = true;
    private List<SubsheetConfig> subSheets = new ArrayList<>();

    @Data
    public static class SubsheetConfig {
        private String name;
        private Integer headerRowPosition;
    }

    @JsonIgnore
    public Integer getHeaderRowPositionForSheet(String sheetName) {
        var subsheetConfig = subSheets.stream().filter(subsheet -> subsheet.getName().equals(sheetName)).findFirst().orElse(null);
        if (subsheetConfig != null
                && subsheetConfig.getHeaderRowPosition() != null) {
            return subsheetConfig.getHeaderRowPosition();
        } else {
            log.warn("Header row position has not been configured for sheet='{}'. Generic column names will be used.", sheetName);
            return null;
        }
    }
}
