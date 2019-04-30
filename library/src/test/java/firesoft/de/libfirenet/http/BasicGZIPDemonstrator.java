package firesoft.de.libfirenet.http;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.content.Context;
import android.test.mock.MockContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import firesoft.de.libfirenet.method.GET;

import static firesoft.de.libfirenet.Definitions.CONNECTION_TIMEOUT;
import static org.junit.Assert.assertTrue;

public class BasicGZIPDemonstrator {

    private final String httpbin = "https://httpbin.org/get?test=test";
    private HttpURLConnection connection;
    private Context context;

    StringBuilder builder = new StringBuilder();
    InputStreamReader reader = null;
    BufferedReader bReader;
    GZIPInputStream gstream = null;


    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    @Before
    public void setUp() throws Exception {
        context = new MockContext();
    }

    @Test
    public void workerShouldUseGZIP() {

        ArrayList<Parameter> parameters = new ArrayList<>();
        parameters.add((new Parameter("test","test",true)));

        HttpWorker worker = null;
        try {
            worker = new HttpWorker(httpbin, GET.class,context,null,parameters,null);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        try {
            worker.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = worker.toString();

        // Ergebnis als String ausgeben
        String txtResponse = builder.toString();
        System.out.println(txtResponse);

        boolean respContExpected = txtResponse.contains("\"test\":");

        assertTrue(respContExpected);
    }

    @Test
    public void gzipreference() throws IOException {

        // URL erzeugen
        URL _url = new URL(httpbin);

        // Verbindungsobjekt initalisieren
        connection = (HttpsURLConnection) _url.openConnection();

        if (connection == null) {
            throw new IOException("Connection is null!");
        }

        // Verbindungseinstellungen, Authentifzierung etc. festlegen

        connection.setConnectTimeout(CONNECTION_TIMEOUT);
        connection.setReadTimeout(CONNECTION_TIMEOUT);
        connection.setRequestProperty("Accept-Encoding","gzip");

        System.out.println();
        System.out.println("========== REQUEST ==========");
        System.out.println();

        for (Map.Entry<String, List<String>> entries : connection.getRequestProperties().entrySet()) {
            String values = "";
            for (String value : entries.getValue()) {
                values += value + ",";
            }
            System.out.println(entries.getKey() + " - " +  values );
        }
        System.out.println();

        // Verbindung herstellen
        connection.connect();

        // Antwortcode prüfen
        int response = connection.getResponseCode(); // Trennung muss hier erfolgen, da es ansonsten zu einem Fehler im Debugger kommt ("Cannot set request property after connection is made")

        switch (response) {
            case HttpURLConnection.HTTP_OK:
                // 200 -> Alles i.O.

                System.out.println();
                System.out.println("========== RESPONSE ==========");
                System.out.println();

                for (Map.Entry<String, List<String>> entries : connection.getHeaderFields().entrySet()) {
                    String values = "";
                    for (String value : entries.getValue()) {
                        values += value + ",";
                    }
                    System.out.println(entries.getKey() + " - " +  values);
                }
                System.out.println();
                break;


            default:
                // Alle anderen Meldungen
                return;
        }

        InputStream stream = connection.getInputStream();

        if (stream != null )
        {
            //reader erstellen und diesen buffern. Ggf. den Stream vorher durch einen Gzip Stream schicken
            gstream = new GZIPInputStream(stream);
            reader = new InputStreamReader(gstream);// , Charset.forName("UTF-8"));
        }
        else {
            return;
        }

        if (reader == null) {
            throw new NullPointerException("Nullpointer!");
        }

        bReader = new BufferedReader(reader);

//            byte[] bytes = {10};
//
//            try {
//                bytes = getBytesFromInputStream(gstream);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            int a = bytes.length +1;
//
//            ArrayList<String> sList = new ArrayList<>();
//
//            for (Byte b: bytes
//            ) {
//                sList.add(Integer.toHexString(b));
//            }

        String line = null;
        line = bReader.readLine();

        while (line != null) {
            builder.append(line).append("\n");
            line = bReader.readLine();
        }


        // Ergebnis als String ausgeben
        String txtResponse = builder.toString();
        System.out.println(txtResponse);

        boolean respContExpected = txtResponse.contains("\"test\":");

        assertTrue(respContExpected);

    }



    @After
    public void tearDown() throws Exception {

        try {
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Streams und Streamreader schließen
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            if (gstream != null) { gstream.close(); }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            bReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}