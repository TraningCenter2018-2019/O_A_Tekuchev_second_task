package parser.serializers;

import parser.exceptions.UnaccessibleFieldException;
import parser.json.JsonSyntax;
import parser.parserannotations.Renamed;
import parser.reflectionutils.TypeChecker;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 *
 */
public class JsonSerializer implements Serializer, JsonSyntax, TypeChecker {

  /**
   * Represents a given object like JSON string
   *
   * @param str
   * @return the string wrapped in quotes
   */
  private String asString(Object str) {
    return QUOTE + str.toString() + QUOTE;
  }

  /**
   * Serializes a plain object
   *
   * @param fileWriter a file writer to serialize
   * @param obj an object it needs to write
   * @param objClass the class of the object
   * @throws IllegalAccessException
   * @throws UnaccessibleFieldException
   * @throws IOException
   */
  private void serializeSimpleObj(FileWriter fileWriter, Object obj, Class<?> objClass)
          throws IllegalAccessException, UnaccessibleFieldException, IOException {
    fileWriter.write(OPEN_OBJECT_BRACE);
    // get the fields
    Field[] declaredFields = objClass.getDeclaredFields();
    for (int i = 0; i < declaredFields.length; ++i) {
      Renamed annotation = declaredFields[i].getDeclaredAnnotation(Renamed.class);
      // whether a field has the second name
      if (annotation == null) {
        fileWriter.write(asString(declaredFields[i].getName()));
      }
      else {
        fileWriter.write(asString(annotation.secondName()));
      }
      fileWriter.write(COLON);
      declaredFields[i].setAccessible(true);
      // get the value of the field
      Object value = declaredFields[i].get(obj);
      // get the type of the value
      Class<?> valueClass = value.getClass();
      if (isString(valueClass)) {
        fileWriter.write(asString(value));
      }
      else if (isWrapperType(valueClass) || valueClass.isPrimitive()) {
        fileWriter.write(value.toString());
      }
      else {
        // recursion
        serializeAnyType(fileWriter, value, valueClass);
      }
      declaredFields[i].setAccessible(false);
      if (i != declaredFields.length - 1) {
        fileWriter.write(COMMA);
      }
    }
    fileWriter.write(CLOSE_OBJECT_BRACE);
  }

  /**
   * Serializes an array
   *
   * @param fileWriter a file writer to serialize
   * @param obj an array it needs to write
   * @param objClass the type of the array (not the type of elements)
   * @throws UnaccessibleFieldException
   * @throws IOException
   */
  private void serializeArray(FileWriter fileWriter, Object obj, Class<?> objClass)
          throws UnaccessibleFieldException, IOException {
    fileWriter.write(OPEN_COLLECTION_BRACE);
    // get the type of the elements
    Class<?> itemType = objClass.getComponentType();
    int len = Array.getLength(obj);
    for (int i = 0; i < len; ++i) {
      if (isWrapperType(itemType) || itemType.isPrimitive()) {
        fileWriter.write(Array.get(obj, i).toString());
      }
      else {
        // recursion
        serializeAnyType(fileWriter, Array.get(obj, i), itemType);
      }
      if (i != len - 1) {
        fileWriter.write(COMMA);
      }
    }
    fileWriter.write(CLOSE_COLLECTION_BRACE);
  }

  /**
   * Serializes any type of the objects
   *
   * @param fileWriter a file writer to serialize
   * @param obj
   * @param objClass
   * @throws UnaccessibleFieldException
   */
  private void serializeAnyType(FileWriter fileWriter, Object obj, Class<?> objClass)
          throws UnaccessibleFieldException, IOException {
    try {
      if (objClass.isArray()) {
        serializeArray(fileWriter, obj, objClass);
      }
      else if (objClass.isAssignableFrom(Collection.class)) {

      }
      else if (objClass.isAssignableFrom(Map.class)) {

      }
      else {
        serializeSimpleObj(fileWriter, obj, objClass);
      }
    }
    catch (IllegalAccessException ex) {
      throw new UnaccessibleFieldException("Reflection error: cannot access the field\n" + ex.getMessage());
    }
  }

  @Override
  public void serialize(String fileName, Object obj) throws UnaccessibleFieldException {
    try (FileWriter writer = new FileWriter(fileName)) {
      serializeAnyType(writer, obj, obj.getClass());
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }
}
