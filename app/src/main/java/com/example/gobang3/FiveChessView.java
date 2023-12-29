package com.example.gobang3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Stack;

/**
 * 五子棋基本功能
 */

public class FiveChessView extends View implements View.OnTouchListener {


    private Paint paint;//画笔
    private Paint paintB;//加粗线

    private int[][] chessArray;//棋子数组

    private boolean isBlack = true;//当前下棋顺序(默认黑棋先下)

    private boolean isGameOver = false;//游戏是否结束

    private Bitmap whiteChess;//白棋图像
    private Bitmap blackChess;//黑棋图像
    //Rect
    private Rect rect;//放棋的框

    private float len;//棋盘宽高

    private int GRID_NUMBER = 12;//棋盘格数

    private float preWidth;//格间距

    private float offset;//边距

    private GameCallBack callBack;//回调

    private int whiteChessCount, blackChessCount;//胜利次数

    private boolean isUserBout = true;//是否是玩家的回合

    private int userChess = WHITE_CHESS;//玩家执子颜色，默认白色

    private int userScore = 0, aiScore = 0;//玩家/AI胜利次数
    private Stack<Point> userMoves; // 用户下棋记录


    /**
     * 一些常量
     */
    //白棋
    public static final int WHITE_CHESS = 1;
    //黑棋
    public static final int BLACK_CHESS = 2;
    //无棋
    public static final int NO_CHESS = 0;
    //白棋赢
    public static final int WHITE_WIN = 101;
    //黑棋赢
    public static final int BLACK_WIN = 102;
    //平局
    public static final int NO_WIN = 103;

    /**
    * 构造方法
    */
    public FiveChessView(Context context) {
        this(context, null);
    }

