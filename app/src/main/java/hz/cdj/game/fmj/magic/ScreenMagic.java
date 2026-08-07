package hz.cdj.game.fmj.magic;

import hz.cdj.game.fmj.GameView;
import hz.cdj.game.fmj.Global;
import hz.cdj.game.fmj.graphics.TextRender;
import hz.cdj.game.fmj.views.BaseScreen;

import java.io.UnsupportedEncodingException;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;

public class ScreenMagic extends BaseScreen {
	
	public interface OnItemSelectedListener {
		public void onItemSelected(BaseMagic magic);
	}
	
	private OnItemSelectedListener mOnItemSelectedListener;
	
	private ResMagicChain mMagicChain;
	
	private int mLearnNum; // 独立存储，避免共享缓存的 ResMagicChain.learnNum 被覆盖
	
	private static final int ITEM_NUM = 2; // 界面上显示的条目数
	
	private int mFirstItemIndex = 0; // 界面上显示的第一个魔法的序号
	
	private int mCurItemIndex = 0; // 当前光标所在位置魔法的序号
	
	private Bitmap mBmpCursor = Bitmap.createBitmap(12, 11, Config.ARGB_8888);
	private Bitmap mBmpMarker = Bitmap.createBitmap(5, 8, Config.ARGB_8888);
	private Bitmap mBmpMarker2= Bitmap.createBitmap(5, 8, Config.ARGB_8888);
	
	private Rect mRectTop = new Rect(Global.csx(10), Global.csy(4), Global.csx(147), Global.csy(39));
	private Rect mRectBtm = new Rect(Global.csx(10), Global.csy(41), Global.csx(147), Global.csy(76));
	private Rect mRectDsp = new Rect(Global.csx(11), Global.csy(42), Global.csx(146), Global.csy(75));
	private int mToDraw = 0; // 当前要画的魔法描述中的字节
	private int mNextToDraw = 0; // 下一个要画的魔法描述中的字节
	private Stack<Integer> mStackLastToDraw = new Stack<Integer>(); // 保存上次魔法描述所画位置
	private Point mTextPos = new Point(Global.csx(10), Global.csy(77));
	private Paint mFramePaint = new Paint();
	
	public ScreenMagic(ResMagicChain magicChain, int learnNum, OnItemSelectedListener l) {
		if (magicChain == null || l == null) {
			throw new IllegalArgumentException("ScreenMagic construtor params can't be null.");
		}
		mMagicChain = magicChain;
		mLearnNum = learnNum;
		System.out.println("[ScreenMagic] created: learnNum=" + learnNum + " chainSize=" + magicChain.getMagicSum());
		mOnItemSelectedListener = l;
		mFramePaint.setColor(Global.COLOR_BLACK);
		mFramePaint.setStyle(Style.STROKE);
		mFramePaint.setStrokeWidth(1);
		
		createBmp();
	}
	
	private void createBmp() {
		Canvas canvas = new Canvas();
		Paint p = new Paint();
		p.setColor(Global.COLOR_BLACK);
		p.setStrokeWidth(1);
		p.setStyle(Style.STROKE);
		
		canvas.setBitmap(mBmpCursor);
		canvas.drawColor(Global.COLOR_WHITE);
		canvas.drawLine(8, 0, 11, 0, p);
		canvas.drawLine(11, 1, 11, 4, p);
		canvas.drawRect(6, 1, 7, 4, p);
		canvas.drawRect(7, 4, 10, 5, p);
		canvas.drawLine(7, 4, 0, 11, p);
		canvas.drawLine(8, 5, 2, 11, p);
		
		canvas.setBitmap(mBmpMarker);
		canvas.drawColor(Global.COLOR_WHITE);
		float[] pts = {2, 0,  4, 2,  4, 2, 4, 6, 4, 6, 2, 8, 2, 7, 0, 5, 0, 5, 0, 2, 0, 3, 3, 0, 2, 3, 2, 5};
		canvas.drawLines(pts, p);
		
		canvas.setBitmap(mBmpMarker2);
		canvas.drawColor(Global.COLOR_WHITE);
		canvas.drawLines(pts, p);
		float[] pts2 = {1, 1, 1, 6, 2, 0, 2, 8, 3, 2, 3, 6};
		canvas.drawLines(pts2, p);
	}

	@Override
	public void update(long delta) {
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(Global.COLOR_WHITE);
		if (mLearnNum == 0) {
			TextRender.drawText(canvas, "未学会任何魔法", Global.csx(11), Global.csy(5));
			return;
		}
		canvas.drawRect(mRectTop, mFramePaint);
		canvas.drawRect(mRectBtm, mFramePaint);
		TextRender.drawText(canvas, mMagicChain.getMagic(mFirstItemIndex).getMagicName(), Global.csx(11), Global.csy(5));
		if (mFirstItemIndex + 1 < mLearnNum) {
			TextRender.drawText(canvas, mMagicChain.getMagic(mFirstItemIndex + 1).getMagicName(), Global.csx(11), Global.csy(21));
		}
		mNextToDraw = TextRender.drawText(canvas, mMagicChain.getMagic(mCurItemIndex).getMagicDescription(), mToDraw, mRectDsp);
		TextRender.drawText(canvas, "耗真气:" + mMagicChain.getMagic(mCurItemIndex).getCostMp(), mTextPos.x, mTextPos.y);
		canvas.drawBitmap(mBmpCursor, Global.csx(100), mFirstItemIndex == mCurItemIndex ? Global.csy(10) : Global.csy(26), null);
		canvas.drawBitmap(mFirstItemIndex == 0 ? mBmpMarker : mBmpMarker2, Global.csx(135), Global.csy(6), null);
		canvas.drawBitmap(mBmpMarker, Global.csx(135), Global.csy(14), null);
		canvas.drawBitmap(mBmpMarker, Global.csx(135), Global.csy(22), null);
		canvas.drawBitmap(mFirstItemIndex + 2 < mLearnNum ? mBmpMarker2 : mBmpMarker, Global.csx(135), Global.csy(30), null);
	}

	@Override
	public void onKeyDown(int key) {
		if (key == Global.KEY_UP && mCurItemIndex > 0) {
			--mCurItemIndex;
			if (mCurItemIndex < mFirstItemIndex) {
				--mFirstItemIndex;
			}
			mToDraw = mNextToDraw = 0;
			mStackLastToDraw.clear();
		} else if (key == Global.KEY_DOWN && mCurItemIndex + 1 < mLearnNum) {
			++mCurItemIndex;
			if (mCurItemIndex >= mFirstItemIndex + ITEM_NUM) {
				++mFirstItemIndex;
			}
			mToDraw = mNextToDraw = 0;
			mStackLastToDraw.clear();
		} else if (key == Global.KEY_PAGEDOWN) {
			try {
				int len = mMagicChain.getMagic(mCurItemIndex).getMagicDescription().getBytes("GBK").length;
				if (mNextToDraw < len) {
					mStackLastToDraw.push(mToDraw); // 保存旧位置
					mToDraw = mNextToDraw; // 更新位置
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		} else if (key == Global.KEY_PAGEUP && mToDraw != 0) {
			if (!mStackLastToDraw.isEmpty()) {
				mToDraw = mStackLastToDraw.pop();
			}
		}
	}

	@Override
	public void onKeyUp(int key) {
		if (key == Global.KEY_ENTER) { // 回调接口
			mOnItemSelectedListener.onItemSelected(mMagicChain.getMagic(mCurItemIndex));
		} else if (key == Global.KEY_CANCEL) {
			GameView.getInstance().popScreen();
		}
	}

}
