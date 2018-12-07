package parser.deserializers.sax;

import parser.deserializers.abstractdeserializers.AbstractJsonDeserializer;
import parser.exceptions.DefaultConstructorException;
import parser.exceptions.InvalidSyntaxException;
import parser.exceptions.InvalidValueException;
import parser.exceptions.UnaccessibleFieldException;
import parser.parserannotations.Renamed;
import parser.reflectionutils.TypeChecker;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * SAX deserializer of JSON files
 */
public class JsonSaxDeserializer extends AbstractJsonDeserializer  {

  /**
   * Deserializes a plain object
   *
   * @param valueClass the class of the object
   * @return
   * @throws IOException
   */
  private Object deserializeSimpleObj(Class<?> valueClass)
          throws IOException, UnaccessibleFieldException, InvalidValueException, DefaultConstructorException, InvalidSyntaxException {
    try {
      String token;
      Constructor<?> constructor = valueClass.getDeclaredConstructor();
      Object obj = constructor.newInstance();
      String closeBrace = Character.toString(CLOSE_OBJECT_BRACE);
      while (!(token = getNextToken()).equals(closeBrace)) {
        token = token.replaceAll(Character.toString(QUOTE), "");
        Field field;
        try {
          field = valueClass.getDeclaredField(token);
        }
        catch (NoSuchFieldException e) {
          field = getFieldBySecondName(token, valueClass);
        }
        field.setAccessible(true);
        Class<?> valueType = field.getType();
        Object value = deserializeAnyType(getNextToken(), valueType);
        field.set(obj, value);
      }
      if (token == null) {
        throw new InvalidSyntaxException(CLOSE_OBJECT_BRACE + "expected");
      }
      return obj;
    }
    catch (NoSuchMethodException e) {
      throw new DefaultConstructorException(
              "The class " + valueClass.getName() + " doesn't have constructor without parameters");
    }
    catch (IllegalAccessException e) {
      throw new UnaccessibleFieldException("Cannot access");
    }
    catch (InvocationTargetException e) {
      throw new UnaccessibleFieldException("WTF4");
    }
    catch (InstantiationException e) {
      throw new UnaccessibleFieldException("Cannot instantiate an object of " + valueClass.getName());
    }
  }

  /**
   * Deserializes an array
   *
   * @param objClass
   * @return
   */
  private Object deserializeArray(Class<?> objClass)
          throws UnaccessibleFieldException, InvalidValueException, IOException, DefaultConstructorException, InvalidSyntaxException {
    Class<?> itemType = objClass.getComponentType();
    List list = new LinkedList();
    String closeColl = Character.toString(CLOSE_COLLECTION_BRACE);
    String token;
    while ((token = getNextToken())!= null && !token.equals(closeColl)) {
      Object value = deserializeAnyType(token, itemType);
      list.add(itemType.cast(value));
    }
    if (token == null) {
      throw new InvalidSyntaxException(CLOSE_COLLECTION_BRACE + "expected");
    }
    Object arr = Array.newInstance(itemType, list.size());
    int i = 0;
    for (Object item : list) {
      Array.set(arr, i++, item);
    }
    return arr;
  }

  /**
   * Deserializes any type of an object
   *
   * @param objClass the class of the object
   * @return
   */
  private Object deserializeAnyType(String token, Class<?> objClass)
          throws IOException, UnaccessibleFieldException, InvalidValueException, DefaultConstructorException, InvalidSyntaxException {

    if (objClass.isArray()) {
      if (token.equals(Character.toString(OPEN_COLLECTION_BRACE))) {
        return deserializeArray(objClass);
      }
      return null;
    }
    else if (objClass.isAssignableFrom(Collection.class)) {
      return null;
    }
    else if (objClass.isAssignableFrom(Map.class)) {
      return null;
    }
    else if (isString(objClass) || isWrapperType(objClass) || objClass.isPrimitive()) {
      return getValue(token, objClass);
    }
    else  {
      if (token.equals(Character.toString(OPEN_OBJECT_BRACE))) {
        return deserializeSimpleObj(objClass);
      }
      return null;
    }
  }

  @Override
  public <T> T deserialize(String fileName, Class<T> cls)
          throws InvalidSyntaxException, IOException, InvalidValueException, DefaultConstructorException {
    try (FileReader fileReader = new FileReader(fileName)) {
      super.setSource(fileReader);
      String token = getNextToken();
      Object obj = deserializeAnyType(token, cls);
      return (T)obj;
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
      return null;
    }
  }
}
