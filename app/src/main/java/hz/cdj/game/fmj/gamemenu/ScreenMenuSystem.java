package hz.cdj.game.fmj.gamemenu;

import hz.cdj.game.fmj.GameView;
import hz.cdj.game.fmj.Global;
import hz.cdj.game.fmj.graphics.TextRender;
import hz.cdj.game.fmj.graphics.Util;
import hz.cdj.game.fmj.views.BaseScreen;
import hz.cdj.game.fmj.views.ScreenSaveLoadGame;
import hz.cdj.game.fmj.views.ScreenSaveLoadGame.Operate;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class ScreenMenuSystem extends BaseScreen {
	
	private int index = 0;
	private String[] str = {"读入进度", "存储进度", "游戏设置", "结束游戏"};
	
	private int frameW = 150, frameH = 110;
	private int frameX = (Global.SCREEN_WIDTH - frameW) / 2;
	private int frameY = (Global.SCREEN_HEIGHT - frameH) / 2;
	private int strX = frameX + 10, strBaseY = frameY + 10;
	private Bitmap bmpFrame = Util.getFrameBitmap(frameW, frameH);
	
	public ScreenMenuSystem() {
	}

	@Override
	public void update(long delta) {
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawBitmap(bmpFrame, frameX, frameY, null);
		for (int i = 0; i < 4; i++) {
			if (i == index) {
				TextRender.drawSelText(canvas, str[i], strX, strBaseY + i * 22);
			} else {
				TextRender.drawText(canvas, str[i], strX, strBaseY + i * 22);
			}
		}
	}

	@Override
	public void onKeyDown(int key) {
		if (key == Global.KEY_UP) {
			--index;
			if (index < 0) index = 3;
		} else if (key == Global.KEY_DOWN) {
			++index;
			if (index > 3) index = 0;
		}
	}

	@Override
	public void onKeyUp(int key) {
		if (key == Global.KEY_CANCEL) {
			GameView.getInstance().popScreen();
		} else if (key == Global.KEY_ENTER) {
			switch (index) {
			case 0:
				GameView.getInstance().pushScreen(new ScreenSaveLoadGame(Operate.LOAD));
				break;
				
			case 1:
				GameView.getInstance().pushScreen(new ScreenSaveLoadGame(Operate.SAVE));
				break;
				
			case 2:
				break;
				
			case 3:
				GameView.getInstance().changeScreen(Global.SCREEN_MENU);
				break;
			}
		}
	}

	@Override
	public boolean isPopup() {
		return true;
	}

}
