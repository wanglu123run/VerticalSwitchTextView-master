package com.viclee.verticalswitchtextview.scrolltextview;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.viclee.verticalswitchtextview.R;


public class ScrollTextView extends ViewGroup {

	private static final int SCROLL = 0;
	private static final int JUSTIFY = 1;
	private static final int ANIMATION_DURATION = 3000;
	
	private int mOrientation;
	private int mSize;
	private int mIndex;
	private int mPosition;
	private Scroller mScroller;
	private Map<View, Integer> mViews;
	private SpinnerAdapter mAdapter;
	private int mPackedViews;
	private int mAnimationDuration;
	
	
	private float measuredWidth = 0;
	private float textSize = 0;
	private float measuredHeight = 0;
	private String text= null;
	private String[] data;
	private boolean flag = true;
	public ScrollTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		int[] linerarLayoutAttrs = {
			android.R.attr.orientation,
		};
		TypedArray a = context.obtainStyledAttributes(attrs, linerarLayoutAttrs);
		mOrientation = a.getInteger(0, LinearLayout.HORIZONTAL);
		a.recycle();

		mAnimationDuration = ANIMATION_DURATION;

		mScroller = new Scroller(context);
		mIndex = -1;
		mPosition = -1;
		mPackedViews = -1;
		mViews = new HashMap<View, Integer>();

