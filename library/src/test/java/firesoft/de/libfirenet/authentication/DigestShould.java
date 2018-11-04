package firesoft.de.libfirenet.authentication;

import org.junit.Before;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import static org.junit.Assert.*;

public class DigestShould {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void BeValid() throws MalformedURLException, UnsupportedEncodingException {

        // Aus Wikipedia
        String username = "Mufasa";
        String password = "Circle Of Life";
        String realm = "testrealm@host.com";
        String nonce = "dcd98b7102dd2f0e8b11d0f600bfb0c093";
        String cnonce = "0a4f113b";
        Digest.EncryptionMethod algorithm = Digest.EncryptionMethod.MD5;
        String qop = "auth";
        String opaque = "5ccc069c403ebaf9f0171e9517f40e41";
        String url = "https://test.org/dir/index.html";
        int nc = 1;

        Digest digest = new Digest(username, password);
        String result = digest.testInterface(url,"GET",username,password,realm,nonce,cnonce,algorithm,qop,opaque,nc);

        String expectedResponse = "6629fae49393a05397450978507c4ef1";

        String[] components = result.split(", ");
        for (String element: components
             ) {
            if (element.contains("response")) {
                assertEquals(element.split("\"")[1],expectedResponse);
                return;
            }
        }

        fail();
    }
}