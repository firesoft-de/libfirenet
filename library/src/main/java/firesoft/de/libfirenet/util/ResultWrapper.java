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

package firesoft.de.libfirenet.util;

import java.util.ArrayList;

/**
 * Wrapperklasse um Ergebnisse und Fehlermeldungen aus AsyncTaskLoadern zurückzugeben
 */
public class ResultWrapper {

    //=======================================================
    //=====================VARIABLEN=========================
    //=======================================================

    private Object result;
    private Exception exception;

    //=======================================================
    //====================KONSTRUKTOR========================
    //=======================================================

    /**
     * Erstellt eine neue Instanz welche ein Ergebnis enthält
     */
    public ResultWrapper(Object result) {
        this.result = result;
        this.exception = null;
    }

    /**
     * Erstellt eine neue Instanz welche eine Fehlermeldung enthält
     */
    public ResultWrapper(Exception exception) {
        this.result = null;
        this.exception = exception;
    }

    //=======================================================
    //===================GETTER SETTER=======================
    //=======================================================


    public Object getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }


}
