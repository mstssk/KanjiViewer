package jp.mstssk.kanji_viewer;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

public class SetteiGamenActivity extends Activity implements OnClickListener {

    private static final int REQUEST_CODE = 1;

    private EditText edittext_font_path;
    
    private SharedPreferences pref;
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_font);
        
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        
        edittext_font_path = (EditText)findViewById(R.id.edittext_font_path);
        
        findViewById(R.id.button_select).setOnClickListener(this);
        findViewById(R.id.button_reset).setOnClickListener(this);
        findViewById(R.id.button_ok).setOnClickListener(this);
        findViewById(R.id.button_cancel).setOnClickListener(this);
        
        edittext_font_path.setText(pref.getString(getString(R.string.key_font_path), ""));
    }
    
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.button_select:
                getFontFilePath();
                break;
            case R.id.button_reset:
                edittext_font_path.setText(null);
                break;
            case R.id.button_ok:
                saveFilePath(edittext_font_path.getText().toString());
            case R.id.button_cancel:
            default:
                finish();
                break;
        }
    }
    
    private void saveFilePath(String path) {
        if (path.length() == 0) {
            path = null;
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(getString(R.string.key_font_path), path);
        editor.commit();
    }
    
    private void getFontFilePath() {
        try {
            // OI File ManagerまたはASTRO File Managerを想定
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/ttf");
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int padding = (int)(10 * metrics.density);
            metrics = null;
            TextView textView = new TextView(this, null, android.R.style.Theme_Dialog);
            textView.setPadding(padding, 0, padding, 0);
            textView.setMovementMethod(LinkMovementMethod.getInstance());
            textView.setText(Html.fromHtml(getString(R.string.text_not_found_filemanager)));
            new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.label_not_found_filemanager)
                .setView(textView)
                .setNegativeButton(R.string.label_close, null)
                .show();
        }
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK == resultCode && REQUEST_CODE == requestCode) {
            String path = data.getData().toString();
            path = Pattern.compile("^file://").matcher(path).replaceFirst("");
            edittext_font_path.setText(path);
        }
    }
}
