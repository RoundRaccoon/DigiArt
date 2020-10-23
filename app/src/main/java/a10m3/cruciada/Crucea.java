package a10m3.cruciada;


public class Crucea {

    private String title,desc,image,username;
    private int like;

    public Crucea()
    {

    }

    public Crucea(String title,String desc,String image, String username, int like)
    {
        this.username=username;
        this.title=title;
        this.desc=desc;
        this.image=image;
        this.like=like;
    }

    public String getTitle(){
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getDesc() {
        return desc;
    }

    public String getUsername() { return username; }

    public int  getLike() { return like; }


    public void setTitle(String title) { this.title = title; }

    public void setImage(String image) { this.image = image; }

    public void setDesc(String desc) { this.desc = desc; }

    public void setUsername(String username) { this.username = username;}

    public void setLike(int like) { this.like = like; }

}

