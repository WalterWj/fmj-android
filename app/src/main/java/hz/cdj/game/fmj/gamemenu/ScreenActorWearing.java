package hz.cdj.game.fmj.gamemenu;

import hz.cdj.game.fmj.GameView;
import hz.cdj.game.fmj.Global;
import hz.cdj.game.fmj.characters.Player;
import hz.cdj.game.fmj.gamemenu.ScreenGoodsList.Mode;
import hz.cdj.game.fmj.goods.BaseGoods;
import hz.cdj.game.fmj.goods.GoodsEquipment;
import hz.cdj.game.fmj.graphics.TextRender;
import hz.cdj.game.fmj.graphics.Util;
import hz.cdj.game.fmj.scene.ScreenMainGame;
import hz.cdj.game.fmj.views.BaseScreen;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;

public class ScreenActorWearing extends BaseScreen {

	private Point[] mPos;
	private GoodsEquipment[] mEquipments;
	private String[] mItemName = {"装饰", "装饰", "护腕", "脚蹬", "手持", "身穿", "肩披", "头戴"};
	private int mCurItem = 0;
	
	private int mActorIndex = -1;
	
	private boolean showingDesc = false;
	private Bitmap bmpName = Util.getFrameBitmap(238, 20);
	private Bitmap bmpDesc = Util.getFrameBitmap(238, 75);
	private byte[] mTextName;
	private byte[] mTextDesc;
	private int mToDraw = 0; // 当前要画的描述中的字节
	private int mNextToDraw = 0; // 下一个要画的描述中的字节
	private Stack<Integer> mStackLastToDraw = new Stack<Integer>(); // 保存上次描述所画位置
	private static Rect sRectDesc = new Rect(12, 91, 244, 158);
	
	public ScreenActorWearing() {
		if (ScreenMainGame.sPlayerList.size() > 0) {
			mEquipments = ScreenMainGame.sPlayerList.get(0).getEquipmentsArray();
			mActorIndex = 0;
		}
		mPos = new Point[] { // w 25
				new Point(4, 3),
				new Point(4, 30),
				new Point(21, 59),
				new Point(51, 65),
				new Point(80, 61),
				new Point(109, 46),
				new Point(107, 9),
				new Point(79, 2)
		};
	}
	
