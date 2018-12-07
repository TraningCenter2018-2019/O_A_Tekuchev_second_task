package parser.deserializers.abstractdeserializers;

import parser.exceptions.InvalidValueException;
import parser.exceptions.UnaccessibleFieldException;
import parser.json.JsonSyntax;
import parser.parserannotations.Renamed;
import parser.reflectionutils.TypeChecker;
import parser.syntaxparsers.FlowScanner;

import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractJsonDeserializer extends AbstractDeserializer implements JsonSyntax, TypeChecker {

  static private final char EMPTY_CHAR = '\0';

  private char charBuff = EMPTY_CHAR;

  private FlowScanner scanner;

  protected final void setSource(FileReader fileReader) {
    scanner = new FlowScanner(fileReader);
  }

  protected final void setSource(String string) {
    scanner = new FlowScanner(string);
  }

  @Override
  protected final String getNextToken() {
    if (charBuff != EMPTY_CHAR) {
      char ch = charBuff;
      charBuff = EMPTY_CHAR;
      return Character.toString(ch);
    }
    int code;
    while ((code = scanner.getNext()) != -1 && isSpaceChar((char) code)) ;
    if (code == -1) {
      return null;
    }
    StringBuilder stringBuilder = new StringBuilder();
    char ch = (char) code;
    switch (ch) {
      case COMMA:
        return getNextToken();

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
          code = scanner.getNext();
          if (code == -1) {
            return null;
          }
          ch = (char) code;
          stringBuilder.append(ch);
        } while (ch != QUOTE);
        return stringBuilder.toString();

      case COLON:
        while ((code = scanner.getNext()) != -1 && isSpaceChar((char) code)) ;
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
          code = scanner.getNext();
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
          code = scanner.getNext();
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
  protected Object getValue(String stringValue, Class<?> valueType) throws InvalidValueException, UnaccessibleFieldException {
    try {
      if (isString(valueType)) {
        return stringValue.replaceAll(Character.toString(QUOTE), "");
      }
      if (isWrapperType(valueType)) {
        if (stringValue.equals("null")) {
          return null;
        }
        Constructor<?> constructor = valueType.getConstructor(String.class);
        return constructor.newInstance(stringValue.replaceAll("\\s+",""));
      }
      if (valueType.isPrimitive()) {
        Class<?> wrapper = getWrapperByPrimitive(valueType);
        Constructor<?> constructor = wrapper.getConstructor(String.class);
        return constructor.newInstance(stringValue.replaceAll("\\s+",""));
      }
      throw new InvalidValueException("Cannot get value from " + valueType.getName());
    }
    catch (NoSuchMethodException e) {
      throw new UnaccessibleFieldException("WTF1");
    }
    catch (InstantiationException e) {
      throw new InvalidValueException("Invalid value for" + valueType.getName());
    }
    catch (IllegalAccessException e) {
      throw new UnaccessibleFieldException("WTF2");
    }
    catch (InvocationTargetException e) {
      throw new InvalidValueException("Invalid value for" + valueType.getName());
    }
  }

  /**
   * Gets field by its annotated second name
   *
   * @param name the name of the field
   * @param valueClass the class of an object
   * @return field or null
   */
  protected Field getFieldBySecondName(String name, Class<?> valueClass) {
    for (Field fl : valueClass.getDeclaredFields()) {
      Renamed annotation = fl.getAnnotation(Renamed.class);
      if (annotation != null && annotation.secondName().equals(name)) {
        return fl;
      }
    }
    return null;
  }
}
