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

package firesoft.de.libfirenet.method;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.ArrayList;

import firesoft.de.libfirenet.http.Parameter;

/**
 * Abstrakte Klasse welche grundlegende Anforderungen für Abfragemethoden definiert.
 */
public abstract class RequestMethod {

    //=======================================================
    //=====================VARIABLEN=========================
    //=======================================================

    /**
     * Enthält den Namen der Abfragemethode. ACHTUNG: Wird 1:1 verwendet um die Abfragemethod der
     */
    protected String name;
    protected HttpURLConnection connection;

    //=======================================================
    //=====================KONSTANTEN========================
    //=======================================================

    //=======================================================
    //====================KONSTRUKTOR========================
    //=======================================================

    //=======================================================
    //==================PUBLIC METHODEN======================
    //=======================================================

    /**
     * Fügt einen Satz von Parametern in die HTTPUrlConnection ein
     * @exception ProtocolException wird geworfen, falls der eingegebene Protkolltyp nicht erkannt wurde
     */
    public void insert(ArrayList<Parameter> parameters) throws IOException {

        // Allgemeiner Code der immer ausgeführt werden muss
        connection.setRequestMethod(name);

        if (parameters != null && parameters.size() > 0) {

            // Jetzt noch den Code der Child-Klasse ausführen
            insertParameters(parameters);

        }
    };

    /**
     * Dient als Aufrufmethode. Über diese können die Child-Klasen den für ihre Methode spezifischen Code ausführen.
     */
    protected abstract void insertParameters(ArrayList<Parameter> parameters) throws IOException;

    /**
     * Gibt den Namen der Anfragemethode aus
     * @return
     */
    public String getName() {
        return name;
    }

    public HttpURLConnection getConnection() {
        return connection;
    }


    public void setConnection(HttpURLConnection connection) {
        this.connection = connection;
    }

}