    public FiveChessView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FiveChessView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //初始化Paint
        paint = new Paint();
        paintB = new Paint();//加粗
        //设置抗锯齿
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2);
        paintB.setAntiAlias(true);
        paintB.setStrokeWidth(10);
        paintB.setColor(Color.BLACK);
        paintB.setStrokeCap(Paint.Cap.ROUND);

        //初始化chessArray
        chessArray = new int[GRID_NUMBER][GRID_NUMBER];
        //初始化棋子图片bitmap
        whiteChess = BitmapFactory.decodeResource(context.getResources(), R.drawable.white_chess);
        blackChess = BitmapFactory.decodeResource(context.getResources(), R.drawable.black_chess);
        //初始化胜利局数
        whiteChessCount = 0;
        blackChessCount = 0;
        //初始化Rect
        rect = new Rect();
        //设置点击监听
        setOnTouchListener(this);
        userMoves = new Stack<>();//初始化用户下棋记录
    }

    /**
     * 重新测量宽高，确保宽高一样
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //获取高宽值
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        //获取宽高中较小的值
        int len = width > height ? height : width;
        //重新设置宽高
        setMeasuredDimension(len, len);
    }
    /**
     * 画棋盘、棋子
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //棋盘为一个GRID_NUMBER*GRID_NUMBER的正方形，所有棋盘宽高必须一样
        len = getWidth() > getHeight() ? getHeight() : getWidth();
        preWidth = len / GRID_NUMBER;
        //边距
        offset = preWidth / 2;
        //棋盘线条
        for (int i = 0; i < GRID_NUMBER; i++) {
            float start = i * preWidth + offset;
            //横线
            if(i == 0 || i == GRID_NUMBER - 1){
                canvas.drawLine(offset, start, len - offset, start, paintB);
                canvas.drawLine(start, offset, start, len - offset, paintB);
            }else {
                canvas.drawLine(offset, start, len - offset, start, paint);
                //竖线
                canvas.drawLine(start, offset, start, len - offset, paint);
            }
        }
        //绘制棋子
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                //rect中点坐标
                float rectX = offset + i * preWidth;
                float rectY = offset + j * preWidth;
                //设置rect位置
                rect.set((int) (rectX - offset*0.75), (int) (rectY - offset*0.75),
                        (int) (rectX + offset*0.75), (int) (rectY + offset*0.75));
                //遍历chessArray
                switch (chessArray[i][j]) {
                    case WHITE_CHESS:
                        //绘制白棋
                        canvas.drawBitmap(whiteChess, null, rect, paint);
                        break;
                    case BLACK_CHESS:
                        //绘制黑棋
                        canvas.drawBitmap(blackChess, null, rect, paint);
                        break;
                }
            }
        }
    }

    /**
     * 判断游戏结束
     */
    private void checkGameOver() {
        //获取落子的颜色(如果当前是白棋，则落子是黑棋)
        int chess = isBlack ? WHITE_CHESS : BLACK_CHESS;
        //棋盘是否填满
        boolean isFull = true;
        //遍历chessArray
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                //判断棋盘是否填满
                if (chessArray[i][j] != BLACK_CHESS && chessArray[i][j] != WHITE_CHESS) {
                    isFull = false;
                }
                //只需要判断落子是否五连即可
                if (chessArray[i][j] == chess) {
                    //判断五子相连
                    if (isFiveSame(i, j)) {
                        //五子相连游戏结束
                        isGameOver = true;
                        if (callBack != null) {
                            if (chess == WHITE_CHESS) {
                                whiteChessCount++;
                            } else {
                                blackChessCount++;
                            }

                            //判断玩家/AI 胜利
                            if (userChess == chess) {
                                userScore++;
                            } else {
                                aiScore++;
                            }

                            callBack.GameOver(chess == WHITE_CHESS ? WHITE_WIN : BLACK_WIN);
                        }
                        return;
                    }
                }
            }
        }
        //如果棋盘填满，平局结束
        if (isFull) {
            isGameOver = true;
            if (callBack != null) {
                callBack.GameOver(NO_WIN);
            }
        }
    }

    /**
     * 重开一局
     */
    public void resetGame() {
        isGameOver = false;
        //重置棋盘状态
        for (int i = 0; i < GRID_NUMBER; i++) {
            for (int j = 0; j < GRID_NUMBER; j++) {
                chessArray[i][j] = 0;
            }
        }
        //更新UI
        postInvalidate();
    }

    /**
     * 判断是否存在五子相连
     */
    private boolean isFiveSame(int x, int y) {
        //判断横向
        if (x + 4 <= GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x + 1][y] && chessArray[x][y] == chessArray[x + 2][y]
                    && chessArray[x][y] == chessArray[x + 3][y] && chessArray[x][y] == chessArray[x + 4][y]) {
                return true;
            }
        }
        //判断纵向
        if (y + 4 <= GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x][y + 1] && chessArray[x][y] == chessArray[x][y + 2]
                    && chessArray[x][y] == chessArray[x][y + 3] && chessArray[x][y] == chessArray[x][y + 4]) {
                return true;
            }
        }
        //判断斜向(左上到右下)
        if (y + 4 <= GRID_NUMBER && x + 4 <= GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x + 1][y + 1] && chessArray[x][y] == chessArray[x + 2][y + 2]
                    && chessArray[x][y] == chessArray[x + 3][y + 3] && chessArray[x][y] == chessArray[x + 4][y + 4]) {
                return true;
            }
        }
        //判断斜向(左下到右上)
        if (y - 4 >= 0 && x + 4 <= GRID_NUMBER) {
            if (chessArray[x][y] == chessArray[x + 1][y - 1] && chessArray[x][y] == chessArray[x + 2][y - 2]
                    && chessArray[x][y] == chessArray[x + 3][y - 3] && chessArray[x][y] == chessArray[x + 4][y - 4]) {
                return true;
            }
        }
        return false;
    }

    public void checkAiGameOver() {        //TODO ???
            isBlack = userChess == BLACK_CHESS;
        checkGameOver();
    }
    /**
     * 点击下棋的过程
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!isGameOver) {
                    //获取按下时的位置
                    float downX = event.getX();
                    float downY = event.getY();
                    //点击的位置在棋盘上
                    if (downX >= offset / 2 && downX <= len - offset / 2
                            && downY >= offset / 2 && downY <= len - offset / 2) {
                        //获取棋子对应的位置
                        int x = (int) (downX / preWidth);
                        int y = (int) (downY / preWidth);
                        //判断当前位置是否已经有子
                        if (chessArray[x][y] != WHITE_CHESS &&
                                chessArray[x][y] != BLACK_CHESS) {
                            //给数组赋值
                            chessArray[x][y] = userChess;
                            userMoves.push(new Point(x, y));//记录用户下棋位置
                            //修改当前落子颜色
                            isBlack = userChess == BLACK_CHESS;
                            //修改当前为电脑执子
                            isUserBout = false;
                            //更新棋盘
                            postInvalidate();
                            //判断是否结束
                            checkGameOver();
                            //回调当前执子
                            if (callBack != null) {
                                callBack.ChangeGamer(isBlack);
                            }
                        }
                    }
                } else if (isGameOver) {
                    Toast.makeText(getContext(), "游戏已经结束，请重新开始！",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return false;
    }

    public void undoMove1(){
        if (!isGameOver) {
            if(userMoves.size() >= 1){
                Point userLastMove = userMoves.pop();
                int userLastX = userLastMove.getX();
                int userLastY = userLastMove.getY();
                chessArray[userLastX][userLastY] = NO_CHESS;
                postInvalidate(); // 更新UI
            } else {
                Toast.makeText(getContext(), "没有棋了", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setCallBack(GameCallBack callBack) {
        this.callBack = callBack;
    }

    public int[][] getChessArray() {
        return chessArray;
    }

    public void setUserBout(boolean userBout) {
        isUserBout = userBout;
    }

    public void setUserChess(int userChess) {
        this.userChess = userChess;
    }

    public int getUserScore() {
        return userScore;
    }

    public int getAiScore() {
        return aiScore;
    }
}