package jp.mstssk.kanji_viewer;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class KanjiViewerActivity extends Activity implements TextWatcher,
        OnClickListener, OnFocusChangeListener {
    
    private static final int MENU_CUSTOM_FONT = 0;
    private static final int MENU_ORIENTATION = 1;
    private static final int MENU_INVERT_COLOR = 2;

    private TextView text_view;
    private EditText input_form;
    private InputMethodManager imm;

    private SharedPreferences pref;
    
    private int current_color;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        
        text_view = (TextView) findViewById(R.id.include_text);
        text_view.setOnClickListener(this);

        input_form = (EditText) findViewById(R.id.input_form);
        input_form.addTextChangedListener(this);
        input_form.setOnFocusChangeListener(this);

        findViewById(R.id.button_enter).setOnClickListener(this);
        
        setThemeColor(pref.getInt(getString(R.string.key_theme), Color.WHITE));
    }

    @Override
    public void onStart() {
        super.onStart();
        
        String str = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (str != null) {
            input_form.setText(str);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        initFont();
    }
    
    @Override
    public void onClick(View v) {
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            imm.showSoftInput(v, 0);
        } else {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    public void afterTextChanged(Editable s) {
        text_view.setText(s.toString());
    }

    public void beforeTextChanged(CharSequence s, int start, int count,
            int after) {}

    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        boolean isOrientationFixed = false;
        try {
            // 本体設定のセンサーによる画面方向切り替えがOFFなら true
            isOrientationFixed =  Settings.System.getInt(getContentResolver(), 
                Settings.System.ACCELEROMETER_ROTATION) == 0 ? true : false;
        } catch (Exception e) {
            Log.e("mstssk", e.getLocalizedMessage(), e);
        }
        
        if (isOrientationFixed) {
            menu.add(0, MENU_ORIENTATION, 0, R.string.menu_rotation).setIcon(
                    android.R.drawable.ic_menu_always_landscape_portrait);
        }
        
        menu.add(0, MENU_CUSTOM_FONT, 0, R.string.menu_custom_font).setIcon(
                android.R.drawable.ic_menu_manage);
        menu.add(0, MENU_INVERT_COLOR, 0, R.string.menu_invert).setIcon(
                R.drawable.ic_menu_invert);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_CUSTOM_FONT:
                showCustomFontDialog();
                break;
            case MENU_ORIENTATION:
                toggleOrientation();
                break;
            case MENU_INVERT_COLOR:
                invertThemeColor();
                break;
            default:
                return false;
        }
        return true;
    }
    
    private void showCustomFontDialog() {
        // カスタムフォントのダイアログ表示
        startActivity(new Intent(this, SetteiGamenActivity.class));
    }
    
    private void invertThemeColor() {
        setThemeColor((current_color == Color.WHITE) ? Color.BLACK : Color.WHITE);
    }
    
    private void setThemeColor(int theme) {
        current_color = theme;
        int inverted = (current_color == Color.WHITE) ? Color.BLACK : Color.WHITE;
        findViewById(R.id.background).setBackgroundColor(current_color);
        text_view.setTextColor(inverted);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(getString(R.string.key_theme), current_color);
        editor.commit();
    }

    private void toggleOrientation() {
        // 画面方向切り替え
        int orientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        switch (getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:
                orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            break;
            case Configuration.ORIENTATION_PORTRAIT:
                orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            break;
            default:
            break;
        }
        if (orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            setRequestedOrientation(orientation);
        }
    }
    
    private void initFont() {
        String path = pref.getString(getString(R.string.key_font_path), null);
        if (path != null) {
            if (new File(path).exists()){
                Typeface typeface = Typeface.createFromFile(path);
                text_view.setTypeface(typeface);
            } else {
                Toast.makeText(this, R.string.label_failed_load_font, Toast.LENGTH_SHORT).show();
            }
        } else {
            text_view.setTypeface(null);
        }
    }
}