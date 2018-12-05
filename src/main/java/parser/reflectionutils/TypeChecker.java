package parser.reflectionutils;

public interface TypeChecker {
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

  /**
   * Checks whether a given class is String
   *
   * @param clazz the class it needs to check
   * @return true if it is String class
   */
  default boolean isString(Class<?> clazz) {
    return clazz.isAssignableFrom(String.class);
  }

  /**
   * Gets a wrapper class above given primitive type
   *
   * @param clazz
   * @return the wrapper class or null if the given class is not primitive
   */
  default Class<?> getWrapperByPrimitive(Class<?> clazz) {
    if (clazz.equals(boolean.class)) {
      return Boolean.class;
    }
    if (clazz.equals(int.class)) {
      return  Integer.class;
    }
    if (clazz.equals(char.class)) {
      return Character.class;
    }
    if (clazz.equals(byte.class)) {
      return Byte.class;
    }
    if (clazz.equals(short.class)) {
      return Short.class;
    }
    if (clazz.equals(double.class)) {
      return Double.class;
    }
    if (clazz.equals(long.class)) {
      return Long.class;
    }
    if (clazz.equals(float.class)) {
      return Float.class;
    }
    return null;
  }
}
