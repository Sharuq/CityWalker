package com.theguardians.citywalker.ui;
/**
 * This class is utilised for safety tip page
 * @Author Richard
 * @Version 1.1
 */
public class TipsModel {

    private int image;
   // private String title;
    //private String desc;

    public TipsModel(int image, String title, String desc) {
        this.image = image;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    /**
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
     **/
}
