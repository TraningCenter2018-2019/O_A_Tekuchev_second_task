package parser.deserializers.abstractdeserializers;


import parser.deserializers.Deserializer;

public abstract class AbstractDeserializer implements Deserializer {

  /**
   * Gets the next token from the file
   *
   * @return a token or null if the file is ended
   */
  protected abstract String getNextToken();

}
