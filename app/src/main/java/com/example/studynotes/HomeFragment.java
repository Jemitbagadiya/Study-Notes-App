package com.example.studynotes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    String API_KEY = ""; //API Key Gimini

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
            v.performClick();
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
                    new OkHttpClient.Builder()
                            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                            .callTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                            .build();

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

            JSONObject textPart = new JSONObject();

            textPart.put(
                    "text",
                    "Generate professional study notes for "
                            + topic
                            + ".\n\n"
                            + "Format properly with headings.\n\n"
                            + "Include:\n"
                            + "1. Detailed Explanation\n"
                            + "2. Important Points\n"
                            + "3. 5 MCQs with answers\n"
                            + "4. 10 Viva Questions with answers\n\n"
                            + "Make notes easy to understand for students."
            );

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject bodyObject = new JSONObject();
            bodyObject.put("contents", contents);

            JSONObject generationConfig = new JSONObject();

            generationConfig.put("temperature", 0.7);
            generationConfig.put("maxOutputTokens", 8192);

            bodyObject.put("generationConfig", generationConfig);

            RequestBody body =
                    RequestBody.create(
                            bodyObject.toString(),
                            MediaType.parse(
                                    "application/json"
                            )
                    );

            Request request =
                    new Request.Builder()
                            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + API_KEY)
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

                                // Check API error response
                                if (!response.isSuccessful()) {
                                    throw new Exception(
                                            "API Error: HTTP " + response.code()
                                    );
                                }

                                JSONArray candidates =
                                        jsonObject.optJSONArray("candidates");

                                if (candidates == null || candidates.length() == 0) {
                                    JSONObject errorObject = jsonObject.optJSONObject("error");

                                    throw new Exception(
                                            errorObject != null
                                                    ? errorObject.optString(
                                                    "message",
                                                    "No candidates returned"
                                            )
                                                    : "Gemini returned no candidates"
                                    );
                                }

                                JSONObject candidate =
                                        candidates.getJSONObject(0);

                                JSONObject content =
                                        candidate.getJSONObject("content");

                                JSONArray parts =
                                        content.getJSONArray("parts");

                                StringBuilder resultBuilder = new StringBuilder();

                                for (int i = 0; i < parts.length(); i++) {
                                    JSONObject part = parts.getJSONObject(i);

                                    String text = part.optString("text", "");

                                    if (!text.isEmpty()) {
                                        resultBuilder.append(text).append("\n");
                                    }
                                }

                                String aiResult = resultBuilder.toString().trim();

                                if (aiResult.isEmpty()) {
                                    throw new Exception("Generated notes are empty");
                                }

                                if (aiResult.trim().isEmpty()) {
                                    throw new Exception("Generated notes are empty");
                                }

                                if (getActivity() == null)
                                    return;

                                getActivity()
                                        .runOnUiThread(() -> {

                                            btnGenerate.setText(
                                                    "Generate Notes"
                                            );

                                            btnGenerate.setEnabled(true);

                                            // Open ResultActivity
                                            Intent intent =
                                                    new Intent(
                                                            getContext(),
                                                            ResultActivity.class
                                                    );

                                            // Check whether user is logged in
                                            if (FirebaseAuth.getInstance().getCurrentUser() == null) {

                                                intent.putExtra("result", aiResult);
                                                intent.putExtra("topic", topic);

                                                startActivity(intent);

                                                Toast.makeText(
                                                        getContext(),
                                                        "Please login to save notes",
                                                        Toast.LENGTH_LONG
                                                ).show();

                                                return;
                                            }

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

                                            // Save notes to Firebase
                                            firestore.collection("users")
                                                    .document(userId)
                                                    .collection("notes")
                                                    .add(note)
                                                    .addOnSuccessListener(documentReference -> {

                                                        if (getContext() != null) {
                                                            Toast.makeText(
                                                                    getContext(),
                                                                    "Notes saved successfully",
                                                                    Toast.LENGTH_SHORT
                                                            ).show();
                                                        }
                                                    })
                                                    .addOnFailureListener(e -> {

                                                        if (getContext() != null) {
                                                            Toast.makeText(
                                                                    getContext(),
                                                                    "Save failed: " + e.getMessage(),
                                                                    Toast.LENGTH_LONG
                                                            ).show();
                                                        }
                                                    });

                                            // Pass generated notes to ResultActivity
                                            intent.putExtra("result", aiResult);
                                            intent.putExtra("topic", topic);

                                            startActivity(intent);
                                        });

                            } catch (Exception e) {

                                if (getActivity() == null)
                                    return;

                                getActivity()
                                        .runOnUiThread(() -> {

                                            btnGenerate.setText("Generate Notes");
                                            btnGenerate.setEnabled(true);

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