package parser.deserializers.dom;

import parser.deserializers.abstractdeserializers.AbstractJsonDeserializer;
import parser.exceptions.DefaultConstructorException;
import parser.exceptions.InvalidSyntaxException;
import parser.exceptions.InvalidValueException;
import parser.exceptions.UnaccessibleFieldException;
import parser.reflectionutils.TypeChecker;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * DOM deserializer of JSON files
 */
public class JsonDomDeserializer extends AbstractJsonDeserializer implements TypeChecker {

  /**
   * Reads all content from a file
   *
   * @param fileName the file name
   * @return the content of the file
   * @throws IOException
   */
  private String readAll(String fileName) throws IOException {
    Paths.get(fileName);
    return new String(Files.readAllBytes(Paths.get(fileName)));
  }

  /**
   * Creates DOM node without name
   *
   * @param stringValue the string value of the node
   * @param valueType the type of the element that the node represents
   * @return
   * @throws IncorrectDomNodeTypeException
   * @throws InvalidSyntaxException
   * @throws UnaccessibleFieldException
   * @throws InvalidValueException
   */
  private DomNode createNode(String stringValue, Class<?> valueType)
          throws IncorrectDomNodeTypeException, InvalidSyntaxException, UnaccessibleFieldException, InvalidValueException {
    return createNode(stringValue, valueType, null);
  }

  /**
   * Creates DOM node
   *
   * @param stringValue the string value of the node
   * @param valueType the type of the element that the node represents
   * @param name the name of the node
   * @return
   * @throws IncorrectDomNodeTypeException
   * @throws InvalidSyntaxException
   * @throws UnaccessibleFieldException
   * @throws InvalidValueException
   */
  private DomNode createNode(String stringValue, Class<?> valueType, String name)
          throws IncorrectDomNodeTypeException, InvalidSyntaxException, UnaccessibleFieldException, InvalidValueException {
    String openObj = Character.toString(OPEN_OBJECT_BRACE);
    String openColl = Character.toString(OPEN_COLLECTION_BRACE);
    DomNode node;
    if (valueType.isAssignableFrom(Collection.class)) {
      node = null;
    }
    else if (valueType.isAssignableFrom(Map.class)) {
      node = null;
    }
    else if (valueType.isArray()) {
      if (stringValue.equals(openColl)) {
        node = new DomNode(NodeType.array, name);
        createTree(node, valueType);
      }
      else {
        node = new DomNode(NodeType.simple, name).setValue(null);
      }
    }
    else if (isString(valueType) || isWrapperType(valueType) || valueType.isPrimitive()) {
      Object value = getValue(stringValue, valueType);
      node = new DomNode(NodeType.simple, name).setValue(value);
    }
    else {
      if (stringValue.equals(openObj)) {
        node = new DomNode(NodeType.object, name);
        createTree(node, valueType);
      }
      else {
        node = new DomNode(NodeType.simple, name).setValue(null);
      }
    }
    return node;
  }

  /**
   * Creates DOM tree starting with a given parent
   *
   * @param parent
   * @param cls
   * @throws InvalidSyntaxException
   * @throws UnaccessibleFieldException
   * @throws InvalidValueException
   */
  private void createTree(DomNode parent, Class<?> cls)
          throws InvalidSyntaxException, UnaccessibleFieldException, InvalidValueException {
    try {
      String token;
      switch (parent.getType()) {
        case object:
          String closeObj = Character.toString(CLOSE_OBJECT_BRACE);
          while ((token = getNextToken()) != null && !token.equals(closeObj)) {
            String name = token.replaceAll(Character.toString(QUOTE), "");
            Field field;
            try {
              field = cls.getDeclaredField(name);
            }
            catch (NoSuchFieldException e) {
              field = getFieldBySecondName(name, cls);
              if (field == null) {
                throw new InvalidSyntaxException("Uknown field: " + name);
              }
              name = field.getName();
            }
            token = getNextToken();
            DomNode child = createNode(token, field.getType(), name);
            parent.addField(child);
          }
          if (token == null) {
            throw new InvalidSyntaxException(CLOSE_OBJECT_BRACE + " expected");
          }
          break;

        case array:
          String closeColl = Character.toString(CLOSE_COLLECTION_BRACE);
          ArrayList<DomNode> ls = new ArrayList<>();
          while ((token = getNextToken()) != null && !token.equals(closeColl)) {
            DomNode elem = createNode(token, cls.getComponentType());;
            ls.add(elem);
          }
          if (token == null) {
            throw new InvalidSyntaxException(CLOSE_COLLECTION_BRACE + " expected");
          }
          parent.setValue(ls.toArray());
          break;
      }
    }
    catch (IncorrectDomNodeTypeException e) {
      e.printStackTrace();
    }
  }

  /**
   * Gets an object by DOM tree
   *
   * @param node the DOM tree
   * @param cls the class of the object
   * @return
   * @throws DefaultConstructorException
   * @throws UnaccessibleFieldException
   * @throws InvalidSyntaxException
   */
  private Object createObject(DomNode node, Class<?> cls) throws DefaultConstructorException, UnaccessibleFieldException, InvalidSyntaxException {
    try {
      switch (node.getType()) {
        case object:
          Constructor<?> cons = cls.getDeclaredConstructor();
          Object obj = cons.newInstance();
          for (int i = 0; i < node.countFields(); ++i) {
            DomNode f = node.getField(i);
            Field field = cls.getDeclaredField(f.getName());
            Object value = createObject(f, field.getType());
            field.setAccessible(true);
            field.set(obj, value);
            field.setAccessible(false);
          }
          return obj;

        case simple:
          return node.getValue();

        case array:
          Object arr = Array.newInstance(cls.getComponentType(), Array.getLength(node.getValue()));
          for (int i = 0; i < Array.getLength(arr); ++i) {
            Array.set(arr,i,  cls.getComponentType().cast(createObject((DomNode)Array.get(node.getValue(), i), cls.getComponentType())));
          }
          return arr;

        default:
          return null;
      }
    }
    catch (InstantiationException e) {
      throw new UnaccessibleFieldException("Cannot instantiate an object of " + cls.getName());
    }
    catch (InvocationTargetException e) {
      throw new UnaccessibleFieldException("WTF4");
    }
    catch (NoSuchMethodException e) {
      throw new DefaultConstructorException(
              "The class " + cls.getName() + " doesn't have constructor without parameters");
    }
    catch (IllegalAccessException e) {
      throw new UnaccessibleFieldException("Cannot access " + e.getMessage());
    }
    catch (IncorrectDomNodeTypeException e) {
      e.printStackTrace();
      return null;
    }
    catch (NoSuchFieldException e) {
      throw new InvalidSyntaxException("The field is not found " + e.getMessage());
    }
  }

  @Override
  public <T> T deserialize(String fileName, Class<T> cls)
          throws InvalidSyntaxException, IOException, InvalidValueException, DefaultConstructorException {
    try {
      String content = readAll(fileName);
      setSource(content);
      String token = getNextToken();
      DomNode root = null;
      try {
        root = createNode(token, cls);
      }
      catch (IncorrectDomNodeTypeException e) {
        e.printStackTrace();
      }
      if (root.getType() == NodeType.simple) {
        throw new InvalidSyntaxException("JSON must start with '{' or '['");
      }
      Object object = createObject(root, cls);
      return (T) object;
    }
    catch (UnaccessibleFieldException e) {
      e.fillInStackTrace();
      return null;
    }
  }
}
