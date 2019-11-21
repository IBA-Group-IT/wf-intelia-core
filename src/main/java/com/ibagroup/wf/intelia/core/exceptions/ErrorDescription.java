package com.ibagroup.wf.intelia.core.exceptions;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public interface ErrorDescription {

	default String getMessageTemplate(Locale locale) {
		return getMessagesBundle(locale).getString(toString());
	}

	default String getMessage(Object... params) {
		return MessageFormat.format(getMessageTemplate(Locale.US), params);
	}

	default String getLocalizedMessage(Object... params) {
		return MessageFormat.format(getMessageTemplate(getMessagesLocale()), params);
	}

	default ResourceBundle getMessagesBundle(Locale locale) {
		return ResourceBundle.getBundle("messages.messages", locale);
	}

	default Locale getMessagesLocale() {
		return Locale.getDefault();
	}
}
