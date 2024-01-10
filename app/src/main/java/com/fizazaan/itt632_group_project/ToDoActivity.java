package com.fizazaan.itt632_group_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ToDoActivity extends AppCompatActivity {

    public static final String ARTIST_NAME = "artistname";
    public static final String ARTIST_ID = "artistid";

   EditText editTextName;
   Button buttonAdd;
   Spinner spinnerGenres;
    DatabaseReference databaseArtists;
    List<Artist> artistList;
    ListView listViewArtists;

    FloatingActionButton fabViewArtists;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        FirebaseApp.initializeApp(this);

        //Create database reference
        databaseArtists = FirebaseDatabase.getInstance().getReference("artists");

        //get values from XML
        editTextName = (EditText) findViewById(R.id.editTextName);
        buttonAdd = (Button) findViewById(R.id.buttonAddArtist);
        spinnerGenres = (Spinner) findViewById(R.id.spinnerGenres);

        listViewArtists = (ListView) findViewById(R.id.ListViewArtists);
        artistList = new ArrayList<>();


        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addArtist();
            } });

        listViewArtists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Artist artist = artistList.get(i);

                Intent intent = new Intent(getApplicationContext(),AddTrackActivity.class);
                intent.putExtra(ARTIST_ID, artist.getArtistId());
                intent.putExtra(ARTIST_NAME, artist.getArtistName());

                startActivity(intent);
            }
        });

       listViewArtists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
           @Override
           public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
               Artist artist = artistList.get(i);

               showUpdateDialog(artist.getArtistId(), artist.getArtistName());
               return false;
           }
       });

        fabViewArtists = findViewById(R.id.fabViewArtists);
        fabViewArtists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click event to open ActivityViewArtist
                Intent intent = new Intent(ToDoActivity.this, ActivityViewArtist.class);
                startActivity(intent);
            }
        });

    }


    public void logout (View view){
        Intent intent = new Intent (this, LoginActivity.class);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logout Successfully!", Toast.LENGTH_LONG).show();
    }
    private void showUpdateDialog(String artistId, String artistName){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog,null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdate);
        final Spinner spinnerGenres = (Spinner)dialogView.findViewById(R.id.spinnerGenres);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDelete);

        dialogBuilder.setTitle("Updating Artist's Name : " +artistName);

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                String name = editTextName.getText().toString().trim();
                String genre = spinnerGenres.getSelectedItem().toString();

                if(TextUtils.isEmpty(name)){
                    editTextName.setError ("Name required");
                    return;
                }
                updateArtist(artistId,name, genre);

                alertDialog.dismiss();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteArtist(artistId);
            }
        });
    }



    private boolean updateArtist(String id, String name, String genre) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("artists").child(id);
        Artist artist = new Artist(id,name,genre);
        databaseReference.setValue(artist);

        Toast.makeText(this, "Artist Updated Successfully", Toast.LENGTH_LONG).show();
        return true;
    }

    private void deleteArtist(String artistId){
        DatabaseReference drArtist = FirebaseDatabase.getInstance().getReference("artists").child(artistId);
        DatabaseReference drTrack = FirebaseDatabase.getInstance().getReference("tracks").child(artistId);

        drArtist.removeValue();
        drTrack.removeValue();

        Toast.makeText(this, "Artist has been deleted", Toast.LENGTH_LONG).show();
    }

    private void addArtist() {

        String name = editTextName.getText().toString().trim();
        String genre = spinnerGenres.getSelectedItem().toString();

        if (!TextUtils.isEmpty(name)) {
            String id = databaseArtists.push().getKey();

            Artist artist = new Artist(id, name, genre);

            databaseArtists.child(id).setValue(artist);

            Toast.makeText(this, "Artist added", Toast.LENGTH_LONG).show();

        } else {
            Toast.makeText(this, "Please Enter Artist Name & Song", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //attaching value event listener
        databaseArtists.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                artistList.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Artist artist = postSnapshot.getValue(Artist.class);
                    artistList.add(artist);
                }

                ArtistList artistAdapter = new ArtistList(ToDoActivity.this, artistList);

                listViewArtists.setAdapter(artistAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
