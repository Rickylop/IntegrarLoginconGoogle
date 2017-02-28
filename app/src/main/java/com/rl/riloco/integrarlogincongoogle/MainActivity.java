package com.rl.riloco.integrarlogincongoogle;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 0;
    private GoogleApiClient mGoogleApiClient;

    private boolean progreso;
    private ConnectionResult connectionResult;
    private boolean firmaUsuario;
    private SignInButton signInButton;
    private TextView usuario, email_usuario;
    private LinearLayout PerfilIniciado, PerfilFinalizado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        signInButton = (SignInButton) findViewById(R.id.signin);
        signInButton.setOnClickListener(this);

        usuario = (TextView) findViewById(R.id.usuario);
        email_usuario = (TextView) findViewById(R.id.email);

        PerfilFinalizado = (LinearLayout) findViewById(R.id.perfil_finalizado);
        PerfilIniciado = (LinearLayout) findViewById(R.id.perfil_iniciado);

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build()).addScope(Plus.SCOPE_PLUS_LOGIN).build();


    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolvesSignInError() {
        if (connectionResult.hasResolution()) {
            try {
                progreso = true;
                connectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                progreso = false;
                mGoogleApiClient.connect();
            }
        }
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.signin:
                googlePlusLogin();
                break;
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        firmaUsuario = false;
        Toast.makeText(this, "¡Conexión Exitosa!", Toast.LENGTH_LONG).show();
        informacionPerfil();

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
        actualizarPerfil(false);

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {

        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }
        if (!progreso) {
            connectionResult = result;

            if (firmaUsuario) {
                resolvesSignInError();
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    firmaUsuario = false;
                }
                progreso = false;
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    private void actualizarPerfil(boolean iniciado) {
        if (iniciado) {
            PerfilFinalizado.setVisibility(View.GONE);
            PerfilIniciado.setVisibility(View.VISIBLE);
        } else {
            PerfilFinalizado.setVisibility(View.VISIBLE);
            PerfilIniciado.setVisibility(View.GONE);
        }
    }

    private void informacionPerfil() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String nombreUsuario = person.getDisplayName();
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                usuario.setText(nombreUsuario);
                email_usuario.setText(email);

                actualizarPerfil(true);

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void signIn(View v){
        googlePlusLogin();
    }

    public void logout(View v){
        googlePlusLogout();
    }

    private void googlePlusLogin(){
        if(!mGoogleApiClient.isConnected()){
            firmaUsuario = true;
            resolvesSignInError();
        }
    }

    private void googlePlusLogout(){
        if(mGoogleApiClient.isConnected()){
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
            actualizarPerfil(false);
        }
    }


}
