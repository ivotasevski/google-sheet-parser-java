package com.ivotasevski.google_sheet_parser.controller;

import com.ivotasevski.google_sheet_parser.dto.ParsedData;
import com.ivotasevski.google_sheet_parser.dto.ParsingConfig;
import com.ivotasevski.google_sheet_parser.service.GoogleSheetParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SheetParserController {

    private final GoogleSheetParserService googleSheetParserService;

    @PostMapping("/parse")
    public ParsedData parseSheetData(@RequestBody ParsingConfig config) throws Exception {
        return googleSheetParserService.parse(config);
    }
}
