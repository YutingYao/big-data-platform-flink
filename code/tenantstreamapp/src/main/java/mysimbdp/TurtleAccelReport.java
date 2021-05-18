package mysimbdp;

public class TurtleAccelReport {

  public String dev_id;
  public float acceleration;

  public TurtleAccelReport() {}

  public TurtleAccelReport(String dev_id, float acceleration) {

    this.dev_id = dev_id;
    this.acceleration = acceleration;
  }

  public String toString() {

    return "Turtle with " + dev_id +
        " has average acceleration " + acceleration;
  }

  public String toJSON() {

    return "{\"turtleAccelReport\":{\"dev_id\":\"" +
        dev_id + "\", \"acceleration\":" + acceleration + "}}";
  }
}
