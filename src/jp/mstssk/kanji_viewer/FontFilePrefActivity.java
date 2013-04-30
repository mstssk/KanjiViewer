
package jp.mstssk.kanji_viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.widget.EditText;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.sharedpreferences.Pref;

/**
 * 漢字ビューワ フォント設定画面
 * 
 * @author mstssk
 */
@EActivity(R.layout.dialog_custom_font)
public class FontFilePrefActivity extends Activity {

    private static final String MIME_TYPE_FONT_FILE = "*/ttf";

    private static final int REQUEST_CODE = 1;

    @ViewById(R.id.edittext_font_path)
    EditText fontPathTextBox;

    @Pref
    KanjiViewerPrefs_ prefs;

    @AfterViews
    void loadFontPath() {
        // テキストボックスに設定を読み込む
        fontPathTextBox.setText(prefs.FONT_PATH().getOr(""));
    }

    /**
     * 画面を閉じる
     */
    @Click({
            R.id.button_cancel
    })
    void closeActivity() {
        finish();
    }

    /**
     * ファイルパスを保存
     */
    @Click({
            R.id.button_ok
    })
    void saveFilePath() {
        String path = fontPathTextBox.getText().toString();
        if (path.length() == 0) {
            path = null;
        }
        prefs.edit().FONT_PATH().put(path).apply();
        finish();
    }

    /**
     * テキストボックスをクリア
     */
    @Click({
            R.id.button_reset
    })
    void clearTextBox() {
        fontPathTextBox.setText(null);
    }

    /**
     * ファイルマネージャのアプリを起動してパスを取得してくる
     */
    @Click({
            R.id.button_select
    })
    void getFontFilePath() {
        // OI File ManagerまたはASTRO File Managerを想定
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(MIME_TYPE_FONT_FILE);
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            showFileManagerNotFoundDialog();
        }
    }

    /**
     * ファイルマネージャがインストールされていない場合の注意書きダイアログを表示
     */
    private void showFileManagerNotFoundDialog() {
        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        final int padding = (int) (10 * metrics.density);
        TextView textView = new TextView(this, null, android.R.style.Theme_Dialog);
        textView.setPadding(padding, 0, padding, 0);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(Html.fromHtml(getString(R.string.text_not_found_filemanager)));
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.label_not_found_filemanager).setView(textView)
                .setNegativeButton(R.string.label_close, null).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_OK != resultCode || REQUEST_CODE != requestCode) {
            return;
        }
        final String path = data.getData().toString().replaceFirst("^file://", "");
        fontPathTextBox.setText(path);
    }
}
