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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import firesoft.de.libfirenet.R;
import firesoft.de.libfirenet.authentication.AuthenticationBase;
import firesoft.de.libfirenet.interfaces.IWorkerCallback;
import firesoft.de.libfirenet.method.RequestMethod;
import firesoft.de.libfirenet.util.HttpState;

import static firesoft.de.libfirenet.Definitions.CONNECTION_TIMEOUT;
import static firesoft.de.libfirenet.Definitions.MAX_CONNECTION_ATTEMPT;

/**
 * Diese Klasse stellt Methoden zur Verfügung mit deren Hilfe HTTP(S) Anfragen durchgeführt werden können
 */
public class HttpWorker {

    //=======================================================
    //==================INTERNE VARIABLEN====================
    //=======================================================

    /**
     * Enthält das Callback Interface
     */
    private IWorkerCallback callback;

    /**
     * Enthält den Authenticator der zum Authentifzieren benutzt wird
     */
    private AuthenticationBase authenticator;

    /**
     * Enthält Parameter welche an den Server übergeben werden sollen
     */
    private ArrayList<Parameter> parameters;

    /**
     * Enthält den Context in dem die Klasse ausgeführt wird
     */
    private Context context;

    /**
     * Enthält die Url
     */
    private String url;

    /**
     * Enthält einen Marker der angibt, ob HTTP erzwungen werden soll
     */
    private Boolean forceHTTP;

    /**
     * Enthält die Anfragemethode
     */
    private RequestMethod requestMethod;

    /**
     * Enthält den HTTP-Antwortcode vom Server
     */
    private MutableLiveData<Integer> responseCode;

    /**
     * Enthält die letzte Verbindung die mit Code 200 durchgelaufen ist.
     */
    private HttpURLConnection conn;

    /**
     * Enthält vorangegangene Verbindung. Diese kann mit allen Statuscodes abgeschlossen worden sein.
     */
    private HttpURLConnection prevCon;

    /**
     * Enthält den Responsestream des Servers.
     */
    private InputStream stream;

    /**
     * Enthält die Serverresponse als String.
     */
    private String response;

    /**
     * Gibt an, ob (wo möglich) Antworten komprimiert werden sollen
     */
    private boolean useGzipCompression;


    /**
     * Gibt den aktuellen Zustand in dem sich der Request befindet wieder
     */
    private MutableLiveData<HttpState> state;

    /**
     * Marker der aktviert wird falls die Antwort gzip codiert ist.
     */
    private MutableLiveData<Boolean> isResponseGzipEncoded;


    //=======================================================
    //=====================KONSTANTEN========================
    //=======================================================

    private static final String EXCEPTION_PATH = "libfirenet.http";

    //=======================================================
    //=====================KONSTRUKTOR=======================
    //=======================================================

    /**
     * Erstellt eine neue Instanz der Klasse
     * @param requestMethod Klasse welche die Anfragemethode festlegt
     * @param context Enthält den Context in dem die Klasse ausgeführt wird
     * @param parameters Enthält Parameter welche an den Server übergeben werden sollen
     * @param url Enthält die Url des Servers
     * @param authenticator Enthält den Authenticator der zum Authentifzieren benutzt wird
     * @param forceHttp Erzwingt die Verwendung von HTTP anstatt HTTPS
     * @param callback Interface über welches Callbacks an die Elternklasse durchgeführt werden können
     * @param useGzipCompression Gibt an, ob der Server aufgefordert werden soll die Antwort zu komprimieren (Default = true)
     */
    public HttpWorker(String url, Class requestMethod, Context context, @Nullable AuthenticationBase authenticator, @Nullable ArrayList<Parameter> parameters, boolean forceHttp, boolean useGzipCompression, @Nullable IWorkerCallback callback) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        // Status initalisieren und den Anfangszustand einstellen
        state = new MutableLiveData<>();
        state.postValue(HttpState.NOT_RUNNING);

        isResponseGzipEncoded = new MutableLiveData<>();
        isResponseGzipEncoded.setValue(false);

        responseCode = new MutableLiveData<>();

        // Eingaben abspeichern
        this.parameters = parameters;
        this.authenticator = authenticator;
        this.context = context;
        this.url = url;
        this.forceHTTP = forceHttp;
        this.useGzipCompression = useGzipCompression;

        // Überprüfen, ob der eingegebene Kandidat für die RequestMethod mit der benötigten Signatur übereinstimmt
        if (!RequestMethod.class.isAssignableFrom(requestMethod)) {
            // Die eingegebene Klasse passt nicht zur benötigten Klassensignatur

            throw new IllegalAccessException(generateExceptionMessage(this.getClass(),R.string.exception_invalid_class_signature_request_method));
        }