	private byte[] getGBKBytes(String s) {
		try {
			return s.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new byte[0];
	}

	@Override
	public void update(long delta) {
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(Global.COLOR_WHITE);
		canvas.drawBitmap(Util.bmpChuandai, Global.SCREEN_WIDTH - Util.bmpChuandai.getWidth(), 0, null);
		
		// 画装备
		for (int i = 0; i < 8; i++) {
			if (mEquipments[i] != null) {
				mEquipments[i].draw(canvas, mPos[i].x + 1, mPos[i].y + 1);
			}
		}
		canvas.drawRect(mPos[mCurItem].x, mPos[mCurItem].y, mPos[mCurItem].x + 25, mPos[mCurItem].y + 25, Util.sBlackPaint);
		
		// 画人物头像、姓名
		if (mActorIndex >= 0) {
			Player p = ScreenMainGame.sPlayerList.get(mActorIndex);
			p.drawHead(canvas, 44, 12);
			TextRender.drawText(canvas, p.getName(), 30, 40);
		}
		
		if (showingDesc) {
			// 明细模式：部位名上移到描述框上方，避免重叠
			TextRender.drawText(canvas, mItemName[mCurItem], 120, 70);
			canvas.drawBitmap(bmpName, 9, 80, null);
			canvas.drawBitmap(bmpDesc, 9, 88, null);
			TextRender.drawText(canvas, mTextName, 9 + 3, 80 + 3);
			mNextToDraw = TextRender.drawText(canvas, mTextDesc, mToDraw, sRectDesc);
		} else {
			TextRender.drawText(canvas, mItemName[mCurItem], 120, 80);
		}
	}

	private void resetDesc() {
		if (showingDesc) {
			showingDesc = false;
			mToDraw = mNextToDraw = 0;
			mStackLastToDraw.clear();
		}
	}
	
	@Override
	public void onKeyDown(int key) {
		if (key == Global.KEY_DOWN && mCurItem < 8 - 1) {
			++mCurItem;
			resetDesc();
		} else if (key == Global.KEY_UP && mCurItem > 0) {
			--mCurItem;
			resetDesc();
		} else if (key == Global.KEY_RIGHT && mActorIndex < ScreenMainGame.sPlayerList.size() - 1) {
			++mActorIndex;
			mEquipments = ScreenMainGame.sPlayerList.get(mActorIndex).getEquipmentsArray();
			resetDesc();
		} else if (key == Global.KEY_LEFT && mActorIndex > 0) {
			--mActorIndex;
			mEquipments = ScreenMainGame.sPlayerList.get(mActorIndex).getEquipmentsArray();
			resetDesc();
		} else if (showingDesc) {
			if (key == Global.KEY_PAGEDOWN) {
				if (mNextToDraw < mTextDesc.length) {
					mStackLastToDraw.push(mToDraw);
					mToDraw = mNextToDraw;
				}
			} else if (key == Global.KEY_PAGEUP && mToDraw != 0) {
				if (!mStackLastToDraw.isEmpty()) {
					mToDraw = mStackLastToDraw.pop();
				}
			}
		}
	}

	@Override
	public void onKeyUp(int key) {
		if (key == Global.KEY_CANCEL) {
			GameView.getInstance().popScreen();
		} else if (key == Global.KEY_ENTER) {
			if (!showingDesc && mEquipments[mCurItem] != null) {
				showingDesc = true;
				mTextName = getGBKBytes(mEquipments[mCurItem].getName());
				mTextDesc = getGBKBytes(mEquipments[mCurItem].getDescription());
			} else { // 更换或卸下装备
				resetDesc();
				List<BaseGoods> equipList = getTheEquipList(Player.sEquipTypes[mCurItem]);
				if (equipList.size() > 0) {
					// 有可更换的装备，进入物品列表选择
					GameView.getInstance().pushScreen(new ScreenGoodsList(equipList,
							new ScreenGoodsList.OnItemSelectedListener() {
								
								@Override
								public void onItemSelected(BaseGoods goods) {
									Player actor = ScreenMainGame.sPlayerList.get(mActorIndex);
									if (goods.canPlayerUse(actor.getIndex())) {
										GameView.getInstance().popScreen();
										GameView.getInstance().pushScreen(new ScreenChgEquipment(actor, (GoodsEquipment) goods));
									} else {
										Util.showMessage("不能装备!", 1000);
									}
								}
							}, Mode.Use));
				} else if (mEquipments[mCurItem] != null) {
					// 没有可更换的装备，但当前槽位有装备 → 直接卸下
					Player actor = ScreenMainGame.sPlayerList.get(mActorIndex);
					GoodsEquipment oldEquip = mEquipments[mCurItem];
					actor.takeOff(oldEquip.getType());
					Player.sGoodsList.addGoods(oldEquip.getType(), oldEquip.getIndex());
					Util.showMessage("已卸下" + oldEquip.getName(), 1500);
				} else {
					Util.showMessage("没有可装备的物品", 1000);
				}
			}
		}
	}
	
	private List<BaseGoods> getTheEquipList(int type) {
		List<BaseGoods> tmplist = new LinkedList<BaseGoods>();
		Iterator<BaseGoods> iter = Player.sGoodsList.getEquipList().iterator();
		while (iter.hasNext()) {
			BaseGoods g = iter.next();
			if (g.getType() == Player.sEquipTypes[mCurItem]) { // 找到所有与当前选择类型相同的装备
				tmplist.add(g);
			}
		}
		return tmplist;
	}
	

}
