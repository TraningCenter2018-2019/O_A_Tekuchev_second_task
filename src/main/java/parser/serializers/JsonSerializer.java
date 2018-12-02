package parser.serializers;

public class JsonSerializer implements Serializer {

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

  @Override
  public String serialize(Object obj) {
    return null;
  }
}
