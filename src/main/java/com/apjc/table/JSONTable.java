package com.apjc.table;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;

public class JSONTable {

    public static String write(Table table) {
        JSONArray jsonRows =  new JSONArray();
        for(String name : table.getRow()){
            JSONObject jsonRow = new JSONObject();
            jsonRow.put("name", name);
            jsonRow.put("type", table.getRowType(name).getName());
            jsonRow.put("is_required", table.isRowRequired(name));
            jsonRows.add(jsonRow);
        }
        JSONObject jsomTable = new JSONObject();
        jsomTable.put("rows", jsonRows);
        JSONArray jsonValues = new JSONArray();
        for(Table.Field field : table){
            JSONObject jsonValue = new JSONObject();
            for(String name : table.getRow()){
                jsonValue.put(name, field.get(name));
            }
            jsonValues.add(jsonValue);
        }
        jsomTable.put("values", jsonValues);
        return jsomTable.toJSONString();
    }

    public static Table reade(String jsonText) throws ParseException, ClassNotFoundException {
        JSONObject jsonTable = (JSONObject) new JSONParser().parse(jsonText);
        Table.Builder builder = new Table.Builder();
        JSONArray jsonRows = (JSONArray) jsonTable.get("rows");
        for (Object row : jsonRows) {
            JSONObject jsonRow = (JSONObject) row;
            String name = (String) jsonRow.get("name");
            Class<?> type = Class.forName((String) jsonRow.get("type"));
            boolean isRequired = (boolean) jsonRow.get("is_required");
            builder.add(name, type, isRequired);
        }
        Table table = builder.build();
        JSONArray jsonValues = (JSONArray) jsonTable.get("values");
        for(Object value : jsonValues){
            JSONObject jsonValue = (JSONObject) value;
            Table.BuilderField builderField = table.addField();
            for(String name : table.getRow()){
                builderField.add(name, jsonValue.get(name));
            }
            builderField.build();
        }
        return table;
    }

}
