package com.spmods.notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotesAdapter adapter;
    private NotesDatabaseHelper dbHelper;
    private TextView tvEmpty;
    private EditText etSearch;
    private ImageView ivSearchClear;
    private List<Note> allNotes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new NotesDatabaseHelper(this);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        etSearch = findViewById(R.id.etSearch);
        ivSearchClear = findViewById(R.id.ivSearchClear);
        FloatingActionButton fab = findViewById(R.id.fab);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, NoteEditActivity.class);
            startActivity(intent);
        });

        // Search text watcher
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotes(s.toString());
                ivSearchClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // × button clears search
        ivSearchClear.setOnClickListener(v -> clearSearch());

        // Back press — if search is active, clear it; otherwise normal back
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSearchActive()) {
                    clearSearch();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        loadNotes();
    }

    private boolean isSearchActive() {
        return etSearch.isFocused() || !etSearch.getText().toString().isEmpty();
    }

    private void clearSearch() {
        etSearch.setText("");
        etSearch.clearFocus();
        ivSearchClear.setVisibility(View.GONE);
        hideKeyboard();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void filterNotes(String query) {
        List<Note> filtered = new ArrayList<>();
        String q = query.toLowerCase().trim();
        for (Note note : allNotes) {
            if (note.getTitle().toLowerCase().contains(q) ||
                note.getContent().toLowerCase().contains(q)) {
                filtered.add(note);
            }
        }
        updateRecycler(filtered);
    }

    private void loadNotes() {
        allNotes = dbHelper.getAllNotes();
        String currentQuery = etSearch != null ? etSearch.getText().toString() : "";
        if (currentQuery.isEmpty()) {
            updateRecycler(allNotes);
        } else {
            filterNotes(currentQuery);
        }
    }

    private void updateRecycler(List<Note> notes) {
        if (notes.isEmpty() && (etSearch == null || etSearch.getText().toString().isEmpty())) {
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
