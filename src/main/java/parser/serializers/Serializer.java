package parser.serializers;

import parser.exceptions.UnaccessibleFieldException;

public interface Serializer {
  /**
   * Serializers a given object to a special format
   *
   * @param obj the serializable object
   * @return the string representation of the object
   */
  void serialize(String fileName, Object obj) throws UnaccessibleFieldException;
}
