/*
 * Copyright 2012 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.newtra.salemrukmani.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeApiServiceUtil;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnFullscreenListener;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailLoader.ErrorReason;
import com.google.android.youtube.player.YouTubeThumbnailView;
import com.newtra.salemrukmani.constants.DeveloperKey;
import com.newtra.salemrukmani.fragments.VideosFragment;
import com.newtra.salemrukmani.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * A sample Activity showing how to manage multiple YouTubeThumbnailViews in an adapter for display
 * in a List. When the list items are clicked, the video is played by using a YouTubePlayerFragment.
 * <p>
 * The demo supports custom fullscreen and transitioning between portrait and landscape without
 * rebuffering.
 */
@TargetApi(13)
public final class VideoListActivity extends Activity implements OnFullscreenListener {

  /** The duration of the animation sliding up the video in portrait. */
  private static final int ANIMATION_DURATION_MILLIS = 300;
  /** The padding between the video list and the video in landscape orientation. */
  private static final int LANDSCAPE_VIDEO_PADDING_DP = 5;

  /** The request code when calling startActivityForResult to recover from an API service error. */
  private static final int RECOVERY_DIALOG_REQUEST = 1;

  private VideoListFragment listFragment;
  private VideoFragment videoFragment;

  private View videoBox;
  private View closeButton;

