package com.mass.connect;

public class Users {

    public String name;
    public String image;
    public String status;
    public String thumb_image;

    public Users(){

    }

    public Users(String name,String image,String status){
        this.name = name;
        this.image = image;
        this.status=status;
    }

    public String getImage() {
        return image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbimage() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
