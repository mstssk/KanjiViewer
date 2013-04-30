
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
import android.view.Menu;
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
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.SystemService;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

// TODO 2013-03-30 AndroidAnnotationを使う形にリファクタリング中

/**
 * 漢字ビューワ メイン画面
 * 
 * @author mstssk
 */
@EActivity(R.layout.main)
@OptionsMenu(R.menu.main_menu)
public class KanjiViewerActivity extends Activity {

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
        // システムの画面回転が無効な場合だけ、手動回転メニューを表示する
        menu.findItem(R.id.menu_rotation).setVisible(isAutoRotationOff());
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 本体設定のセンサーによる画面方向切り替えがOFFなら true
     * 
     * @return
     */
    private boolean isAutoRotationOff() {
        String settingRotation = Settings.System.ACCELEROMETER_ROTATION;
        return Settings.System.getInt(getContentResolver(), settingRotation, 1) == 0;
    }

    /**
     * カスタムフォントのダイアログ表示
     */
    @OptionsItem(R.id.menu_custom_font)
    void showCustomFontDialog() {
        startActivity(new Intent(this, FontFilePrefActivity_.class));
    }

    /**
     * 表示色反転
     */
    @OptionsItem(R.id.menu_invert)
    void invertThemeColor() {
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
    @OptionsItem(R.id.menu_rotation)
    void toggleOrientation() {
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
