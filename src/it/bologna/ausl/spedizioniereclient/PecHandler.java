/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.bologna.ausl.spedizioniereclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author andrea
 */
public class PecHandler {

    static Logger log = LogManager.getLogger(PecHandler.class);

    public enum RecipientType {
        PEC,
        REGULAR_EMAIL,
        UNKNOWN
    };

    public static Session getGenericSmtpSession() {
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        return Session.getDefaultInstance(props, null);
    }

    public static ArrayList<Part> getAllParts(Part in) throws IOException, MessagingException {
        ArrayList<Part> res = new ArrayList<>();
        if (!in.isMimeType("multipart/*")) {
            res.add(in);
            return res;
        } else {
            Multipart mp = (Multipart) in.getContent();
            for (int i = 0, n = mp.getCount(); i < n; i++) {
                Part part = mp.getBodyPart(i);
                if (!part.isMimeType("multipart/*")) {
                    res.add(part);
                } else {
                    res.addAll(getAllParts(part));
                }

            }
            return res;

        }
    }

    public static MimeMessage BuildMailMessageFromString(String s) throws UnsupportedEncodingException, MessagingException {

        ByteArrayInputStream bais = null;
        MimeMessage m;

        bais = new ByteArrayInputStream(s.getBytes("utf8"));
        m = new MimeMessage(getGenericSmtpSession(), bais);
        return m;

    }

    public static MimeMessage BuildMailMessageFromIS(InputStream mimeInputStream) throws UnsupportedEncodingException, MessagingException {

        MimeMessage m = new MimeMessage(getGenericSmtpSession(), mimeInputStream);
        return m;

    }

    public static Map<String, RecipientType> getRecipientsType(String message) throws UnsupportedEncodingException, MessagingException, IOException, ParsingException {
        MimeMessage recepitMessage = BuildMailMessageFromString(message);
        return getRecipientsType(recepitMessage);
    }

    public static Map<String, RecipientType> getRecipientsType(InputStream message) throws UnsupportedEncodingException, MessagingException, IOException, ParsingException {
        MimeMessage recepitMessage = BuildMailMessageFromIS(message);
        return getRecipientsType(recepitMessage);
    }

    /**
     * Dato una ricevuta Pec di accettazione in ingresso restituisce una mappa
     * indirizzo mail -> tipo destinatario Se non vengono trovati i dati o non
     * viene capita la tipologia di mail l'inidirizzo non compare nella mappa
     * ritornata.
     *
     * @param recepitMessage
     * @return
     * @throws UnsupportedEncodingException
     * @throws MessagingException
     * @throws IOException
     * @throws ParsingException
     */
    public static Map<String, RecipientType> getRecipientsType(MimeMessage recepitMessage) throws UnsupportedEncodingException, MessagingException, IOException, ParsingException {
        Map<String, RecipientType> res = new HashMap<>();

        ArrayList<Part> recepitParts = getAllParts(recepitMessage);
        Part daticert = null;
        for (Part p : recepitParts) {
            System.out.println(p.getContentType());
            System.out.println(p.getFileName());
            if ("daticert.xml".equalsIgnoreCase(p.getFileName())) {
                daticert = p;
            }
        }
        if (daticert != null) {
            String xmlbody = IOUtils.toString(daticert.getInputStream());
            System.out.println(xmlbody);
            Builder parser = new Builder();
            Document daticertDocument = parser.build(xmlbody, null);
            Element destinatario = null;
            Nodes nodes = daticertDocument.query("/postacert/intestazione/destinatari");
            for (int i = 0; i < nodes.size(); i++) {
                destinatario = (Element) daticertDocument.query("/postacert/intestazione/destinatari").get(i);
                String mailAddress = destinatario.getChild(0).toXML();
                String mailType = destinatario.getAttribute("tipo").getValue();
                log.debug("address: " + mailAddress + " type: " + mailType);
                if (mailType != null) {
                    if ("certificato".equalsIgnoreCase(mailType)) {
                        res.put(mailAddress, RecipientType.PEC);
                    } else if ("esterno".equalsIgnoreCase(mailType)) {
                        res.put(mailAddress, RecipientType.REGULAR_EMAIL);
                    } else {
                        res.put(mailAddress, RecipientType.UNKNOWN);
                    }
                }
            }

        }
        return res;
    }

}
