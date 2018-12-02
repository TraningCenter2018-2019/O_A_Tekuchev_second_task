package parsertests;

import org.junit.Test;
import org.junit.Assert;
import parser.serializableclasses.Car;
import parser.serializers.JsonSerializer;
import parser.serializers.Serializer;

public class TestSerializer {

  @Test
  public void testSerializingObjectToJson() {
    Car car = new Car("BMW", 160);
    Serializer serializer = new JsonSerializer();
    String expected = "{\"model\":\"BMW\",\"maxSpeed\":160}";
    String actual = serializer.serialize(car);
    Assert.assertEquals("Test test", expected,actual);
  }
}
