package com.apjc.table;

import java.util.*;
import java.util.function.Function;

public class Table implements Iterable<Table.Field> {

    private final Set<Row> ROWS = new HashSet<>();
    private final List<Field> VALUES = new ArrayList<>();

    private Table(Builder builder){
        ROWS.addAll(builder.ROWS);
    }

    public int size(){
        return VALUES.size();
    }

    public Set<String> getRow(){
        Set<String> strings = new HashSet<>();
        ROWS.forEach(row -> strings.add(row.name()));
        return strings;
    }

    public Class<?> getRowType(String name){
        for(Row row : ROWS){
            if(row.name().equals(name)){
                return row.type();
            }
        }
        throw new IllegalArgumentException("The '" + name + "' field does not exist");
    }

    public boolean isRowRequired(String name){
        for(Row row : ROWS){
            if(row.name().equals(name)){
                return row.isRequired();
            }
        }
        throw new IllegalArgumentException("The '" + name + "' field does not exist");
    }

    public BuilderField addField(){
        return new BuilderField(ROWS, VALUES::add);
    }

    public Field getField(int index){
        return VALUES.get(index);
    }

    public Field removeField(int index){
        return VALUES.remove(index);
    }

    @Override
    public Iterator<Field> iterator(){
        return VALUES.iterator();
    }

    public <T> Table filter(String name, Class<T> type, Function<T, Boolean> fil){
        Table table = tableRow();
        for(Field field : VALUES){
            if(fil.apply(field.get(name, type))){
               setField(table, field);
            }
        }
        return table;
    }

    public <T> Table sort(String name, Class<T> type, Sorter<T> sorter){
        List<Field> fields = new ArrayList<>();
        for(Field field : VALUES){
            boolean isNotAdd = true;
            for(int i = 0; i < fields.size() && isNotAdd; i++){
                Field l = fields.get(i);
                if(sorter.sort(field.get(name, type), l.get(name, type))){
                    fields.add(i, field);
                    isNotAdd = false;
                }
            }
            if(isNotAdd){
                fields.add(field);
            }
        }
        Table table = tableRow();
        for(Field field : fields){
            setField(table, field);
        }
        return table;
    }

    private Table tableRow(){
        Builder builder = new Builder();
        for(Row row : ROWS){
            builder.add(row.name(), row.type(), row.isRequired());
        }
        return builder.build();
    }

    private void setField(Table table, Field field){
        BuilderField fieldBuild = table.addField();
        for(Row row : ROWS){
            fieldBuild.add(row.name(), field.get(row.name()));
        }
        fieldBuild.build();
    }

    public static final class Builder{

        private final Set<Row> ROWS = new HashSet<>();

        public Builder add(String name, Class<?> type, boolean isRequired){
            ROWS.add(new Row(name, type, isRequired));
            return this;
        }

        public Table build(){
            return new Table(this);
        }

    }

    public final static class Field {

        private final Map<Row, Object> VALUES = new HashMap<>();

        private Field(Map<Row, Object> map){
            for (Row row : map.keySet()){
                VALUES.put(row, map.get(row));
            }
        }

        public void set(String name, Object value){
            Row row = getRow(name);
            if(row != null){
                if((value == null && !row.isRequired()) || row.type().isInstance(value)){
                    VALUES.put(row, value);
                }else if(value == null){
                    throw new IllegalArgumentException("The '" + name + "' field is required and cannot be null");
                }else {
                    throw new ClassCastException("The field '" + name + "' to type '" + row.type() + "' cannot be assigned type '" + value.getClass() + "'");
                }
            }else {
                throw new IllegalArgumentException("The '" + name + "' field does not exist");
            }
        }

        public Object get(String name){
            Row row = getRow(name);
            if(row == null){
                throw new IllegalArgumentException("The '" + name + "' field does not exist");
            }
            return VALUES.get(row);
        }

        public <T> T get(String name, Class<T> type){
            Row row = getRow(name);
            Object value = VALUES.get(row);
            if((value == null) || type.isInstance(value)){
                return (T)value;
            }else if(row == null){
                throw new IllegalArgumentException("The '" + name + "' field does not exist");
            }
            throw new ClassCastException("Field '" + name + "' of type '" + row.type() + "' cannot be converted to type '" + type + "'");
        }

        private Row getRow(String name){
            for(Row row : VALUES.keySet()){
                if(row.name().equals(name)){
                    return row;
                }
            }
            return null;
        }

    }

    public static final class BuilderField{

        private final Set<Row> ROWS;
        private final Map<Row, Object> VALUES = new HashMap<>();
        private final Registration REGISTRATION;

        private BuilderField(Set<Row> rows, Registration registration){
            ROWS = new HashSet<>(rows);
            REGISTRATION = registration;
        }

        public BuilderField add(String name, Object value){
            Row row = getRow(name);
            if(row != null){
                if((value == null && !row.isRequired()) || row.type().isInstance(value)){
                    VALUES.put(row, value);
                    return this;
                }else if(value == null){
                    throw new IllegalArgumentException("The '" + name + "' field is required and cannot be null");
                }
                throw new ClassCastException("The field '" + name + "' to type '" + row.type() + "' cannot be assigned type '" + value.getClass() + "'");
            }
            throw new IllegalArgumentException("The '" + name + "' field does not exist");
        }

        public void build(){
            for(Row row : ROWS){
                if(!VALUES.containsKey(row)){
                    add(row.name(), null);
                }
            }
            REGISTRATION.registry(new Field(VALUES));
        }

        public BuilderField buildNext(){
            build();
            return new BuilderField(ROWS, REGISTRATION);
        }

        private Row getRow(String name){
            for(Row row : ROWS){
                if(row.name().equals(name)){
                    return row;
                }
            }
            return null;
        }

    }

    @FunctionalInterface
    private interface Registration{

        void registry(Field field);

    }

    @FunctionalInterface
    public interface Sorter <T>{

        boolean sort(T val1, T val2);

    }

    private record Row(String name, Class<?> type, boolean isRequired){}

}
