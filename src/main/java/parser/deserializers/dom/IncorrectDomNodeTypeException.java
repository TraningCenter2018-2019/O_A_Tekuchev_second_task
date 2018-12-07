package parser.deserializers.dom;

class IncorrectDomNodeTypeException extends Exception {
  public IncorrectDomNodeTypeException(NodeType real, String msg) {
    super("Can't call for " + real + " " + msg);
  }
}
