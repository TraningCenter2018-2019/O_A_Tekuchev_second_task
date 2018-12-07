package parsertests;

import org.junit.Assert;
import org.junit.Test;
import parser.deserializers.Deserializer;
import parser.deserializers.sax.JsonSaxDeserializer;
import parser.exceptions.DefaultConstructorException;
import parser.exceptions.InvalidSyntaxException;
import parser.exceptions.InvalidValueException;
import parser.exceptions.UnaccessibleFieldException;
import parser.serializableclasses.Car;
import parser.serializers.JsonSerializer;
import parser.serializers.Serializer;

import java.io.IOException;

public class TestSaxParsing {
  private Serializer serializer = new JsonSerializer();
  private Deserializer deserializer = new JsonSaxDeserializer();

  @Test
  public void testArrayParsing() {
    try {
      Car[] expected = new Car[] {
              new Car("Toyota", 140),
              new Car("BMW", 170),
              new Car("Lada", 120)
      };
      String file = "./src/test/testfiles/testSaxArr.json";
      serializer.serialize(file, expected);
      Car[] actual = deserializer.deserialize(file, Car[].class);
      //Assert.assertEquals(expected, actual);
      Assert.assertArrayEquals(expected, actual);
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (InvalidSyntaxException e) {
      e.printStackTrace();
    }
    catch (InvalidValueException e) {
      e.printStackTrace();
    }
    catch (DefaultConstructorException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testObjectParsing() {
    try {
      Car expected = new Car("GAZ", 100);
      String file = "./src/test/testfiles/testSaxObj.json";
      serializer.serialize(file, expected);
      Car actual = deserializer.deserialize(file, Car.class);
      Assert.assertEquals(expected, actual);
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    catch (InvalidSyntaxException e) {
      e.printStackTrace();
    }
    catch (InvalidValueException e) {
      e.printStackTrace();
    }
    catch (DefaultConstructorException e) {
      e.printStackTrace();
    }
  }
}
