
package jp.mstssk.kanji_viewer;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.androidannotations.annotations.AfterTextChange;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

// TODO 2013-03-30 AndroidAnnotationを使う形にリファクタリング中
// TODO 2013-04-02 表示色, フォントについては、管理する別クラスを持った方が良さそう

/**
 * 漢字ビューワ メイン画面
 * 
 * @author mstssk
 */
@EActivity(R.layout.main)
public class KanjiViewerActivity extends Activity {

    private static final int MENU_CUSTOM_FONT = 0;
    private static final int MENU_ORIENTATION = 1;
    private static final int MENU_INVERT_COLOR = 2;

    @ViewById(R.id.include_text)
    TextView textView;

    @ViewById(R.id.input_form)
    EditText textBox;

    @ViewById(R.id.background)
    View background;

    @SystemService
    InputMethodManager imm;

    @Pref
    KanjiViewerPrefs_ prefs;

    @AfterViews
    void initViews() {
        textBox.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                focusChange(v, hasFocus);
            }
        });
        setThemeColor(prefs.KEY_THEME().get());
    }

    /**
     * 入力フォーカス変更時にSIPの表示制御をする
     * 
     * @param v
     * @param hasFocus
     */
    private void focusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            imm.showSoftInput(v, 0);
        } else {
            hideIMEPanel(v);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        String str = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        if (str != null) {
            textBox.setText(str);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initFont();
    }

    /**
     * 文字入力パネルを隠す
     * 
     * @param v
     */
    @Click({
            R.id.button_enter, R.id.include_text
    })
    void hideIMEPanel(View v) {
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * 文字入力時、拡大エリアに反映する
     * 
     * @param s textBoxの内容
     */
    @AfterTextChange({
            R.id.input_form
    })
    void setText(Editable s) {
        textView.setText(s.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean isOrientationFixed = false;
        try {
            // 本体設定のセンサーによる画面方向切り替えがOFFなら true
            isOrientationFixed = Settings.System.getInt(getContentResolver(),
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

    /**
     * カスタムフォントのダイアログ表示
     */
    private void showCustomFontDialog() {
        startActivity(new Intent(this, FontFilePrefActivity.class));
    }

    /**
     * 表示色反転
     */
    private void invertThemeColor() {
        setThemeColor(invertColor(prefs.KEY_THEME().get()));
    }

    /**
     * 表示色設定
     */
    private void setThemeColor(int color) {
        background.setBackgroundColor(color);
        textView.setTextColor(invertColor(color));
        prefs.edit().KEY_THEME().put(color).apply();
    }

    /**
     * 逆転した表示色を取得
     * 
     * @param color
     * @return
     */
    private int invertColor(int color) {
        return (color == Color.WHITE) ? Color.BLACK : Color.WHITE;
    }

    /**
     * 画面方向切り替え
     */
    private void toggleOrientation() {
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
            this.setRequestedOrientation(orientation);
        }
    }

    /**
     * フォント読み込み、初期化
     */
    private void initFont() {
        String path = prefs.FONT_PATH().getOr(null);

        if (path == null) {
            textView.setTypeface(null);
            return;
        }

        if (new File(path).exists()) {
            Typeface typeface = Typeface.createFromFile(path);
            textView.setTypeface(typeface);
        } else {
            Toast.makeText(this, R.string.label_failed_load_font, Toast.LENGTH_SHORT).show();
        }
    }
}
