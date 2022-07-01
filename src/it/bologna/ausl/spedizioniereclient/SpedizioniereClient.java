/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.spedizioniereclient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.commons.io.Charsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 *
 * @author andrea
 */
public class SpedizioniereClient {

    private String username, password, url;
    private DefaultHttpClient c;
    static Logger log = LogManager.getLogger(SpedizioniereClient.class);
    static final String SENDMAIL_URL = "/sendmail";
    static final String GETSTATUS_URL = "/get_status/";
    static final String GETRECEPITS_URL = "/get_recepits/";
    static final String GETEMAILS_URL = "/get_emails";
    static final String GETEMAILSWITHPERMISSION_URL = "/get_emails_with_permission";

    private boolean auth = false;

    public SpedizioniereClient() {
    }

    public SpedizioniereClient(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        if (username != null && password != null) {
            auth = true;
        }
        this.c = new DefaultHttpClient();
        c.getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", SSLSocketFactory.getSocketFactory(), 443)
        );
        trustAllSSL(c);
    }

    /**
     * non funziona più
     *
     * @param c
     * @deprecated
     */
    @Deprecated
    private static void trustAllSSL(HttpClient c) {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("SSL");

            // set up a TrustManager that trusts everything
            try {
                sslContext.init(null,
                        new TrustManager[]{new X509TrustManager() {
                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            log.info("getAcceptedIssuers =============");
                            return null;
                        }

                        @Override
                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                            log.info("checkClientTrusted =============");
                        }

                        @Override
                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                            log.info("checkServerTrusted =============");
                        }
                    }}, new SecureRandom());
            } catch (KeyManagementException e) {
                log.error("errore KeyManagementException", e);
            }
            SSLSocketFactory ssf = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = c.getConnectionManager();
            SchemeRegistry sr = ccm.getSchemeRegistry();
            sr.register(new Scheme("https", 443, ssf));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public String sendMail(SpedizioniereMessage m, boolean force) throws ClientProtocolException, IOException {

        MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
        entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

        //MultipartEntity me = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        List<NameValuePair> formparams = m.getFormValues();

        if (auth) {
            formparams.add(new BasicNameValuePair("username", this.username));
            formparams.add(new BasicNameValuePair("password", this.password));
        }
        if (force) {
            formparams.add(new BasicNameValuePair("force", "True"));
        }
        log.info("Building name values pairs to post:");
        for (NameValuePair nvp : formparams) {
            if (nvp.getValue() != null) {
                log.info(nvp.getName() + " : " + nvp.getValue());
                entityBuilder.addPart(nvp.getName(), new StringBody(nvp.getValue(), ContentType.create("text/plain", Charsets.UTF_8)));
                //me.addPart(nvp.getName(), new StringBody(nvp.getValue(), Charsets.UTF_8));
            } else {
                log.info(nvp.getName() + " non valorizzato!");
            }
        }
        int fileindex = 0;
        if (m.getAttachments() != null) {
            for (SpedizioniereAttachment a : m.getAttachments()) {
                if (a.getFile() != null) {
                    entityBuilder.addPart("file" + fileindex++, new FileBody(a.getFile(), ContentType.create(a.getMime(), Charsets.UTF_8), a.getName()));
                } //me.addPart("file" + fileindex++, new FileBody(a.getFile(), a.getName(), a.getMime(), "UTF-8"));
                else if (a.getPayload() != null) {
                    entityBuilder.addPart("file" + fileindex++, new InputStreamBody(a.getPayload(), ContentType.create(a.getMime()), a.getName()));
                } //me.addPart("file" + fileindex++, new InputStreamBody(a.getPayload(), a.getMime(), a.getName()));
                else {
                    throw new IOException("file or stream not found");
                }
                log.info("Attached file: " + a.getName());

            }
        }

        HttpPost httppost = new HttpPost(this.url + SENDMAIL_URL);
        httppost.setEntity(entityBuilder.build());
        try {
            log.info("Sending mail to: " + this.url + SENDMAIL_URL);
            HttpResponse response = c.execute(httppost);
            HttpEntity res = response.getEntity();
            if (handleStatus(response)) {
                return EntityUtils.toString(res);
            } else {
                httppost.abort();
                return null;
            }
        } catch (Throwable e) {
            log.error("errore", e);
            throw new IOException(e);
        }

//        return null;
    }

    private boolean handleStatus(HttpResponse response) throws IOException {
        //check status
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode < 300 && statusCode >= 200) {
            return true;
        } else if (statusCode == 404) {
            response.getEntity().getContent().close();
            return false;
        } else if (statusCode < 500 && statusCode >= 400) {
            throw new IllegalArgumentException("Bad request:\nStatus: " + statusCode + "Error:\n" + EntityUtils.toString(response.getEntity()));
        } else if (statusCode >= 500 && statusCode < 600) {
            response.getEntity().getContent().close();
            return false;
        }
        throw new IllegalArgumentException("Unhandled statusCode :" + statusCode);
    }

    public SpedizioniereStatus getStatus(String messageId) throws IOException {
        URI uri = null;
        try {
            uri = new URI(this.url + GETSTATUS_URL + messageId);
            if (auth) {
                uri = new URIBuilder(uri).addParameter("username", username).addParameter("password", password).build();
            }
        } catch (URISyntaxException ex) {
            log.error("errore", ex);
            throw new IllegalArgumentException(ex);
        }

        HttpGet get = new HttpGet(uri);
        log.info(messageId);
        try {
            HttpResponse response = c.execute(get);
            if (handleStatus(response)) {
                HttpEntity res = response.getEntity();
                String resString = EntityUtils.toString(res);
                log.info("response:\n" + resString);
                return SpedizioniereStatus.statusFromJson(resString);
            } else {
                return null;
            }
        } catch (Throwable e) {
            log.warn("errore", e);
            return null;
        }

    }

    public ArrayList<String> getAvailableEmails() throws IOException {
        URI uri = null;
        try {
            uri = new URI(this.url + GETEMAILS_URL);
            if (auth) {
                uri = new URIBuilder(uri).addParameter("username", username).addParameter("password", password).build();
            }
        } catch (URISyntaxException ex) {
            log.error("errore", ex);
            throw new IllegalArgumentException(ex);
        }

        HttpGet get = new HttpGet(uri);
        try {
            HttpResponse response = c.execute(get);
            if (handleStatus(response)) {
                HttpEntity res = response.getEntity();
                String resString = EntityUtils.toString(res);
                log.debug("response:\n" + resString);
                return availableEmailsFromJson(resString);
            } else {
                return null;
            }
        } catch (IOException e) {
            log.warn("errore", e);
            return null;
        }

    }

    public ArrayList<String> getAvailableEmailsWithPermission() throws IOException {
        URI uri = null;
        try {
            uri = new URI(this.url + GETEMAILSWITHPERMISSION_URL);
            if (auth) {
                uri = new URIBuilder(uri).addParameter("username", username).addParameter("password", password).build();
            }
        } catch (URISyntaxException ex) {
            log.error("errore", ex);
            throw new IllegalArgumentException(ex);
        }

        HttpGet get = new HttpGet(uri);
        try {
            HttpResponse response = c.execute(get);
            if (handleStatus(response)) {
                HttpEntity res = response.getEntity();
                String resString = EntityUtils.toString(res);
                log.info("response:\n" + resString);
                return availableEmailsFromJson(resString);
            } else {
                return null;
            }
        } catch (IOException e) {
            log.warn("errore", e);
            return null;
        }

    }

    private ArrayList<String> availableEmailsFromJson(String json) {
        JSONArray availableEmailsJson = (JSONArray) JSONValue.parse(json);
        return availableEmailsJson;
    }

    public boolean isAvailableEmail(String email) throws IOException {
        ArrayList<String> availableEmails = getAvailableEmails();
        return availableEmails != null && availableEmails.contains(email);
    }

    public ArrayList<SpedizioniereRecepit> getRecepits(String messageId) throws IOException {
        URI uri = null;
        try {
            uri = new URI(this.url + GETRECEPITS_URL + messageId);
            if (auth) {
                uri = new URIBuilder(uri).addParameter("username", username).addParameter("password", password).build();
            }
        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(SpedizioniereClient.class.getName()).log(Level.SEVERE, null, ex);
        }

        HttpGet get = new HttpGet(uri);
        log.info(messageId);
        try {
            HttpResponse response = c.execute(get);
            if (handleStatus(response)) {
                HttpEntity res = response.getEntity();
                String resString = EntityUtils.toString(res);
                log.info("Response:\n" + resString);
                return SpedizioniereRecepit.recepitsFromJson(resString);
            } else {
                return null;
            }
        } catch (IOException e) {
            log.warn("errore", e);
            return null;
        }
//        finally {
//
//        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
