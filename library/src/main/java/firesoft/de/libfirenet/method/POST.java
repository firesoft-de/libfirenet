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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;

import firesoft.de.libfirenet.http.Parameter;

/**
 * Klasse die Methoden für die Bearbeitung der POST Methode bereit stellt
 */
public class POST extends RequestMethod {

    //=======================================================
    //=====================VARIABLEN=========================
    //=======================================================

    //=======================================================
    //=====================KONSTANTEN========================
    //=======================================================

    //=======================================================
    //====================KONSTRUKTOR========================
    //=======================================================

    public POST() {
        this.name = "POST";
    }

    //=======================================================
    //==================PUBLIC METHODEN======================
    //=======================================================

    @Override
    public void insertParameters(ArrayList<Parameter> parameters) throws IOException {

        connection.setDoInput(true);
        connection.setDoOutput(true);

        // Benötigte Objekte erstellen
        BufferedWriter writer = null;
        OutputStream stream = connection.getOutputStream();

        writer = new BufferedWriter(new OutputStreamWriter(stream, "UTF-8"));

        StringBuilder query = new StringBuilder();
        boolean first = true;

        // Query zusammenbauen
        for (Parameter parameter : parameters) {

            AbstractMap.SimpleEntry simpleParameter = parameter.toSimpleEntry();

            if (first) {
                first = false;
            } else {
                query.append("&");
            }

            try {
                query.append(URLEncoder.encode(simpleParameter.getKey().toString(), "UTF-8"));
                query.append("=");
                query.append(URLEncoder.encode(simpleParameter.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // Post Query in den OutputStream schreiben
        writer.write(query.toString());

        // Schreibvorgang beenden
        writer.flush();
        writer.close();
        stream.close();

        // Post erzwingen
        connection.setDoInput(true);
        connection.setDoOutput(true);

    }

}
