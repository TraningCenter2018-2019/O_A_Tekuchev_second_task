package parsertests;

import org.junit.Test;
import org.junit.Assert;
import parser.exceptions.UnaccessibleFieldException;
import parser.serializableclasses.Car;
import parser.serializers.JsonSerializer;
import parser.serializers.Serializer;

public class TestSerializer {

  private Serializer serializer = new JsonSerializer();

  @Test
  public void testArraySerialization() {
    Car[] array = new Car[] {
            new Car("A", 1),
            new Car("B", 2)
    };
    try {
      String expected = "[{\"model\":\"A\",\"maxSpeed\":1},{\"model\":\"B\",\"maxSpeed\":2}]";
      String actual = serializer.serialize(array);
      Assert.assertEquals("Array", expected,actual);
      System.out.println(actual);
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testSerializingObjectToJson() {
    Car car = new Car("BMW", 160);
    String expected = "{\"model\":\"BMW\",\"maxSpeed\":160}";
    String actual = null;
    JsonSerializer s = new JsonSerializer();
    try {
      actual = serializer.serialize(car);
    }
    catch (UnaccessibleFieldException e) {
      e.printStackTrace();
    }
    Assert.assertEquals("Simple object", expected,actual);
  }
}
