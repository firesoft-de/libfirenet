/*
 * Copyright (c) 2018.  David Schlossarczyk
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For the full license visit https://www.gnu.org/licenses/gpl-3.0.
 */

package firesoft.de.libfirenet.authentication;

import android.annotation.SuppressLint;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import firesoft.de.libfirenet.R;
import firesoft.de.libfirenet.http.Parameter;

/**
 * Autehtifizierungsklasse zur Umsetzung von Digest. Achtung: "auth-int" wird nicht unterstützt.
 */
public class Digest extends AuthenticationBase {

    //=======================================================
    //======================KONSTANTEN=======================
    //=======================================================

    private static final String HEX_LOOKUP = "0123456789abcdef";
    private static final short CNONCE_LENGTH = 16;

    //=======================================================
    //======================VARIABLEN========================
    //=======================================================

    private String username;
    private String password;
    private String realm;

    /**
     * Nonce des Servers
     */
    private String nonce;

    /**
     * Verwendeter Algorithmus
     */
    private EncryptionMethod algorithm;

    /**
     * Verfahrensmarker
     */
    private String qop;

    /**
     * Ein Wert der einfach an den Server zurückgegeben werden soll
     */
    private String opaque;

    /**
     * Nonce der vom Client festgelegt wird
     */
    private String cnonce;

    /**
     * Nonce-Count
     */
    private int nc;

    //=======================================================
    //=====================KONSTRUKTOR=======================
    //=======================================================

    public Digest(String username, String password) { //, String qop, EncryptionMethod algorithm

        this.username = username;
        this.password = password;

    }

    //=======================================================
    //===================PUBLIC METHODEN=====================
    //=======================================================

    /**
     *
     * @param connection Benötigt ZWINGEND ein HttpURLConnection Objekt!
     */
    @Override
    protected Parameter nextAuth(HttpURLConnection connection) throws SecurityException, UnsupportedEncodingException {

        // Falls keine Verbindung übergeben wird, muss ein Fehler geworfen werden
        if (connection == null) {
            throw new UnsupportedOperationException(generateExceptionMessage(this.getClass(), R.string.exception_connection_needed));
        }

        // Antwortheader verarbeiten um die benötigten Daten zu erhalten
        if (!processHeader(connection)) {
            // Es fehlen Daten. Das kann bspw. bei der ersten Anfrage passieren. D.h. die Bearbeitung der Challenge wird hier abgebrochen und es wird auf den zweiten Verbindungsversuch gewartet.
            return new Parameter("Authorization", "");
        }

        // Prüfen ob die Integritätssicherung aktiviert ist
        if (qop.equals("auth-int")) {

            // Für die Integritätssicherung wird der Entity Body der HTTP Nachricht benötigt.
            throw new UnsupportedOperationException(generateExceptionMessage(this.getClass(),R.string.exception_authint_not_implemented));

        }

        // Digest erstellen
        String digest = generateDigest(connection.getRequestMethod(), connection.getURL());

        // Nonce-Count erhöhen
        nc ++;
        return new Parameter("Authorization", digest, true);
    }

    //=======================================================
    //==================PRIVATE METHODEN=====================
    //=======================================================

