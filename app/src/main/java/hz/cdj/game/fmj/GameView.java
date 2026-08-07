package hz.cdj.game.fmj;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.ListIterator;
import java.util.Stack;

import hz.cdj.game.fmj.graphics.TextRender;
import hz.cdj.game.fmj.graphics.Util;
import hz.cdj.game.fmj.lib.DatLib;
import hz.cdj.game.fmj.scene.ScreenMainGame;
import hz.cdj.game.fmj.script.ScriptProcess;
import hz.cdj.game.fmj.views.BaseScreen;
import hz.cdj.game.fmj.views.ScreenAnimation;
import hz.cdj.game.fmj.views.ScreenMenu;
import hz.cdj.game.fmj.views.ScreenSaveLoadGame;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private static GameView instance;

    private Stack<BaseScreen> mScreenStack;
    private Context mContext;

    // Internal game canvas (160x106)
    private Bitmap mGameBitmap;
    private Canvas mGameCanvas;

    // Game loop
    private Thread mGameThread;
    private boolean mKeepRunning = false;
    private SurfaceHolder mHolder;

    // Touch controls
    private TouchControls mTouchControls;

    // D-pad repeat for continuous movement
    private volatile boolean mDpadHolding = false;
    private long mDpadRepeatTimer = 0;
    private static final long DPAD_REPEAT_INITIAL = 200; // 首次重复前等待
    private static final long DPAD_REPEAT_INTERVAL = 80;  // 重复间隔

    public GameView(Context context) {
        super(context);
        mContext = context;
        instance = this;

        mHolder = getHolder();
        mHolder.addCallback(this);

        mGameBitmap = Bitmap.createBitmap(Global.SCREEN_WIDTH, Global.SCREEN_HEIGHT, Bitmap.Config.ARGB_8888);
        mGameCanvas = new Canvas(mGameBitmap);

        mTouchControls = new TouchControls();

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mTouchControls.handleTouch(event);
            }
        });
    }

    public static GameView getInstance() {
        return instance;
    }

    public Context getGameContext() {
        return mContext;
    }

    public void initGame() throws IOException {
        DatLib.init();
        TextRender.init();
        Util.init();
        ScriptProcess.init();

        mScreenStack = new Stack<>();
        mScreenStack.push(new ScreenAnimation(247));
    }

    public void changeScreen(int screenCode) {
        BaseScreen tmp = null;
        switch (screenCode) {
            case Global.SCREEN_DEV_LOGO:
                tmp = new ScreenAnimation(247);
                break;
            case Global.SCREEN_GAME_LOGO:
                tmp = new ScreenAnimation(248);
                break;
            case Global.SCREEN_MENU:
                tmp = new ScreenMenu();
                break;
            case Global.SCREEN_MAIN_GAME:
                tmp = new ScreenMainGame();
                break;
            case Global.SCREEN_GAME_FAIL:
                tmp = new ScreenAnimation(249);
                break;
            case Global.SCREEN_SAVE_GAME:
                tmp = new ScreenSaveLoadGame(ScreenSaveLoadGame.Operate.SAVE);
                break;
            case Global.SCREEN_LOAD_GAME:
                tmp = new ScreenSaveLoadGame(ScreenSaveLoadGame.Operate.LOAD);
                break;
        }
        if (tmp != null) {
            mScreenStack.clear();
            mScreenStack.push(tmp);
        }
        System.gc();
    }

    public void pushScreen(BaseScreen screen) {
        mScreenStack.push(screen);
    }

    public void popScreen() {
        mScreenStack.pop();
    }

    public BaseScreen getCurScreen() {
        return mScreenStack.peek();
    }

    public void keyDown(int key) {
        if (mScreenStack == null || mScreenStack.isEmpty()) return;
        synchronized (mScreenStack) {
            mScreenStack.peek().onKeyDown(key);
        }
    }

    public void keyUp(int key) {
        if (mScreenStack == null || mScreenStack.isEmpty()) return;
        synchronized (mScreenStack) {
            mScreenStack.peek().onKeyUp(key);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mKeepRunning = true;
        mGameThread = new Thread(this, "logic update");
        mGameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mKeepRunning = false;
        try {
            if (mGameThread != null) {
                mGameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long curTime, lastTime = System.currentTimeMillis();

        while (mKeepRunning) {
            curTime = System.currentTimeMillis();
            long delta = curTime - lastTime;
            lastTime = curTime;

            if (mScreenStack == null || mScreenStack.isEmpty()) {
                try {
                    Thread.sleep(Global.TIME_GAMELOOP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            // D-pad repeat for continuous movement
            if (mDpadHolding) {
                mDpadRepeatTimer -= delta;
                if (mDpadRepeatTimer <= 0) {
                    mDpadRepeatTimer = DPAD_REPEAT_INTERVAL;
                    int key = mTouchControls.dpadCurrentKey;
                    if (key != -1) {
                        synchronized (mScreenStack) {
                            mScreenStack.peek().onKeyDown(key);
                        }
                    }
                }
            }

            synchronized (mScreenStack) {
                // Update
                mScreenStack.peek().update(delta);

                // Draw to internal game canvas
                ListIterator<BaseScreen> iter = mScreenStack.listIterator(mScreenStack.size());
                while (iter.hasPrevious()) {
                    BaseScreen tmp = iter.previous();
                    if (!tmp.isPopup()) {
                        break;
                    }
                }

                if (mGameCanvas != null) {
                    mGameCanvas.drawColor(Color.BLACK);
                    while (iter.hasNext()) {
                        iter.next().draw(mGameCanvas);
                    }
                }
            }

            // Render to screen
            Canvas screenCanvas = null;
            try {
                screenCanvas = mHolder.lockCanvas();
                if (screenCanvas != null) {
                    renderToScreen(screenCanvas);
                }
            } finally {
                if (screenCanvas != null) {
                    mHolder.unlockCanvasAndPost(screenCanvas);
                }
            }

            try {
                Thread.sleep(Global.TIME_GAMELOOP);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void renderToScreen(Canvas screenCanvas) {
        int screenW = screenCanvas.getWidth();
        int screenH = screenCanvas.getHeight();

        // Calculate scale to fit while maintaining aspect ratio
        float scaleX = (float) screenW / Global.SCREEN_WIDTH;
        float scaleY = (float) screenH / Global.SCREEN_HEIGHT;
        float scale = Math.min(scaleX, scaleY);

        int scaledW = (int) (Global.SCREEN_WIDTH * scale);
        int scaledH = (int) (Global.SCREEN_HEIGHT * scale);
        int offsetX = (screenW - scaledW) / 2;
        int offsetY = (screenH - scaledH) / 2;

        // Fill background black
        screenCanvas.drawColor(Color.BLACK);

        // Draw game bitmap scaled
        RectF dst = new RectF(offsetX, offsetY, offsetX + scaledW, offsetY + scaledH);
        screenCanvas.drawBitmap(mGameBitmap, null, dst, null);

        // Draw touch controls on top
        mTouchControls.draw(screenCanvas, screenW, screenH);
    }

    // ==================== Touch Controls ====================

    public class TouchControls {
        // D-pad area (left side)
        private float dpadCenterX, dpadCenterY, dpadRadius;
        // Action buttons (right side)
        private float[] btnX, btnY;
        private float btnRadius;
        private int[] btnKeys = {Global.KEY_ENTER, Global.KEY_CANCEL, Global.KEY_PAGEUP, Global.KEY_PAGEDOWN};
        private String[] btnLabels = {"确定", "返回", "上页", "下页"};

        // Cheat toggle buttons (top area)
        private RectF mToggleEncounter;    // 遇敌开关
        private RectF mToggleExpGold;      // 百倍开关
        private boolean mToggleEncounterDown = false;
        private boolean mToggleExpGoldDown = false;

        // Tracking
        private int dpadPointerId = -1;
        private int dpadCurrentKey = -1;
        private int[] btnPointerIds = {-1, -1, -1, -1};

        // Paint for drawing controls
        private Paint ctrlPaint;
        private Paint textPaint;

        public TouchControls() {
            ctrlPaint = new Paint();
            ctrlPaint.setAntiAlias(true);

            textPaint = new Paint();
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
        }

        public boolean handleTouch(MotionEvent event) {
            int action = event.getActionMasked();

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    int idx = event.getActionIndex();
                    int id = event.getPointerId(idx);
                    float x = event.getX(idx);
                    float y = event.getY(idx);

                    // Check D-pad
                    float dx = x - dpadCenterX;
                    float dy = y - dpadCenterY;
                    if (Math.sqrt(dx * dx + dy * dy) < dpadRadius * 1.3f) {
                        dpadPointerId = id;
                        int key = getDpadKey(dx, dy);
                        if (key != -1 && key != dpadCurrentKey) {
                            if (dpadCurrentKey != -1) {
                                keyUp(dpadCurrentKey);
                            }
                            dpadCurrentKey = key;
                            keyDown(key);
                        }
                        // Start repeat timer for continuous movement
                        mDpadHolding = true;
                        mDpadRepeatTimer = DPAD_REPEAT_INITIAL;
                        return true;
                    }

                    // Check toggle buttons first
                    if (mToggleEncounter != null && mToggleEncounter.contains(x, y)) {
                        Global.sCheatNoEncounter = !Global.sCheatNoEncounter;
                        mToggleEncounterDown = true;
                        return true;
                    }
                    if (mToggleExpGold != null && mToggleExpGold.contains(x, y)) {
                        Global.sCheatExpGold = !Global.sCheatExpGold;
                        mToggleExpGoldDown = true;
                        return true;
                    }

                    // Check action buttons
                    for (int i = 0; i < 4; i++) {
                        float bdx = x - btnX[i];
                        float bdy = y - btnY[i];
                        if (Math.sqrt(bdx * bdx + bdy * bdy) < btnRadius * 1.3f) {
                            btnPointerIds[i] = id;
                            keyDown(btnKeys[i]);
                            return true;
                        }
                    }
                    return true;
                }

                case MotionEvent.ACTION_MOVE: {
                    for (int i = 0; i < event.getPointerCount(); i++) {
                        int id = event.getPointerId(i);
                        float x = event.getX(i);
                        float y = event.getY(i);

                        if (id == dpadPointerId) {
                            float dx = x - dpadCenterX;
                            float dy = y - dpadCenterY;
                            int key = getDpadKey(dx, dy);
                            if (key != dpadCurrentKey) {
                                if (dpadCurrentKey != -1) {
                                    keyUp(dpadCurrentKey);
                                }
                                dpadCurrentKey = key;
                                if (key != -1) {
                                    keyDown(key);
                                }
                            }
                        }
                    }
                    return true;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP: {
                    int idx = event.getActionIndex();
                    int id = event.getPointerId(idx);

                    if (id == dpadPointerId) {
                        if (dpadCurrentKey != -1) {
                            keyUp(dpadCurrentKey);
                        }
                        dpadCurrentKey = -1;
                        dpadPointerId = -1;
                        mDpadHolding = false;
                    }

                    for (int i = 0; i < 4; i++) {
                        if (btnPointerIds[i] == id) {
                            keyUp(btnKeys[i]);
                            btnPointerIds[i] = -1;
                        }
                    }
                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    if (dpadCurrentKey != -1) {
                        keyUp(dpadCurrentKey);
                    }
                    dpadCurrentKey = -1;
                    dpadPointerId = -1;
                    mDpadHolding = false;
                    for (int i = 0; i < 4; i++) {
                        if (btnPointerIds[i] != -1) {
                            keyUp(btnKeys[i]);
                            btnPointerIds[i] = -1;
                        }
                    }
                    return true;
                }
            }
            return true;
        }

        private int getDpadKey(float dx, float dy) {
            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);
            if (absDx < dpadRadius * 0.25f && absDy < dpadRadius * 0.25f) {
                return -1; // center, no key
            }
            if (absDx > absDy) {
                return dx > 0 ? Global.KEY_RIGHT : Global.KEY_LEFT;
            } else {
                return dy > 0 ? Global.KEY_DOWN : Global.KEY_UP;
            }
        }

        public void draw(Canvas canvas, int screenW, int screenH) {
            // Layout: D-pad on left, buttons on right
            int baseSize = Math.min(screenW, screenH);
            dpadRadius = baseSize * 0.12f;
            dpadCenterX = dpadRadius * 1.8f;
            dpadCenterY = screenH - dpadRadius * 1.8f;

            btnRadius = baseSize * 0.07f;
            float btnSpacing = btnRadius * 2.5f;
            btnX = new float[4];
            btnY = new float[4];

            // Two columns x two rows on the right side
            btnX[0] = screenW - btnRadius * 1.5f;
            btnY[0] = screenH - btnRadius * 3.5f;  // 确定 (top right)
            btnX[1] = screenW - btnRadius * 3.5f;
            btnY[1] = screenH - btnRadius * 1.5f;  // 返回 (bottom left)
            btnX[2] = screenW - btnRadius * 3.5f;
            btnY[2] = screenH - btnRadius * 3.5f;  // 上页 (top left)
            btnX[3] = screenW - btnRadius * 1.5f;
            btnY[3] = screenH - btnRadius * 1.5f;  // 下页 (bottom right)

            // ===== Draw cheat toggle buttons at top =====
            float toggleW = screenW * 0.18f;
            float toggleH = screenH * 0.05f;
            float toggleY = screenH * 0.015f;
            float padding = screenW * 0.02f;

            mToggleEncounter = new RectF(padding, toggleY, padding + toggleW, toggleY + toggleH);
            mToggleExpGold = new RectF(screenW - padding - toggleW, toggleY, screenW - padding, toggleY + toggleH);

            drawToggle(canvas, mToggleEncounter, "遇敌", !Global.sCheatNoEncounter);
            drawToggle(canvas, mToggleExpGold, "百倍", Global.sCheatExpGold);

            // Draw D-pad
            ctrlPaint.setColor(Color.argb(80, 128, 128, 128));
            canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, ctrlPaint);
            ctrlPaint.setColor(Color.argb(120, 200, 200, 200));
            ctrlPaint.setStyle(Paint.Style.STROKE);
            ctrlPaint.setStrokeWidth(2);
            canvas.drawCircle(dpadCenterX, dpadCenterY, dpadRadius, ctrlPaint);
            ctrlPaint.setStyle(Paint.Style.FILL);

            // D-pad arrows
            ctrlPaint.setColor(Color.argb(180, 255, 255, 255));
            float arrowSize = dpadRadius * 0.3f;
            // Up
            canvas.drawLine(dpadCenterX, dpadCenterY - dpadRadius * 0.3f,
                    dpadCenterX, dpadCenterY - dpadRadius * 0.7f, ctrlPaint);
            canvas.drawLine(dpadCenterX, dpadCenterY - dpadRadius * 0.7f,
                    dpadCenterX - arrowSize * 0.5f, dpadCenterY - dpadRadius * 0.5f, ctrlPaint);
            canvas.drawLine(dpadCenterX, dpadCenterY - dpadRadius * 0.7f,
                    dpadCenterX + arrowSize * 0.5f, dpadCenterY - dpadRadius * 0.5f, ctrlPaint);
            // Down
            canvas.drawLine(dpadCenterX, dpadCenterY + dpadRadius * 0.3f,
                    dpadCenterX, dpadCenterY + dpadRadius * 0.7f, ctrlPaint);
            canvas.drawLine(dpadCenterX, dpadCenterY + dpadRadius * 0.7f,
                    dpadCenterX - arrowSize * 0.5f, dpadCenterY + dpadRadius * 0.5f, ctrlPaint);
            canvas.drawLine(dpadCenterX, dpadCenterY + dpadRadius * 0.7f,
                    dpadCenterX + arrowSize * 0.5f, dpadCenterY + dpadRadius * 0.5f, ctrlPaint);
            // Left
            canvas.drawLine(dpadCenterX - dpadRadius * 0.3f, dpadCenterY,
                    dpadCenterX - dpadRadius * 0.7f, dpadCenterY, ctrlPaint);
            canvas.drawLine(dpadCenterX - dpadRadius * 0.7f, dpadCenterY,
                    dpadCenterX - dpadRadius * 0.5f, dpadCenterY - arrowSize * 0.5f, ctrlPaint);
            canvas.drawLine(dpadCenterX - dpadRadius * 0.7f, dpadCenterY,
                    dpadCenterX - dpadRadius * 0.5f, dpadCenterY + arrowSize * 0.5f, ctrlPaint);
            // Right
            canvas.drawLine(dpadCenterX + dpadRadius * 0.3f, dpadCenterY,
                    dpadCenterX + dpadRadius * 0.7f, dpadCenterY, ctrlPaint);
            canvas.drawLine(dpadCenterX + dpadRadius * 0.7f, dpadCenterY,
                    dpadCenterX + dpadRadius * 0.5f, dpadCenterY - arrowSize * 0.5f, ctrlPaint);
            canvas.drawLine(dpadCenterX + dpadRadius * 0.7f, dpadCenterY,
                    dpadCenterX + dpadRadius * 0.5f, dpadCenterY + arrowSize * 0.5f, ctrlPaint);

            // Draw action buttons
            for (int i = 0; i < 4; i++) {
                ctrlPaint.setColor(Color.argb(60, 128, 128, 128));
                canvas.drawCircle(btnX[i], btnY[i], btnRadius, ctrlPaint);
                ctrlPaint.setColor(Color.argb(120, 200, 200, 200));
                ctrlPaint.setStyle(Paint.Style.STROKE);
                ctrlPaint.setStrokeWidth(2);
                canvas.drawCircle(btnX[i], btnY[i], btnRadius, ctrlPaint);
                ctrlPaint.setStyle(Paint.Style.FILL);

                // Label
                textPaint.setColor(Color.argb(200, 255, 255, 255));
                textPaint.setTextSize(btnRadius * 0.8f);
                canvas.drawText(btnLabels[i], btnX[i], btnY[i] + btnRadius * 0.3f, textPaint);
            }
        }

        private void drawToggle(Canvas canvas, RectF rect, String label, boolean isOn) {
            // Background
            if (isOn) {
                ctrlPaint.setColor(Color.argb(140, 0, 200, 0));
            } else {
                ctrlPaint.setColor(Color.argb(100, 100, 100, 100));
            }
            canvas.drawRoundRect(rect, 6, 6, ctrlPaint);

            // Border
            ctrlPaint.setColor(Color.argb(160, 180, 180, 180));
            ctrlPaint.setStyle(Paint.Style.STROKE);
            ctrlPaint.setStrokeWidth(1.5f);
            canvas.drawRoundRect(rect, 6, 6, ctrlPaint);
            ctrlPaint.setStyle(Paint.Style.FILL);

            // Text
            String text = label + (isOn ? ":开" : ":关");
            textPaint.setColor(Color.argb(220, 255, 255, 255));
            textPaint.setTextSize(rect.height() * 0.55f);
            canvas.drawText(text, rect.centerX(), rect.centerY() + rect.height() * 0.2f, textPaint);
        }
    }
}
