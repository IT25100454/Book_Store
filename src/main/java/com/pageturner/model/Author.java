package com.pageturner.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "authors")
public class Author extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nationality;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Size(max = 510, message = "Photo URL cannot exceed 510 characters")
    @Column(name = "photoUrl", length = 510)
    private String photoUrl;

    public Author() {
    }

    public Author(String name, String nationality, String bio, String photoUrl) {
        this.name = name;
        this.nationality = nationality;
        this.bio = bio;
        this.photoUrl = photoUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
