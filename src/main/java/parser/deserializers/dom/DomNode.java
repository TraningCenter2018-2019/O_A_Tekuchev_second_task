package parser.deserializers.dom;

import java.util.ArrayList;
import java.util.List;

/**
 * The node of DOM tree
 */
class DomNode {
  /** The type of the node */
  private NodeType type;

  /** The name of the node */
  private String name;

  /** The plain value of the node or the array of subtrees if it is an array*/
  private Object value;

  /** The list of subtrees if it is an object */
  private List<DomNode> fields;

  /**
   * Constructor
   *
   * @param atype the type of the node
   * @param aname the name of the node
   */
  public DomNode(NodeType atype, String aname) {
    type = atype;
    name = aname;
    if (type == NodeType.object) {
      fields = new ArrayList<>();
    }
  }

  /**
   * Gets the type
   *
   * @return
   */
  public NodeType getType() {
    return type;
  }

  /**
   * Gets the name
   *
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Gets the value
   *
   * @return
   * @throws IncorrectDomNodeTypeException
   */
  public Object getValue() throws IncorrectDomNodeTypeException {
    if (type == NodeType.object) {
      throw new IncorrectDomNodeTypeException(type, "getValue");
    }
    return value;
  }

  public DomNode setValue(Object value) throws IncorrectDomNodeTypeException {
    if (type == NodeType.object) {
      throw new IncorrectDomNodeTypeException(type, "setValue");
    }
    this.value = value;
    return this;
  }

  public DomNode addField(DomNode node) throws IncorrectDomNodeTypeException {
    if (type != NodeType.object) {
      throw new IncorrectDomNodeTypeException(type, "addField");
    }
    fields.add(node);
    return this;
  }

  public DomNode getField(int ind) throws IncorrectDomNodeTypeException {
    if (type != NodeType.object) {
      throw new IncorrectDomNodeTypeException(type, "getField");
    }
    return fields.get(ind);
  }

  public int countFields() throws IncorrectDomNodeTypeException {
    if (type != NodeType.object) {
      throw new IncorrectDomNodeTypeException(type, "countFields");
    }
    return fields.size();
  }
}
