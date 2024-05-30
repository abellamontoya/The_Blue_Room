package com.example.blueroom;

import java.io.Serializable;
import java.util.List;

public class products implements Serializable {
    public String author;
    public String imageurl;
    public String name;
    public float quantity;
    public float price;
    public long date;
    public String type;
    public List<String> tag;  // Cambia este campo a List<String>

    public products() {}

    public products(String imageUrl, String name, String author, float price, float quantity) {
        this.imageurl = imageUrl;
        this.name = name;
        this.author = author;
        this.price = price;
        this.quantity = quantity;
    }

    public products(String author, String imageURL, String name, float quantity, float price, long date, String type, List<String> tag) {
        this.author = author;
        this.imageurl = imageURL;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.date = date;
        this.type = type;
        this.tag = tag;
    }

    public products(String author, String imageUrl, String name, double price) {
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setImageurl(String imageurl) {  // Cambiado a setImageurl
        this.imageurl = imageurl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(float quantity) {
        this.quantity = quantity;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }

    public String getAuthor() {
        return author;
    }

    public String getImageurl() {  // Cambiado a getImageurl
        return imageurl;
    }

    public String getName() {
        return name;
    }

    public float getQuantity() {
        return quantity;
    }

    public float getPrice() {
        return price;
    }
}
