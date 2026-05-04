package com.example.studynotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotesFragment extends Fragment {

    RecyclerView recyclerNotes;
    ArrayList<NoteModel> notesList;
    NotesAdapter adapter;

    public NotesFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_notes,
                        container,
                        false
                );

        recyclerNotes =
                view.findViewById(R.id.recyclerNotes);

        recyclerNotes.setLayoutManager(
                new LinearLayoutManager(getContext())
        );

        notesList = new ArrayList<>();

        adapter =
                new NotesAdapter(
                        getContext(),
                        notesList
                );

        recyclerNotes.setAdapter(adapter);

        loadNotes();

        return view;
    }

    private void loadNotes() {

        String userId =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("notes")
                .orderBy(
                        "timestamp",
                        com.google.firebase.firestore.Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    notesList.clear();

                    for (com.google.firebase.firestore.DocumentSnapshot document :
                            queryDocumentSnapshots.getDocuments()) {

                        String id =
                                document.getId();

                        String topic =
                                document.getString("topic");

                        String content =
                                document.getString("content");

                        String date =
                                document.getString("date");

                        notesList.add(
                                new NoteModel(
                                        id,
                                        topic,
                                        content,
                                        date
                                )
                        );
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}