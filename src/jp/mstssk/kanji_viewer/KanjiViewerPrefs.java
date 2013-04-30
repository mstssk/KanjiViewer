
package jp.mstssk.kanji_viewer;

import android.graphics.Color;

import com.googlecode.androidannotations.annotations.sharedpreferences.DefaultInt;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref;
import com.googlecode.androidannotations.annotations.sharedpreferences.SharedPref.Scope;

/**
 * 漢字ビューワの設定項目
 * 
 * @author mstssk
 */
@SharedPref(Scope.APPLICATION_DEFAULT)
public interface KanjiViewerPrefs {

    // XXX 各項目の名前が残念な感じだが、名とpreferecneのkeyは一致させなければいけないので

    @DefaultInt(Color.WHITE)
    int KEY_THEME();

    String FONT_PATH();

}
