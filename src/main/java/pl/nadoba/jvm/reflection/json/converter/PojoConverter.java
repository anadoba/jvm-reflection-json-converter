package pl.nadoba.jvm.reflection.json.converter;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Collection;

public class PojoConverter {

    private Object pojoObj;
    private String objName = "";

    public PojoConverter(Object pojoObj) {
        this.pojoObj = pojoObj;
    }

    public PojoConverter(Object pojoObj, String name) {
        this.pojoObj = pojoObj;
        this.objName = name;
    }

    public JSONObject convertToJson() {
        JSONObject json = new JSONObject();

        if (isPrimitive(pojoObj.getClass())) {
            return new JSONObject().put(objName, pojoObj);
        }

        for (Field field : pojoObj.getClass().getDeclaredFields()) {
            String key = field.getName();
            Object newObj = null;
            try {
                newObj = convertField(field);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            json.put(key, newObj);
        }

        return json;
    }

    private Object convertField(Field field) throws IllegalAccessException {
        JSONObject json = new JSONObject();

        if (isPrimitive(field.getType())) {
            System.out.println("Primitive");
            return convertPrimitiveField(field);
        } else if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<Object> collection = (Collection<Object>) field.get(pojoObj);
            for (Object obj : collection) {
                for (Field f : obj.getClass().getDeclaredFields()) {
                    json.append(f.getName(), convertPrimitiveField(f));
                }
            }
        } else {
            System.out.println("UNKNOWN");
            json.put(field.getName(), field.get(pojoObj));
        }

        return json;
    }

    private Object convertPrimitiveField(Field field) {
        Class<?> type = field.getType();
        field.setAccessible(true);
        try {
            if (isTypeConvertableToJsBoolean(type)) {
                return field.getBoolean(pojoObj);
            } else if (isTypeConvertableToJsNumber(type)) {
                return field.getDouble(pojoObj);
            } else if (isTypeConvertableToJsString(type)) {
                return field.get(pojoObj);
            } else {
                return null;
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException("Error during converting primitive field - " + e);
        }
    }

    private boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz.isAssignableFrom(String.class);
    }

    private boolean isTypeConvertableToJsString(Class<?> type) {
        return type.isAssignableFrom(Character.class) ||
                type.isAssignableFrom(char.class) ||
                type.isAssignableFrom(String.class);
    }

    private boolean isTypeConvertableToJsBoolean(Class<?> type) {
        return type.isAssignableFrom(Boolean.class) ||
                type.isAssignableFrom(boolean.class);
    }

    private boolean isTypeConvertableToJsNumber(Class<?> type) {
        return type.isAssignableFrom(Short.class) ||
                type.isAssignableFrom(short.class) ||
                type.isAssignableFrom(Integer.class) ||
                type.isAssignableFrom(int.class) ||
                type.isAssignableFrom(Long.class) ||
                type.isAssignableFrom(long.class) ||
                type.isAssignableFrom(Float.class) ||
                type.isAssignableFrom(float.class) ||
                type.isAssignableFrom(Double.class) ||
                type.isAssignableFrom(double.class);
    }
}
