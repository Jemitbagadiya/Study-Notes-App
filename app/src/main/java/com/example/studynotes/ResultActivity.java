package com.example.studynotes;

import android.content.ContentValues;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.content.ClipData;
import android.content.ClipboardManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.OutputStream;

import io.noties.markwon.Markwon;

public class ResultActivity extends AppCompatActivity {

    TextView txtResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        // Back Button
        toolbar.setNavigationOnClickListener(v -> {
            finish();
        });

        txtResult = findViewById(R.id.txtResult);

        String result = getIntent().getStringExtra("result");

        if (result == null) {
            result = "No Notes Generated";
        }

        // Markdown Renderer
        Markwon markwon = Markwon.create(this);

        // Render Markdown
        markwon.setMarkdown(txtResult, result);
    }

    // Toolbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    // Menu Item Click
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_more) {

            PopupMenu popupMenu =
                    new PopupMenu(
                            this,
                            findViewById(R.id.toolbar),
                            android.view.Gravity.END,
                            0,
                            R.style.PopupMenuStyle
                    );

            popupMenu.getMenuInflater()
                    .inflate(
                            R.menu.popup_menu,
                            popupMenu.getMenu()
                    );

            popupMenu.setOnMenuItemClickListener(menuItem -> {

                int id = menuItem.getItemId();

                // Download PDF
                if (id == R.id.menu_download) {
                    generatePdf();
                    return true;
                }

                // Share
                if (id == R.id.menu_share) {
                    Intent shareIntent = new Intent(
                            Intent.ACTION_SEND
                    );
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(
                            Intent.EXTRA_TEXT,
                            txtResult.getText().toString()
                    );
                    startActivity(
                            Intent.createChooser(
                                    shareIntent,
                                    "Share Notes"
                            )
                    );

                    return true;
                }

                // Copy
                if (id == R.id.menu_copy) {

                    ClipboardManager clipboard =
                            (ClipboardManager)
                                    getSystemService(CLIPBOARD_SERVICE);

                    ClipData clip =
                            ClipData.newPlainText(
                                    "Study Notes",
                                    txtResult.getText().toString()
                            );

                    clipboard.setPrimaryClip(clip);
                    return true;
                }
                return false;
            });

            popupMenu.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Generate Professional PDF
    private void generatePdf() {
        String notes =
                txtResult.getText().toString();

        // Dynamic File Name
        String fileName =
                notes.split("\n")[0]
                        .replaceAll("[^a-zA-Z0-9]", "_")
                        + ".pdf";

        int width =
                txtResult.getWidth();

        int height =
                txtResult.getHeight();

        PdfDocument pdfDocument =
                new PdfDocument();

        int padding = 40;

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        width + (padding * 2),
                        height + (padding * 2),
                        1
                ).create();

        PdfDocument.Page page =
                pdfDocument.startPage(pageInfo);

        Canvas canvas =
                page.getCanvas();

        // Reduce drawing area
        canvas.translate(padding, padding);

        // Draw exact TextView content
        txtResult.draw(canvas);

        pdfDocument.finishPage(page);

        try {

            ContentValues values =
                    new ContentValues();

            values.put(
                    MediaStore.Downloads.DISPLAY_NAME,
                    fileName
            );

            values.put(
                    MediaStore.Downloads.MIME_TYPE,
                    "application/pdf"
            );

            values.put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS
            );

            Uri uri =
                    getContentResolver().insert(
                            MediaStore.Files
                                    .getContentUri("external"),
                            values
                    );

            OutputStream outputStream =
                    getContentResolver()
                            .openOutputStream(uri);

            pdfDocument.writeTo(outputStream);

            outputStream.close();

            Toast.makeText(
                    this,
                    "PDF Downloaded Successfully",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Download Failed",
                    Toast.LENGTH_SHORT
            ).show();
        }
        pdfDocument.close();
    }
}