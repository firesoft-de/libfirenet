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

package firesoft.de.firenet_test_app;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import firesoft.de.libfirenet.authentication.BasicAuth;
import firesoft.de.libfirenet.authentication.Digest;
import firesoft.de.libfirenet.http.HttpLoader;
import firesoft.de.libfirenet.method.GET;
import firesoft.de.libfirenet.util.HttpState;
import firesoft.de.libfirenet.util.ResultWrapper;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ResultWrapper> {

    //=======================================================
    //=====================VARIABLEN=========================
    //=======================================================

    private LoaderManager loaderManager;

    private ResultWrapper result;

    //=======================================================
    //=====================KONSTANTEN========================
    //=======================================================

    // Testkonstanten für Anfrage ohne Auth
    private final String google = "httpbin.org/";

    // Testkonstanten für Basic-Auth
    private final String basic_auth_url = "httpbin.org/basic-auth/user/passwd";
    private final String basic_auth_user = "user";
    private final String basic_auth_passwd = "passwd";

    // Testkonstanten für Digest-Auth
    private final String digest_auth_url = "httpbin.org/digest-auth/auth-int/user/passwd/MD5";
    private final String digest_auth_user = "user";
    private final String digest_auth_passwd = "passwd";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findViewById(R.id.bt_fire_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchTest();
            }
        });

        loaderManager = getSupportLoaderManager();

    }

    private void launchTest() {

        if (loaderManager.getLoader(0) == null) {
            loaderManager.initLoader(0, null, this);
        } else {
            loaderManager.restartLoader(0, null, this);
        }

    }

    @NonNull
    @Override
    public Loader<ResultWrapper> onCreateLoader(int id, @Nullable Bundle args) {

        RadioGroup radioGroup = this.findViewById(R.id.radioGroup);

        HttpLoader loader;

        boolean http = !((CheckBox) this.findViewById(R.id.checkBox)).isChecked();

        switch (radioGroup.getCheckedRadioButtonId()) {

            case R.id.rB_google:
                loader = google(http);
                break;

            case R.id.rB_basic:
                loader = basic(http);
                break;

            case R.id.rB_digest:
                loader = digest(http);
                break;

            default:
                throw new IllegalArgumentException();
        }

        assert loader != null;

        loader.getState().observe(this, new Observer<HttpState>() {
            @Override
            public void onChanged(@Nullable HttpState httpState) {
                switchColor(httpState);
            }
        });

        loader.getResponseCode().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {
                adjustResonseCode(integer);
            }
        });

        return loader;

    }

    private HttpLoader google(boolean forceHttp) {

        return new HttpLoader(google,GET.class,getApplicationContext(),null,null,forceHttp, true);
    }

    private HttpLoader basic(boolean forceHttp) {

        BasicAuth authenticator = new BasicAuth(basic_auth_user,basic_auth_passwd);
        return new HttpLoader(basic_auth_url,GET.class,getApplicationContext(),authenticator,null,forceHttp, true);

    }

    private HttpLoader digest(boolean forceHttp) {

        Digest authenticator = new Digest(digest_auth_user,digest_auth_passwd);
        return new HttpLoader(digest_auth_url,GET.class,getApplicationContext(),authenticator,null,forceHttp, true);

    }


    @Override
    public void onLoadFinished(@NonNull Loader<ResultWrapper> loader, ResultWrapper data) {

        result = data;
        Toast.makeText(getApplicationContext(),"Loader finished!",Toast.LENGTH_LONG).show();
        loader.reset();

    }

    @Override
    public void onLoaderReset(@NonNull Loader<ResultWrapper> loader) {

    }

    void switchColor(HttpState state) {

        switch (state) {

            case NOT_RUNNING:
                (this.findViewById(R.id.iV_display_basic)).setBackgroundColor(getResources().getColor(R.color.workerNotInitalized));
                ((TextView) this.findViewById(R.id.tV_basic)).setText("Dead");
                ((TextView) this.findViewById(R.id.tV_basic)).setTextColor(getResources().getColor(R.color.white));
                ((TextView) this.findViewById(R.id.tv_response)).setTextColor(getResources().getColor(R.color.white));
                break;

            case FAILED:
                this.findViewById(R.id.iV_display_basic).setBackgroundColor(getResources().getColor(R.color.workerFailed));
                ((TextView) this.findViewById(R.id.tV_basic)).setText("Failed");
                ((TextView) this.findViewById(R.id.tV_basic)).setTextColor(getResources().getColor(R.color.white));
                ((TextView) this.findViewById(R.id.tv_response)).setTextColor(getResources().getColor(R.color.white));
                break;

            case COMPLETED:
                this.findViewById(R.id.iV_display_basic).setBackgroundColor(getResources().getColor(R.color.workerFinished));
                ((TextView) this.findViewById(R.id.tV_basic)).setText("Finished");
                ((TextView) this.findViewById(R.id.tV_basic)).setTextColor(getResources().getColor(R.color.white));
                ((TextView) this.findViewById(R.id.tv_response)).setTextColor(getResources().getColor(R.color.white));
                break;

            case RUNNING:
                this.findViewById(R.id.iV_display_basic).setBackgroundColor(getResources().getColor(R.color.workerRunning));
                ((TextView) this.findViewById(R.id.tV_basic)).setText("Running");
                ((TextView) this.findViewById(R.id.tV_basic)).setTextColor(getResources().getColor(R.color.black));
                ((TextView) this.findViewById(R.id.tv_response)).setTextColor(getResources().getColor(R.color.black));
                break;

            case INITALIZING:
                this.findViewById(R.id.iV_display_basic).setBackgroundColor(getResources().getColor(R.color.workerInitalizing));
                ((TextView) this.findViewById(R.id.tV_basic)).setText("Inializing");
                ((TextView) this.findViewById(R.id.tV_basic)).setTextColor(getResources().getColor(R.color.black));
                ((TextView) this.findViewById(R.id.tv_response)).setTextColor(getResources().getColor(R.color.black));
                break;

        }
    }


    void adjustResonseCode(Integer code) {
        ((TextView) this.findViewById(R.id.tv_response)).setText(String.valueOf(code));
    }
}
