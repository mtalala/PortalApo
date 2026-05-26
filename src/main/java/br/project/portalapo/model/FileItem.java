package br.project.portalapo.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class FileItem {

    private String name;
    private String url;

    public FileItem() {}

    public FileItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}