package parser.serializableclasses;

import parser.parserannotations.Renamed;

public class Car {
  @Renamed(secondName = "mark")
  private String model;

  private int maxSpeed;

  //private Double[] array;

  private Owner owner;

  public Car() { }

  public Car(String aModel, int aMaxSpeed) {
    model = aModel;
    maxSpeed = aMaxSpeed;
    owner = new Owner();
    /*array = new Double[] {
            2.0, 9.45, 3.14
    };*/
  }

  public int getMaxSpeed() {
    return maxSpeed;
  }

  public String getModel() {
    return model;
  }

  @Override
  public boolean equals(Object obj) {
    Car car = (Car)obj;
    return model.equals(car.model) && maxSpeed == car.maxSpeed;
  }
}
