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

package firesoft.de.libfirenet.http;

import java.util.AbstractMap;

/**
 * Stellt eine Datentyp bereit mit dem Parameter die per POST/GET etc. an den Server geschickt werden können oder als RequestProperty an den
 */
public class Parameter implements AbstractMap.Entry {

    //=======================================================
    //==================INTERNE VARIABLEN====================
    //=======================================================

    private String key;
    private Object value;
    private Boolean request; // Flag der angibt, ob der Parameter für die Verwendung als RequestProperty vorgesehen ist


    //=======================================================
    //=====================KONSTRUKTOR=======================
    //=======================================================

    /**
     * Erstellt ein neues Objekt auf Basis der Klasse
     *
     * @param key   Inhalt des Keys
     * @param value Inhalt des Values
     */
    public Parameter(String key, Object value) {
        this.key = key;
        this.value = value;
        this.request = false;
    }

    /**
     * Erstellt ein neues Objekt auf Basis der Klasse
     *
     * @param key             Name des Keys
     * @param value           Der eigentliche Inhalt der an den Server geschickt werden soll
     * @param requestProperty Flag die angibt, ob der Parameter ein RequestProperty ist. Durch setzten des Flags, wird dem HttpWorker bspw. signalisiert den Paramter nicht per POST/GET etc. anzuhängen, sondern Anfrageeingenschaft (z.B. Authorization) anzuhängen
     */
    public Parameter(String key, Object value, Boolean requestProperty) {
        this.key = key;
        this.value = value;
        this.request = requestProperty;
    }

    //=======================================================
    //====================HILFSMETHODEN======================
    //=======================================================

    /**
     * Erstellt den Parameter in ein Objekt der Klasse AbstractMap.SimpleEntry
     */
    public AbstractMap.SimpleEntry toSimpleEntry() {
        return new AbstractMap.SimpleEntry<>(key, value.toString());
    }

    //=======================================================
    //==================GETTER UND SETTER====================
    //=======================================================

    /**
     * Gibt den Namen / die Bezeichnung des Parameters aus
     */
    @Override
    public String getKey() {
        return key;
    }

    /**
     * Gibt den Inhalt des Parameters aus
     */
    @Override
    public Object getValue() {
        return value;
    }

    /**
     * Gibt den Inhalt des Parameters als String aus
     */
    public String getValueAsString() {
        return value.toString();
    }

    /**
     * Gibt den Inhalt des Parameters als Integer aus
     */
    public Integer getValueAsInteger() {
        return (Integer) value;
    }


    /**
     * Setzt den Inhalt des Parameters
     */
    @Override
    public Object setValue(Object input) {

        if (value instanceof String) {
            this.value = String.valueOf(input);
        } else if (value instanceof Integer) {
            this.value = input;
        }

        return null;
    }

    /**
     * Setzt den Namen / die Bezeichnung des Parameters
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Fragt die RequestProperty Flag ab
     */
    public Boolean isRequestProperty() {
        return request;
    }

    /**
     * Setzt die RequestProperty Flag
     */
    public void setRequestProperty(Boolean request) {
        this.request = request;
    }

}


