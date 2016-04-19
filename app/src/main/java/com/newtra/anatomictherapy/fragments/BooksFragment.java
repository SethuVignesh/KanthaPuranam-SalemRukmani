package com.newtra.anatomictherapy.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ListView;

import com.newtra.anatomictherapy.Beans.Book;
import com.newtra.anatomictherapy.R;
import com.newtra.anatomictherapy.adapters.ListViewAdapter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class BooksFragment extends Fragment {
    ListView booksListView;
    private ProgressDialog mProgressDialog;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;


    public BooksFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_books, container, false);

        populateListView(rootView);
        return rootView;
    }
    ArrayList<Book> mStoreListNames;

    private void populateListView(View rootView) {
       mStoreListNames= new ArrayList<Book>();
        mStoreListNames.add(new Book("Anatomic Therapy - Tamil","",R.drawable.at_tamil,"https://newtra.files.wordpress.com/2015/11/126715654-anatomic-therapy-tamil-pdf-book.pdf"));
        mStoreListNames.add(new Book("Anatomic Therapy - Telugu","",R.drawable.at_telugu,"https://newtra.files.wordpress.com/2015/11/anatomic_therapy_telugu2014.pdf"));
        mStoreListNames.add(new Book("Anatomic Therapy - Kannada","",R.drawable.at_kannada,"https://newtra.files.wordpress.com/2015/11/anatomic_therapy_kannada2014.pdf"));
        mStoreListNames.add(new Book("Anatomic Therapy - Hindi","", R.drawable.at_hindi,"https://newtra.files.wordpress.com/2015/11/anatomic_therapy_hindi2015.pdf"));
        mStoreListNames.add(new Book("Anatomic Therapy - Malay","",R.drawable.at_malay,"https://newtra.files.wordpress.com/2015/11/anatomic_therapy_malay.pdf"));
        mStoreListNames.add(new Book("Men are from Mars \n Women are from Venus","",R.drawable.mmwv,"https://newtra.files.wordpress.com/2015/11/men-are-from-mars-women-are-from-venus.pdf"));
        mStoreListNames.add(new Book("Ranga ratina ragasiyam - Tamil","",R.drawable.rrr,"https://newtra.files.wordpress.com/2015/11/rangaratinam-ragasiyam.pdf"));
        mStoreListNames.add(new Book("World Politics - Tamil","",R.drawable.wp,"https://newtra.files.wordpress.com/2015/11/world-politics-peace-o-master.pdf"));


        booksListView =(ListView)rootView.findViewById(R.id.listViewBooks);
        ListViewAdapter adapter = new ListViewAdapter(getActivity(), mStoreListNames);

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
                startDownload(mStoreListNames.get(position).getUrl(),mStoreListNames.get(position).getTitle());
            }
        });

    }
    private void startDownload(String url,String title) {
//        String url = "http://farm1.static.flickr.com/114/298125983_0e4bf66782_b.jpg";
        new DownloadFileAsync().execute(url, title);
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Downloading file..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

        }
        File file;
        @Override
        protected String doInBackground(String... aurl) {
            int count;


            try {
                // Find the SD Card path
                File filepath = Environment.getExternalStorageDirectory();

                // Create a new folder in SD Card
                File dir = new File(filepath.getAbsolutePath()
                        + "/Save Image Tutorial/");
                dir.mkdirs();

                // Create a name for the saved image
                file = new File(dir, aurl[1]);
                if(checkExists(dir.toString(),aurl[1])){

                    }
                else{
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());



                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();}
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC", progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));

        }

        @Override
        protected void onPostExecute(String unused) {
//            getActivity().dismissDialog(DIALOG_DOWNLOAD_PROGRESS);
            if( mProgressDialog.isShowing()){
                mProgressDialog.dismiss();
            }
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(".PDF");

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            startActivity(intent);

        }
    }
    public static boolean  checkExists(String directory, String file) {
        String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        path=directory.toString();
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File files[] = f.listFiles();
        Log.d("Files", "Size: " + files.length);
        for (int i=0; i < files.length; i++)
        {
            if(files[i].getName().equals(file)){
            return true;
        }

        }

        return false;
}}
