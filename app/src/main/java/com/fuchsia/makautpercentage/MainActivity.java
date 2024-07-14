 package com.fuchsia.makautpercentage;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;

 public class MainActivity extends AppCompatActivity implements UpdateHelper.OnUpdateCheckListener {

    TextView number, total, percent;
    TextInputEditText sub, gpa;
    Button button;

    Double p1,p2,p3,p4;

     NavigationView navigationView;
     ActionBarDrawerToggle toggle;
     DrawerLayout drawerLayout;
     Toolbar toolbar;
     private long backPressTime;

     private AdView mAdView;

     private static InterstitialAd mInterstitialAd;

     private ConsentInformation consentInformation;
     // Use an atomic boolean to initialize the Google Mobile Ads SDK and load ads once.
     private final AtomicBoolean isMobileAdsInitializeCalled = new AtomicBoolean(false);



     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

         // Set tag for under age of consent. false means users are not under age
         // of consent.
         ConsentRequestParameters params = new ConsentRequestParameters
                 .Builder()
                 .setTagForUnderAgeOfConsent(false)
                 .build();

         consentInformation = UserMessagingPlatform.getConsentInformation(this);
         consentInformation.requestConsentInfoUpdate(
                 this,
                 params,
                 (ConsentInformation.OnConsentInfoUpdateSuccessListener) () -> {
                     UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                             this,
                             (ConsentForm.OnConsentFormDismissedListener) loadAndShowError -> {

                                 // Consent has been gathered.
                                 if (consentInformation.canRequestAds()) {
                                     initializeMobileAdsSdk();
                                 }
                             }
                     );
                 },
                 (ConsentInformation.OnConsentInfoUpdateFailureListener) requestConsentError -> {
                     // Consent gathering failed.

                 });

         // Check if you can initialize the Google Mobile Ads SDK in parallel
         // while checking for new consent information. Consent obtained in
         // the previous session can be used to request ads.
         if (consentInformation.canRequestAds()) {
             initializeMobileAdsSdk();
         }

        number= findViewById(R.id.number);
        percent= findViewById(R.id.outputPercentage);
        total= findViewById(R.id.total);

        sub= findViewById(R.id.noOfSub);
        gpa= findViewById(R.id.gpa);
        button= findViewById(R.id.gettext);

        UpdateHelper.with(this)
                .onUpdateCheck(this)
                .check();

         MobileAds.initialize(MainActivity.this, new OnInitializationCompleteListener() {
             @Override
             public void onInitializationComplete(InitializationStatus initializationStatus) {
             }
         });
         bannerAds();

        AppRate.with(this)
                .setInstallDays(0)
                .setLaunchTimes(4)
                .setRemindInterval(10)
                .setShowLaterButton(true)
                .setDebug(false)
                .setOnClickButtonListener(new OnClickButtonListener() {
                    @Override
                    public void onClickButton(int which) {
                        Log.d(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .monitor();

        // Show a dialog if meets conditions
        AppRate.showRateDialogIfMeetsConditions(this);
         
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View v) {

                if (mInterstitialAd != null) {

                    mInterstitialAd.show(MainActivity.this);

                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
                        @Override
                        public void onAdDismissedFullScreenContent() {

                            String num1 = Objects.requireNonNull(sub.getText()).toString();
                            String num2 = Objects.requireNonNull(gpa.getText()).toString();

                            if (!num1.isEmpty() && !num2.isEmpty()){

                                double a = Double.parseDouble(sub.getText().toString());
                                double b = Double.parseDouble(gpa.getText().toString());
                                p1 = b - 0.75;
                                p2 = p1*10;
                                p3 = a*100;
                                p4 = p2*a;

                                double roundPer = Math.round(p2*100.0)/100.0;
                                double roundNum = Math.round(p4*100.0)/100.0;

                                @SuppressLint("DefaultLocale") String s2 = String.format("%.00f", p3);

                                String s1=String.valueOf(roundPer);
                                String s3=String.valueOf(roundNum);

                                percent.setText(s1);
                                number.setText(s3+" ");
                                total.setText(s2);

                            }else{

                                Toast.makeText(getBaseContext(),"Please enter total subjects and gpa.",Toast.LENGTH_SHORT).show();
                            }

                            AdRequest adRequest = new AdRequest.Builder().build();

                            InterstitialAd.load(MainActivity.this,getResources().getString(R.string.interstitialId), adRequest, new InterstitialAdLoadCallback() {
                                @Override
                                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                                    mInterstitialAd = interstitialAd;

                                }

                                @Override
                                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                                    mInterstitialAd = null;
                                    if (!num1.isEmpty() && !num2.isEmpty()){

                                        double a = Double.parseDouble(sub.getText().toString());
                                        double b = Double.parseDouble(gpa.getText().toString());
                                        p1 = b - 0.75;
                                        p2 = p1*10;
                                        p3 = a*100;
                                        p4 = p2*a;

                                        double roundPer = Math.round(p2*100.0)/100.0;
                                        double roundNum = Math.round(p4*100.0)/100.0;

                                        @SuppressLint("DefaultLocale") String s2 = String.format("%.00f", p3);

                                        String s1=String.valueOf(roundPer);
                                        String s3=String.valueOf(roundNum);

                                        percent.setText(s1);
                                        number.setText(s3+" ");
                                        total.setText(s2);

                                    }else{

                                        Toast.makeText(getBaseContext(),"Please enter total subjects and gpa.",Toast.LENGTH_SHORT).show();
                                    }

                                }
                            });

                        }

                    });

                }
                else {

                    String num1 = Objects.requireNonNull(sub.getText()).toString();
                    String num2 = Objects.requireNonNull(gpa.getText()).toString();

                    if (!num1.isEmpty() && !num2.isEmpty()){

                        double a = Double.parseDouble(sub.getText().toString());
                        double b = Double.parseDouble(gpa.getText().toString());
                        p1 = b - 0.75;
                        p2 = p1*10;
                        p3 = a*100;
                        p4 = p2*a;

                        double roundPer = Math.round(p2*100.0)/100.0;
                        double roundNum = Math.round(p4*100.0)/100.0;

                        @SuppressLint("DefaultLocale") String s2 = String.format("%.00f", p3);

                        String s1=String.valueOf(roundPer);
                        String s3=String.valueOf(roundNum);

                        percent.setText(s1);
                        number.setText(s3+" ");
                        total.setText(s2);

                    }else{

                        Toast.makeText(getBaseContext(),"Please enter total subjects and gpa.",Toast.LENGTH_SHORT).show();
                    }

                }




            }
        });

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navdrawer);
        toggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, toolbar, R.string.Open, R.string.Close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.menuHome:

                        drawerLayout.closeDrawer(GravityCompat.START);
                        return true;

                    case R.id.menuprivacy:
                        Intent browse = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.lkmkm)));
                        startActivity(browse);
                        drawerLayout.closeDrawer(GravityCompat.START);

                        return true;

                    case R.id.menurate:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        final String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }

                        return true;

                    case R.id.menuwhatsapp:

                        drawerLayout.closeDrawer(GravityCompat.START);
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.fuchsia.saver")));


                        return true;

                    case R.id.menumoreapp:
                        Intent browses = new Intent(Intent.ACTION_VIEW, Uri.parse(("https://play.google.com/store/apps/collection/cluster?clp=igM4ChkKEzUzNjIwODY3OTExNjgyNTA2MTkQCBgDEhkKEzUzNjIwODY3OTExNjgyNTA2MTkQCBgDGAA%3D:S:ANO1ljJMw2s&gsr=CjuKAzgKGQoTNTM2MjA4Njc5MTE2ODI1MDYxORAIGAMSGQoTNTM2MjA4Njc5MTE2ODI1MDYxORAIGAMYAA%3D%3D:S:ANO1ljI3U6g")));
                        startActivity(browses);
                        drawerLayout.closeDrawer(GravityCompat.START);

                        return true;

                    case R.id.menushare:

                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        String shareBody = "Download MAKAUT GPA TO PERCENTAGE Calculator App.  https://play.google.com/store/apps/details?id=com.fuchsia.makautpercentage";
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "MAKAUT GPA TO PERCENTAGE");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        startActivity(Intent.createChooser(sharingIntent, "Share via"));
                        drawerLayout.closeDrawer(GravityCompat.START);

                        return true;

                    case R.id.menuexit:

                        finishAffinity();

                        return true;
                }
                return false;


            }


        });
    }

     @Override
     public void onUpdateCheckListener(String urlApp) {
         final String appPackageName = getPackageName();

         AlertDialog alertDialog=new AlertDialog.Builder(this)
                 .setTitle("New Version Available")
                 .setMessage(" Please update for better experience")
                 .setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));

                     }
                 }).setNegativeButton("NOT NOW", new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         dialogInterface.dismiss();
                     }
                 }).create();
         alertDialog.show();

     }

     public void bannerAds(){

         mAdView = findViewById(R.id.bannerad);
         AdRequest adRequest = new AdRequest.Builder().build();
         mAdView.loadAd(adRequest);

         //MediationTestSuite.launch(MainActivity.this);

         InterstitialAd.load(MainActivity.this,getResources().getString(R.string.interstitialId), adRequest, new InterstitialAdLoadCallback() {
             @Override
             public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                 mInterstitialAd = interstitialAd;

             }

             @Override
             public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                 mInterstitialAd = null;

             }
         });


     }


     @Override
     public void onBackPressed() {


         if (backPressTime+2000>System.currentTimeMillis()){
             super.onBackPressed();
             return;
         }else {
             Toast.makeText(getBaseContext(),"Press back again to exit",Toast.LENGTH_SHORT).show();
         }

         backPressTime= System.currentTimeMillis();



     }

     private void initializeMobileAdsSdk() {
         if (isMobileAdsInitializeCalled.getAndSet(true)) {
             return;
         }

         // Initialize the Google Mobile Ads SDK.
         MobileAds.initialize(this);

         // TODO: Request an ad.
         // InterstitialAd.load(...);
     }

 }