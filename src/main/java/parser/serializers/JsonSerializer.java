package parser.serializers;

import parser.exceptions.UnaccessibleFieldException;
import parser.parserannotations.Renamed;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class JsonSerializer implements Serializer {

  static private final char OPEN_OBJECT_BRACE = '{';
  static private final char CLOSE_OBJECT_BRACE = '}';
  static private final char OPEN_COLLECTION_BRACE = '[';
  static private final char CLOSE_COLLECTION_BRACE = ']';
  static private final char QUOTE = '"';
  static private final char COMMA = ',';
  static private final char COLON = ':';

  /**
   * Checks whether a class is a wrapper above primitive types
   *
   * @param clazz
   * @return
   */
  private boolean isWrapperType(Class<?> clazz) {
    return clazz.equals(Boolean.class) ||
            clazz.equals(Integer.class) ||
            clazz.equals(Character.class) ||
            clazz.equals(Byte.class) ||
            clazz.equals(Short.class) ||
            clazz.equals(Double.class) ||
            clazz.equals(Long.class) ||
            clazz.equals(Float.class);
  }

  private String asString(Object str) {
    return QUOTE + str.toString() + QUOTE;
  }

  private boolean isString(Class<?> clazz) {
    return clazz.isAssignableFrom(String.class);
  }

  private String serializeSimpleObj(Object obj, Class<?> objClass)
          throws IllegalAccessException, UnaccessibleFieldException {

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(OPEN_OBJECT_BRACE);
    Field[] declaredFields = objClass.getDeclaredFields();
    for (int i = 0; i < declaredFields.length; ++i) {
      Renamed annotation = declaredFields[i].getDeclaredAnnotation(Renamed.class);
      if (annotation == null) {
        stringBuilder.append(asString(declaredFields[i].getName()));
      }
      else {
        stringBuilder.append(asString(annotation.secondName()));
      }
      stringBuilder.append(COLON);
      declaredFields[i].setAccessible(true);
      Object value = declaredFields[i].get(obj);
      Class<?> valueClass = value.getClass();
      if (isString(valueClass)) {
        stringBuilder.append(asString(value));
      }
      else if (isWrapperType(valueClass) || valueClass.isPrimitive()) {
        stringBuilder.append(value);
      }
      else {
        // recursion
        stringBuilder.append(serializeHelp(value, valueClass));
      }
      declaredFields[i].setAccessible(false);
      if (i != declaredFields.length - 1) {
        stringBuilder.append(COMMA);
      }
    }
    stringBuilder.append(CLOSE_OBJECT_BRACE);
    return stringBuilder.toString();
  }

  private String serializeArray(Object obj, Class<?> objClass) throws UnaccessibleFieldException {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(OPEN_COLLECTION_BRACE);
    Class<?> itemType = objClass.getComponentType();
    int len = Array.getLength(obj);
    for (int i = 0; i < len; ++i) {
      if (isWrapperType(itemType) || itemType.isPrimitive()) {
        stringBuilder.append(Array.get(obj, i));
      }
      else {
        // recursion
        stringBuilder.append(serializeHelp(Array.get(obj, i), itemType));
      }
      if (i != len - 1) {
        stringBuilder.append(COMMA);
      }
    }
    stringBuilder.append(CLOSE_COLLECTION_BRACE);
    return stringBuilder.toString();
  }

  private String serializeHelp(Object obj, Class<?> objClass) throws UnaccessibleFieldException {
    try {
      if (objClass.isArray()) {
        return serializeArray(obj, objClass);
      }
      else if (objClass.isAssignableFrom(Collection.class)) {
        return null;
      }
      else if (objClass.isAssignableFrom(Map.class)) {
        return null;
      }
      else {
        return serializeSimpleObj(obj, objClass);
      }
    }
    catch (IllegalAccessException ex) {
      throw new UnaccessibleFieldException("Reflection error: cannot access the field\n" + ex.getMessage());
    }
  }

  @Override
  public String serialize(Object obj) throws UnaccessibleFieldException {
    return serializeHelp(obj, obj.getClass());
  }
}
