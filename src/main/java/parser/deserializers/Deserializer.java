package parser.deserializers;

import parser.exceptions.DefaultConstructorException;
import parser.exceptions.InvalidSyntaxException;
import parser.exceptions.InvalidValueException;

import java.io.IOException;

public interface Deserializer {
  /**
   * Deserializes object from a given file
   *
   * @param fileName the file from which read the object
   * @param cls The class of the object
   * @param <T> The type of the object
   * @return extracted object or null
   */
  <T> T deserialize(String fileName, Class<T> cls)
          throws InvalidSyntaxException, IOException, InvalidValueException, DefaultConstructorException;
}
