package dev.edmt.android_app.model;

public class Food {
    private String Name,Image,Decscription,Price,Discount,MenuId;

    public Food() {
    }

    public Food(String name, String image, String decscription, String price, String discount, String menuId) {
        Name = name;
        Image = image;
        Decscription = decscription;
        Price = price;
        Discount = discount;
        MenuId = menuId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDecscription() {
        return Decscription;
    }

    public void setDecscription(String decscription) {
        Decscription = decscription;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }
}
