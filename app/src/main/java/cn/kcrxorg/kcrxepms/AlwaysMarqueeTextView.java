package cn.kcrxorg.kcrxepms;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class AlwaysMarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {
	
	public AlwaysMarqueeTextView(Context context) {
		super(context);
		}

		public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		}

		public AlwaysMarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		}

		@Override
		public boolean isFocused() {
		return true;
		}
}
