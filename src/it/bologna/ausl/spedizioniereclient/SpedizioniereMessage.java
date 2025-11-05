package it.bologna.ausl.spedizioniereclient;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

/**
 *
 * @author andrea
 */
public class SpedizioniereMessage {
private String to,cc,subject,externalId,from,message;
private ArrayList<SpedizioniereAttachment> attachments;

    public SpedizioniereMessage() {
    }

    public SpedizioniereMessage(String from, String to, String cc, String subject, String message, String externalId) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.message = message;
        this.externalId = externalId;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getMail() {
        return from;
    }

    public void setMail(String mail) {
        this.from = mail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<SpedizioniereAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(ArrayList<SpedizioniereAttachment> attachments) {
        this.attachments = attachments;
    }
    
    public List<NameValuePair> getFormValues() throws UnsupportedEncodingException{
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        formparams.add(new BasicNameValuePair("mail", this.from));
        formparams.add(new BasicNameValuePair("to", this.to));
        formparams.add(new BasicNameValuePair("cc", this.cc));
        formparams.add(new BasicNameValuePair("subject", this.subject));
        formparams.add(new BasicNameValuePair("message", this.message));
        formparams.add(new BasicNameValuePair("external_id", this.externalId));
        return formparams;
    }
}
