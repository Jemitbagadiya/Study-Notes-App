package com.example.studynotes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class NotesAdapter extends
        RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    Context context;

    ArrayList<NoteModel> list;

    public NotesAdapter(Context context,
                        ArrayList<NoteModel> list) {

        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_note,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        NoteModel model = list.get(position);

        holder.txtTopic.setText(
                model.getTopic()
        );

        holder.txtPreview.setText(
                model.getContent()
        );

        holder.txtDate.setText(
                model.getDate()
        );

        // Open Notes
        holder.cardNote.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            context,
                            ResultActivity.class
                    );

            intent.putExtra(
                    "result",
                    model.getContent()
            );

            context.startActivity(intent);
        });

        // Delete Notes
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Note")
                    .setMessage("Are you sure you want to delete this note?")
                    .setPositiveButton("Delete", (dialog, which) -> {

                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(
                                        FirebaseAuth.getInstance()
                                                .getCurrentUser()
                                                .getUid()
                                )
                                .collection("notes")
                                .document(model.getId())
                                .delete();

                        int adapterPosition =
                                holder.getBindingAdapterPosition();

                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            list.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);
                            notifyItemRangeChanged(
                                    adapterPosition,
                                    list.size()
                            );
                        }

                        Toast.makeText(
                                context,
                                "Note Deleted",
                                Toast.LENGTH_SHORT
                        ).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {

        return list.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView txtTopic,
                txtPreview,
                txtDate;

        ImageView btnDelete;

        CardView cardNote;

        public ViewHolder(
                @NonNull View itemView) {

            super(itemView);

            txtTopic =
                    itemView.findViewById(
                            R.id.txtTopic
                    );

            txtPreview =
                    itemView.findViewById(
                            R.id.txtPreview
                    );

            txtDate =
                    itemView.findViewById(
                            R.id.txtDate
                    );

            btnDelete =
                    itemView.findViewById(
                            R.id.btnDelete
                    );

            cardNote =
                    (CardView) itemView;
        }
    }
}