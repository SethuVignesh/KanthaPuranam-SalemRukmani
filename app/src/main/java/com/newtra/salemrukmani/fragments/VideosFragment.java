package com.newtra.salemrukmani.fragments;

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

import com.newtra.salemrukmani.Beans.Languages;
import com.newtra.salemrukmani.R;
import com.newtra.salemrukmani.activity.VideoListActivity;
import com.newtra.salemrukmani.adapters.LanguagesAdapter;

import java.util.ArrayList;


public class VideosFragment extends Fragment {
    ListView booksListView;
    private ProgressDialog mProgressDialog;
    public static String selectedLanguage;
    public VideosFragment() {
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
        mStoreListNames.add(new Languages("kandharalankaaram","கந்தர் அலங்காரம்   "));//Tamil
        mStoreListNames.add(new Languages("soodiKoduththaSudarkodi","சூடி கொடுத்த சுடர்கொடி"));//Englidh
        mStoreListNames.add(new Languages("akrAcademySchool","AKR Academy School"));//Kannada
        mStoreListNames.add(new Languages("kanthaPuraanam"," கந்த  புராணம் "));//TElugu
        mStoreListNames.add(new Languages("kandharsastiPerurai","கந்தர் சஷ்டி பேருரை "));//chinese
        mStoreListNames.add(new Languages("krishnaavatharam","கிருஷ்ணா அவதாரம் "));//Hindi
        mStoreListNames.add(new Languages("maanamKathaMadhusudhanan","மானம் காத்த மதுசூதனன் "));//Malay

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