		setFocusable(true);
		setFocusableInTouchMode(true);
	
	}
	
	public float getCharacterWidth(String text, float size){
		if(null == text || "".equals(text))
			return 0;
		float width = 0;
		Paint paint = new Paint();
		paint.setTextSize(size);
		float text_width = paint.measureText(text);//得到总体长度		
		width = text_width/text.length();//字符的长度
		return width;
	}
	
	
	public void setText(String text)
	{
		this.text = text;
		data  = new String[]{this.text};
		LayoutInflater inflater= (LayoutInflater) getContext().getSystemService(getContext().LAYOUT_INFLATER_SERVICE);
		TextView view = (TextView) inflater.inflate(R.layout.scroll_text_view, null);
		textSize = view.getTextSize();
		measuredHeight = textSize;
		textSize = getCharacterWidth(text,textSize);
	}
	
	public void beginScroll()
	{
		thread.start();
	}
	Thread thread = new Thread(new Runnable() {
		boolean enabled = true;
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(enabled)
			{
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				setNextView();
				enabled = mIndex + 1 < mAdapter.getCount();
			}
		}
	});
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		measuredWidth = getMeasuredWidth();
		mSize = mOrientation == LinearLayout.HORIZONTAL? getMeasuredWidth() : getMeasuredHeight();
		if(flag && text!=null)
		{
			int length = text.length();
			Log.i("AAA", "length*textSize:"+length*textSize+" measuredWidth:"+measuredWidth);
			if(length*textSize >= measuredWidth)
			{
				int textCountInOneLine = (int) (measuredWidth/textSize -1);
				int lineCount =(int) ((length%textCountInOneLine==0)? (length/textCountInOneLine):(length/textCountInOneLine+1));
				Log.i("AAA", "length:"+length+" textCountInOneLine:"+textCountInOneLine+" lineCount:"+lineCount);
				data = new String[lineCount];
				for(int i=0;i<lineCount;i++)
				{
					if((i+1)*textCountInOneLine<length)
						data[i] =  text.substring(i*textCountInOneLine, (i+1)*textCountInOneLine);
					else
						data[i] =  text.substring(i*textCountInOneLine, length);
					Log.i("AAA", "data:"+data[i]);
				}		
			}
			else
			{
				data = new String[]{text};
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.scroll_text_view, data);
			setAdapter(adapter);
			flag = false;
		}	
		View view = getChildAt(0);
		if(view!=null)
		{
			measureChild(view, widthMeasureSpec, widthMeasureSpec);
			measuredHeight = view.getMeasuredHeight();
		}
		
		setMeasuredDimension((int)measuredWidth, (int)measuredHeight);
	}

	

	private int getPackedViews(int offset) {
		int size = mSize;
		int start = offset / size;
		int numViews = offset % size != 0? 1 : 0;
		return start << 1 | numViews;
	}
	
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {			
			mScroller.computeScrollOffset();
			int currX = mScroller.getCurrX();
			int delta = mPosition - currX;
			mPosition = currX;
			int packed = getPackedViews(mPosition);
			manageViews(packed);
			scroll(delta);
			if (!mScroller.isFinished()) {
				handler.sendEmptyMessage(msg.what);
			} else {
				if (msg.what == SCROLL) {
					justify();
				} else {
					mIndex = mPosition / mSize;

				}
			}
		}
	};

	private void justify() {
		int offset = mPosition % mSize;
		if (offset != 0) {
			int endPosition = mPosition - offset;
			if (offset > mSize / 2) {
				endPosition += mSize;
			}
			mScroller.startScroll(mPosition, 0, endPosition - mPosition, 0, mAnimationDuration);
			handler.sendEmptyMessage(JUSTIFY);
		} else {
			mIndex = mPosition / mSize;

		}
	}

	private void scroll(int offset) {
		if (mOrientation == LinearLayout.HORIZONTAL) {
			for (View view : mViews.keySet()) {
				view.offsetLeftAndRight(offset);
			}
		} else {
			for (View view : mViews.keySet()) {
				view.offsetTopAndBottom(offset);
			}
		}
		invalidate();
	}

	public void setSelection(int index, boolean animate) {
		if (index == mIndex) {
			return;
		}
		int endPosition = index * mSize;
		int diff = Math.abs(index - mIndex);
		int sign = index > mIndex? 1 : -1;
		mIndex = index;
		if (diff > 1) {
			mPosition = endPosition - sign * mSize;
		}
		if (animate) {
			mScroller.startScroll(mPosition, 0, endPosition - mPosition, 0, mAnimationDuration);
			handler.removeMessages(JUSTIFY);
			handler.removeMessages(SCROLL);
			handler.sendEmptyMessage(JUSTIFY);
		} else {
			mPosition = endPosition;
			manageViews(index << 1);

			invalidate();
		}
	}
	
	private void manageViews(int packedViews) {
		if (packedViews == mPackedViews) {
			return;
		}

		mPackedViews = packedViews;
		int startIdx = packedViews >> 1;
		int endIdx = startIdx + (packedViews & 1);
		int viewIdx = startIdx;
		while (viewIdx <= endIdx) {
			if (!mViews.containsValue(viewIdx)) {
				if (viewIdx >= 0 && viewIdx < mAdapter.getCount()) {
					View view = mAdapter.getView(viewIdx, null, this);
					mViews.put(view, viewIdx);
					addView(view);
				}
			}
			viewIdx++;
		}

		// remove not visible views
		Iterator<View> iterator = mViews.keySet().iterator();
		while (iterator.hasNext()) {
			View view = iterator.next();
			int idx = mViews.get(view);
			if (idx < startIdx || idx > endIdx) {
				iterator.remove();
				removeView(view);
			}
		}
	}

	public int getSelection() {
		return mIndex;
	}
	
	public void setPreviousView() {
		if (mAdapter != null && mIndex > 0) {
			setSelection(mIndex-1, true);

		}
	}

	public void setNextView() {
		if (mAdapter != null && mIndex + 1 < mAdapter.getCount()) {
			setSelection(mIndex+1, true);
		}
	}
	
	public void setAdapter(SpinnerAdapter adapter) {
		mAdapter = adapter;
		if (mAdapter != null) {
			setSelection(0, false);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		for (View view : mViews.keySet()) {
			if (view.getWidth() == 0) {
				// new View: not layout()ed
				int idx = mViews.get(view);
				if (mOrientation == LinearLayout.HORIZONTAL) {
					int left = mSize * idx - mPosition;
					view.layout(left, 0, left+r-l, b-t);
				} else {
					int top = mSize * idx - mPosition;
					view.layout(0, top, r-l, top+b-t);
				}
			}
		}
	}

}
