package com.ivotasevski.google_sheet_parser.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParsedData {
    private String id;
    private List<ParsedSheet> sheets = new ArrayList<>();
}
