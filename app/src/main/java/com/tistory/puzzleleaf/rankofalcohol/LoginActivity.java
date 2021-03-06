package com.tistory.puzzleleaf.rankofalcohol;

import android.app.ApplicationErrorReport;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.tistory.puzzleleaf.rankofalcohol.animation.LoginAnimation;
import com.tistory.puzzleleaf.rankofalcohol.fb.FbAuth;
import com.tistory.puzzleleaf.rankofalcohol.fb.FbDataBase;
import com.tistory.puzzleleaf.rankofalcohol.model.FbUser;
import com.tistory.puzzleleaf.rankofalcohol.util.progress.Loading;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by cmtyx on 2017-07-25.
 */

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    private static final int RC_SIGN_IN = 9001;
    private GoogleApiClient mGoogleApiClient;
    private Loading loading;

    @BindView(R.id.sign_check_button) Button signInButton;
    @BindView(R.id.login_input_how_many) EditText inputHowMany;
    @BindView(R.id.login_gender_m) TextView genderM;
    @BindView(R.id.login_gender_w) TextView genderW;

    private String gender = null;
    private boolean isUserData = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        showAnimation();
        loginInit();
    }

    private void showAnimation(){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.login_animation, new LoginAnimation());
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show();
                loading.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        FbAuth.mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        task.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                FirebaseCrash.report(e);
                            }
                        });

                        if (task.isSuccessful()) {
                            userRegister();
                        } else {
                            Toast.makeText(getApplicationContext(), "네트워크 에러가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }


    private void userRegister(){
        Log.d(this.toString(),FbAuth.getGoogleUserId());
        FbDataBase.database.getReference()
                .child(getString(R.string.FireBaseUserKey))
                .child(FbAuth.getGoogleUserId())
                .child(getString(R.string.FireBaseUserInfo))
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        FbUser fbUser = mutableData.getValue(FbUser.class);
                        if(fbUser == null){
                            fbUser = new FbUser(Float.parseFloat(inputHowMany.getText().toString()),gender);
                        }else{
                            isUserData = true;
                            fbUser.sethMany(Float.parseFloat(inputHowMany.getText().toString()));
                        }
                        mutableData.setValue(fbUser);
                        return Transaction.success(mutableData);
                    }
                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        loading.dismiss();
                        if (isUserData){
                            Toast.makeText(getApplicationContext(),"기존 데이터가 존재하여\n주량 데이터만 수정가능합니다.",Toast.LENGTH_SHORT).show();
                        }
                        Toast.makeText(getApplicationContext(),FbAuth.getGoogleUserName() + "님 환영합니다.",Toast.LENGTH_SHORT).show();
                        loginResult();
                    }
                });
    }

    private void loginResult(){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean checkData(){
        if(gender!=null && inputHowMany.length()>0){
            return true;
        }else{
            return false;
        }
    }


    private void loginInit(){
        loading = new Loading(this,"login");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)//onConnectionFailed Listener
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

    }

    @OnClick(R.id.sign_check_button)
    public void googleSignIn(){
        if(checkData()) {
            loading.show();
            Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);
        }else{
            Toast.makeText(this,"데이터를 입력해 주세요",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.login_gender_m)
    public void genderMSelect(){
        genderW.setTextColor(ContextCompat.getColor(this,R.color.colorHint));
        genderM.setTextColor(ContextCompat.getColor(this,R.color.colorWhite));
        gender = getString(R.string.genderM);
    }

    @OnClick(R.id.login_gender_w)
    public void genderWSelect(){
        genderM.setTextColor(ContextCompat.getColor(this,R.color.colorHint));
        genderW.setTextColor(ContextCompat.getColor(this,R.color.colorWhite));
        gender = getString(R.string.genderW);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this,"로그인에 실패했습니다.",Toast.LENGTH_SHORT).show();
    }

}
