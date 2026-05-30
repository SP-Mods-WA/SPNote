package com.spmods.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private NotesDatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new NotesDatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        FloatingActionButton fab = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteEditActivity.class);
            startActivity(intent);
        });

        loadNotes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        List<Note> notes = dbHelper.getAllNotes();

        if (notes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        }

        if (adapter == null) {
            adapter = new NotesAdapter(notes, new NotesAdapter.OnNoteClickListener() {
                @Override
                public void onNoteClick(Note note) {
                    Intent intent = new Intent(MainActivity.this, NoteEditActivity.class);
                    intent.putExtra("note_id", note.getId());
                    startActivity(intent);
                }

                @Override
                public void onNoteLongClick(Note note, int position) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Note")
                            .setMessage("Delete \"" + note.getTitle() + "\"?")
                            .setPositiveButton("Delete", (dialog, which) -> {
                                dbHelper.deleteNote(note.getId());
                                loadNotes();
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateNotes(notes);
        }
    }
}
