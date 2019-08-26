package com.ibagroup.wf.intelia.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import com.workfusion.utils.csv.CSVReader;

public class CsvUtils {

    public static List<Map<String, String>> csvAsMap(InputStream inputStream) {
        List<Map<String, String>> result = new ArrayList<>();
        CSVReader reader = null;
        try {
            reader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"), null);

            List<String> headers = Arrays.asList(reader.readNext());
            Map<String, Integer> columnIndexes = new HashMap<>();
            headers.forEach(column -> columnIndexes.put(column, headers.indexOf(column)));

            String[] row = null;
            while ((row = reader.readNext()) != null) {
                final String[] finalRow = row;
                Map<String, String> rowMap = new HashMap<>();
                columnIndexes.forEach((column, index) -> rowMap.put(column, (index != -1) ? (finalRow[index]) : ""));
                result.add(rowMap);
            }

        } catch (Throwable t) {
            throw new RuntimeException("Failed to read CSV input stream", t);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            IOUtils.closeQuietly(inputStream);
        }
        return result;
    }
}
