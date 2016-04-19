package com.newtra.anatomictherapy.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.newtra.anatomictherapy.Beans.Languages;
import com.newtra.anatomictherapy.R;
import com.newtra.anatomictherapy.activity.VideoListActivity;
import com.newtra.anatomictherapy.adapters.LanguagesAdapter;

import java.util.ArrayList;


public class YoutubeFragment extends Fragment {
    ListView booksListView;
    private ProgressDialog mProgressDialog;
    public static String selectedLanguage;
    public YoutubeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_youtube, container, false);

        populateListView(rootView);

        return rootView;
    }
    ArrayList<Languages> mStoreListNames;

    private void populateListView(View rootView) {
        mStoreListNames= new ArrayList<Languages>();
        mStoreListNames.add(new Languages("tamil","தமிழ்"));//Tamil
        mStoreListNames.add(new Languages("english","English"));//Englidh
        mStoreListNames.add(new Languages("kannada","ಕನ್ನಡ"));//Kannada
        mStoreListNames.add(new Languages("telugu","ತೆಲುಗು"));//TElugu
        mStoreListNames.add(new Languages("chinese","中文"));//chinese
        mStoreListNames.add(new Languages("hindi","हिंदी"));//Hindi
//        mStoreListNames.add(new Languages("malay","Malay"));//Malay

        booksListView =(ListView)rootView.findViewById(R.id.listViewLanguages);
        LanguagesAdapter adapter = new LanguagesAdapter(getActivity(), mStoreListNames);

        // Binds the Adapter to the ListView
        booksListView.setAdapter(adapter);
        /*booksListView.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // GET the clicked book
                //check the folder if book available
                // if available open the book
                //else downlaod and save in the folder

            }
        });*/

        booksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                startDownload(mStoreListNames.get(position).getUrl(),mStoreListNames.get(position).getTitle());
                selectedLanguage=mStoreListNames.get(position).getTitle();
                Log.d("YOUTUBEFRAGMENT","Selected Language "+selectedLanguage);
                Intent intent = new Intent(getActivity(),VideoListActivity.class);
//                intent.putExtra("language",mStoreListNames.get(position).getTitle());

                startActivity(intent);
            }
        });

    }
}
