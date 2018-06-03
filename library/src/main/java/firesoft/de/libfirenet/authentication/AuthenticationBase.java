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

import android.content.res.Resources;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

import firesoft.de.libfirenet.R;
import firesoft.de.libfirenet.http.Parameter;

/**
 * Eine abstrakte Klasse die eine gemeinsame Basis für sicherheitsbezogene Objekte bereitstellt
 */
public abstract class AuthenticationBase {

    //=======================================================
    //======================KONSTANTEN=======================
    //=======================================================

    /**
     * Maximale Anzahl von Verbindungsversuchen die unternommen werden sollen
     */
    private static final short MAX_CONNECTION_ATTEMPTS = 10;

    /**
     * Enthält den Fehlerpfad
     */
    private static final String EXCEPTION_PATH = "libfirenet.authentication";

    //=======================================================
    //======================VARIABLEN========================
    //=======================================================

    /**
     * Enthält die aktuelle Anzahl von Verbindungsversuchen
     */
    private short connection_counter;

    //=======================================================
    //===================PUBLIC METHODEN=====================
    //=======================================================

    /**
     * Erstellt einen Parameter der Authentifzierungsdaten für eine HTTP Anfrage enthält
     * @return Gibt ein Objekt der Klasse Parameter aus. Dieses ist mit aktivem RequestProperty Flag markiert! Der Key lautet dabei "Authorization"
     */
    //public abstract Parameter generate();

    /**
     * Erstellt einen neuen Authentifzierungsparameter, falls das Authentifzierungsverfahren keine konstanten Authentifzierungsparameter bei mehrmaligen Anfragen verwendet (bspw. Digest).
     * @param connection Hier kann ggf. die HttpURLConnection übergeben werden. Der Child-Klasse wird damit ermöglicht eventuelle Antworten des Servers abzuspeichern und in einem weiteren Versuch zu speichern
     * @return Gibt ein Objekt der Klasse Parameter aus. Dieses ist mit aktivem RequestProperty Flag markiert! Der Key lautet dabei "Authorization"
     * @throws IOException Wird bspw. geworfen wenn wiederholt 401 Antworten vom Server gegebenen werden, da dann davon auszugehen ist, dass die Zugangsdaten fehlerhaft sind
     */
    public Parameter generate(@Nullable HttpURLConnection connection) throws IOException, SecurityException {

        return nextAuth(connection);

    }

    //=======================================================
    //=================PROTECTED METHODEN====================
    //=======================================================

    /**
     * Reicht den Methodenaufruf generate() dieser Klasse an ihre Kinder weiter
     */
    protected abstract Parameter nextAuth(@Nullable HttpURLConnection connection) throws SecurityException, UnsupportedEncodingException;


    protected String generateExceptionMessage(Class reporter, int message_id) {
        return EXCEPTION_PATH + reporter.getSimpleName() + ": " + Resources.getSystem().getString(message_id);
    }

    //=======================================================
    //==================PRIVATE METHODEN=====================
    //=======================================================



}
