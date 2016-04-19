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
package com.newtra.anatomictherapy.activity;

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
import com.newtra.anatomictherapy.constants.DeveloperKey;
import com.newtra.anatomictherapy.fragments.YoutubeFragment;
import com.newtra.anatomictherapy.R;

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

    private static final  List<VideoEntry> TAMIL_VIDEO_LIST;
      private static final  List<VideoEntry> TELUGU_VIDEO_LIST;
      private static final  List<VideoEntry> KANNADA_VIDEO_LIST;
      private static final  List<VideoEntry> HINDI_VIDEO_LIST;
      private static final  List<VideoEntry> CHINESE_VIDEO_LIST;
      private static final  List<VideoEntry> MALAYALAM_VIDEO_LIST;
      private static final  List<VideoEntry> ENGLISH_VIDEO_LIST;

    static {
      List<VideoEntry> tamil_list = new ArrayList<VideoEntry>();
        List<VideoEntry> english_list = new ArrayList<VideoEntry>();
        List<VideoEntry> hindi_list = new ArrayList<VideoEntry>();
        List<VideoEntry> kannada_list = new ArrayList<VideoEntry>();
        List<VideoEntry> telugu_list = new ArrayList<VideoEntry>();
        List<VideoEntry> malayalam_list = new ArrayList<VideoEntry>();
        List<VideoEntry> chinese_list = new ArrayList<VideoEntry>();

        tamilVideos(tamil_list);
        teluguVideos(telugu_list);
        kannadaVideos(kannada_list);
        malayalamVideos(malayalam_list);
        chineseVideos(chinese_list);
        englishVideos(english_list);
        hindiVideos(hindi_list);


      TAMIL_VIDEO_LIST = Collections.unmodifiableList(tamil_list);
        ENGLISH_VIDEO_LIST = Collections.unmodifiableList(english_list);
        HINDI_VIDEO_LIST = Collections.unmodifiableList(hindi_list);
        KANNADA_VIDEO_LIST = Collections.unmodifiableList(kannada_list);
        TELUGU_VIDEO_LIST = Collections.unmodifiableList(telugu_list);
        MALAYALAM_VIDEO_LIST = Collections.unmodifiableList(malayalam_list);
        CHINESE_VIDEO_LIST = Collections.unmodifiableList(chinese_list);

    }

       private static void teluguVideos(List<VideoEntry> list) {
           list.add(new VideoEntry("Anatomic Therapy(Healer's Baskar) - Telugu Part 1- 2012- 1/5", "5hA8gOi-NsY"));
           list.add(new VideoEntry("Anatomic Therapy(Healer's Baskar) - Telugu Part 1- 2012- 2/5", "NPCkwiNr6Q0"));
           list.add(new VideoEntry("Anatomic Therapy (Healer's Baskar) Telugu Part1 - 2012 - 3/5", "t2435AVmYIo"));
           list.add(new VideoEntry("Anatomic Therapy (Healer's Baskar) Telugu Part1 - 2012 -4/5", "yTgoCxdy6b4"));
           list.add(new VideoEntry("Anatomic Therapy (Healer's Baskar) Telugu - part-1 - 2012 - 5/5", "KkHMX3b0feE"));
           list.add(new VideoEntry("Anatomic Therapy (Healer's Baskar) Telugu Part2 - 2012 - 1/4", "qUXmxNwtOP4"));
           list.add(new VideoEntry("Anatomic Therapy ( Healer's Baskar) Telugu Part-2 (2012) 2/4", "taNo2XC9Sew"));
           list.add(new VideoEntry("Anatomic Therapy ( Healer's Baskar) Telugu Part-2 (2012) 3/4", "HgdkpyLAc3A"));
           list.add(new VideoEntry("Anatomic Therapy (Healer's Baskar) Telugu Part - 2 (2012) 4/4", "_RiHGy9ihmY"));

    }
       private static void malayVideos(List<VideoEntry> list) {

           list.add(new VideoEntry("Anatomic Therapy Kannada Video Part 1 Healer Baskar", "rXGVOUVDGJg"));
           list.add(new VideoEntry("Anatomic Therapy kannada Video Part-2", "dlrtSNAId44"));
           list.add(new VideoEntry("Healer Baskar ANATOMIC THERAPY THE ART OF SELF TREATMENT (KANNADAM)BY Mr.SUKUMAR (Peace O Master)", "ghnxIIrh_vw"));


       }  private static void kannadaVideos(List<VideoEntry> list) {
          list.add(new VideoEntry("Anatomic Therapy Kannada Video Part 1 Healer Baskar", "rXGVOUVDGJg"));
          list.add(new VideoEntry("Anatomic Therapy kannada Video Part-2", "dlrtSNAId44"));
          list.add(new VideoEntry("Healer Baskar ANATOMIC THERAPY THE ART OF SELF TREATMENT (KANNADAM)BY Mr.SUKUMAR (Peace O Master)", "ghnxIIrh_vw"));


    }  private static void malayalamVideos(List<VideoEntry> list) {
          list.add(new VideoEntry("Malayalam(part 1) - Anatomic Therapy Foundation", "oK3H-7InwrM"));
          list.add(new VideoEntry("Malayalam(part 2) - Anatomic Therapy Foundation", "Z24u080JPfo"));
    }  private static void hindiVideos(List<VideoEntry> list) {
          list.add(new VideoEntry("Anatomic Therapy Hindi Video Part-1", "86YmRlQFb2o"));
          list.add(new VideoEntry("Anatomic Therapy Hindi video Part-2", "YVs3_RvVDZQ"));


      }  private static void chineseVideos(List<VideoEntry> list) {
          list.add(new VideoEntry("Anatomic Therapy Chinese Language Healer Baskar", "0aIk9my0RM0"));


      }
       private static void englishVideos(List<VideoEntry> list) {
           list.add(new VideoEntry("Anatomic Therapy English Video Part-1", "elmawuxqcYs"));
           list.add(new VideoEntry("Anatomic Therapy English Video Part-2", "ZQ9UgINV678"));

    }
       private static void tamilVideos(List<VideoEntry> list) {
           list.add(new VideoEntry("The leading tamil channal - hr baskar interview", "aI5BabaKdEk"));
           list.add(new VideoEntry("Anatomic Therapy Video(2013) - Part 1", "hDHSb0w-Ce4"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 2", "cJh5DuY37Cs"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 3", "kbGcALc6yiA"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 4", "rI7uS9iYNDs"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - part 5", "sZMzGXduQ3Q"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 6", "2oU4mW4Sh6g"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 7", "PgTnG-ZlHtw"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 8", "DOay45idneU"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 9", "4YMzKrvxzI0"));
           list.add(new VideoEntry("Anatomic Therapy Tamil Video(2013) - Part 10", "I88_7nq6qTI"));

           list.add(new VideoEntry("Live for mind (மனதிற்காக வாழுங்கள்) - 2015", "6OEpMmuDAY0"));
           list.add(new VideoEntry("Ranga Ratina Ragasiyam (ரங்க ராட்டின ரகசியம்) - 2015", "VAdNLIc5gTM"));

           list.add(new VideoEntry("Astrology (ஜோதிடம் - ஜாதகம்) - 2015", "3kXsWzeaQWM"));


           list.add(new VideoEntry("வள்ளலாரின் நீர் சிகிச்சை", "Ph8TwURipLE"));
      list.add(new VideoEntry("கண்ணடிக்கலாம் வாங்க", "krM6XPins0I"));
      list.add(new VideoEntry("தூக்கமா? யோகவா?", "DWG12QpJTlw"));
      list.add(new VideoEntry("ஓசி விபாசனா", "T3LduBSyyWw"));
      list.add(new VideoEntry("ஒரு குடி நாலு முழுங்கு", "IqUqk76_-6o"));
      list.add(new VideoEntry("ஒரு ஓய்வே ஓய்வெடுக்கிறதே!", "HiL5WfJUU0A"));
      list.add(new VideoEntry("சீட் பெல்ட்", "NKqjWBqNmXE"));
      list.add(new VideoEntry("ஓசோன் குளியல்", "ymm1k0KPqwM"));

      list.add(new VideoEntry("சர்க்கரை வில்வம்", "-KEKUf2TX00"));
      list.add(new VideoEntry("வெஜ் சூப்", "tYEshTY2Qps"));
      list.add(new VideoEntry("நீங்களும் வாதாடலாம்", "xIzJbnrtd-A"));
      list.add(new VideoEntry("ஒரிஜினல் தைலம்", "ri4MVU5dFYI"));
      list.add(new VideoEntry("ஆரோக்கியம் வேண்டுமா ஃபுல்லா குடிங்க !!", "IY9qJFrrcdg"));
      list.add(new VideoEntry("பஞ்சாயத்து", "IXAyhE4C22Q"));
      list.add(new VideoEntry("நல்ல அரிசி", "D1-rvUXImmg"));
      list.add(new VideoEntry("ஆரோக்கியமும் PH மதிப்பும்", "poKlFkux9ao"));

      list.add(new VideoEntry("ஜனனி", "5-Smdukm06Y"));
      list.add(new VideoEntry("உயிரே உயிரே வந்து என்னோடு கலந்துவிடு", "YawaOu2-HmM"));
      list.add(new VideoEntry("ராமர் பிள்ளை", "2TbUv3uxuDo"));
      list.add(new VideoEntry("புண்ணியம் வேண்டுமா?", "We8pJl2j0v4"));
      list.add(new VideoEntry("ஜர்கண்டி  ஜர்கண்டி ", "ipY-EvPIkS8"));
      list.add(new VideoEntry("காதுக்கென்ன பூட்டு", "9nKTp0hp8D4"));
      list.add(new VideoEntry("பிடித்ததை பிடி", "lfyBxroK4Sw"));
      list.add(new VideoEntry("காத்து! கணடு!! கல!!!", "kSQJ86_TUKg"));

      list.add(new VideoEntry("மொட்டை மாடிக்கு பச்சை தொப்பி ", "HgawOjsgQho"));

      list.add(new VideoEntry("அவள் இல்லாத போது, அவல் இருந்தால்! எவலும் தேவை இல்லை", "n8pItomr-HQ"));
      list.add(new VideoEntry("பஹரைனில் நடந்த உண்மை", "aeJB3AfR110"));
      list.add(new VideoEntry("நிம்மதி வந்தால் காய்ச்சல்? வருமா?", "6q2Nv2Zzv0k"));
      list.add(new VideoEntry("பைபிளில் அனாடமிகக் செவிவழி தொடுசிகிச்சை", "o5M3pBKJTh0"));
      list.add(new VideoEntry("Thairiya Kolaaru", "XJHHdwwOuhM"));
      list.add(new VideoEntry("Thevai Vanthaal Theriya Varum", "1NjK3JDgQos"));
      list.add(new VideoEntry("Yenga ooru Vandi", "tYEi-zb4Ywo"));
      list.add(new VideoEntry("Neengalum Eluthalaam", "DdZtzzS3UMA"));
      list.add(new VideoEntry("Vivasaaya Call centre", "O1hpvDQsfS4"));
      list.add(new VideoEntry("Ore naalil siruneeraga kallai karikkum Beans Vaithiyam", "peuSjBt3rhw"));
      list.add(new VideoEntry("Kotta Paakkum Kolunthu Vethalayum Vottal Kulanthai Pirakkum", "K3cMKTuqg4A"));
      list.add(new VideoEntry("Josiyargal jakirathai.", "D3rg1gnr7KA"));
      list.add(new VideoEntry("சத்தம் இல்லாத தனிமை கேட்டேன்", "MHohPH_stiU"));
      list.add(new VideoEntry("நீர் பிராணன்", "VQcKEhjqPV8"));
      list.add(new VideoEntry("anjarai பெட்டி", "0QApy96A8gk"));
      list.add(new VideoEntry("டுவா தூக்கம்", "rPP09j95CV4"));
      list.add(new VideoEntry("இயற்கை விவசாய காலனி", "a2urFyOYiWI"));
    }

    private PageAdapter adapter;
    private View videoBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
        switch (YoutubeFragment.selectedLanguage){
            case "tamil":
                adapter = new PageAdapter(getActivity(), TAMIL_VIDEO_LIST);
                break;
            case "telugu":
                adapter = new PageAdapter(getActivity(), TELUGU_VIDEO_LIST);
                break;
            case "kannada":
                adapter = new PageAdapter(getActivity(), KANNADA_VIDEO_LIST);
                break;
            case "malayalam":
                adapter = new PageAdapter(getActivity(), MALAYALAM_VIDEO_LIST);
                break;
            case "chinese":
                adapter = new PageAdapter(getActivity(), CHINESE_VIDEO_LIST);
                break;
            case "english":
                adapter = new PageAdapter(getActivity(), ENGLISH_VIDEO_LIST);
                break;
            case "hindi":
                adapter = new PageAdapter(getActivity(), HINDI_VIDEO_LIST);
                break;


            default:
                adapter = new PageAdapter(getActivity(), TAMIL_VIDEO_LIST);

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
        switch (YoutubeFragment.selectedLanguage){
            case "tamil":
                videoId = TAMIL_VIDEO_LIST.get(position).videoId;
                break;
            case "telugu":
                videoId = TELUGU_VIDEO_LIST.get(position).videoId;
                break;
            case "kannada":
                videoId = KANNADA_VIDEO_LIST.get(position).videoId;
                break;
            case "malayalam":
                videoId = MALAYALAM_VIDEO_LIST.get(position).videoId;
                break;
            case "chinese":
                videoId = CHINESE_VIDEO_LIST.get(position).videoId;
                break;
            case "english":
                videoId = ENGLISH_VIDEO_LIST.get(position).videoId;
                break;
            case "hindi":
                videoId = HINDI_VIDEO_LIST.get(position).videoId;
                break;
            default:
                videoId = TAMIL_VIDEO_LIST.get(position).videoId;
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
