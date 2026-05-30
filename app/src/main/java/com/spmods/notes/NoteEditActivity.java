package com.spmods.notes;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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

    private final Handler autoSaveHandler = new Handler(Looper.getMainLooper());
    private Runnable autoSaveRunnable;
    private boolean hasUnsavedChanges = false;
    private static final long AUTO_SAVE_DELAY = 2000; // 2 seconds

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

        // Auto-save TextWatcher
        TextWatcher autoSaveWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                hasUnsavedChanges = true;
                autoSaveHandler.removeCallbacks(autoSaveRunnable);
                autoSaveHandler.postDelayed(autoSaveRunnable, AUTO_SAVE_DELAY);
            }
            @Override public void afterTextChanged(Editable s) {}
        };

        autoSaveRunnable = () -> {
            String title = etTitle.getText().toString().trim();
            String content = etContent.getText().toString().trim();
            if (!title.isEmpty() || !content.isEmpty()) {
                autoSave(title, content);
            }
        };

        etTitle.addTextChangedListener(autoSaveWatcher);
        etContent.addTextChangedListener(autoSaveWatcher);
    }

    private void autoSave(String title, String content) {
        if (noteId == -1) {
            noteId = (int) dbHelper.insertNote(title, content);
            getSupportActionBar().setTitle("Edit Note");
        } else {
            dbHelper.updateNote(noteId, title, content);
        }
        hasUnsavedChanges = false;
        // Subtle auto-save indicator in title
        getSupportActionBar().setSubtitle("Saved");
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getSupportActionBar() != null)
                getSupportActionBar().setSubtitle(null);
        }, 1500);
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
            autoSaveHandler.removeCallbacks(autoSaveRunnable);
            if (hasUnsavedChanges) {
                String title = etTitle.getText().toString().trim();
                String content = etContent.getText().toString().trim();
                if (!title.isEmpty() || !content.isEmpty()) {
                    autoSave(title, content);
                }
            }
            finish();
            return true;
        } else if (id == R.id.action_save) {
            autoSaveHandler.removeCallbacks(autoSaveRunnable);
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
            noteId = (int) dbHelper.insertNote(title, content);
            Toast.makeText(this, "Note saved ✓", Toast.LENGTH_SHORT).show();
        } else {
            dbHelper.updateNote(noteId, title, content);
            Toast.makeText(this, "Note updated ✓", Toast.LENGTH_SHORT).show();
        }
        hasUnsavedChanges = false;
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autoSaveHandler.removeCallbacks(autoSaveRunnable);
    }
}
