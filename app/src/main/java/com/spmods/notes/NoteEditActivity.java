package com.spmods.notes;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NoteEditActivity extends AppCompatActivity {

    private EditText etTitle, etContent;
    private NotesDatabaseHelper dbHelper;
    private int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        dbHelper = new NotesDatabaseHelper(this);

        noteId = getIntent().getIntExtra("note_id", -1);
        if (noteId != -1) {
            getSupportActionBar().setTitle("Edit Note");
            Note note = dbHelper.getNoteById(noteId);
            if (note != null) {
                etTitle.setText(note.getTitle());
                etContent.setText(note.getContent());
            }
        } else {
            getSupportActionBar().setTitle("New Note");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_save) {
            saveNote();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty() && content.isEmpty()) {
            Toast.makeText(this, "Note is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteId == -1) {
            dbHelper.insertNote(title, content);
            Toast.makeText(this, "Note saved ✓", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateNote(noteId, title, content);
            Toast.makeText(this, "Note updated ✓", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