    private boolean processHeader(HttpURLConnection connection) {

        String headerField = connection.getHeaderField("WWW-Authenticate");

        // Prüfen, ob im Header ein Wert steht. Falls nein: keine Daten = kein Digest
        if (headerField == null) {
            return false;
        }

        headerField = headerField.replace("Digest ", "");

        String[] auth_elements = headerField.split(", ");

        // Dieser Counter zählt wie viele Elemente gefunden wurden. Wenn alle benötigten Elemente da sind, steht er auf >= 5. Falls er kleiner ist, fehlt ein Wert und die Berechnung der Challenge kann nicht fortgesetzt werden.
        int all_needed_elements_gathered_counter = 0;

        for (String element: auth_elements
                ) {

            if (element.contains("MD5")) {
                // Es wird MD5 verwendet
                algorithm = EncryptionMethod.MD5;
                all_needed_elements_gathered_counter ++;

            }
            else {
                String[] element_fields = element.split("(=\")");

                switch (element_fields[0]) {

                    case "realm":
                        realm = element_fields[1];
                        realm = realm.replace("\"","");
                        all_needed_elements_gathered_counter ++;
                        break;

                    case "nonce":
                        nonce = element_fields[1];
                        nonce = nonce.replace("\"","");
                        all_needed_elements_gathered_counter ++;
                        break;

                    case "qop":
                        qop = element_fields[1];
                        qop = qop.replace("\"","");
                        all_needed_elements_gathered_counter ++;
                        break;

                    case "opaque":
                        opaque = element_fields[1];
                        opaque = opaque.replace("\"","");
                        break;

                    // Falls es zu Fehlern beim bilden des Digest kommt, muss eventuell dieser Abschnitt wieder aktiviert werden und die restlichen Bereiche wo clientseitig ein NC gehandelt wird müssen angepasst werden.

                    case "nc":
                        String tmp = element_fields[1].replace("\"","");
                        nc = Integer.valueOf(tmp);
                        break;

                }
            }
        }

        if (all_needed_elements_gathered_counter >= 4) {
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * Erzeugt einen neuen Digest String
     */
    private String generateDigest(String method, URL url) throws UnsupportedEncodingException {

        // Eine Digest-Instanz mit dem vom Server angegebenen Algorithmus erstellen
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance(algorithm.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new SecurityException(e.getMessage());
        }

        // Falls nc = 0, diesen auf 1 setzen
        if (nc == 0) {
            nc = 1;
        }

        // String A1 (RFC2617 3.2.2.2) erstellen und hashen. Dieser enthält Nutzername, Realm und Passwort
        String rawA1;
        String hashedA1;

        rawA1 = username + ":" + realm + ":" + password;
        digest.update(rawA1.getBytes("ISO-8859-1"));

        hashedA1 = bytesToHexString(digest.digest());
        digest.reset();


        // String A2 (RFC2617 3.2.2.3) erstellen und hashen. Dieser enthält die Request Methode und der URL
        String rawA2 = "";
        String hashedA2;

        if (qop.equals("auth")) {
            rawA2 = method+ ":" + url.getPath();
        }
//        else if (qop.equals("auth-int")) {
//            rawA2 = method+ ":" + url.getPath() + ":" + ;
//        }

        digest.update(rawA2.getBytes("ISO-8859-1"));
        hashedA2 = bytesToHexString(digest.digest());
        digest.reset();


        // Den Response String aus A1 und A2 zusammebauen
        String rawResponse;
        String hashedResponse;

        if (qop.equals("auth") || qop.equals("auth-int")) {
            // "auth-int" wird nicht wirklich unterstüzt. Alibiweise wird versucht mit normalen "auth" durchzukommen.


            // Für die Tests wird ggf. ein cnonce vorgegeben
            if (cnonce == null) {
                cnonce = generateCnonce(CNONCE_LENGTH);
            }

            @SuppressLint("DefaultLocale")
            String nc_string = String.format("%08d", nc);

            rawResponse = hashedA1 + ":" + nonce + ":" + nc_string + ":" + cnonce + ":" + qop + ":" + hashedA2;
        }
        else {
            rawResponse = method + ":" + nonce + ":" +  url.getPath();
        }

        digest.update(rawResponse.getBytes("ISO-8859-1"));

        hashedResponse = bytesToHexString(digest.digest());
        digest.reset();

        // Die Authentifzierungsbestandteile zusammenführen und an die Connection anhängen

        StringBuilder builder = new StringBuilder();

        builder.append("Digest ");
        builder.append("username" + "=\"").append(username).append("\"");
        builder.append(", realm" + "=\"").append(realm).append("\"");
        builder.append(", nonce" + "=\"").append(nonce).append("\"");
        builder.append(", uri" + "=\"").append(url.getPath()).append("\"");
        builder.append(", algorithm" + "=").append(algorithm.toString()).append("");
        builder.append(", response" + "=\"").append(hashedResponse).append("\"");
        builder.append(", qop" + "=").append(qop).append("");
        builder.append(", cnonce" + "=\"").append(cnonce).append("\"");

        if (nc != 0) {
            @SuppressLint("DefaultLocale")
            String nc_string = String.format("%08d", nc);
            builder.append(", nc" + "=").append(nc_string).append("");
        }

        if (opaque != null) {
            builder.append(", opaque" + "=\"").append(opaque).append("\"");
        }

        //builder.append(";");

        return builder.toString();

    }


    // (c) Slightfood, https://gist.github.com/slightfoot/5624590
    private static String bytesToHexString(byte[] bytes)
    {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            sb.append(HEX_LOOKUP.charAt((aByte & 0xF0) >> 4));
            sb.append(HEX_LOOKUP.charAt((aByte & 0x0F)));
        }
        return sb.toString();
    }

    /**
     * Generiert über eine unsicheren Zufallsgenerator einen CNONCE
     * @param length Gewünschte Länge des CNONCE
     */
    private String generateCnonce(int length) {

        final String baseData = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random generator = new Random();

        StringBuilder builder = new StringBuilder(length);

        for (int i = 0; i<length; i++) {
            builder.append(baseData.charAt(generator.nextInt(baseData.length())));
        }

        cnonce = builder.toString();

        return builder.toString();

    }

    //=======================================================
    //========================Testmethode===========================
    //=======================================================

    /**
     * Diese Methode stellt ein Interface bereit, um die grundlegenenden Berechnungsmethoden dieser Klasse testen zu können
     */
    protected String testInterface(String url, String requestMethod, String username, String password, String realm, String nonce, String cnonce, Digest.EncryptionMethod algorithm, String qop, String opaque, int nc) throws MalformedURLException, UnsupportedEncodingException {

        this.username = username;
        this.password = password;
        this.realm = realm;
        this.nonce = nonce;
        this.cnonce = cnonce;
        this.algorithm = algorithm;
        this.qop = qop;
        this.opaque = opaque;
        this.nc = nc;

        URL javaURL = new URL(url);

        return generateDigest(requestMethod, javaURL);


    }

    //=======================================================
    //========================ENUM===========================
    //=======================================================

    /**
     * Enum mit integrierter toString Funktion
     * https://stackoverflow.com/a/3978690
     */
    public enum EncryptionMethod {
        MD5("MD5"),
        SHA256("SHA-256"),
        SHA512("SHA-512");

        private final String name;

        EncryptionMethod(final String name) {
            this.name = name;
        }

            /* (non-Javadoc)
             * @see java.lang.Enum#toString()
             */
        @Override
        public String toString() {
            return name;
        }
    }

}
