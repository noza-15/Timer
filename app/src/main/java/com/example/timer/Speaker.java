package com.example.timer;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

class Speaker implements TextToSpeech.OnInitListener {
    private TextToSpeech tts;

    Speaker(Context context) {
        this.tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (TextToSpeech.SUCCESS == status) {
            //言語選択
            Locale locale = Locale.JAPAN;
            if (tts.isLanguageAvailable(locale) >= TextToSpeech.LANG_AVAILABLE) {
                tts.setLanguage(locale);
            } else {
                Log.d("Error", "Locale");
            }
        } else {
            Log.d("Error", "Init");
        }
    }
    void shutdown(){
        tts.shutdown();
    }

    void speechText(String contents) {
        if (0 < contents.length()) {
            if (tts.isSpeaking()) {
                // 読み上げ中なら停止
                tts.stop();
            }
            //読み上げられているテキストを確認
            System.out.println(contents);
            //読み上げ開始
            tts.speak(contents, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}