        // Anhand des gegebenen Kandidaten eine neue Instanz erstellen
        this.requestMethod = (RequestMethod) requestMethod.getConstructor().newInstance();

        // Status updaten
        state.postValue(HttpState.INITIALIZING);

    }

    //=======================================================
    //===================PUBLIC METHODEN=====================
    //=======================================================

    /**
     * Führt die Http-Anfrage mit den vorher eingegebene Parametern aus
     * @throws IOException falls der Rückgabestream null ist, wird eine IOException generiert
     */
    public void execute() throws IOException{

        state.postValue(HttpState.RUNNING);

        // Prüfen, ob ein Authenticator verwendet werden muss und dann die Anfrage starten
        if (authenticator != null) {
            stream = request(true,(short) 1);
        }
        else {
            stream = request(false,(short) 1);
        }

        // Enthält der Stream Daten?
        if (stream == null) {
            throw new IOException(generateExceptionMessage(this.getClass(),R.string.exception_stream_null));
        }

        response = toString();

        state.postValue(HttpState.COMPLETED);

        if (callback != null) {
            callback.downloadCompleted();
        }

    }


    /**
     * Prüft ob eine Verbindung zum Internet besteht
     * @param context Context in dem die Anwendung läuft
     * @return true falls Verbindung vorhanden, false falls nicht
     */
    public boolean checkNetwork(Context context) {
        // Instanz des VerbindungsManagers abrufen
        ConnectivityManager connMgr;
        connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Abfragen, ob ein Netzwerk vorhanden ist
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // Prüfen ob das Netzwerk mit dem Internet verbunden ist
            return networkInfo != null && networkInfo.isConnected();
        }
        else {
            return false;
        }
    }

    /**
     * Konvertiert den Inhalt des AntwortSTREAM (!) des Servers in einen String. Unter Umständen kann der Stream zum Abfragezeitpunkt vom GC geleert worden sein. Falls das Ergebnis dieser Methode als null oder leer ist, bitte mit getResponse() arbeiten.
     * ACHTUNG: Nach auslesen des Stream unbedingt .disconnect() verwenden, um die Verbindung sauber zu trennen.
     * @return Null, falls es beim Konvertieren zu Fehlern kommt
     * @throws NullPointerException wird geworfen falls der verwendete InputStreamReader nicht initalisiert wurde.
     */
    @Override
    public String toString() throws NullPointerException {

        StringBuilder builder = new StringBuilder();

        if (stream != null ){

            //reader erstellen und diesen buffern. Ggf. den Stream vorher durch einen Gzip Stream schicken
            InputStreamReader reader = null;
            GZIPInputStream gstream = null;

            if (isResponseGzipEncoded.getValue()) {

                try {
                    gstream = new GZIPInputStream(stream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    reader = new InputStreamReader(gstream, Charset.forName("UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                reader = new InputStreamReader(stream, Charset.forName("UTF-8"));
            }

            if (reader == null) {
                throw new NullPointerException(generateExceptionMessage(this.getClass(),R.string.exception_reader_null));
            }

            BufferedReader bReader = new BufferedReader(reader);

            byte[] bytes = {10};

            try {
                bytes = getBytesFromInputStream(gstream);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int a = bytes.length +1;

            ArrayList<String> sList = new ArrayList<>();

            for (Byte b: bytes
            ) {
                sList.add(Integer.toHexString(b));
            }

            String line = null;
            try {
                line = bReader.readLine();

                while (line != null) {
                    builder.append(line).append("\n");
                    line = bReader.readLine();
                }

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }
        else {
            throw new NullPointerException(generateExceptionMessage(this.getClass(),R.string.exception_stream_null));
        }

        return builder.toString();
    }



    public static byte[] getBytesFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[0xFFFF];
        for (int len = is.read(buffer); len != -1; len = is.read(buffer)) {
            os.write(buffer, 0, len);
        }
        return os.toByteArray();
    }



    /**
     * Trennt die Verbindung vom Server
     */
    public void disconnect() {
        if (conn != null) {
            conn.disconnect();
        }

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //=======================================================
    //==================PRIVATE METHODEN=====================
    //=======================================================

    /**
     * Nimmt verschiedene Einstellungen an der HttpURLConnection vor
     * @param con
     * @param useAuthenticator
     * @return
     * @throws IOException
     */
    private HttpURLConnection setupConnection(HttpURLConnection con, boolean useAuthenticator) throws IOException {

        // Anfragemethode festlegen und die Parameter einpflegen
        requestMethod.setConnection(con);

        Parameter authentication = null;

        if (useAuthenticator) {
            // Hier muss die vorangegangene Verbindung genutzt werden. Digest baut bspw. seine Challenge aus Daten der Connection auf. Das Abrufen der Daten setzt allerdings HttpUrlConnection.connected = true.
            // Damit ist eine weitere Modifzierung (also bspw. das Anhängen des Auth-Strings) nicht mehr möglich. Das Programm beendet sich dann mit einer Fehlermeldung.
            if (prevCon != null) {
                authentication = authenticator.generate(prevCon);
            }
        }

        requestMethod.insert(parameters);

        con = requestMethod.getConnection();

        // Falls vorhanden Authentifzierung anhängen
        if (authentication != null && !authentication.getKey().equals("") && !authentication.getValue().equals("")) {
            con.setRequestProperty(authentication.getKey(),  authentication.getValueAsString());
        }

        // Falls gewünscht Kompressionsanforderung an den Server schicken
        if (useGzipCompression) {
            con.setRequestProperty("Accept-Encoding","gzip");
        }

        // Verbindungsparameter einrichten
        con.setConnectTimeout(CONNECTION_TIMEOUT);
        con.setReadTimeout(CONNECTION_TIMEOUT);

        return con;
    }

    /**
     * Führt den eigentlichen Request aus
     * @param useAuthenticator gibt an ob der Authenticator verwendet werden soll
     */
    private InputStream request(boolean useAuthenticator, short attempt) throws IOException {

        HttpURLConnection connection;

        // Prüfen, ob bereits die maximal erlaubte Anzahl von Verbindungsversuchen durchgeführt wurde.
        if (attempt >= MAX_CONNECTION_ATTEMPT) {
            throw new IOException(generateExceptionMessage(this.getClass(),R.string.exception_attempt_limit));
        }
        else if (attempt <= 1) {

            if (attempt < 1) {
                attempt = 1;
            }

            // Internetverbindung prüfen und ggf. einen Fehler werfen. Prüfen der Verbindung beim ersten Versuch sollte ausreichen
            if (!checkNetwork(context)) {
                throw new IOException(generateExceptionMessage(this.getClass(),R.string.exception_network_missing));
            }
        }

        // URL erzeugen
        URL _url = generateURL(url, forceHTTP);

        // Verbindungsobjekt initalisieren
        if (forceHTTP) {
            connection = (HttpURLConnection) _url.openConnection();
        }
        else {
            connection = (HttpsURLConnection) _url.openConnection();
        }

        if (connection == null) {
            throw new IOException(generateExceptionMessage(this.getClass(),R.string.exception_creating_connection_failed));
        }

        // Verbindungseinstellungen, Authentifzierung etc. festlegen
        // Für Digest werden Rückgabewerte benötigt. Für Digest eine spezielle Programmierung anzulegen bei der ein Fehlversuch durchgeführt wird und für alle anderen Methoden direkt beim ersten Mal die Authentifizierung mitgeschickt wird, würde die Allgemeingültigkeit des Codes kaputt machen.
        // Es ist einfacher für jede Methode einen Fehlerversuch zuzulassen. Deswegen wird beim ersten Attempt immer ein false an die Setup Methode gesendet
        if (attempt == 1) {
            connection = setupConnection(connection, false);
        }
        else {
            connection = setupConnection(connection, useAuthenticator);
        }

        // Zur Sicherheit vorhandene Verbindungen trennen. Dann kann sauber neu begonnen werden.
        if (conn != null) {
            conn.disconnect();
        }

        if (prevCon != null) {
            prevCon.disconnect();
        }

        // Verbindung herstellen
        connection.connect();

        // Antwortcode prüfen
        int response = connection.getResponseCode(); // Trennung muss hier erfolgen, da es ansonsten zu einem Fehler im Debugger kommt ("Cannot set request property after connection is made")
        responseCode.postValue(response);

        // Aktuelle Verbindung abspeichern.
        prevCon = connection;

        switch (response) {
            case HttpURLConnection.HTTP_OK:
                // 200 -> Alles i.O.
                conn = connection; // Erfolgreiche Verbindung abspeichern

                if ("gzip".equals(connection.getContentEncoding())) {
                    isResponseGzipEncoded.postValue(true);
                }
                else {
                    isResponseGzipEncoded.postValue(false);
                }

                return connection.getInputStream();

            case HttpURLConnection.HTTP_UNAUTHORIZED:
                // 401 -> Authentifzierung erforderlich
                if (useAuthenticator) {
                    return request(true, (short)(attempt + 1));
                }
                else {
                    throw new IOException(generateExceptionMessage(this.getClass(),R.string.exception_auth_needed));
                }

            default:
                // Alle anderen Meldungen
                return null;
        }
    }

    /**
     * Erstellt aus einem String ein URL Objekt. Dabei wird ggf. das Protkoll angepasst
     * @param url Zu prüfende URL
     * @param targetHTTP True wenn als Protokoll HTTP verwendet werden soll. False falls HTTPS verwendet werden soll
     * @return bearbeitete URL
     */
    private URL generateURL(String url, boolean targetHTTP) throws MalformedURLException {
        URL _url;

        // Infrastruktur für Regex erstellen
        Pattern word = Pattern.compile("(http)[s]?[:]?(//)");

        // Prüfe, ob http oder https vorangestellt wurde
        if (!word.matcher(url).find()) {
            url = "https://" + url;
        }

        // Protkollprüfung
        if (url.contains("https://")) {
            // Die Eingabe verwendet HTTPS
            // Prüfen ob das auch so gewünscht ist
            if (targetHTTP) {
                // Zielprotokoll: HTTP
                // HTTPS gegen HTTP austauschen
                url = url.replace("https://", "http://");
            }
        } else if (url.contains("http://")) {
            // Die Eingabe verwendet HTTP
            // Prüfen ob das auch so gewünscht ist
            if (!targetHTTP) {
                // Zielprotokoll: HTTPS
                // HTTP gegen HTTPS austauschen
                url = url.replace("http://", "https://");
            }
        } else {
            // Es wurden kein passendes Protkoll gefunden
            throw new MalformedURLException("Kein korrektes Protkoll gefunden!");
        }

        // Prüfen, ob ein abschließendes '/' vorhanden ist
        if (url.substring(url.length() - 2, url.length() - 1).equals("/")) {
            url = url + "/";
        }

        // URL erzeugen
        _url = new URL(url);
        return _url;

    }

    private String generateExceptionMessage(Class reporter, int message_id) {
        state.postValue(HttpState.FAILED);
        String test = EXCEPTION_PATH + reporter.getSimpleName() + ": " + context.getString(message_id);
        return test;
    }


    //=======================================================
    //===================GETTER SETTER=======================
    //=======================================================

    /**
     * Gibt den aktuellen Responsecode des Servers wieder
     */
    public MutableLiveData<Integer> getResponseCode() {
        return responseCode;
    }

    /**
     * Gibt die aktuelle URL aus
     */
    public String getUrl() {
        return url;
    }

    // region Hilfsmethoden

    /**
     * Setzt die URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public MutableLiveData<HttpState> getState() {
        return state;
    }

    public MutableLiveData<Boolean> isResponseGzipEncoded() {
        return isResponseGzipEncoded;
    }

    /**
     * Löscht nicht mehr benötigte Variablen
     */
    public void cleanUp() {
        conn = null;
        prevCon = null;
    }

    /**
     * Gibt die Serverantwort als Stream aus. ACHTUNG: Nach auslesen des Stream unbedingt .disconnect() verwenden, um die Verbindung sauber zu trennen.
     * @param autoDisconnect Falls, true wird die Verbindung automatisch getrennt.
     */
    public InputStream getStream(boolean autoDisconnect) {
        if (autoDisconnect) {
            disconnect();
        }

        return stream;
    }

    /**
     * Gibt den Inhalt des Streams unmittelbar nach Ausführung der Abfrage aus. ACHTUNG: Nach auslesen des Stream unbedingt .disconnect() verwenden, um die Verbindung sauber zu trennen.
     * @param autoDisconnect Falls, true wird die Verbindung automatisch getrennt.
     */
    public String getResponse(boolean autoDisconnect) {
        if (autoDisconnect) {
            disconnect();
        }

        return response;
    }

    /**
     * Wandelt den Antwortstream des Servers in eine Bitmap um
     * @param autoDisconnect Falls, true wird die Verbindung nach dem fehlerfreien Umwandeln des Streams automatisch getrennt.
     * @return
     * @throws Exception Ausnahmen können während der Konvertierung des Stream in eine Bitmap auftreten. Sie werden dann automatisch generiert. Falls das Ergebnis der Konvertierung null ist, wird eine Ausnahme mit entsprechendem Fehlertext generiert.
     */
    public Bitmap extractBitmapFromResponse(boolean autoDisconnect) throws Exception {
        Bitmap image = null;

        try {
            image = BitmapFactory.decodeStream(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (autoDisconnect) {
            disconnect();
        }

        if (image == null) {
            throw new Exception("Could not convert response stream to bitmap!");
        }
        else {
            return image;
        }
    }

    // endregion
}
