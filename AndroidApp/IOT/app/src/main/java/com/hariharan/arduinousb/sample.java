package com.hariharan.arduinousb;
public class sample {

    private  static float temperature=10, dielectric=20;
    private static String time_stamp;



    public static float getTemperature() {

        return temperature;
    }
    public float getDielectric() {

        return dielectric;
    }
    public String getTime_stamp() {

        return time_stamp;
    }

    public static void setTemperature(float temp)
    {

            temperature = temp;


        }




    public void setDielectric(float diel)
    {
        dielectric=diel;
    }

    public static void setTime_stamp(String stamp)
    {
        time_stamp=stamp;
    }
}
