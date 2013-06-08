package com.nuraytec.circulargauge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class Gauge extends View {
	int StrokeColor;// 表盘框颜色
	int FillColor;// 表盘背景渐变颜色
	int FillEndColor;
	int PointColor;// 指针颜色
	int OptimalRangeColor; // 正常范围的区域颜色
	int AboveOptimalRangeColor;// 超过高报范围颜色
	int BelowOptimalRangeColor;// 超过低报范围颜色
	int DialTextColor; // 文字颜色
	int MinorTickColor; // 小刻度颜色
	int MajorTickColor;// 大刻度颜色
	float GaugeThickness;// 外表盘厚度
	int MajorDivisionsCount;// 大刻度个数
	int MinorDivisionsCount;// 小刻度个数
	boolean IsLogarithm;// 是否对数表盘
	String DialText;// 文字内容
	String DialUnit; // 文字单位
	float MaxValue;// 最大值
	float MinValue;// 最小值
	float ScaleStartAngle; // 开始角度
	float ScaleSweepAngle;// 表盘整个角度
	float OptimalRangeStartValue;// 低报值
	float OptimalRangeEndValue;// 高报值
	float CurrentValue;// 当前值

	public Gauge(final Context context, final AttributeSet attrs,
			final int defStyle) {
		super(context, attrs, defStyle);
		readAttrs(context, attrs, defStyle);
	}

	public Gauge(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public Gauge(final Context context) {
		this(context, null, 0);
	}

	private void readAttrs(final Context context, final AttributeSet attrs,
			final int defStyle) {
		final TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.Gauge, defStyle, 0);
		StrokeColor = a.getColor(R.styleable.Gauge_StrokeColor, Color.GRAY);
		FillColor = a.getColor(R.styleable.Gauge_FillColor, Color.BLACK);
		FillEndColor = a.getColor(R.styleable.Gauge_FillEndColor, FillColor);
		PointColor = a.getColor(R.styleable.Gauge_PointColor, Color.RED);
		OptimalRangeColor = a.getColor(R.styleable.Gauge_OptimalRangeColor,
				Color.GREEN);
		AboveOptimalRangeColor = a.getColor(
				R.styleable.Gauge_AboveOptimalRangeColor, Color.YELLOW);
		BelowOptimalRangeColor = a.getColor(
				R.styleable.Gauge_BelowOptimalRangeColor, Color.RED);
		DialTextColor = a
				.getColor(R.styleable.Gauge_DialTextColor, Color.WHITE);
		MinorTickColor = a.getColor(R.styleable.Gauge_MinorTickColor,
				Color.WHITE);
		MajorTickColor = a.getColor(R.styleable.Gauge_MajorTickColor,
				Color.WHITE);

		GaugeThickness = a.getFloat(R.styleable.Gauge_GaugeThickness, 10);
		MajorDivisionsCount = a.getInt(R.styleable.Gauge_MajorDivisionsCount,
				10);
		MinorDivisionsCount = a.getInt(R.styleable.Gauge_MinorDivisionsCount,
				10);
		IsLogarithm = a.getBoolean(R.styleable.Gauge_IsLogarithm, false);
		DialText = a.getString(R.styleable.Gauge_DialText);
		DialUnit = a.getString(R.styleable.Gauge_DialUnit);
		MaxValue = a.getFloat(R.styleable.Gauge_MaxValue, 10);
		MinValue = a.getFloat(R.styleable.Gauge_MinValue, 0);
		ScaleStartAngle = a.getFloat(R.styleable.Gauge_ScaleStartAngle, 30);
		ScaleSweepAngle = a.getFloat(R.styleable.Gauge_ScaleSweepAngle, 300);
		OptimalRangeStartValue = a.getFloat(
				R.styleable.Gauge_OptimalRangeStartValue, 6);
		OptimalRangeEndValue = a.getFloat(
				R.styleable.Gauge_OptimalRangeEndValue, 8);
		CurrentValue = a.getFloat(R.styleable.Gauge_CurrentValue, 0);

		a.recycle();
		mPaint.setAntiAlias(true);
	}

	float mSize = 200;
	float mGaugeRadius = 100;
	float mRangeIndicatorRadius = 78;
	float mRangeIndicatorThickness = 10;
	float mScaleRadius = 74;
	// float mScaleLabelRadius=62;
	// float mMajorTickSize=10;
	// float mMinorTickSize=3;
	float mPointerLength = 62;
	float mPointerThickness = 16;
	float mPointerCapRadius = 25;

	float mRangeIndicatorLightRadius = 12;
	float mRangeIndicatorLightOffset = 50;
	float mDialTextOffset = -35;
	float mCurrentValueOffset = 74;
	float mScaleValuePrecision = 4;
	final RectF mRect = new RectF();
	final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private void drawGauge() {
		mSize = Math.min(getWidth(), getHeight());
		mGaugeRadius = mSize / 2f;
		mRangeIndicatorRadius = mGaugeRadius - GaugeThickness;

		mRangeIndicatorThickness = 0.1f * mRangeIndicatorRadius;
		mScaleRadius = 0.9f * mRangeIndicatorRadius;
		mPointerLength = 0.7f * mRangeIndicatorRadius;

		mPointerCapRadius = 0.12f * mRangeIndicatorRadius;
		mPointerThickness = mPointerCapRadius / 2f;

		mBackgroundPaint = new Paint();
		mBackgroundPaint.setFilterBitmap(true);
		if (null != mBackground) {
			// Let go of the old background
			mBackground.recycle();
		}
		mBackground = Bitmap.createBitmap(getWidth(), getHeight(),
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(mBackground);
		canvas.translate((mSize == getHeight()) ? (getWidth() - mSize) / 2 : 0,
				(mSize == getWidth()) ? (getHeight() - mSize) / 2 : 0);

		drawGaugeStroke(canvas);
		drawGaugeFill(canvas);
		drawGaugeRange(canvas);

		drawScale(canvas);

	}

	private void drawGaugeStroke(final Canvas canvas) {
		mRect.set(GaugeThickness / 2f, GaugeThickness / 2f, mSize
				- GaugeThickness / 2f, mSize - GaugeThickness / 2f);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(GaugeThickness);
		mPaint.setColor(StrokeColor);
		canvas.drawOval(mRect, mPaint);
	}

	private void drawGaugeFill(final Canvas canvas) {
		mRect.set(mRect.left + GaugeThickness / 2f, mRect.top + GaugeThickness
				/ 2f, mRect.right - GaugeThickness / 2f, mRect.bottom
				- GaugeThickness / 2f);
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setShader(new RadialGradient(mGaugeRadius, mGaugeRadius,
				mGaugeRadius, FillEndColor, FillColor, TileMode.CLAMP));
		canvas.drawOval(mRect, mPaint);

	}

	private void drawGaugeRange(final Canvas canvas) {
		mPaint.reset();
		mPaint.setAntiAlias(true);
		mPaint.setColor(Color.RED);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mRangeIndicatorThickness);
		mRect.set(mRect.left + mRangeIndicatorThickness / 2f, mRect.top
				+ mRangeIndicatorThickness / 2f, mRect.right
				- mRangeIndicatorThickness / 2f, mRect.bottom
				- mRangeIndicatorThickness / 2f);
		canvas.drawArc(mRect, ScaleStartAngle + 90, ScaleSweepAngle, false,
				mPaint);
	}

	private void drawScale(final Canvas canvas) {
		float mSubdivisionAngle = (360 - 2 * (ScaleStartAngle))
				/ (MajorDivisionsCount * MinorDivisionsCount);

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(mGaugeRadius, mGaugeRadius);
		canvas.rotate(ScaleStartAngle, 0, 0);

		Paint paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setColor(Color.GREEN);
		paint.setStrokeWidth(3);

		Paint tmpPaint = new Paint(paint); // 小刻度画笔对象
		tmpPaint.setStrokeWidth(1);
		tmpPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		tmpPaint.setAntiAlias(true);

		float y = mScaleRadius;
		int count = MajorDivisionsCount * MinorDivisionsCount; // 总刻度数

		for (int i = 0; i <= count; i++) {
			if (i % MinorDivisionsCount == 0) {
				canvas.drawLine(0f, y - 10f, 0f, y, paint);

				canvas.drawText(
						String.valueOf((int) ((i / MinorDivisionsCount) * (MaxValue / MajorDivisionsCount))),
						-6f, y - 13f, tmpPaint);

				// Path path = new Path();
				// path.addCircle(10,10,mScaleLabelRadius,Direction.CW);
				// canvas.drawTextOnPath(String.valueOf(i/MinorDivisionsCount),path,0,
				// 0,tmpPaint);

			} else {
				canvas.drawLine(0f, y - 5f, 0f, y, tmpPaint);
			}

			canvas.rotate(mSubdivisionAngle, 0f, 0f); // 旋转画纸

		}

		canvas.restore();
	}

	private void drawPointer(final Canvas canvas) {

		// 绘制指针
		Paint paint = new Paint();
		paint.setFlags(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Style.FILL);
		paint.setColor(PointColor);

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		canvas.translate(mGaugeRadius, mGaugeRadius);
		final float angle = getAngleForValue(CurrentValue);
		canvas.rotate(angle, 0, 0);

		float mNeedleWidth = mPointerThickness;
		float mNeedleHeight = mPointerLength;

		final float x = 0.5f, y = 0.5f;
		Path mNeedleLeftPath = new Path();
		mNeedleLeftPath.moveTo(x, y);
		mNeedleLeftPath.lineTo(x - mNeedleWidth, y);
		mNeedleLeftPath.lineTo(x, y - mNeedleHeight);
		mNeedleLeftPath.lineTo(x, y);
		mNeedleLeftPath.lineTo(x - mNeedleWidth, y);

		Path mNeedleRightPath = new Path();
		mNeedleRightPath.moveTo(x, y);
		mNeedleRightPath.lineTo(x + mNeedleWidth, y);
		mNeedleRightPath.lineTo(x, y - mNeedleHeight);
		mNeedleRightPath.lineTo(x, y);
		mNeedleRightPath.lineTo(x + mNeedleWidth, y);

		canvas.drawPath(mNeedleLeftPath, paint);
		canvas.drawPath(mNeedleRightPath, paint);

		Paint tmpPaint = new Paint(paint);
		tmpPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
		tmpPaint.setAntiAlias(true);
		tmpPaint.setColor(Color.GRAY);
		tmpPaint.setStrokeWidth(0.3f * mPointerCapRadius);
		canvas.drawCircle(0, 0, mPointerCapRadius, tmpPaint);
		tmpPaint.setStyle(Style.FILL);
		tmpPaint.setColor(Color.GREEN);
		canvas.drawCircle(0, 0, mPointerCapRadius - (0.3f * mPointerCapRadius)
				/ 2f, tmpPaint);

		canvas.restore();
	}

	private float getAngleForValue(final float value) {
		float valueTmp = value;
		if (value < MinValue)
			valueTmp = MinValue;
		if (value > MaxValue)
			valueTmp = MaxValue;

		return (ScaleSweepAngle * valueTmp / MaxValue + 180 + ScaleStartAngle) % 360;
	}

	@Override
	protected void onSizeChanged(int arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		super.onSizeChanged(arg0, arg1, arg2, arg3);
		drawGauge();
	}

	private Bitmap mBackground;
	private Paint mBackgroundPaint;

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(mBackground, 0, 0, mBackgroundPaint);
		canvas.translate((mSize == getHeight()) ? (getWidth() - mSize) / 2 : 0,
				(mSize == getWidth()) ? (getHeight() - mSize) / 2 : 0);

		drawPointer(canvas);
	}

	public void setTargetValue(final float value) {
		CurrentValue = value;
		invalidate();
	}

}
