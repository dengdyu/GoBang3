package com.example.gobang3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GameCallBack , AICallBack ,View.OnClickListener{

    private FiveChessView fiveChessView;//五子棋基本功能
    private TextView userScoreTv, aiScoreTv;//分数
    private ImageView userChessIv, aiChessIv;//显示玩家/ai执子
    private AI ai;
    private PopupWindow chooseChess;//玩家选择执子窗口，TODO：加上选择难度功能

    //private Button restartB;//重新开始按钮
    private Button retractB;//悔棋按钮
    /*private Button exitB;//退出按钮*/
    private TextView whiteTurn, blackTurn;//显示谁的回合
    public boolean isUserBlack;//用户执黑棋吗
    private RadioGroup difficultyB;
    public int difficulty = -3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        //初始化ai
        ai = new AI(fiveChessView.getChessArray(), this);
        //view加载完成
        //注册全局布局监听器
        fiveChessView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {//获取屏幕的宽和高
                //初始化PopupWindow
                WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                initPop(wm.getDefaultDisplay().getWidth(), wm.getDefaultDisplay().getHeight());
            }
        });
        retractB = findViewById(R.id.retract);
        retractB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                fiveChessView.undoMove1();
                ai.undoMove2();
            }
        });
        difficultyB.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener () {
            //一旦单选框状态改变
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //根据id确定选择对象
                if(checkedId == R.id.easy){
                    ai.setAiDiff(-1);
                    fiveChessView.resetGame();
                }else if(checkedId == R.id.hard){
                    ai.setAiDiff(-3);
                    fiveChessView.resetGame();
                }
            }
        });

        /*retractB = findViewById(R.id.retract);//TODO 退出功能
        exitB = findViewById(R.id.exit_game);

        exitB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

            }
        });*/
        whiteTurn.setVisibility(View.GONE);//显示
        blackTurn.setVisibility(View.GONE);//隐藏
    }
    private void initViews() {
        //五子棋UI
        fiveChessView = (FiveChessView) findViewById(R.id.five_chess_view);
        fiveChessView.setCallBack(this);
        //显示用户以及ai得分
        userScoreTv = (TextView) findViewById(R.id.user_score_tv);
        aiScoreTv = (TextView) findViewById(R.id.ai_score_tv);
        //显示玩家/ai执子
        userChessIv = (ImageView) findViewById(R.id.user_chess_iv);
        aiChessIv = (ImageView) findViewById(R.id.ai_chess_iv);
        //玩家/ai回合标识
        whiteTurn = findViewById(R.id.white_turn);
        blackTurn = findViewById(R.id.black_turn);
        difficultyB = findViewById(R.id.difficulty);
        //restartB = findViewById(R.id.restart_game);
        //重开游戏设置点击事件
        findViewById(R.id.restart_game).setOnClickListener(this);
    }

        //初始化PopupWindow
        private void initPop(int width, int height) {
            if (chooseChess == null) {
                View view = View.inflate(this, R.layout.pop_choose_chess, null);
                ImageButton white = (ImageButton) view.findViewById(R.id.choose_white);
                ImageButton black = (ImageButton) view.findViewById(R.id.choose_black);
                white.setOnClickListener(this);
                black.setOnClickListener(this);
                chooseChess = new PopupWindow(view, width, height);
                chooseChess.setOutsideTouchable(false);//外部区域设置为不可点击
                chooseChess.showAtLocation(fiveChessView, Gravity.CENTER, 0, 0);//显示在屏幕上
            }
        }
    @Override
    public void GameOver(int winner) {
        //更新游戏胜利局数
        updateWinInfo();
        switch (winner) {
            case FiveChessView.BLACK_WIN:
                showToast("黑棋胜利！");//TODO：改成不是Toast
                break;
            case FiveChessView.NO_WIN:
                showToast("平局！");
                break;
            case FiveChessView.WHITE_WIN:
                showToast("白棋胜利！");
                break;
        }
    }

    //更新游戏胜利局数
    private void updateWinInfo(){
        userScoreTv.setText(fiveChessView.getUserScore()+" ");
        aiScoreTv.setText(fiveChessView.getAiScore()+" ");
    }

    @Override
    public void ChangeGamer(boolean isBlack) {
        //ai回合
        ai.aiBout();
        //更改当前落子
        if(isUserBlack) {
            whiteTurn.setVisibility(View.VISIBLE);//显示
            blackTurn.setVisibility(View.GONE);//隐藏
        } else {
            blackTurn.setVisibility(View.VISIBLE);
            whiteTurn.setVisibility(View.GONE);
        }
    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void aiAtTheBell() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //更新UI
                fiveChessView.postInvalidate();
                //检查游戏是否结束
                fiveChessView.checkAiGameOver();
                //设置为玩家回合
                fiveChessView.setUserBout(true);
                //更改当前落子
                if(isUserBlack) {
                    blackTurn.setVisibility(View.VISIBLE);
                    whiteTurn.setVisibility(View.GONE);
                } else {
                    whiteTurn.setVisibility(View.VISIBLE);
                    blackTurn.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.restart_game) {
            //显示PopupWindow
            chooseChess.showAtLocation(fiveChessView, Gravity.CENTER, 0, 0);
            //重新开始游戏
            fiveChessView.resetGame();
        } else if (viewId == R.id.choose_black) {
            changeUI(true);
            chooseChess.dismiss();
        } else if (viewId == R.id.choose_white) {
            changeUI(false);
            chooseChess.dismiss();
        }
    }

    //根据玩家选择执子，更新UI
    private void changeUI(boolean isUserBlack) {
        this.isUserBlack = isUserBlack;
        if (isUserBlack) {
            //玩家选择黑棋
            fiveChessView.setUserChess(FiveChessView.BLACK_CHESS);
            ai.setAiChess(FiveChessView.WHITE_CHESS);
            //玩家先手
            fiveChessView.setUserBout(true);
            //更改当前落子
            userChessIv.setBackgroundResource(R.drawable.black_chess);
            aiChessIv.setBackgroundResource(R.drawable.white_chess);
            whiteTurn.setVisibility(View.GONE);
            blackTurn.setVisibility(View.VISIBLE);
        } else {
            //玩家选择白棋
            fiveChessView.setUserChess(FiveChessView.WHITE_CHESS);
            fiveChessView.setUserBout(false);
            //ai先手
            ai.setAiChess(FiveChessView.BLACK_CHESS);
            ai.aiBout();
            //更改当前落子
            userChessIv.setBackgroundResource(R.drawable.white_chess);
            aiChessIv.setBackgroundResource(R.drawable.black_chess);
            blackTurn.setVisibility(View.VISIBLE);
            whiteTurn.setVisibility(View.GONE);
        }
    }


}