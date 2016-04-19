package com.newtra.anatomictherapy.Beans;

/**
 * Created by sethu on 2/29/2016.
 */
public class Book {
    private String title;
    private String description;
    private int bookCoverId;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Book(String _tiltle, String _description, int _resId, String _url)
    {
        title=_tiltle;
        description=_description;
        bookCoverId=_resId;
        url=_url;


    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBookCoverId() {
        return bookCoverId;
    }

    public void setBookCoverId(int bookCoverId) {
        this.bookCoverId = bookCoverId;
    }
}
