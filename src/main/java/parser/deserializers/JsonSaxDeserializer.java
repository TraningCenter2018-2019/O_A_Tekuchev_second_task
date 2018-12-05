package parser.deserializers;

import parser.json.JsonSyntax;
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

public class JsonSaxDeserializer implements Deserializer, JsonSyntax, TypeChecker {

  static private final char EMPTY_CHAR = '\0';

  private char charBuff = EMPTY_CHAR;

  /**
   * Gets the next token from the file
   *
   * @param fileReader
   * @return a token or null if the file is ended
   * @throws IOException
   */
  private String getNextToken(FileReader fileReader) throws IOException {
    if (charBuff != EMPTY_CHAR) {
      char ch = charBuff;
      charBuff = EMPTY_CHAR;
      return Character.toString(ch);
    }
    int code;
    while ((code = fileReader.read()) != -1 && isSpaceChar((char) code)) ;
    if (code == -1) {
      return null;
    }
    StringBuilder stringBuilder = new StringBuilder();
    char ch = (char) code;
    switch (ch) {
      case COMMA:
        return getNextToken(fileReader);

      case OPEN_OBJECT_BRACE:
        return Character.toString(OPEN_OBJECT_BRACE);

      case CLOSE_OBJECT_BRACE:
        return Character.toString(CLOSE_OBJECT_BRACE);

      case OPEN_COLLECTION_BRACE:
        return Character.toString(OPEN_COLLECTION_BRACE);

      case CLOSE_COLLECTION_BRACE:
        return Character.toString(CLOSE_COLLECTION_BRACE);

      case QUOTE:
        stringBuilder.append(ch);
        do {
          code = fileReader.read();
          if (code == -1) {
            return null;
          }
          ch = (char) code;
          stringBuilder.append(ch);
        } while (ch != QUOTE);
        return stringBuilder.toString();

      case COLON:
        while ((code = fileReader.read()) != -1 && isSpaceChar((char) code)) ;
        if (code == -1) {
          return null;
        }
        ch = (char)code;
        if (ch == OPEN_OBJECT_BRACE) {
          return Character.toString(OPEN_OBJECT_BRACE);
        }
        if (ch == OPEN_COLLECTION_BRACE) {
          return Character.toString(OPEN_COLLECTION_BRACE);
        }
        stringBuilder.append(ch);
        do {
          code = fileReader.read();
          if (code == -1) {
            return null;
          }
          ch = (char) code;
          stringBuilder.append(ch);
        } while (ch != COMMA && ch != CLOSE_OBJECT_BRACE);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        if (ch == CLOSE_OBJECT_BRACE) {
          charBuff = ch;
        }
        return stringBuilder.toString();

      default:
        stringBuilder.append(ch);
        do {
          code = fileReader.read();
          if (code == -1) {
            return null;
          }
          ch = (char) code;
          stringBuilder.append(ch);
        } while (ch != COMMA && ch != CLOSE_COLLECTION_BRACE);
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        if (ch == CLOSE_COLLECTION_BRACE) {
          charBuff = ch;
        }
        return stringBuilder.toString();
    }
  }

  /**
   * Gets value from its string representation
   * @param stringValue the string representation of the value
   * @param valueType the type of the value
   * @return the value or null if extracting is impossible
   */
  private Object getValue(String stringValue, Class<?> valueType) {
    try {
      if (isString(valueType)) {
        return stringValue.replaceAll(Character.toString(QUOTE), "");
      }
      if (isWrapperType(valueType)) {
        Constructor<?> constructor = valueType.getConstructor(String.class);
        return constructor.newInstance(stringValue.replaceAll("\\s+",""));
      }
      if (valueType.isPrimitive()) {
        Class<?> wrapper = getWrapperByPrimitive(valueType);
        Constructor<?> constructor = wrapper.getConstructor(String.class);
        return constructor.newInstance(stringValue.replaceAll("\\s+",""));
      }
      return null;
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Gets field by its annotated second name
   *
   * @param name the name of the field
   * @param valueClass the class of an object
   * @return field or null
   */
  private Field getFieldBySecondName(String name, Class<?> valueClass) {
    for (Field fl : valueClass.getDeclaredFields()) {
      Renamed annotation = fl.getAnnotation(Renamed.class);
      if (annotation != null && annotation.secondName().equals(name)) {
        return fl;
      }
    }
    return null;
  }

  /**
   * Deserializes a plain object
   *
   * @param reader file reader from which read the object
   * @param valueClass the class of the object
   * @return
   * @throws IOException
   */
  private Object deserializeSimpleObj(FileReader reader, Class<?> valueClass) throws IOException {
    try {
      String token;
      Constructor<?> constructor = valueClass.getDeclaredConstructor();
      Object obj = constructor.newInstance();
      String closeBrace = Character.toString(CLOSE_OBJECT_BRACE);
      while (!(token = getNextToken(reader)).equals(closeBrace)) {
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
        Object value = deserializeAnyType(reader, valueType);
        field.set(obj, value);
      }
      return obj;
    }
    catch (InstantiationException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      e.printStackTrace();
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Deserializes an array
   *
   * @param fileReader file reader from which read the object
   * @param objClass
   * @return
   */
  private Object[] deserializeArray(FileReader fileReader, Class<?> objClass) {
    Class<?> itemType = objClass.getComponentType();
    List list = new LinkedList();
    Object value = deserializeAnyType(fileReader, itemType);
    if (value != null) {
      list.add(value);
    }
    while (value != null) {
       value = deserializeAnyType(fileReader, itemType);
      if (value != null) {
        list.add(value);
      }
    }
    return list.toArray();
  }

  /**
   * Deserializes any type of an object
   *
   * @param fileReader file reader from which read the object
   * @param objClass the class of the object
   * @return
   */
  private Object deserializeAnyType(FileReader fileReader, Class<?> objClass) {
    try {
      String nextToken = getNextToken(fileReader);
      if (objClass.isArray() && nextToken.equals(Character.toString(OPEN_COLLECTION_BRACE))) {
        return deserializeArray(fileReader, objClass);
      }
      else if (objClass.isAssignableFrom(Collection.class)) {
        return null;
      }
      else if (objClass.isAssignableFrom(Map.class)) {
        return null;
      }
      else if (nextToken.equals(Character.toString(OPEN_OBJECT_BRACE))) {
        return deserializeSimpleObj(fileReader, objClass);
      }
      else {
        return getValue(nextToken, objClass);
      }
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public <T> T deserialize(String fileName, Class<T> cls) {
    try (FileReader fileReader = new FileReader(fileName)) {

      Object obj = deserializeAnyType(fileReader, cls);
      if (cls.isArray()) {
        Class<?> item = cls.getComponentType();
        int len = Array.getLength(obj);
        T arr = (T)Array.newInstance(item, len);
        for (int i = 0; i < len; ++i) {
          Array.set(arr, i, Array.get(obj, i));
        }
        return arr;
      }
      return (T)obj;
    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }
}
