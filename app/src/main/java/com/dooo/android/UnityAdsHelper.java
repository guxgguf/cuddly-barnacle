package com.dooo.android;

import android.app.Activity;
import android.view.ViewGroup;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.ArrayList;
import java.util.List;

/**
 * Unity Ads (SDK 4.x) helper: one-time initialization, banner and interstitial ads.
 */
public class UnityAdsHelper {

    private static boolean initializing = false;
    private static final List<Runnable> pending = new ArrayList<>();

    private static void whenReady(Activity activity, Runnable onReady) {
        if(AppConfig.Unity_Game_ID == null || AppConfig.Unity_Game_ID.isEmpty()) {
            return;
        }
        if(UnityAds.isInitialized()) {
            onReady.run();
            return;
        }
        pending.add(onReady);
        if(initializing) {
            return;
        }
        initializing = true;
        UnityAds.initialize(activity.getApplicationContext(), AppConfig.Unity_Game_ID, AppConfig.unity_ad_testMode, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                initializing = false;
                List<Runnable> toRun = new ArrayList<>(pending);
                pending.clear();
                for(Runnable r : toRun) {
                    r.run();
                }
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                initializing = false;
                pending.clear();
            }
        });
    }

    public static void showBanner(final Activity activity, final ViewGroup container) {
        if(AppConfig.Unity_Banner_ID == null || AppConfig.Unity_Banner_ID.isEmpty()) {
            return;
        }
        whenReady(activity, () -> {
            if(activity.isFinishing() || activity.isDestroyed()) {
                return;
            }
            BannerView banner = new BannerView(activity, AppConfig.Unity_Banner_ID, new UnityBannerSize(320, 50));
            banner.load();
            container.addView(banner);
        });
    }

    public static void showInterstitial(final Activity activity) {
        final String placementId = AppConfig.Unity_Interstitial_ID;
        if(placementId == null || placementId.isEmpty()) {
            return;
        }
        whenReady(activity, () -> UnityAds.load(placementId, new IUnityAdsLoadListener() {
            @Override
            public void onUnityAdsAdLoaded(String loadedPlacementId) {
                if(activity.isFinishing() || activity.isDestroyed()) {
                    return;
                }
                UnityAds.show(activity, loadedPlacementId, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
                    @Override
                    public void onUnityAdsShowFailure(String placement, UnityAds.UnityAdsShowError error, String message) {
                    }

                    @Override
                    public void onUnityAdsShowStart(String placement) {
                    }

                    @Override
                    public void onUnityAdsShowClick(String placement) {
                    }

                    @Override
                    public void onUnityAdsShowComplete(String placement, UnityAds.UnityAdsShowCompletionState state) {
                    }
                });
            }

            @Override
            public void onUnityAdsFailedToLoad(String placement, UnityAds.UnityAdsLoadError error, String message) {
            }
        }));
    }
}
