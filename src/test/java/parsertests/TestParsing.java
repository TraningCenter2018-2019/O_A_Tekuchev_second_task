package parsertests;

import org.junit.Assert;
import org.junit.Test;
import parser.deserializers.Deserializer;
import parser.deserializers.JsonSaxDeserializer;
import parser.exceptions.UnaccessibleFieldException;
import parser.serializableclasses.Car;
import parser.serializers.JsonSerializer;
import parser.serializers.Serializer;

public class TestParsing {
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
      String file = "./src/test/testfiles/test1.txt";
      serializer.serialize(file, expected);
      Car[] actual = deserializer.deserialize(file, Car[].class);
      //Assert.assertEquals(expected, actual);
      Assert.assertArrayEquals(expected, actual);
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testObjectParsing() {
    try {
      Car expected = new Car("GAZ", 100);
      String file = "./src/test/testfiles/test12.txt";
      serializer.serialize(file, expected);
      Car actual = deserializer.deserialize(file, Car.class);
      Assert.assertEquals(expected, actual);
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
    }
  }
}
