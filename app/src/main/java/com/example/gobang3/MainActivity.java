package com.example.gobang3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GameCallBack {

    private FiveChessView fiveChessView;
    private TextView whiteWinTv,blackWinTv;
    private Button restartB;//重新开始按钮
    private Button retractB;//悔棋按钮
    private Button exitB;//悔棋按钮

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fiveChessView = (FiveChessView) findViewById(R.id.five_chess_view);
        fiveChessView.setCallBack(this);
        whiteWinTv = (TextView) findViewById(R.id.white_count_tv);
        blackWinTv = (TextView) findViewById(R.id.black_count_tv);
        restartB = findViewById(R.id.restart_game);
        retractB = findViewById(R.id.retract);
        exitB = findViewById(R.id.exit_game);

        restartB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                fiveChessView.resetGame();
            }
        });

        retractB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

            }
        });

        retractB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){

            }
        });
    }

    @Override
    public void GameOver(int winner) {
        //更新游戏胜利局数
        updateWinInfo();
        switch (winner) {
            case FiveChessView.BLACK_WIN:
                showToast("黑棋胜利！");
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
        whiteWinTv.setText(fiveChessView.getWhiteChessCount()+" ");
        blackWinTv.setText(fiveChessView.getBlackChessCount()+" ");
    }

    @Override
    public void ChangeGamer(boolean isWhite) {

    }

    private void showToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }


}