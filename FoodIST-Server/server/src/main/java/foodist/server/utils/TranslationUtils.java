package foodist.server.utils;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;

public class TranslationUtils {

    public static String translate(String content, String sourceLanguage, String targetLanguage) {
        Translate translate = TranslateOptions.getDefaultInstance().getService();

        Translation translation = translate.translate(
                content,
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage));

        return translation.getTranslatedText();
    }
}
