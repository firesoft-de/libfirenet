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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import firesoft.de.libfirenet.http.Parameter;

/**
 * Klasse die Methoden für die Bearbeitung der GET Methode bereit stellt
 */
public class GET extends RequestMethod {

    //=======================================================
    //=====================VARIABLEN=========================
    //=======================================================

    //=======================================================
    //=====================KONSTANTEN========================
    //=======================================================

    //=======================================================
    //====================KONSTRUKTOR========================
    //=======================================================

    public GET() {
        this.name = "GET";
    }

    //=======================================================
    //==================PUBLIC METHODEN======================
    //=======================================================

    @Override
    protected void insertParameters(ArrayList<Parameter> parameters) throws IOException {

        // TODO -> Hier muss die URL angepasst werden

    }
}
