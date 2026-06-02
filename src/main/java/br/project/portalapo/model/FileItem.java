package br.project.portalapo.model;

import jakarta.persistence.Embeddable;

@Embeddable
public class FileItem {

    private String name;
    private String url;
    private String hash;
    private String contentType;
    private Long tamanhoBytes;

    public FileItem() {}

    public FileItem(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public FileItem(String name, String url, String hash, String contentType, Long tamanhoBytes) {
        this.name = name;
        this.url = url;
        this.hash = hash;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
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

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(Long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }
}