  private boolean isFullscreen;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.video_list_demo);


    listFragment = (VideoListFragment) getFragmentManager().findFragmentById(R.id.list_fragment);
    videoFragment =
        (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);

    videoBox = findViewById(R.id.video_box);
    closeButton = findViewById(R.id.close_button);

    videoBox.setVisibility(View.INVISIBLE);

    layout();

    checkYouTubeApi();

  }

  private void checkYouTubeApi() {
    YouTubeInitializationResult errorReason =
        YouTubeApiServiceUtil.isYouTubeApiServiceAvailable(this);
    if (errorReason.isUserRecoverableError()) {
      errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
    } else if (errorReason != YouTubeInitializationResult.SUCCESS) {
      String errorMessage =
          String.format(getString(R.string.error_player), errorReason.toString());
      Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == RECOVERY_DIALOG_REQUEST) {
      // Recreate the activity if user performed a recovery action
      recreate();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    layout();
  }

  @Override
  public void onFullscreen(boolean isFullscreen) {
    this.isFullscreen = isFullscreen;

    layout();
  }

  /**
   * Sets up the layout programatically for the three different states. Portrait, landscape or
   * fullscreen+landscape. This has to be done programmatically because we handle the orientation
   * changes ourselves in order to get fluent fullscreen transitions, so the xml layout resources
   * do not get reloaded.
   */
  private void layout() {

    boolean isPortrait =
        getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;

    listFragment.getView().setVisibility(isFullscreen ? View.GONE : View.VISIBLE);
    listFragment.setLabelVisibility(isPortrait);
    closeButton.setVisibility(isPortrait ? View.VISIBLE : View.GONE);

    if (isFullscreen) {
      videoBox.setTranslationY(0); // Reset any translation that was applied in portrait.
      setLayoutSize(videoFragment.getView(), MATCH_PARENT, MATCH_PARENT);
      setLayoutSizeAndGravity(videoBox, MATCH_PARENT, MATCH_PARENT, Gravity.TOP | Gravity.LEFT);
    } else if (isPortrait) {
      setLayoutSize(listFragment.getView(), MATCH_PARENT, MATCH_PARENT);
      setLayoutSize(videoFragment.getView(), MATCH_PARENT, WRAP_CONTENT);
      setLayoutSizeAndGravity(videoBox, MATCH_PARENT, WRAP_CONTENT, Gravity.BOTTOM);
    } else {
      videoBox.setTranslationY(0); // Reset any translation that was applied in portrait.
      int screenWidth = dpToPx(getResources().getConfiguration().screenWidthDp);
      setLayoutSize(listFragment.getView(), screenWidth / 4, MATCH_PARENT);
      int videoWidth = screenWidth - screenWidth / 4 - dpToPx(LANDSCAPE_VIDEO_PADDING_DP);
      setLayoutSize(videoFragment.getView(), videoWidth, WRAP_CONTENT);
      setLayoutSizeAndGravity(videoBox, videoWidth, WRAP_CONTENT,
          Gravity.RIGHT | Gravity.CENTER_VERTICAL);
    }
  }

  public void onClickClose(@SuppressWarnings("unused") View view) {
    listFragment.getListView().clearChoices();
    listFragment.getListView().requestLayout();
    videoFragment.pause();
    ViewPropertyAnimator animator = videoBox.animate()
        .translationYBy(videoBox.getHeight())
        .setDuration(ANIMATION_DURATION_MILLIS);
    runOnAnimationEnd(animator, new Runnable() {
      @Override
      public void run() {
        videoBox.setVisibility(View.INVISIBLE);
      }
    });
  }

  @TargetApi(16)
  private void runOnAnimationEnd(ViewPropertyAnimator animator, final Runnable runnable) {
    if (Build.VERSION.SDK_INT >= 16) {
      animator.withEndAction(runnable);
    } else {
      animator.setListener(new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
          runnable.run();
        }
      });
    }
  }

  /**
   * A fragment that shows a static list of videos.
   */
  public static final class VideoListFragment extends ListFragment {

    private static final  List<VideoEntry> KANDHARALANKAARAM_LIST;
      private static final  List<VideoEntry> SOODIKODUTHTHASUDARKODI_LIST;
      private static final  List<VideoEntry> AKR_ACADEMY_SCHOOL_LIST;
      private static final  List<VideoEntry> KANTHAPURAANAM_LIST;
      private static final  List<VideoEntry> KANDHARSASTI_PERURAI_LIST;
      private static final  List<VideoEntry> KRISHNAVATHARAM_LIST;
      private static final  List<VideoEntry> MAANAM_KATHA_MADHUSUDHANAN_LIST;

    static {
      List<VideoEntry> kandharalankaaram_list = new ArrayList<VideoEntry>();
        List<VideoEntry> soodiKoduththaSudarkodi_list = new ArrayList<VideoEntry>();
        List<VideoEntry> akrAcademySchool_list = new ArrayList<VideoEntry>();
        List<VideoEntry> kanthaPuraanam_list = new ArrayList<VideoEntry>();
        List<VideoEntry> kandharsastiPerurai_list = new ArrayList<VideoEntry>();
        List<VideoEntry> krishnaavatharam_list = new ArrayList<VideoEntry>();
        List<VideoEntry> maanamKathaMadhusudhanan_list = new ArrayList<VideoEntry>();

        kandharalankaaram(kandharalankaaram_list);
        kandharsastiPerurai(kandharsastiPerurai_list);
        kanthaPuraanam(kanthaPuraanam_list);
        krishnaavatharam(krishnaavatharam_list);
        maanamKathaMadhusudhanan(maanamKathaMadhusudhanan_list);
        soodiKoduththaSudarkodi(soodiKoduththaSudarkodi_list);
        akrAcademySchool(akrAcademySchool_list);


      KANDHARALANKAARAM_LIST = Collections.unmodifiableList(kandharalankaaram_list);
        MAANAM_KATHA_MADHUSUDHANAN_LIST = Collections.unmodifiableList(maanamKathaMadhusudhanan_list);
        KANTHAPURAANAM_LIST = Collections.unmodifiableList(kanthaPuraanam_list);
        AKR_ACADEMY_SCHOOL_LIST = Collections.unmodifiableList(akrAcademySchool_list);
        SOODIKODUTHTHASUDARKODI_LIST = Collections.unmodifiableList(soodiKoduththaSudarkodi_list);
        KRISHNAVATHARAM_LIST = Collections.unmodifiableList(krishnaavatharam_list);
        KANDHARSASTI_PERURAI_LIST = Collections.unmodifiableList(kandharsastiPerurai_list);

    }




      private static void kanthaPuraanam(List<VideoEntry> list) {
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-1", "GR8ibLS3UnM"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-2", "FbKeP8rGmmE"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-3", "MdZeQ-tOymI"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-4", "V6j0LNL0WDM"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-5", "sj3Viwsy6go"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-6", "ESvIApW-Eeo"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-7", "643QklORSTE"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-8", "ImKcWmcL66E"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-9", "eEL2UWewHXc"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-10", "G-fsvlXyKDs"));

          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-11", "9hilkM1ZXI8"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-12", "mHR5NLlpXnY"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-13", "KsowKTGCYsA"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-14", "CflOuZITB9M"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-15", "pHHGOTU6a5I"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-16", "PI5tIvqMpJc"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-17", "7H2BImBdr1k"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-18", "jAyjrJNbfy8"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-19", "ytShVNvSwoA"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-20", "sZioyfNNndc"));

          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-21", "uyHw8XpP3VE"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-22", "lomGD0lozxM"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-23", "iaIsN4kKcRQ"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-24", "92mlKpAr0m4"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-25", "gYy6lr8cJYQ"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-26", "ykh3owpX87c"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-27", "tQZZu0BJbPc"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-28", "I_OEA4lWN7w"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-29", "IcIrERCBZk8"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-30", "dgz-XEpN9BM"));

          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-31", "QkV8QEn6sOc"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-32", "vBiv-8dABAo"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-33", "26weFyDGpWg"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-34", "sS2m6IOgzcs"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-35", "JCeMQDE7FmA"));
          list.add(new VideoEntry("KanthaPuraanam by Salem Rukmani Part-36", "c6vQ-Lp1AXk"));
      }
      private static void akrAcademySchool(List<VideoEntry> list) {
          list.add(new VideoEntry("Mannum Pennum", "F2cHsDKgNso"));
          list.add(new VideoEntry("Pallankuzhi", "6a-Gm6jS7Fk"));
          list.add(new VideoEntry("Wealth", "E7iJLG0jIvg"));
          list.add(new VideoEntry("Navarathri and catering", "c7nuNvm3tMk"));
          list.add(new VideoEntry("A fine comparison of Hindu gods", "qvAbg_t0Biw"));
          list.add(new VideoEntry("Indicating habbits", "RVHSC6Ww5C4"));
          list.add(new VideoEntry("Refresher course", "7uKkY613rUU"));
          list.add(new VideoEntry("Ammikkal", "kzebYR6zJk4"));
          list.add(new VideoEntry("200 out of 200", "QiDYGpL2_NA"));

      }

      private static void soodiKoduththaSudarkodi(List<VideoEntry> list) {
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 01", "5mZuUKVLh10"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 02", "6QpLfyLO2yQ"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 03", "nmlbA6dsyV8"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 04", "C9Iqxos5370"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 05", "jh_wFZOf8no"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 06", "G6mjpuDm5sA"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 07", "Pw4epsuu3DM"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 08", "ixkN3P0Irg0"));
          list.add(new VideoEntry("Soodi koduththa sudarkodi part 09", "h0uCAl0LZog"));

      }


      private static void kandharalankaaram (List<VideoEntry> list) {
          list.add(new VideoEntry("Kandharalankaaram  part 01", "kqfcvHVwKTA"));
          list.add(new VideoEntry("Kandharalankaaram  part 02", "Pz75CyMbhQM"));
          list.add(new VideoEntry("Kandharalankaaram  part 03", "edSHsn5ZxB4"));
          list.add(new VideoEntry("Kandharalankaaram  part 04", "p2fMnLqR8Wk"));
          list.add(new VideoEntry("Kandharalankaaram  part 05", "K2A_M8Pph-Q"));
          list.add(new VideoEntry("Kandharalankaaram  part 06", "qzaP1P2x5jY"));
          list.add(new VideoEntry("Kandharalankaaram  part 07", "EWO9i1lc0Fs"));
          list.add(new VideoEntry("Kandharalankaaram  part 08", "OF0ny9sxOzA"));
          list.add(new VideoEntry("Kandharalankaaram  part 09", "2Tzv3RNh2Wc"));
          list.add(new VideoEntry("Kandharalankaaram  part 10", "VYbrot4pjjM"));

      }

      private static void kandharsastiPerurai (List<VideoEntry> list) {
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 01", "PbIuKwU9Kto"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 02", "-opq0tIn2y0"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 03", "5dBLZ93QdtA"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 04", "AyMa6u-pyjA"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 05", "jIV3NB3jVwY"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 06", "gCiP4FtYLfk"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 07", "5eBe5zCkAgw"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 08", "J-4BOvnLqPs"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 09", "e7J7XIDkImA"));
          list.add(new VideoEntry("Puthiya vidiyalil boopalam   part 10", "CHi2S3tiG8c"));

          list.add(new VideoEntry("Nathikkarai Naanal  part 11", "uT4ZAGzj2nA"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 12", "ODFqeLpv0as"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 13", "7tLe_MTKEfU"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 14", "jOvesMI98_U"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 15", "ulrNdxlZ3RY"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 16", "FYI_IZMkk0Y"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 17", "64zJJYf7clk"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 18", "nVQybXbwb2c"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 19", "zf0kTdaYh2M"));
          list.add(new VideoEntry("Nathikkarai Naanal  part 20", "0E08hEqgvGI"));

          list.add(new VideoEntry("Pulli Maariya Kolangal  part 21", "9YH8YJHZjwI"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 22", "sdjoZY7mSaA"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 23", "ac7QzXeLtJM"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 24", "98Pa3-cb6aE"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 25", "g5uX8gHQB8A"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 26", "3GyNJ361rNk"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 27", "2eQtaCg-FwY"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 28", "zIFoVx9amAk"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 29", "wyFJIPR8rw8"));
          list.add(new VideoEntry("Pulli Maariya Kolangal  part 30", "LEYfZzYrbF0"));



              list.add(new VideoEntry("Villum Ambum   part 31", "OS2Emj-fe0g"));
              list.add(new VideoEntry("Villum Ambum   part 32", "hpNx1j6SO-g"));
              list.add(new VideoEntry("Villum Ambum   part 33", "RAem4T6LuFU"));
              list.add(new VideoEntry("Villum Ambum   part 34", "UbPlBYtFiNk"));
              list.add(new VideoEntry("Villum Ambum   part 35", "zbPaxnrSZ74"));
              list.add(new VideoEntry("Villum Ambum   part 36", "Ur3uZ3Jly78"));
              list.add(new VideoEntry("Villum Ambum   part 37", "Hgko73h3T5U"));
              list.add(new VideoEntry("Villum Ambum   part 38", "51ZC0Qw7N2o"));
              list.add(new VideoEntry("Villum Ambum   part 39", "tMpWA8gEoNQ"));
              list.add(new VideoEntry("Villum Ambum   part 40", "q7Q1u12g-TQ"));

          list.add(new VideoEntry("Vinaiyum Vilaivum   part 41", "CsPOaxWqhUw"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 42", "JjwNt-IQHcQ"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 43", "zTJi33a4z4c"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 44", "3BjMiuNP5vM"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 45", "nC5hAZojudM"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 46", "WVkkQTVPi2w"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 47", "P6bjbMiGVJs"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 48", "j6zaMNyjLlY"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 49", "P1LzEE-hYwE"));
          list.add(new VideoEntry("Vinaiyum Vilaivum   part 50", "4oSi7ceSa3o"));

          list.add(new VideoEntry("Mayileriya Manickam  part 51", "6KVhgE3w0Y0"));
          list.add(new VideoEntry("Mayileriya Manickam  part 52", "vdZJQn1O0Fs"));
          list.add(new VideoEntry("Mayileriya Manickam  part 53", "jbt3Kpx5L-Y"));
          list.add(new VideoEntry("Mayileriya Manickam  part 54", "4GvH-lYxZkE"));
          list.add(new VideoEntry("Mayileriya Manickam  part 55", "eHAWqNk71hE"));
          list.add(new VideoEntry("Mayileriya Manickam  part 56", "gmY36-vbUyU"));
          list.add(new VideoEntry("Mayileriya Manickam  part 57", "mk60ytfsE6o"));
          list.add(new VideoEntry("Mayileriya Manickam  part 58", "IuYAZibohi4"));
          list.add(new VideoEntry("Mayileriya Manickam  part 59", "p95A7TME0Gs"));
          list.add(new VideoEntry("Mayileriya Manickam  part 60", "GXt4pO3zkkE"));

          list.add(new VideoEntry("Sangamam  part 61", "DKt11Ulnaoc"));
          list.add(new VideoEntry("Sangamam  part 62", "NJB7Yz6jzJQ"));
          list.add(new VideoEntry("Sangamam  part 63", "d4GDWjSRafs"));
          list.add(new VideoEntry("Sangamam  part 64", "hPgH48d54Oo"));
          list.add(new VideoEntry("Sangamam  part 65", "y8GSCE0Qq8o"));
          list.add(new VideoEntry("Sangamam  part 66", "r1eK3nBbzqQ"));
          list.add(new VideoEntry("Sangamam  part 67", "-vrAsZRD2No"));
          list.add(new VideoEntry("Sangamam  part 68", "gma5GdBnMIE"));
          list.add(new VideoEntry("Sangamam  part 69", "iB_RfsTW_jk"));
          list.add(new VideoEntry("Sangamam  part 70", "W8i7jPs6MGs"));
      }


      private static void krishnaavatharam(List<VideoEntry> list) {
          list.add(new VideoEntry("Krishnaavathaaram part 01", "NtCdVPoCFXU"));
          list.add(new VideoEntry("Krishnaavathaaram part 02", "J3DBLdeR4nU"));
          list.add(new VideoEntry("Krishnaavathaaram part 03", "z0yMaCYr6G0"));
          list.add(new VideoEntry("Krishnaavathaaram part 04", "gU2U5fxia_U"));
          list.add(new VideoEntry("Krishnaavathaaram part 05", "pJ-cE2J9W8s"));
          list.add(new VideoEntry("Krishnaavathaaram part 06", "4HCHUBxj51s"));
          list.add(new VideoEntry("Krishnaavathaaram part 07", "cRYREAMbY_s"));
          list.add(new VideoEntry("Krishnaavathaaram part 08", "-FcDMRNQxHM"));
          list.add(new VideoEntry("Krishnaavathaaram part 09", "UyxQoohbWPA"));
          list.add(new VideoEntry("Krishnaavathaaram part 10", "8_GEDAzBdg0"));
      }

      private static void maanamKathaMadhusudhanan(List<VideoEntry> list) {
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 01", "BdmimLnclUo"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 02", "_HPtn_HUbjU"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 03", "7FtnvUTqg0o"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 04", "Jz6rib7vrKo"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 05", "Irf4COTT180"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 06", "MBu_-FtKLsw"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 07", "WSFjJFTvjp8"));
          list.add(new VideoEntry("Maanam kaththa Madhusudhanan part 08", "S96PjpJs0_g"));

      }


    private PageAdapter adapter;
    private View videoBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
        switch (VideosFragment.selectedLanguage){
            case "kandharalankaaram":
                adapter = new PageAdapter(getActivity(), KANDHARALANKAARAM_LIST);
                break;
            case "soodiKoduththaSudarkodi":
                adapter = new PageAdapter(getActivity(), SOODIKODUTHTHASUDARKODI_LIST);
                break;
            case "akrAcademySchool":
                adapter = new PageAdapter(getActivity(), AKR_ACADEMY_SCHOOL_LIST);
                break;
            case "krishnaavatharam":
                adapter = new PageAdapter(getActivity(), KRISHNAVATHARAM_LIST);
                break;
            case "kandharsastiPerurai":
                adapter = new PageAdapter(getActivity(), KANDHARSASTI_PERURAI_LIST);
                break;
            case "maanamKathaMadhusudhanan":
                adapter = new PageAdapter(getActivity(), MAANAM_KATHA_MADHUSUDHANAN_LIST);
                break;
            case "kanthaPuraanam":
                adapter = new PageAdapter(getActivity(), KANTHAPURAANAM_LIST);
                break;


            default:
                adapter = new PageAdapter(getActivity(), KANDHARALANKAARAM_LIST);

                break;

        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      videoBox = getActivity().findViewById(R.id.video_box);
      getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
      setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

      String videoId = null;
        switch (VideosFragment.selectedLanguage){
            case "kandharalankaaram":
                videoId = KANDHARALANKAARAM_LIST.get(position).videoId;
                break;
            case "soodiKoduththaSudarkodi":
                videoId = SOODIKODUTHTHASUDARKODI_LIST.get(position).videoId;
                break;
            case "akrAcademySchool":
                videoId = AKR_ACADEMY_SCHOOL_LIST.get(position).videoId;
                break;
            case "krishnaavatharam":
                videoId = KRISHNAVATHARAM_LIST.get(position).videoId;
                break;
            case "kandharsastiPerurai":
                videoId = KANDHARSASTI_PERURAI_LIST.get(position).videoId;
                break;
            case "maanamKathaMadhusudhanan":
                videoId = MAANAM_KATHA_MADHUSUDHANAN_LIST.get(position).videoId;
                break;
            case "kanthaPuraanam":
                videoId = KANTHAPURAANAM_LIST.get(position).videoId;
                break;
            default:
                videoId = KANDHARALANKAARAM_LIST.get(position).videoId;
                break;

        }

      VideoFragment videoFragment =
          (VideoFragment) getFragmentManager().findFragmentById(R.id.video_fragment_container);
      videoFragment.setVideoId(videoId);

      // The videoBox is INVISIBLE if no video was previously selected, so we need to show it now.
      if (videoBox.getVisibility() != View.VISIBLE) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
          // Initially translate off the screen so that it can be animated in from below.
          videoBox.setTranslationY(videoBox.getHeight());
        }
        videoBox.setVisibility(View.VISIBLE);
      }

      // If the fragment is off the screen, we animate it in.
      if (videoBox.getTranslationY() > 0) {
        videoBox.animate().translationY(0).setDuration(ANIMATION_DURATION_MILLIS);
      }
    }

    @Override
    public void onDestroyView() {
      super.onDestroyView();

      adapter.releaseLoaders();
    }

    public void setLabelVisibility(boolean visible) {
      adapter.setLabelVisibility(visible);
    }

  }

  /**
   * Adapter for the video list. Manages a set of YouTubeThumbnailViews, including initializing each
   * of them only once and keeping track of the loader of each one. When the ListFragment gets
   * destroyed it releases all the loaders.
   */
  private static final class PageAdapter extends BaseAdapter {

    private final List<VideoEntry> entries;
    private final List<View> entryViews;
    private final Map<YouTubeThumbnailView, YouTubeThumbnailLoader> thumbnailViewToLoaderMap;
    private final LayoutInflater inflater;
    private final ThumbnailListener thumbnailListener;

    private boolean labelsVisible;

    public PageAdapter(Context context, List<VideoEntry> entries) {
      this.entries = entries;

      entryViews = new ArrayList<View>();
      thumbnailViewToLoaderMap = new HashMap<YouTubeThumbnailView, YouTubeThumbnailLoader>();
      inflater = LayoutInflater.from(context);
      thumbnailListener = new ThumbnailListener();

      labelsVisible = true;
    }

    public void releaseLoaders() {
      for (YouTubeThumbnailLoader loader : thumbnailViewToLoaderMap.values()) {
        loader.release();
      }
    }

    public void setLabelVisibility(boolean visible) {
      labelsVisible = visible;
      for (View view : entryViews) {
        view.findViewById(R.id.text).setVisibility(visible ? View.VISIBLE : View.GONE);
      }
    }

    @Override
    public int getCount() {
      return entries.size();
    }

    @Override
    public VideoEntry getItem(int position) {
      return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      VideoEntry entry = entries.get(position);

      // There are three cases here
      if (view == null) {
        // 1) The view has not yet been created - we need to initialize the YouTubeThumbnailView.
        view = inflater.inflate(R.layout.video_list_item, parent, false);
        YouTubeThumbnailView thumbnail = (YouTubeThumbnailView) view.findViewById(R.id.thumbnail);
        thumbnail.setTag(entry.videoId);
        thumbnail.initialize(DeveloperKey.DEVELOPER_KEY, thumbnailListener);
      } else {
        YouTubeThumbnailView thumbnail = (YouTubeThumbnailView) view.findViewById(R.id.thumbnail);
        YouTubeThumbnailLoader loader = thumbnailViewToLoaderMap.get(thumbnail);
        if (loader == null) {
          // 2) The view is already created, and is currently being initialized. We store the
          //    current videoId in the tag.
          thumbnail.setTag(entry.videoId);
        } else {
          // 3) The view is already created and already initialized. Simply set the right videoId
          //    on the loader.
          thumbnail.setImageResource(R.drawable.loading_thumbnail);
          loader.setVideo(entry.videoId);
        }
      }
      TextView label = ((TextView) view.findViewById(R.id.text));
      label.setText(entry.text);
      label.setVisibility(labelsVisible ? View.VISIBLE : View.GONE);
      return view;
    }

    private final class ThumbnailListener implements
        YouTubeThumbnailView.OnInitializedListener,
        YouTubeThumbnailLoader.OnThumbnailLoadedListener {

      @Override
      public void onInitializationSuccess(
          YouTubeThumbnailView view, YouTubeThumbnailLoader loader) {
        loader.setOnThumbnailLoadedListener(this);
        thumbnailViewToLoaderMap.put(view, loader);
        view.setImageResource(R.drawable.loading_thumbnail);
        String videoId = (String) view.getTag();
        loader.setVideo(videoId);
      }

      @Override
      public void onInitializationFailure(
          YouTubeThumbnailView view, YouTubeInitializationResult loader) {
        view.setImageResource(R.drawable.no_thumbnail);
      }

      @Override
      public void onThumbnailLoaded(YouTubeThumbnailView view, String videoId) {
      }

      @Override
      public void onThumbnailError(YouTubeThumbnailView view, ErrorReason errorReason) {
        view.setImageResource(R.drawable.no_thumbnail);
      }
    }

  }

  public static final class VideoFragment extends YouTubePlayerFragment
      implements OnInitializedListener {

    private YouTubePlayer player;
    private String videoId;

    public static VideoFragment newInstance() {
      return new VideoFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      initialize(DeveloperKey.DEVELOPER_KEY, this);
    }

    @Override
    public void onDestroy() {
      if (player != null) {
        player.release();
      }
      super.onDestroy();
    }

    public void setVideoId(String videoId) {
      if (videoId != null && !videoId.equals(this.videoId)) {
        this.videoId = videoId;
        if (player != null) {
          player.cueVideo(videoId);
        }
      }
    }

    public void pause() {
      if (player != null) {
        player.pause();
      }
    }

    @Override
    public void onInitializationSuccess(Provider provider, YouTubePlayer player, boolean restored) {
      this.player = player;
      player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
      player.setOnFullscreenListener((VideoListActivity) getActivity());
      if (!restored && videoId != null) {
        player.cueVideo(videoId);
      }
    }

    @Override
    public void onInitializationFailure(Provider provider, YouTubeInitializationResult result) {
      this.player = null;
    }

  }

  private static final class VideoEntry {
    private final String text;
    private final String videoId;

    public VideoEntry(String text, String videoId) {
      this.text = text;
      this.videoId = videoId;
    }
  }

  // Utility methods for layouting.

  private int dpToPx(int dp) {
    return (int) (dp * getResources().getDisplayMetrics().density + 0.5f);
  }

  private static void setLayoutSize(View view, int width, int height) {
    LayoutParams params = view.getLayoutParams();
    params.width = width;
    params.height = height;
    view.setLayoutParams(params);
  }

  private static void setLayoutSizeAndGravity(View view, int width, int height, int gravity) {
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
    params.width = width;
    params.height = height;
    params.gravity = gravity;
    view.setLayoutParams(params);
  }

}
