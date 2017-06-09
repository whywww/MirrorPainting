package activitytest.example.com.test01;

import android.provider.ContactsContract;

/**
 * Created by 魏昊妤 on 2017/6/6.
 */

public class Image {
    private String name;
    private int imageId;
    private String description;

    public Image(String name, int imageId, String description){
        this.name = name;
        this.imageId = imageId;
        this.description = description;
    }

    public String getName(){
        return name;
    }

    public int getImageId(){
        return imageId;
    }

    public String getDescription(){
        return description;
    }
}
