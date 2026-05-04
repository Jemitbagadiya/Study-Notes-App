package com.example.studynotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    public static ArrayList<NoteModel> savedNotes =
            new ArrayList<>();

    EditText etTopic;
    MaterialButton btnGenerate;

    String API_KEY = "YOUR_OPENROUTER_API_KEY"; //API Key OpenRouter

    public HomeFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View view =
                inflater.inflate(
                        R.layout.fragment_home,
                        container,
                        false
                );

        etTopic =
                view.findViewById(R.id.etTopic);

        btnGenerate =
                view.findViewById(R.id.btnGenerate);

        btnGenerate.setOnClickListener(v ->
                generateNotes());

        View scrollView =
                view.findViewById(R.id.scrollView);

        scrollView.setOnTouchListener((v, event) -> {

            InputMethodManager imm =
                    (InputMethodManager)
                            requireActivity()
                                    .getSystemService(
                                            android.content.Context.INPUT_METHOD_SERVICE
                                    );

            if (imm != null) {

                imm.hideSoftInputFromWindow(
                        etTopic.getWindowToken(),
                        0
                );
            }

            etTopic.clearFocus();

            return false;
        });

        return view;
    }

    private void generateNotes() {

        String topic =
                etTopic.getText()
                        .toString()
                        .trim();

        if (topic.isEmpty()) {

            Toast.makeText(
                    getContext(),
                    "Enter Topic",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnGenerate.setText("Generating...");
        btnGenerate.setEnabled(false);

        try {

            OkHttpClient client =
                    new OkHttpClient();

            JSONObject messageObject =
                    new JSONObject();

            messageObject.put(
                    "role",
                    "user"
            );

            messageObject.put(
                    "content",

                    "Generate professional study notes for "
                            + topic +
                            ".\n\n"
                            + "Format properly with headings.\n\n"
                            + "Include:\n"
                            + "1. Detailed Explanation\n"
                            + "2. Important Points\n"
                            + "3. 5 MCQs with answers\n"
                            + "4. 10 Viva Questions with answers\n\n"
                            + "Make notes easy to understand for students."
            );

            JSONArray messagesArray =
                    new JSONArray();

            messagesArray.put(messageObject);

            JSONObject bodyObject =
                    new JSONObject();

            bodyObject.put(
                    "model",
                    "deepseek/deepseek-chat"
            );

            bodyObject.put(
                    "messages",
                    messagesArray
            );

            RequestBody body =
                    RequestBody.create(
                            bodyObject.toString(),
                            MediaType.parse(
                                    "application/json"
                            )
                    );

            Request request =
                    new Request.Builder()
                            .url("https://openrouter.ai/api/v1/chat/completions")
                            .addHeader(
                                    "Authorization",
                                    "Bearer " + API_KEY
                            )
                            .post(body)
                            .build();

            client.newCall(request)
                    .enqueue(new Callback() {

                        @Override
                        public void onFailure(
                                Call call,
                                IOException e) {

                            if (getActivity() == null)
                                return;

                            getActivity()
                                    .runOnUiThread(() -> {

                                        btnGenerate.setText(
                                                "Generate Notes"
                                        );

                                        btnGenerate.setEnabled(true);

                                        Toast.makeText(
                                                getContext(),
                                                e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show();
                                    });
                        }

                        @Override
                        public void onResponse(
                                Call call,
                                Response response)
                                throws IOException {

                            try {

                                String responseBody =
                                        response.body()
                                                .string();

                                JSONObject jsonObject =
                                        new JSONObject(
                                                responseBody
                                        );

                                JSONArray choices =
                                        jsonObject.getJSONArray(
                                                "choices"
                                        );

                                JSONObject message =
                                        choices.getJSONObject(0)
                                                .getJSONObject(
                                                        "message"
                                                );

                                String aiResult =
                                        message.getString(
                                                "content"
                                        );

                                if (getActivity() == null)
                                    return;

                                getActivity()
                                        .runOnUiThread(() -> {

                                            btnGenerate.setText(
                                                    "Generate Notes"
                                            );

                                            btnGenerate.setEnabled(true);

                                            Intent intent =
                                                    new Intent(
                                                            getContext(),
                                                            ResultActivity.class
                                                    );

                                            FirebaseFirestore firestore =
                                                    FirebaseFirestore.getInstance();

                                            String userId =
                                                    FirebaseAuth.getInstance()
                                                            .getCurrentUser()
                                                            .getUid();

                                            String currentDate =
                                                    new java.text.SimpleDateFormat(
                                                            "dd MMM yyyy",
                                                            java.util.Locale.getDefault()
                                                    ).format(new java.util.Date());

                                            String currentTime =
                                                    new java.text.SimpleDateFormat(
                                                            "hh:mm a",
                                                            java.util.Locale.getDefault()
                                                    ).format(new java.util.Date());

                                            Map<String, Object> note =
                                                    new HashMap<>();

                                            note.put("topic", topic);

                                            note.put("content", aiResult);
                                            note.put(
                                                    "date",
                                                    currentDate + "   " + currentTime
                                            );

                                            note.put(
                                                    "timestamp",
                                                    System.currentTimeMillis()
                                            );

                                            firestore.collection("users")
                                                    .document(userId)
                                                    .collection("notes")
                                                    .add(note);

                                            intent.putExtra(
                                                    "result",
                                                    aiResult
                                            );

                                            startActivity(intent);
                                        });

                            } catch (Exception e) {

                                if (getActivity() == null)
                                    return;

                                getActivity()
                                        .runOnUiThread(() -> {

                                            Toast.makeText(
                                                    getContext(),
                                                    e.getMessage(),
                                                    Toast.LENGTH_LONG
                                            ).show();
                                        });
                            }
                        }
                    });

        } catch (Exception e) {

            Toast.makeText(
                    getContext(),
                    e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}