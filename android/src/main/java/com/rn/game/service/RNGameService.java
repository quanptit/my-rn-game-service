package com.rn.game.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.leaderboard.LeaderboardScore;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.example.games.basegameutils.BaseGameUtils;

public class RNGameService extends ReactContextBaseJavaModule implements LifecycleEventListener, ActivityEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "RNGameService";

    private int scoreNeedSubmitWhenConnected = 0;
    private String LEADERBOARD_ID;
    private boolean needShowLeadboard = false;

    public RNGameService(ReactApplicationContext reactContext) {
        super(reactContext);
        getReactApplicationContext().addLifecycleEventListener(this);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public String getName() {
        return "RNGameService";
    }

    @ReactMethod
    public void getCurrentRank(String LEADERBOARD_ID, final Promise promise) {
        this.LEADERBOARD_ID = LEADERBOARD_ID;
        initGoogleApiClientIfNeed();
        if (mGoogleApiClient.isConnected()) {
            Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient, LEADERBOARD_ID, LeaderboardVariant.TIME_SPAN_ALL_TIME,
                    LeaderboardVariant.COLLECTION_PUBLIC).setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                @Override
                public void onResult(@NonNull Leaderboards.LoadPlayerScoreResult arg0) {
                    LeaderboardScore score = arg0.getScore();
                    if (score == null)
                        promise.resolve(-1);
                    else
                        promise.resolve((int) score.getRank());
                }
            });
        } else
            promise.resolve(-1);
    }

    @ReactMethod
    public void submitScore(String LEADERBOARD_ID, int score) throws Exception {
        this.LEADERBOARD_ID = LEADERBOARD_ID;
        initGoogleApiClientIfNeed();
        if (mGoogleApiClient.isConnected()) {
            try {
                Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, score);
                scoreNeedSubmitWhenConnected = 0;
            } catch (Exception e) {
                try {
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            scoreNeedSubmitWhenConnected = score;
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }

    @ReactMethod
    public void openLeadboard(String LEADERBOARD_ID) {
        this.LEADERBOARD_ID = LEADERBOARD_ID;
        Log.d(TAG, "openLeadboard");
        initGoogleApiClientIfNeed();
        if (mGoogleApiClient.isConnected() && getCurrentActivity() != null) {
            try {
                getCurrentActivity().startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, LEADERBOARD_ID), 153);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        } else {
            needShowLeadboard = true;
            mSignInClicked = true;
            mGoogleApiClient.connect();
        }
    }


    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostDestroy() {
        if (mGoogleApiClient != null)
            mGoogleApiClient.disconnect();
    }

    @Override public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == Activity.RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                BaseGameUtils.showActivityResultError(getCurrentActivity(), requestCode, resultCode, R.string.signin_failure);
            }
        }
    }

    @Override public void onNewIntent(Intent intent) { }


    //region Google play game =====
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mSignInClicked = false;

    private void initGoogleApiClientIfNeed() {
        if (mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(getReactApplicationContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                    // add other APIs and scopes here as needed
                    .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (scoreNeedSubmitWhenConnected > 0) {
            Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_ID, scoreNeedSubmitWhenConnected);
            scoreNeedSubmitWhenConnected = 0;
        }

        if (needShowLeadboard && getCurrentActivity() != null) {
            try {
                getCurrentActivity().startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient, LEADERBOARD_ID), 153);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    mGoogleApiClient.disconnect();
                    mGoogleApiClient = null;
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            needShowLeadboard = false;
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            return;
        }
        if (mSignInClicked) {
            mSignInClicked = false;
            mResolvingConnectionFailure = true;
            if (!BaseGameUtils.resolveConnectionFailure(getCurrentActivity(), mGoogleApiClient, connectionResult, RC_SIGN_IN, "There was an issue with sign in.  Please try again later.")) {
                mResolvingConnectionFailure = false;
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }
    //endregion
}
