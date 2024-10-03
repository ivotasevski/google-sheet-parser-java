package com.ivotasevski.google_sheet_parser.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ParsedSheet {
    private String name;
    private List<Map<String, Object>> data = new ArrayList<>();
}
