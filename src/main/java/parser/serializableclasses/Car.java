package parser.serializableclasses;

public class Car {
  private String model;

  private int maxSpeed;

  public Car() { }

  public Car(String aModel, int aMaxSpeed) {
    model = aModel;
    maxSpeed = aMaxSpeed;
  }

  public int getMaxSpeed() {
    return maxSpeed;
  }

  public String getModel() {
    return model;
  }
}
