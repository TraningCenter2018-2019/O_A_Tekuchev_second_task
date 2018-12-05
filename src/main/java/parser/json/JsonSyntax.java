package parser.json;

/**
 * An interface contains JSON syntax elements and help functions
 */
public interface JsonSyntax {
  char OPEN_OBJECT_BRACE = '{';
  char CLOSE_OBJECT_BRACE = '}';
  char OPEN_COLLECTION_BRACE = '[';
  char CLOSE_COLLECTION_BRACE = ']';
  char QUOTE = '"';
  char COMMA = ',';
  char COLON = ':';

  /**
   * Checks whether a given character is a space character
   * @param ch the character it needs to check
   * @return true if it is
   */
  default boolean isSpaceChar(char ch) {
    return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
  }
}
