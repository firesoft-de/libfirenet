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

import android.support.annotation.Nullable;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import firesoft.de.libfirenet.http.Parameter;


/**
 * Ermöglicht die Authentifzierung mittels BasicAuth
 */
public class BasicAuth extends AuthenticationBase {

    //=======================================================
    //======================KONSTANTEN=======================
    //=======================================================

    private static final String EXCEPTION_REPORTER = "libfirenet.authentication.BasicAuth: ";

    //=======================================================
    //======================VARIABLEN========================
    //=======================================================

    private String user;
    private String password;
    private Parameter auth;

    // Feld für zusätzliche Daten
    protected Object additionalData;

    //=======================================================
    //=====================KONSTRUKTOR=======================
    //=======================================================

    /**
     * Eine neue Instanz der BasicAuth erstellen
     * @param username Nutzername der verwendet werden soll
     * @param password Password das verwendet werden soll
     */
    public BasicAuth(String username, String password) {
        user = username;
        this.password = password;

    }

    //=======================================================
    //===================PUBLIC METHODEN=====================
    //=======================================================

    @Override
    protected Parameter nextAuth(@Nullable HttpURLConnection connection) throws SecurityException, UnsupportedEncodingException {

        // Basierend auf https://robert-reiz.com/2014/10/05/java-http-request-with-basic-auth/
        String user_pass = user + ":" + password;
        String encoded = Base64.encodeToString(user_pass.getBytes(), android.util.Base64.DEFAULT);

        // Um bei öfteren Aufrufen Rechenleistung zu sparen, wird der Parameter gespeichert und später nur noch ausgegeben
        auth = new Parameter("Authorization","Basic " + encoded, true);

        return auth;
    }


    public Object getAdditionalData() {
        return additionalData;
    }

    public void setAdditionalData(Object additionalData) {
        this.additionalData = additionalData;
    }

    //=======================================================
    //==================PRIVATE METHODEN=====================
    //=======================================================

}
