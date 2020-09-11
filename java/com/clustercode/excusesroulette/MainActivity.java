package org.clustercode.excusesroulette;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView textView, txtCoins;  //Widgets
    private ImageView img;
    private FloatingActionButton fabToken, fabCopy;

    private AdView mAdView;

    private int numberExcuses = 103;    //Remember is the number of excuses
    private int coins = 0;
    private boolean isSpinning = false, isFirstTime = true;

    private static final Random RANDOM = new Random();  //Spin
    private int degree = 0, degreeOld = 0;
    private String excuse = "";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, "ca-app-pub-3733751731897567~9885649996");

        mAdView = findViewById(R.id.adBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //SharePreferences
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        coins = sharedPreferences.getInt("coins", coins);

        textView = findViewById(R.id.text);
        txtCoins = findViewById(R.id.textView4);
        updateCoins(coins);
        img = findViewById(R.id.imageView);
        fabToken = findViewById(R.id.fabToken);
        fabCopy = findViewById(R.id.fabCopy);

        //See if it is the first time that the user open the app
        isFirstTime = sharedPreferences.getBoolean("first", isFirstTime);
        if (isFirstTime) {
            coins = 5;
            saveCoins(coins);
            isFirstTime = false;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("first", isFirstTime);
            editor.commit();
        }

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isSpinning) {
                    if (coins > 0) {
                        spin();
                        isSpinning = true;
                    } else {
                        Toast.makeText(MainActivity.this, getText(R.string.you_have_to_get_some_tokens), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        fabToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TokenActivity.class);
                intent.putExtra("coins", coins);
                startActivityForResult(intent, 1);
            }
        });

        fabCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textView.getText().toString() != "") {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("excuse", textView.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(MainActivity.this, getString(R.string.excuse_copied), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateCoins(int coins) {
        txtCoins.setText(coins + " " + getString(R.string.coins));
    }

    private void saveCoins(int coins) {
        SharedPreferences sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("coins", coins);
        editor.commit();
        updateCoins(coins);
    }

    private void spin() {
        degreeOld = degree % 360;
        degree = RANDOM.nextInt(360) + 720;


        RotateAnimation rotateAnim = new RotateAnimation(degreeOld, degree,
                RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        rotateAnim.setDuration(3600);
        rotateAnim.setFillAfter(true);
        rotateAnim.setInterpolator(new DecelerateInterpolator());
        rotateAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Random random = new Random();
                int randomInt = random.nextInt(numberExcuses);
                DocumentReference mDocRef = FirebaseFirestore.getInstance().document("excuses/all/excuses/" + randomInt);

                mDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            excuse = documentSnapshot.getString("en");
                            if (Locale.getDefault().getLanguage() == "es") {
                                excuse = documentSnapshot.getString("es");
                            }

                            coins--;
                            saveCoins(coins);
                        }
                    }
                });
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textView.setText(excuse);
                isSpinning = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        // we start the animation
        img.startAnimation(rotateAnim);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + this.getApplicationContext().getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getApplicationContext().getPackageName())));
                }

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                coins = data.getIntExtra("coins", coins);
                saveCoins(coins);
            }
        }
    }
}
