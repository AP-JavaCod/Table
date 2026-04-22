package com.apjc.table;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONTable {

    public static String write(Table table) {
        JSONArray jsonRows =  new JSONArray();
        for(String name : table.getRows()){
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
            for(String name : table.getRows()){
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
            for(String name : table.getRows()){
                Object val = jsonValue.get(name);
                Class<?> valType = table.getRowType(name);
                builderField.add(name, convert(valType, val));
            }
            builderField.build();
        }
        return table;
    }

    private static <T> T convert(Class<T> type, Object value){
        if(value == null || type.isInstance(value)){
            return (T)value;
        }else if(Number.class.isAssignableFrom(type) && value instanceof Number val){
           if(type == Byte.class){
                return (T)new Byte(val.byteValue());
            }else if(type == Short.class){
                return (T)new Short(val.shortValue());
            } else if (type == Integer.class) {
                return (T)new Integer(val.intValue());
            } else if (type == Long.class) {
                return (T)new Long(val.longValue());
            } else if (type == Float.class) {
                return (T) new Float(val.floatValue());
            }else if(type == Double.class){
                return (T)new Double(val.doubleValue());
            }
        }
        throw new ClassCastException("To type '" + type.getName() + "' cannot be assigned type '" + value.getClass().getName() + "'");
    }

}
