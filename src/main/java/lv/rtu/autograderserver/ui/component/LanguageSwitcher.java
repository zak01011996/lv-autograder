package lv.rtu.autograderserver.ui.component;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.select.Select;
import lv.rtu.autograderserver.ui.TranslationProvider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Language switcher component, due to it should be used in several places
 */
public class LanguageSwitcher extends Select<Locale> {
    private Map<Locale, String> languageNames = new HashMap<>() {{
        put(TranslationProvider.LOCALE_EN, "English");
        put(TranslationProvider.LOCALE_LV, "Latviešu");
    }};

    public LanguageSwitcher(boolean showLabel) {
        if (showLabel) {
            setLabel(getTranslation("app_language_select"));
        }

        setEmptySelectionAllowed(false);
        setWidth(7.5f, Unit.EM);

        TranslationProvider translationProvider = new TranslationProvider();
        setItems(translationProvider.getProvidedLocales());
        setValue(UI.getCurrent().getSession().getLocale());
        setItemLabelGenerator(languageNames::get);

        addValueChangeListener(event -> {
            UI.getCurrent().getSession().setLocale(event.getValue());
            UI.getCurrent().getPage().reload();
        });
    }

}
