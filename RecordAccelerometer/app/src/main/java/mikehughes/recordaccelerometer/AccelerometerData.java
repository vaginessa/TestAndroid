package mikehughes.recordaccelerometer;

/**
 * Created by mike on 7/17/2015.
 */
public class AccelerometerData
{
    private long timestamp;
    private double x;
    private double y;
    private double z;

    public AccelerometerData(long timestamp, double x, double y, double z)
    {
        this.timestamp = timestamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public long getTimestamp()
    {
        return timestamp;
    }
    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }
    public double getX()
    {
        return x;
    }
    public void setX(double x)
    {
        this.x = x;
    }
    public double getY()
    {
        return y;
    }
    public void setY(double y)
    {
        this.y = y;
    }
    public double getZ()
    {
        return z;
    }
    public void setZ(double z)
    {
        this.z = z;
    }

    public String toString()
    {
        //String one = "t="+timestamp+", x="+x+", y="+y+", z="+z + "\n";
        String two = timestamp + ", " + x + ", " + y + ", " + z + "\n";

        return two;
    }

}
