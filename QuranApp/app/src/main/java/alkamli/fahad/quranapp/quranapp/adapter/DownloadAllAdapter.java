package alkamli.fahad.quranapp.quranapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import alkamli.fahad.quranapp.quranapp.CommonFunctions;
import alkamli.fahad.quranapp.quranapp.DownloadAllQuranActivity;
import alkamli.fahad.quranapp.quranapp.PlayerActivity;
import alkamli.fahad.quranapp.quranapp.PlayerActivity2;
import alkamli.fahad.quranapp.quranapp.R;
import alkamli.fahad.quranapp.quranapp.entity.SurahItem;

import static alkamli.fahad.quranapp.quranapp.CommonFunctions.TAG;
import static alkamli.fahad.quranapp.quranapp.CommonFunctions.getFreeSpace;
import static alkamli.fahad.quranapp.quranapp.CommonFunctions.validateFileSize;


public class DownloadAllAdapter extends  RecyclerView.Adapter<DownloadAllAdapter.MyViewHolder>{

     private static ArrayList<SurahItem> SourahList;
    private Activity activity;
    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
       public ProgressBar progressBar;
        public View view;

        public MyViewHolder(View view)
        {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            progressBar=(ProgressBar)  view.findViewById(R.id.progressBar);
            this.view=view;
        }
    }


    public DownloadAllAdapter(Activity activity,ArrayList<SurahItem> SourahList) {
        this.SourahList=SourahList;
        this.activity=activity;
    }

    @Override
    public DownloadAllAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View CustomView = inflater.inflate(R.layout.download_item, parent, false);
        return new MyViewHolder(CustomView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        if(this.SourahList==null)
        {
            return;
        }
        Log.d(TAG,""+position);
        String sourahTitle=SourahList.get(position).getTitle();

        final SurahItem item=SourahList.get(position);
        final String order=SourahList.get(position).getOrder();
        holder.title.setText(sourahTitle);
        holder.view.setTag(SourahList.get(position));
        holder.view.setClickable(true);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show an option to pause or cancel the download
                //change the view tag state
                SurahItem file=(SurahItem) view.getTag();
                if(file.isDownloadState())
                {
                    file.setDownloadState(false);
                }else{
                    file.setDownloadState(true);
                }

            }
        });

        //we start the download here but we need to keep track the state. meaning if the user clicked paused we need that function to pause
        //
        new Thread(new Runnable(){
            @Override
            public void run() {

                downloadFile(item,holder.progressBar);
            }
        }).start();


    }
    @Override
    public int getItemCount() {
        return SourahList.size();
    }



    private void downloadFile(SurahItem item,final ProgressBar progressBar)
    {

        //http://server6.mp3quran.net/thubti/001.mp3
        final String file=item.getOrder()+".mp3";
        int count;
        try {
            //Check for available internet connection first
            if (!CommonFunctions.isNetworkAvailable(activity)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(), R.string.internet_connection_error, Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                });
            }

            //let's check for internet connection first

            //Hide the content and show the progress bar
            if(CommonFunctions.getQueue().contains(file))
            {
                //file is being downloaded by other process
                return;
            }
            {
                File file2 = new File(activity.getApplicationInfo().dataDir + "/" + file);
                // Log.e(TAG,getApplicationInfo().dataDir+"/"+order+".mp3");
                //The file already exists then we should change the progress to 100%
                if (file2.exists()) {
                    if (progressBar != null)
                    {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                                progressBar.setMax(100);
                                progressBar.setProgress(100);


                        }
                    });
                    }
                    return;
                }
            }
            Looper.prepare();
            URL url1 = new URL(activity.getString(R.string.files_url) + file);
            //  Log.e(TAG,"http://server6.mp3quran.net/thubti/"+file);
            URLConnection conexion = url1.openConnection();
            conexion.connect();
            final int lenghtOfFile = conexion.getContentLength();
            if (lenghtOfFile != -1 && lenghtOfFile > 0)
            {
                if (getFreeSpace() < lenghtOfFile) {
                    //Notify the user that the free space is not enough for the file
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), R.string.not_enough_space, Toast.LENGTH_LONG).show();
                           // activity.finish();

                        }
                    });
                    return;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setMax(lenghtOfFile);
                    }
                });
            }
            //put the file in the queue
            CommonFunctions.putInQueue(file);
            InputStream input = new BufferedInputStream(url1.openStream());
            FileOutputStream output = new FileOutputStream(activity.getApplicationInfo().dataDir + "/" + file);
            java.nio.channels.FileLock lock = output.getChannel().lock();
            byte data[] = new byte[1024];
            long total = 0;
            System.out.println("downloading.............");

            Log.e(TAG, "File Total length: " + lenghtOfFile);
            while (CommonFunctions.isNetworkAvailable(activity.getApplicationContext()) && (count = input.read(data)) != -1)
            {
                total += count;
                output.write(data, 0, count);
                final long temp = total;
                if (progressBar != null)
                {
                    if((progressBar.getProgress()*3)<((int) temp))
                    {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(((int) temp));

                            }
                        });
                    }

                }
                //Log.d(TAG,Long.toString(temp));
                //Here we will detect if the user has clicked on pause on the download
                    synchronized (this)
                    {
                        int countPauses=0;
                        while(!item.isDownloadState())
                        //we pause
                        try{
                            countPauses+=1;
                            Log.d(TAG,"we pause");
                            wait(10000);
                            if(countPauses==3)
                            {
                                File file2 = new File(activity.getApplicationInfo().dataDir + "/" + file);
                                if (file2.exists())
                                {
                                    file2.delete();
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(activity.getApplicationContext(), R.string.file_is_corrupt, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    // activity.finish();
                                }

                                CommonFunctions.removeFromQueue(file);
                                return;
                            }

                        }catch(Exception e)
                        {
                            Log.e(TAG,e.getMessage());
                        }
                    }

            }
            Log.e(TAG, "Done");
            output.flush();
            //Release the lock on the file so others can use it
            lock.release();
            output.close();
            input.close();

            //Here i will try to find the file and check it's size to make sure it's not corrupt
            //I can check the size only if the server respond with the expected size
            if (lenghtOfFile != -1 && !validateFileSize(lenghtOfFile, file,activity))
            {
                Log.e(TAG, "File is corrupt , deleting the file");
                //Delete the file
                File file2 = new File(activity.getApplicationInfo().dataDir + "/" + file);
                if (file2.exists())
                {
                    file2.delete();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), R.string.file_is_corrupt, Toast.LENGTH_SHORT).show();
                        }
                    });
                   // activity.finish();
                }
            } else {
                Log.e(TAG, "File has been downloaded secessfuly");

            }

        }catch(Exception e)
        {

//            Log.e(TAG,e.getMessage());
            Log.e(TAG, "File is corrupt , deleting the file");
            //Delete the file
            File file2 = new File(activity.getApplicationInfo().dataDir + "/" + file);
            if (file2.exists())
            {
                file2.delete();
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity.getApplicationContext(), R.string.file_is_corrupt, Toast.LENGTH_SHORT).show();
                    }
                });
                //activity.finish();
            }
            //remove it from the queue first
            CommonFunctions.removeFromQueue(file);
        }

    }



}

