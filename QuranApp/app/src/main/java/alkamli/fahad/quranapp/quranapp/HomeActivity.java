package alkamli.fahad.quranapp.quranapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import alkamli.fahad.quranapp.quranapp.adapter.HomeAdapter;


public class HomeActivity extends AppCompatActivity {

    RecyclerView list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        list=(RecyclerView) findViewById(R.id.list);
        list.setAdapter(new HomeAdapter(this, CommonFunctions.getSourahList(this)));
        list.setLayoutManager(new LinearLayoutManager(this));
        //Delete files if the option has been selected
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                CommonFunctions.checkForDeleteFilesOption(getApplicationContext());
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();

            inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        try {
            switch (item.getItemId())
            {

                case R.id.settings:
                {
                    Intent i = new Intent(getApplicationContext(), UserSettingsActivity.class);
                    startActivity(i);
                    //overridePendingTransition(R.anim.fadeout, R.anim.fadein);
                    return true;
                }
                case R.id.downloadAll:
                {

                    return true;
                }

            }
        }catch(Exception e)
        {
            class Local {};
            Log.e(CommonFunctions.TAG, ("MethodName: " + Local.class.getEnclosingMethod().getName() + " || ErrorMessage: " + e.getMessage()));
        }


        return false;
    }

    private void downloadAllFiles()
    {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(new Runnable(){
            @Override
            public void run()
            {
                CommonFunctions.checkForDeleteFilesOption(getApplicationContext());
            }
        }).start();
    }
}
