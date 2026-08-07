package hz.cdj.game.fmj.gamemenu;

import hz.cdj.game.fmj.GameView;
import hz.cdj.game.fmj.Global;
import hz.cdj.game.fmj.characters.Player;
import hz.cdj.game.fmj.graphics.TextRender;
import hz.cdj.game.fmj.magic.MagicRestore;
import hz.cdj.game.fmj.scene.ScreenMainGame;
import hz.cdj.game.fmj.views.BaseScreen;
import android.graphics.Canvas;
import android.graphics.Rect;

public class ScreenUseMagic extends BaseScreen {
	
	private static Rect sNameRect = new Rect(4, 4, 252, 24);
	private MagicRestore mMagic;
	
	Player mScr;
	
	private int mCurActor = 0;
	
	public ScreenUseMagic(MagicRestore magic, Player scr) {
		mMagic = magic;
		mScr = scr;
	}

	@Override
	public void update(long delta) {
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawColor(Global.COLOR_WHITE);
		// 魔法名 — 顶部居中全宽
		TextRender.drawText(canvas, mMagic.getMagicName(), 0, sNameRect);
		// 人物属性 — 左侧
		Player actor = ScreenMainGame.sPlayerList.get(mCurActor);
		actor.drawState(canvas, 0);
		// 头像 — 右侧
		actor.drawHead(canvas, 200, 36);
	}

	@Override
	public void onKeyDown(int key) {
		if (key == Global.KEY_RIGHT && mCurActor < ScreenMainGame.sPlayerList.size() - 1) {
			++mCurActor;
		} else if (key == Global.KEY_LEFT && mCurActor > 0) {
			--mCurActor;
		}
	}

	@Override
	public void onKeyUp(int key) {
		if (key == Global.KEY_CANCEL) {
			GameView.getInstance().popScreen();
		} else if (key == Global.KEY_ENTER) {
			mMagic.use(mScr, ScreenMainGame.sPlayerList.get(mCurActor));
			GameView.getInstance().popScreen();
		}
	}

}
