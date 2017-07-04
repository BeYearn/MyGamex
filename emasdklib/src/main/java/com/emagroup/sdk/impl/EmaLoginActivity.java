package com.emagroup.sdk.impl;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.emagroup.sdk.EmaCallBackConst;
import com.emagroup.sdk.EmaUser;
import com.emagroup.sdk.ResourceManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by beyearn on 2017/6/30.
 */

public class EmaLoginActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private ResourceManager mResourceManager;
    private GoogleApiClient mGoogleApiClient;


    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResourceManager = ResourceManager.getInstance(this.getApplicationContext());
        setContentView(mResourceManager.getIdentifier("ema_activity_login", "layout"));

        SignInButton signInButton = (SignInButton) findViewById(mResourceManager.getIdentifier("bt_gl_login","id"));
        signInButton.setOnClickListener(this);

        //setDefaultFragment();

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Set the dimensions of the sign-in button.
        signInButton.setSize(SignInButton.SIZE_STANDARD);
    }


    private void signIn() {

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            String gpId = acct.getId();
            EmaUser.getInstance().setAllianceUid(gpId);
            this.setResult(EmaCallBackConst.LOGINSUCCESS);
        } else {
            this.setResult(EmaCallBackConst.LOGINFALIED);
        }
        this.finish();
    }



    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == mResourceManager.getIdentifier("bt_gl_login","id")) {
            signIn();
        } else {

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
