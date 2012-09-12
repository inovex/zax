package com.inovex.zabbixmobile.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.inovex.zabbixmobile.R;

public class PredicateLayout extends ViewGroup {
    private final class RowMeasurement {
        private final int maxWidth;
        private final int widthMode;
        private int width;
        private int height;

        public RowMeasurement(int maxWidth, int widthMode) {
            this.maxWidth = maxWidth;
            this.widthMode = widthMode;
        }

        public void addChildDimensions(int childWidth, int childHeight) {
            width = getNewWidth(childWidth);
            height = Math.max(height, childHeight);
        }

        public int getHeight() {
            return height;
        }

        private int getNewWidth(int childWidth) {
            return width == 0 ? childWidth : width + horizontalSpacing + childWidth;
        }

        public int getWidth() {
            return width;
        }

        public boolean wouldExceedMax(int childWidth) {
            return widthMode == MeasureSpec.UNSPECIFIED ? false : getNewWidth(childWidth) > maxWidth;
        }
    }
    public static final int DEFAULT_HORIZONTAL_SPACING = 5;
    public static final int DEFAULT_VERTICAL_SPACING = 5;
    private final int horizontalSpacing;
    private final int verticalSpacing;

    private List<RowMeasurement> currentRows = Collections.emptyList();

    public PredicateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.PredicateLayout);
        horizontalSpacing = styledAttributes.getDimensionPixelSize(R.styleable.PredicateLayout_android_horizontalSpacing,
                DEFAULT_HORIZONTAL_SPACING);
        verticalSpacing = styledAttributes.getDimensionPixelSize(R.styleable.PredicateLayout_android_verticalSpacing,
                DEFAULT_VERTICAL_SPACING);
        styledAttributes.recycle();
    }

    private int createChildMeasureSpec(int childLayoutParam, int max, int parentMode) {
        int spec;
        if (childLayoutParam == LayoutParams.FILL_PARENT) {
            spec = MeasureSpec.makeMeasureSpec(max, MeasureSpec.EXACTLY);
        } else if (childLayoutParam == LayoutParams.WRAP_CONTENT) {
            spec = MeasureSpec.makeMeasureSpec(max, parentMode == MeasureSpec.UNSPECIFIED ? MeasureSpec.UNSPECIFIED
                    : MeasureSpec.AT_MOST);
        } else {
            spec = MeasureSpec.makeMeasureSpec(childLayoutParam, MeasureSpec.EXACTLY);
        }
        return spec;
    }

    protected int getHorizontalPadding() {
        return getPaddingLeft() + getPaddingRight();
    }

    private List<View> getLayoutChildren() {
        List<View> children = new ArrayList<View>();
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() != View.GONE) {
                children.add(child);
            }
        }
        return children;
    }

    protected int getVerticalPadding() {
        return getPaddingTop() + getPaddingBottom();
    }

    @Override
    protected void onLayout(boolean changed, int leftPosition, int topPosition, int rightPosition, int bottomPosition) {
        final int widthOffset = getMeasuredWidth() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();

        Iterator<RowMeasurement> rowIterator = currentRows.iterator();
        RowMeasurement currentRow = rowIterator.next();
        for (View child : getLayoutChildren()) {
            final int childWidth = child.getMeasuredWidth();
            final int childHeight = child.getMeasuredHeight();
            if (x + childWidth > widthOffset) {
                x = getPaddingLeft();
                y += currentRow.height + verticalSpacing;
                if (rowIterator.hasNext()) {
                    currentRow = rowIterator.next();
                }
            }
            child.layout(x, y, x + childWidth, y + childHeight);
            x += childWidth + horizontalSpacing;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int maxInternalWidth = MeasureSpec.getSize(widthMeasureSpec) - getHorizontalPadding();
        final int maxInternalHeight = MeasureSpec.getSize(heightMeasureSpec) - getVerticalPadding();
        List<RowMeasurement> rows = new ArrayList<RowMeasurement>();
        RowMeasurement currentRow = new RowMeasurement(maxInternalWidth, widthMode);
        rows.add(currentRow);
        for (View child : getLayoutChildren()) {
            LayoutParams childLayoutParams = child.getLayoutParams();
            int childWidthSpec = createChildMeasureSpec(childLayoutParams.width, maxInternalWidth, widthMode);
            int childHeightSpec = createChildMeasureSpec(childLayoutParams.height, maxInternalHeight, heightMode);
            child.measure(childWidthSpec, childHeightSpec);
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (currentRow.wouldExceedMax(childWidth)) {
                currentRow = new RowMeasurement(maxInternalWidth, widthMode);
                rows.add(currentRow);
            }
            currentRow.addChildDimensions(childWidth, childHeight);
        }

        int longestRowWidth = 0;
        int totalRowHeight = 0;
        for (int index = 0; index < rows.size(); index++) {
            RowMeasurement row = rows.get(index);
            totalRowHeight += row.getHeight();
            if (index < rows.size() - 1) {
                totalRowHeight += verticalSpacing;
            }
            longestRowWidth = Math.max(longestRowWidth, row.getWidth());
        }
        setMeasuredDimension(widthMode == MeasureSpec.EXACTLY ? MeasureSpec.getSize(widthMeasureSpec) : longestRowWidth
                + getHorizontalPadding(), heightMode == MeasureSpec.EXACTLY ? MeasureSpec.getSize(heightMeasureSpec)
                : totalRowHeight + getVerticalPadding());
        currentRows = Collections.unmodifiableList(rows);
    }
}
