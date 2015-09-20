package me.gurinderhans.sfumaps.utils;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;

/**
 * Created by ghans on 15-09-19.
 */

public class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
	ArrayList<Character> splitChar;

	public SpaceTokenizer() {
		this.splitChar = new ArrayList(1);
		this.splitChar.add(Character.valueOf(','));
	}

	public SpaceTokenizer(char[] splitChar) {
		this.splitChar = new ArrayList(splitChar.length);
		char[] var2 = splitChar;
		int var3 = splitChar.length;

		for (int var4 = 0; var4 < var3; ++var4) {
			char c = var2[var4];
			this.splitChar.add(Character.valueOf(c));
		}

	}

	public SpaceTokenizer(char splitChar) {
		this.splitChar = new ArrayList(1);
		this.splitChar.add(Character.valueOf(splitChar));
	}

	@Override
	public int findTokenStart(CharSequence text, int cursor) {
		int i = cursor;
		while (i > 0 && (text.charAt(i - 1) != ' ' && text.charAt(i - 1) != '\n')) {
			i--;
		}
		while (i < cursor && (text.charAt(i) == ' ')) {
			i++;
		}
		return i;
	}

	@Override
	public int findTokenEnd(CharSequence text, int cursor) {
		int i = cursor;
		int len = text.length();

		while (i < len) {
			if (text.charAt(i) == ' ') {
				return i;
			} else {
				i++;
			}
		}
		return len;
	}

	@Override
	public CharSequence terminateToken(CharSequence text) {
		int i = text.length();

		while (i > 0 && (text.charAt(i - 1) == ' ')) {
			i--;
		}
		if (i > 0 && (text.charAt(i - 1) == ' ')) {
			return text;
		} else {
			if (text instanceof Spanned) {
				SpannableString sp = new SpannableString(text + " ");
				TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
						Object.class, sp, 0);
				return sp;
			} else {
				return text + " ";
			}
		}
	}
}