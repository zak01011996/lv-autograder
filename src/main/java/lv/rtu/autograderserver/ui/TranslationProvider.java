package lv.rtu.autograderserver.ui;

import com.vaadin.flow.i18n.I18NProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Author notes:
 * Code was taken and modified from official documentation
 * Source: https://vaadin.com/docs/latest/advanced/i18n-localization
 */
@Component
public class TranslationProvider implements I18NProvider {
    private final Logger logger = LoggerFactory.getLogger(TranslationProvider.class);

    public static final String BUNDLE_PREFIX = "translate";

    public static final Locale LOCALE_EN = new Locale("en", "GB");
    public static final Locale LOCALE_LV = new Locale("lv", "LV");

    private final List<Locale> locales = List.of(LOCALE_EN, LOCALE_LV);

    @Override
    public List<Locale> getProvidedLocales() {
        return locales;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        if (key == null) {
            logger.warn("Got lang request for key with null value!");
            return "";
        }

        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_PREFIX, locale);

        String value;
        try {
            value = bundle.getString(key);
        } catch (final MissingResourceException e) {
            logger.warn("Missing resource", e);
            return "!" + locale.getLanguage() + ": " + key;
        }

        if (params.length > 0) {
            value = MessageFormat.format(value, params);
        }
        return value;
    }
}
