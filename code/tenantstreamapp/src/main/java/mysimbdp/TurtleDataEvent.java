package mysimbdp;

import java.util.Date;

public class TurtleDataEvent {

  // time
  // readable_time
  // acceleration
  // acceleration_x
  // acceleration_y
  // acceleration_z
  // battery
  // humidity
  // pressure
  // temperature
  // dev-id
  public String dev_id;
  public String readable_time;
  public float acceleration;

  TurtleDataEvent() {}

  TurtleDataEvent(
      String dev_id,
      String readable_time,
      float acceleration) {

    this.dev_id = dev_id;    
    this.readable_time = readable_time;
    this.acceleration = acceleration;
  }
  
  public String toString() {

    return "dev_id=" + dev_id +
        " readable_time=" + readable_time +
        " acceleration=" + acceleration;
  }
}
