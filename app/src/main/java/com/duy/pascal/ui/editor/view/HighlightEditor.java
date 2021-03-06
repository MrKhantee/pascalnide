/*
 *  Copyright (c) 2017 Tran Le Duy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duy.pascal.ui.editor.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.Toast;

import com.duy.pascal.interperter.linenumber.LineNumber;
import com.duy.pascal.ui.R;
import com.duy.pascal.ui.editor.highlight.BracketHighlighter;
import com.duy.pascal.ui.editor.highlight.CodeHighlighter;
import com.duy.pascal.ui.editor.highlight.IEditorColorScheme;
import com.duy.pascal.ui.editor.highlight.spans.ErrorSpan;
import com.duy.pascal.ui.themefont.model.CodeTheme;
import com.duy.pascal.ui.themefont.themes.ThemeManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HighlightEditor extends CodeSuggestsEditText implements View.OnKeyListener, GestureDetector.OnGestureListener {
    public static final String TAG = HighlightEditor.class.getSimpleName();

    public static final int SYNTAX_DELAY_MILLIS_SHORT = 100;
    public static final int SYNTAX_DELAY_MILLIS_LONG = 700;
    public static final int CHARS_TO_COLOR = 2500;
    public boolean mShowLines = true;
    public boolean mWordWrap = true;
    protected Paint mPaintNumbers;
    protected Paint mPaintHighlight;
    protected int mPaddingDP = 4;
    protected int mPadding, mLinePadding;
    protected float mScale;
    protected int mHighlightedLine;
    protected int mHighlightStart;
    protected Rect mDrawingRect, mLineBounds;
    /**
     * the scroller instance
     */
    protected Scroller mTedScroller;
    /**
     * the velocity tracker
     */
    protected GestureDetector mGestureDetector;
    /**
     * the Max size of the view
     */
    protected Point mMaxSize;
    private Handler mHandler;
    private Runnable mPostHighlight;
    private IEditorColorScheme mCodeTheme;
    private boolean mCanEdit = true;
    @Nullable
    private ScrollView mVerticalScroll;
    private int lastPinLine = -1;
    private LineUtils mLineUtils;
    private boolean[] mIsGoodLineArray;
    private int[] mRealLines;
    private int mLineCount;
    private boolean mIsFinding = false;
    /**
     * Disconnect this undo/redo from the text
     * view.
     */
    private boolean enabledChangeListener = false;
    /**
     * The change listener.
     */
    private EditTextChangeListener mChangeListener;
    private CodeHighlighter mCodeHighlighter;
    private BracketHighlighter mBracketHighlighter;

    public HighlightEditor(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    public HighlightEditor(Context context) {
        super(context);

    }

    public HighlightEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @CallSuper
    protected void setup(Context context) {
        super.setup(context);
        mHandler = new Handler();
        mPostHighlight = new Runnable() {
            @Override
            public void run() {
                highlightText();
            }
        };
        mHandler = new Handler();
        mCodeTheme = new CodeTheme(true);

        mLineUtils = new LineUtils();
        mPaintNumbers = new Paint();
        mPaintNumbers.setColor(getResources().getColor(R.color.color_number_color));
        mPaintNumbers.setAntiAlias(true);

        mPaintHighlight = new Paint();

        mScale = context.getResources().getDisplayMetrics().density;
        mPadding = (int) (mPaddingDP * mScale);
        mHighlightedLine = mHighlightStart = -1;
        mDrawingRect = new Rect();
        mLineBounds = new Rect();
        mGestureDetector = new GestureDetector(getContext(), HighlightEditor.this);
        mChangeListener = new EditTextChangeListener();
        mCodeHighlighter = new CodeHighlighter(this);
        mBracketHighlighter = new BracketHighlighter(this, mCodeTheme);

        updateFromSettings();
        enableTextChangedListener();
    }

    public IEditorColorScheme getCodeTheme() {
        return mCodeTheme;
    }

    public void setCodeTheme(IEditorColorScheme codeTheme) {
        this.mCodeTheme = codeTheme;
        this.mCodeHighlighter.setCodeTheme(codeTheme);
        mBracketHighlighter.setCodeTheme(codeTheme);
        setTextColor(codeTheme.getTextColor());
        setBackgroundColor(codeTheme.getBackgroundColor());
        mPaintNumbers.setColor(codeTheme.getNumberColor());
        refresh();
    }

    public boolean canEdit() {
        return mCanEdit;
    }

    public void setCanEdit(boolean canEdit) {
        this.mCanEdit = canEdit;
    }


    public void computeScroll() {

        if (mTedScroller != null) {
            if (mTedScroller.computeScrollOffset()) {
                scrollTo(mTedScroller.getCurrX(), mTedScroller.getCurrY());
            }
        } else {
            super.computeScroll();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return mGestureDetector == null || mGestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        if (isEnabled()) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!mEditorSetting.flingToScroll()) {
            return true;
        }

        if (mTedScroller != null) {
            mTedScroller.fling(getScrollX(), getScrollY(), -(int) velocityX, -(int) velocityY,
                    0, mMaxSize.x, 0, mMaxSize.y);
        }
        return true;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        int lineX, baseline;
        if (mLineCount != getLineCount()) {
            mLineCount = getLineCount();
            mLineUtils.updateHasNewLineArray(mLineCount, getLayout(), getText().toString());
            mIsGoodLineArray = mLineUtils.getGoodLines();
            mRealLines = mLineUtils.getRealLines();
        }
        if (mShowLines) {
            int padding = calculateLinePadding();
            if (mLinePadding != padding) {
                mLinePadding = padding;
                setPadding(mLinePadding, mPadding, mPadding, mPadding);
            }
        }

        getDrawingRect(mDrawingRect);
        lineX = mDrawingRect.left + mLinePadding - mPadding;
        int min = 0;
        int max = mLineCount;
        getLineBounds(0, mLineBounds);
        int startBottom = mLineBounds.bottom;
        int startTop = mLineBounds.top;
        getLineBounds(mLineCount - 1, mLineBounds);
        int endBottom = mLineBounds.bottom;
        int endTop = mLineBounds.top;
        if (mLineCount > 1 && endBottom > startBottom && endTop > startTop) {
            min = Math.max(min, ((mDrawingRect.top - startBottom) * (mLineCount - 1)) / (endBottom - startBottom));
            max = Math.min(max, ((mDrawingRect.bottom - startTop) * (mLineCount - 1)) / (endTop - startTop) + 1);
        }
        for (int i = min; i < max; i++) {
            baseline = getLineBounds(i, mLineBounds);

            if ((mMaxSize != null) && (mMaxSize.x < mLineBounds.right)) {
                mMaxSize.x = mLineBounds.right;
            }

            if ((i == mHighlightedLine) && (!mWordWrap)) {
                canvas.drawRect(mLineBounds, mPaintHighlight);
            }
            if (mShowLines && mIsGoodLineArray[i]) {
                int realLine = mRealLines[i];
                canvas.drawText("" + (realLine), mDrawingRect.left, baseline, mPaintNumbers);
            }
        }
        if (mShowLines) {
            canvas.drawLine(lineX, mDrawingRect.top, lineX, mDrawingRect.bottom, mPaintNumbers);
        }

        getLineBounds(mLineCount - 1, mLineBounds);
        if (mMaxSize != null) {
            mMaxSize.y = mLineBounds.bottom;
            mMaxSize.x = Math.max(mMaxSize.x + mPadding - mDrawingRect.width(), 0);
            mMaxSize.y = Math.max(mMaxSize.y + mPadding - mDrawingRect.height(), 0);
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        return false;
    }

    public void updateFromSettings() {
        String name = mEditorSetting.getString(getContext().getString(R.string.key_code_theme));
        IEditorColorScheme theme = ThemeManager.getTheme(name, getContext());
        setCodeTheme(theme);

        Typeface editorFont = mEditorSetting.getEditorFont();
        setTypeface(editorFont);
        mPaintNumbers.setTypeface(editorFont);

        setHorizontallyScrolling(!mEditorSetting.isWrapText());
        setOverScrollMode(OVER_SCROLL_ALWAYS);

        setTextSize(TypedValue.COMPLEX_UNIT_SP, mEditorSetting.getEditorTextSize());
        mPaintNumbers.setTextSize(getTextSize());

        mShowLines = mEditorSetting.isShowLines();

        if (mShowLines) {
            mLinePadding = calculateLinePadding();
            setPadding(mLinePadding, mPadding, mPadding, mPadding);
        } else {
            setPadding(mPadding, mPadding, mPadding, mPadding);
        }
        mWordWrap = mEditorSetting.isWrapText();
        if (mWordWrap) {
            setHorizontalScrollBarEnabled(false);
        } else {
            setHorizontalScrollBarEnabled(true);
        }

        postInvalidate();
        refreshDrawableState();

        if (mEditorSetting.useImeKeyboard()) {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        } else {
            setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        }  // use Fling when scrolling settings ?
        if (mEditorSetting.flingToScroll()) {
            mTedScroller = new Scroller(getContext());
            mMaxSize = new Point();
        } else {
            mTedScroller = null;
            mMaxSize = null;
        }

    }

    private int calculateLinePadding() {
        int count = getLineCount();
        int result = (int) (Math.floor(Math.log10(count)) + 1);

        float width = mPaintNumbers.measureText("0", 0, 1);
        result = (int) ((result * width) + width * 0.5f + mPadding);
        return result;
    }

    @Override
    protected boolean getDefaultEditable() {
        return true;
    }

    @Override
    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    /**
     * This method used to set text and high light text
     */
    public void setTextHighlighted(CharSequence text) {
        mLineErrors.clear();
        setText(text);
        refresh();
    }

    public void refresh() {
        mHandler.removeCallbacks(mPostHighlight);
        mHandler.postDelayed(mPostHighlight, SYNTAX_DELAY_MILLIS_SHORT);
    }

    public String getCleanText() {
        return getText().toString();
    }

    /**
     * Gets the first lineInfo that is visible on the screen.
     */
    @SuppressWarnings("unused")
    public int getFirstLineIndex() {
        int scrollY;
        if (mVerticalScroll != null) {
            scrollY = mVerticalScroll.getScrollY();
        } else {
            scrollY = getScrollY();
        }
        Layout layout = getLayout();
        if (layout != null) {
            return layout.getLineForVertical(scrollY);
        }
        return -1;
    }

    /**
     * Gets the last visible lineInfo number on the screen.
     *
     * @return last lineInfo that is visible on the screen.
     */
    public int getLastLineIndex() {
        int height;
        if (mVerticalScroll != null) {
            height = mVerticalScroll.getHeight();
        } else {
            height = getHeight();
        }
        int scrollY;
        if (mVerticalScroll != null) {
            scrollY = mVerticalScroll.getScrollY();
        } else {
            scrollY = getScrollY();
        }
        Layout layout = getLayout();
        if (layout != null) {
            return layout.getLineForVertical(scrollY + height);
        }
        return -1;
    }

    private <T> void removeSpan(Editable e, Class<T> clazz, int start, int end) {
        T spans[] = e.getSpans(start, end, clazz);
        for (T span : spans) {
            e.removeSpan(span);
        }
    }

    private void highlightLineError(Editable e) {
        try {
            removeSpan(e, ErrorSpan.class, 0, length());
            //high light error lineInfo
            for (LineNumber lineNumber : mLineErrors) {
                Layout layout = getLayout();
                int line = lineNumber.getLine();
                int temp = line;
                while (mRealLines[temp] < line) temp++;
                line = temp;
                if (layout != null && line < getLineCount()) {
                    int lineStart = getLayout().getLineStart(line);
                    int lineEnd = getLayout().getLineEnd(line);
                    lineStart += lineNumber.getColumn();

                    //check if it contains offset from start index error to
                    //(start + offset) index
                    if (lineNumber.getLength() > -1) {
                        lineEnd = lineStart + lineNumber.getLength();
                    }

                    //normalize
                    lineStart = Math.max(0, lineStart);
                    lineEnd = Math.min(lineEnd, getText().length());

                    if (lineStart < lineEnd) {
                        e.setSpan(new ErrorSpan(mCodeTheme.getErrorColor()),
                                lineStart,
                                lineEnd,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }

                }
            }
        } catch (Exception ignored) {
        }
    }

    public void replaceAll(String what, String replace, boolean regex, boolean matchCase) {
        Pattern pattern;
        if (regex) {
            if (matchCase) {
                pattern = Pattern.compile(what);
            } else {
                pattern = Pattern.compile(what, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        } else {
            if (matchCase) {
                pattern = Pattern.compile(Pattern.quote(what));
            } else {
                pattern = Pattern.compile(Pattern.quote(what), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        }
        setText(getText().toString().replaceAll(pattern.toString(), replace));
    }

    /**
     * move cursor to lineInfo
     *
     * @param line - lineInfo in editor, start at 0
     */
    public void goToLine(int line) {
        Layout layout = getLayout();
        line = Math.min(line - 1, getLineCount() - 1);
        line = Math.max(0, line);
        if (layout != null) {
            int index = layout.getLineEnd(line);
            setSelection(index);
        }
    }

    /**
     * @param line   - current line
     * @param column - column of line
     * @return Position (in pixels) for edittext at line and column
     */
    public Point getDebugPosition(int line, int column, int gravity) {
        Layout layout = getLayout();
        if (layout != null) {
            int pos = layout.getLineStart(line) + column;

            int baseline = layout.getLineBaseline(line);
            int ascent = layout.getLineAscent(line);

            int offsetHorizontal = (int) layout.getPrimaryHorizontal(pos) + mLinePadding; //x

            float y;
            int offsetVertical = 0;

            if (gravity == Gravity.BOTTOM) {
                y = baseline + ascent;
                if (mVerticalScroll != null) {
                    offsetVertical = (int) ((y + mCharHeight) - mVerticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) ((y + mCharHeight) - getScrollY());
                }
                return new Point(offsetHorizontal, offsetVertical);
            } else if (gravity == Gravity.TOP) {
                y = layout.getLineTop(line);
                if (mVerticalScroll != null) {
                    offsetVertical = (int) (y - mVerticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) (y - getScrollY());
                }
                return new Point(offsetHorizontal, offsetVertical);
            }

            return new Point(offsetHorizontal, offsetVertical);
        }
        return new Point();
    }

    @Override
    public void onPopupChangePosition() {
        try {
            Layout layout = getLayout();
            if (layout != null) {
                int pos = getSelectionStart();
                int line = layout.getLineForOffset(pos);
                int baseline = layout.getLineBaseline(line);
                int ascent = layout.getLineAscent(line);

                float x = layout.getPrimaryHorizontal(pos);
                float y = baseline + ascent;

                int offsetHorizontal = (int) x + mLinePadding;
                setDropDownHorizontalOffset(offsetHorizontal);

                int heightVisible = getHeightVisible();
                int offsetVertical = 0;
                if (mVerticalScroll != null) {
                    offsetVertical = (int) ((y + mCharHeight) - mVerticalScroll.getScrollY());
                } else {
                    offsetVertical = (int) ((y + mCharHeight) - getScrollY());
                }

                int tmp = offsetVertical + getDropDownHeight() + mCharHeight * 2;
                if (tmp < heightVisible) {
                    tmp = offsetVertical + mCharHeight;
                    setDropDownVerticalOffset(tmp);
                } else {
                    tmp = offsetVertical - getDropDownHeight() - mCharHeight;
                    setDropDownVerticalOffset(tmp);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void setVerticalScroll(@Nullable ScrollView verticalScroll) {
        this.mVerticalScroll = verticalScroll;
    }

    /**
     * highlight find word
     *
     * @param what     - input
     * @param regex    - is java regex
     * @param wordOnly - find word only
     */
    public void find(String what, boolean regex, boolean wordOnly, boolean matchCase) {
        Pattern pattern;
        if (regex) {
            if (matchCase) {
                pattern = Pattern.compile(what);
            } else {
                pattern = Pattern.compile(what, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
        } else {
            if (wordOnly) {
                if (matchCase) {
                    pattern = Pattern.compile("\\s" + what + "\\s");
                } else {
                    pattern = Pattern.compile("\\s" + Pattern.quote(what) + "\\s", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
            } else {
                if (matchCase) {
                    pattern = Pattern.compile(Pattern.quote(what));
                } else {
                    pattern = Pattern.compile(Pattern.quote(what), Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
                }
            }
        }
        Editable e = getEditableText();
        int count = 0;
        for (Matcher m = pattern.matcher(e); m.find(); ) {
            count++;
            e.setSpan(new BackgroundColorSpan(mCodeTheme.getErrorColor()),
                    m.start(),
                    m.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        Toast.makeText(getContext(), "Count: " + count, Toast.LENGTH_SHORT).show();
        mIsFinding = true;
    }

    public void pinLine(@Nullable LineNumber lineNumber) {
        Layout layout = getLayout();
        Editable e = getEditableText();

        if (lastPinLine < getLineCount() && lastPinLine >= 0) {
            int lineStart = getLayout().getLineStart(lastPinLine);
            int lineEnd = getLayout().getLineEnd(lastPinLine);
            BackgroundColorSpan[] backgroundColorSpan = e.getSpans(lineStart, lineEnd,
                    BackgroundColorSpan.class);
            for (BackgroundColorSpan colorSpan : backgroundColorSpan) {
                e.removeSpan(colorSpan);
            }
        }
        if (lineNumber == null) return;
        if (layout != null && lineNumber.getLine() < getLineCount()) {
            try {
                int lineStart = getLayout().getLineStart(lineNumber.getLine());
                int lineEnd = getLayout().getLineEnd(lineNumber.getLine());
                lineStart += lineNumber.getColumn();

                //normalize
                lineStart = Math.max(0, lineStart);
                lineEnd = Math.min(lineEnd, getText().length());

                if (lineStart < lineEnd) {
                    e.setSpan(new BackgroundColorSpan(mCodeTheme.getErrorColor()),
                            lineStart,
                            lineEnd,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                lastPinLine = lineNumber.getLine();
            } catch (Exception ignored) {
            }
        }
    }

    public void highlightText() {
        if (mIsFinding) return;

        disableTextChangedListener();
        highlight(false);
        highlightLineError(getText());
        enableTextChangedListener();
    }

    /**
     * remove span from start to end
     */
    private void clearSpans(Editable e, int start, int end) {
        removeSpan(e, ForegroundColorSpan.class, start, end);
        removeSpan(e, BackgroundColorSpan.class, start, end);
        removeSpan(e, UnderlineSpan.class, start, end);
        removeSpan(e, StyleSpan.class, start, end);
    }

    public void highlight(boolean newText) {
        Editable editable = getText();
        if (editable.length() == 0) return;

        int editorHeight = getHeightVisible();

        int firstVisibleIndex;
        int lastVisibleIndex;
        if (!newText && editorHeight > 0) {
            if (mVerticalScroll != null && getLayout() != null) {
                firstVisibleIndex = getLayout().getLineStart(getFirstLineIndex());
            } else {
                firstVisibleIndex = 0;
            }
            if (mVerticalScroll != null && getLayout() != null) {
                lastVisibleIndex = getLayout().getLineStart(getLastLineIndex());
            } else {
                lastVisibleIndex = getText().length();
            }
        } else {
            firstVisibleIndex = 0;
            lastVisibleIndex = CHARS_TO_COLOR;
        }
        // normalize
        if (firstVisibleIndex < 0) firstVisibleIndex = 0;
        if (lastVisibleIndex > editable.length()) lastVisibleIndex = editable.length();
        if (firstVisibleIndex > lastVisibleIndex) firstVisibleIndex = lastVisibleIndex;

        //clear all span for firstVisibleIndex to lastVisibleIndex
        clearSpans(editable, firstVisibleIndex, lastVisibleIndex);

        CharSequence textToHighlight = editable.subSequence(firstVisibleIndex, lastVisibleIndex);
        mCodeHighlighter.highlight(editable, textToHighlight, firstVisibleIndex);
        applyTabWidth(editable, firstVisibleIndex, lastVisibleIndex);
    }

    public void enableTextChangedListener() {
        if (!enabledChangeListener) {
            addTextChangedListener(mChangeListener);
            enabledChangeListener = true;
        }
    }

    public void disableTextChangedListener() {
        enabledChangeListener = false;
        removeTextChangedListener(mChangeListener);
    }

    public void updateTextHighlight() {
        if (hasSelection() || mHandler == null) {
            return;
        }
        mHandler.removeCallbacks(mPostHighlight);
        mHandler.postDelayed(mPostHighlight, SYNTAX_DELAY_MILLIS_LONG);
    }

    public void showKeyboard() {
        requestFocus();
        InputMethodManager inputMethodManager =
                (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void highlightAll() {
        mCodeHighlighter.highlight(getText(), getText(), 0);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (mBracketHighlighter != null) {
            mBracketHighlighter.onSelectChange(selStart, selEnd);
        }
    }

    public void disableTextWatcher() {
        disableTextChangedListener();
        mEnableSyntaxParser = false;
    }

    public void enableTextWatcher() {
        enableTextChangedListener();
        mEnableSyntaxParser = true;
    }


    /**
     * Class that listens to changes in the text.
     */
    private final class EditTextChangeListener
            implements TextWatcher {

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mIsFinding = false;
        }

        public void afterTextChanged(Editable s) {
            mLineErrors.clear();
            updateTextHighlight();
        }
    }
}
