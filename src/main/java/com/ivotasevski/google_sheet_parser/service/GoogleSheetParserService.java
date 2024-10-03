package com.ivotasevski.google_sheet_parser.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.ivotasevski.google_sheet_parser.dto.ParsedData;
import com.ivotasevski.google_sheet_parser.dto.ParsedSheet;
import com.ivotasevski.google_sheet_parser.dto.ParsingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSheetParserService {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${app.google.auth.credentials}")
    private String authCredentials;

    public ParsedData parse(ParsingConfig config) throws Exception {

        ByteArrayInputStream credentialsStream = new ByteArrayInputStream(Base64.decodeBase64(authCredentials));

        // Authentication/Authorization against Google API
        GoogleCredential credentials = GoogleCredential.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS_READONLY));
        Sheets service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), GsonFactory.getDefaultInstance(),
                credentials).setApplicationName(applicationName)
                .build();

        // Get the data from the Spreadsheet
        var sheetData = service.spreadsheets().values()
                .batchGet(config.getSheetId())
                .setRanges(config.getSubSheets().stream().map(s -> s.getName()).toList())
                .execute();

        ParsedData parsedData = new ParsedData();
        parsedData.setId(config.getSheetId());

        // every value range item contains the data about a single sub-sheet
        for (var r = 0; r < sheetData.getValueRanges().size(); r++) {
            ValueRange valueRange = sheetData.getValueRanges().get(r);

            // the order of returned sub-sheet values is always the same as the one specified in config.
            // use the config to find the name of the sub-sheet
            var parsedSheet = new ParsedSheet();
            var subSheetName = config.getSubSheets().get(r).getName();
            parsedSheet.setName(subSheetName);

            var rows = valueRange.getValues();

            // parse the header row and extract the column names. If no header row is specified, generic names for columns will be used.
            var headerRowPosition = config.getHeaderRowPositionForSheet(subSheetName);
            Map<Integer, String> columnNames = new HashMap<>();
            if (headerRowPosition != null) {
                if (rows.size() <= headerRowPosition) {
                    throw new RuntimeException(String.format("The header row position (=%s) is greater than the number of rows in the sheet range (Sheet: %s, noRows: %s)", headerRowPosition, valueRange.getRange(), rows.size()));
                }
                var headerRow = rows.get(headerRowPosition - 1);
                for (int i = 0; i < headerRow.size(); i++) {
                    String columnName = StringUtils.trimToNull(headerRow.get(i).toString());
                    if (columnName != null) {
                        columnNames.put(i, columnName);
                    }
                }
            }

            // parse data rows below header row
            for (int i = Optional.ofNullable(headerRowPosition).orElse(0); i < rows.size(); i++) {
                var row = rows.get(i);
                var columnNamesClone = new HashMap<>(columnNames);
                var parsedRow = new HashMap<String, Object>();
                for (int j = 0; j < row.size(); j++) {
                    String column = StringUtils.trimToNull(row.get(j).toString());
                    if (column != null) {
                        parsedRow.put(columnNamesClone.getOrDefault(j, "Column_" + j), column);
                        columnNamesClone.remove(j);
                    }
                }
                if (config.getIncludeNullValuesInOutput()) {
                    /*
                        The row size is determined by the position of last non-empty cell. Because of this, not all rows have the same length
                        so some of the columns specified in the header row might not be present at all in the output.
                        If we want to have them visible with the value "null" we must add them.
                     */
                    columnNamesClone.forEach((k, v) -> {
                        parsedRow.put(v, null);
                    });
                }
                parsedSheet.getData().add(parsedRow);
            }
            parsedData.getSheets().add(parsedSheet);
        }
        return parsedData;
    }
}
