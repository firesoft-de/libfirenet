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

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import firesoft.de.libfirenet.authentication.AuthenticationBase;
import firesoft.de.libfirenet.method.RequestMethod;
import firesoft.de.libfirenet.util.HttpState;
import firesoft.de.libfirenet.util.ResultWrapper;

/**
 * Implementierung des HTTPWorker in einem AsyncTaskLoader
 */
public class HTTPLoader extends AsyncTaskLoader<ResultWrapper> {

    //=======================================================
    //=====================VARIABLEN=========================
    //=======================================================

    private String username;
    private String password;

    private HttpWorker worker;

    private String result;

    private MutableLiveData<HttpState> state;
    private MutableLiveData<Integer> responseCode;

    private boolean forceLoad;

    //=======================================================
    //=====================KONSTANTEN========================
    //=======================================================

    //=======================================================
    //====================KONSTRUKTOR========================
    //=======================================================

    /**
     * Erstellt eine neue Instanz der Klasse
     * @param requestMethod Klasse welche die Anfragemethode festlegt
     * @param context Enthält den Context in dem die Klasse ausgeführt wird
     * @param parameters Enthält Parameter welche an den Server übergeben werden sollen
     * @param url Enthält die Url des Servers
     * @param authenticator Enthält den Authenticator der zum Authentifzieren benutzt wird
     * @param forceHttp Erzwingt die Verwendung von HTTP anstatt HTTPS
     * @param forceLoad Soll forceLoad() in der Methode onStartLoading verwendet werden?
     */
    public HTTPLoader(String url, Class requestMethod, Context context, @Nullable AuthenticationBase authenticator, @Nullable ArrayList<Parameter> parameters, boolean forceHttp, boolean forceLoad) {
        super(context);

        try {
            worker = new HttpWorker(url,requestMethod,getContext(),authenticator,parameters,forceHttp, null);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }

        state = worker.getState();
        responseCode = worker.getResponseCode();
        this.forceLoad = forceLoad;

    }

    //=======================================================
    //==================PUBLIC METHODEN======================
    //=======================================================

    @Nullable
    @Override
    public ResultWrapper loadInBackground() {

        try {
            worker.excecute(); // Die Funktion gibt einen InputStream zurück. Aus diesem können alle Daten wie gewünscht ausgelesen werden.
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            // Die Daten als String abrufen
            result = worker.toString();

            // Daten wurden runtergeladen. Fairerweise wird jetzt die Verbindung zum Server getrennt und nicht darauf gewartet, dass der Timeout ausläuft.
            worker.disconnect();
            return new ResultWrapper(result);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return new ResultWrapper(e);
        }

    }

    @Override
    protected void onStartLoading() {
        if (forceLoad) {
            forceLoad();
        }
    }

    //=======================================================
    //=================PRIVATE METHODEN======================
    //=======================================================

    //=======================================================
    //===================GETTER SETTER=======================
    //=======================================================

    /**
     * Gibt die Serverantwort als String aus
     */
    public String getResult() {
        return result;
    }

    /**
     * Stellt ein LiveData Objekt bereit mit welchem der Zustand des HttpWorkers überwacht werden kann
     */
    public MutableLiveData<HttpState> getState() {
        return state;
    }

    public MutableLiveData<Integer> getResponseCode() {
        return responseCode;
    }
}
