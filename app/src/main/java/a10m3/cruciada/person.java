package a10m3.cruciada;

/**
 * Created by Dany on 27-Aug-18.
 */

public class person {

    private String Name;

    public person()
    {}

    public person(String Name)
    {
        this.Name=Name;
    }

    public String getName(){return Name;}

    public void setName(String Name){
        this.Name=Name;
    }

}
