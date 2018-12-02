package parser.serializers;

public interface Serializer {
  /**
   * Serializers a given object to a special format
   *
   * @param obj the serializable object
   * @return the string representation of the object
   */
  String serialize(Object obj);

  /**
   * Checks whether a class is a wrapper above primitive types
   *
   * @param clazz
   * @return
   */
  default boolean isWrapperType(Class<?> clazz) {
    return clazz.equals(Boolean.class) ||
            clazz.equals(Integer.class) ||
            clazz.equals(Character.class) ||
            clazz.equals(Byte.class) ||
            clazz.equals(Short.class) ||
            clazz.equals(Double.class) ||
            clazz.equals(Long.class) ||
            clazz.equals(Float.class);
  }
}
