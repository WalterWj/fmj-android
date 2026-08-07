package hz.cdj.game.fmj;

import android.graphics.Color;

public class Global {
	public static int COLOR_WHITE = Color.argb(255, 199, 237, 204);
	public static int COLOR_BLACK = Color.argb(255, 0, 0, 0);
	public static int COLOR_TRANSP = Color.argb(0, 0, 0, 0);

	// Canvas: 256x170, map area: 240x160 (15x10 tiles)
	public static final int SCREEN_WIDTH = 256;
	public static final int SCREEN_HEIGHT = 170;
	public static final int MAP_PIXEL_HEIGHT = SCREEN_HEIGHT - 10; // 160
	public static final int MAP_LEFT_OFFSET = 8;
	public static final int MAP_RIGHT_OFFSET = SCREEN_WIDTH - 8; // 248

	// 战斗场景缩放比例（原 160x96 → 256x160）
	public static final float COMBAT_SCALE_X = (float)SCREEN_WIDTH / 160f;       // 1.6
	public static final float COMBAT_SCALE_Y = (float)MAP_PIXEL_HEIGHT / 96f;    // ~1.667
	/** 战斗 x 坐标缩放辅助 */
	public static int csx(int x) { return Math.round(x * COMBAT_SCALE_X); }
	/** 战斗 y 坐标缩放辅助 */
	public static int csy(int y) { return Math.round(y * COMBAT_SCALE_Y); }

	// 系统 UI 缩放比例（原 160x106 → 256x170）
	public static final float SX = (float)SCREEN_WIDTH / 160f;       // 1.6
	public static final float SY = (float)SCREEN_HEIGHT / 106f;      // ~1.604

	public static final int KEY_UP = 1;
	public static final int KEY_DOWN = 2;
	public static final int KEY_LEFT = 3;
	public static final int KEY_RIGHT = 4;
	public static final int KEY_PAGEUP = 5;
	public static final int KEY_PAGEDOWN = 6;
	public static final int KEY_ENTER = 7;
	public static final int KEY_CANCEL = 8;

	public static final long TIME_GAMELOOP = 45;

	// 作弊开关
	public static boolean sCheatExpGold = false;     // 百倍经验金钱
	public static boolean sCheatNoEncounter = false;  // 不遇敌

	public static final int SCREEN_DEV_LOGO = 1;
	public static final int SCREEN_GAME_LOGO = 2;
	public static final int SCREEN_MENU = 3;
	public static final int SCREEN_MAIN_GAME = 4;
	public static final int SCREEN_GAME_FAIL = 5;
	public static final int SCREEN_SAVE_GAME = 6;
	public static final int SCREEN_LOAD_GAME = 7;
}
