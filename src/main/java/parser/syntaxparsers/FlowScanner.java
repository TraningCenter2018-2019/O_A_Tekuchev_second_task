package parser.syntaxparsers;

import java.io.FileReader;
import java.io.IOException;

/**
 * The scanner of a flow of characters
 */
public class FlowScanner {

  /** The string source */
  private String str = null;

  /** The position of a pointer on the string */
  private int strPosition = 0;

  /** The file source */
  private FileReader fileReader = null;

  public FlowScanner(String anStr) {
    str = anStr;
  }

  public FlowScanner(FileReader aFileReader) {
    fileReader = aFileReader;
  }

  /**
   * Gets the next symbol
   *
   * @return the code of the symbol or -1 if the flow if over
   */
  public int getNext() {
    try {
      if (str == null) {
        return fileReader.read();
      }
      return strPosition < str.length() ? str.charAt(strPosition++) : -1;
    }
    catch (IOException e) {
      return -1;
    }
  }
}
