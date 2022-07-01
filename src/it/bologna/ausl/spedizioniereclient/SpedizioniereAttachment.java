/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.spedizioniereclient;

import java.io.File;
import java.io.InputStream;

/**
 *
 * @author andrea
 */
public class SpedizioniereAttachment {

    private InputStream payload;
    private String mime = "binary/octet-stream";
    private String name;
    private File file;

    public SpedizioniereAttachment(String name, String mime, InputStream is) {
        this.name = name;
        setMime(mime);
        this.payload = is;
    }

    public SpedizioniereAttachment(String name, String mime, File file) {
        this.name = name;
        setMime(mime);
        this.file = file;
    }

    public InputStream getPayload() {
        return payload;
    }

    public String getMime() {
        return mime;
    }

    private void setMime(String mime) {
        if (mime == null || mime.equals("")) {
            return;
        }
        this.mime = mime;
    }

    public String getName() {
        return name;
    }

    public File getFile() {
        return file;
    }
}