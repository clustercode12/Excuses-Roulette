package org.clustercode.excusesroulette;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.session.MediaSession;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

public class TokenActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private Button btCoins;
    private TextView txtNumberCoins, txtWait;
    private RewardedVideoAd rewardedVideoAd;

    private int coins = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        Intent intent = getIntent();
        coins = intent.getIntExtra("coins", 0);

        btCoins = findViewById(R.id.btMoreCoins);
        txtNumberCoins = findViewById(R.id.txtNumberCoins);
        updateCoins(coins);
        txtWait = findViewById(R.id.txtWait);

        rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(TokenActivity.this);
        rewardedVideoAd.setRewardedVideoAdListener(this);

        loadRewardedVideoAd();

        btCoins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rewardedVideoAd.isLoaded()) {
                    rewardedVideoAd.show();
                }
            }
        });


    }

    private void updateCoins(int coins) {
        txtNumberCoins.setText("" + coins);
    }

    private void exit() {
        Intent intent = new Intent();
        intent.putExtra("coins", coins);
        setResult(Activity.RESULT_OK, intent);
    }

    private void loadRewardedVideoAd() {
        btCoins.setEnabled(false);
        TokenActivity.this.rewardedVideoAd.loadAd("ca-app-pub-3733751731897567/5096411649", new AdRequest.Builder().build());
        txtWait.setText(getString(R.string.wait_until_the_ad_loads));
    }

    
    @Override
    public void onResume() {
        rewardedVideoAd.resume(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        rewardedVideoAd.pause(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        rewardedVideoAd.destroy(this);
        exit();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        exit();
        super.onBackPressed();
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        btCoins.setEnabled(true);
        txtWait.setText(getString(R.string.ad_loaded));
    }

    @Override
    public void onRewardedVideoAdOpened() {
    }

    @Override
    public void onRewardedVideoStarted() {
    }

    @Override
    public void onRewardedVideoAdClosed() {
        loadRewardedVideoAd();
    }

    @Override
    public void onRewarded(RewardItem reward) {
        coins += reward.getAmount();
        updateCoins(coins);
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
    }

    @Override
    public void onRewardedVideoCompleted() {
    }
}
