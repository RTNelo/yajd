package me.rotatingticket.yajd;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import me.rotatingticket.yajd.viewmodel.FullTextTranslateActivityViewModel;

public class FullTextTranslateActivity extends AppCompatActivity {

    private FullTextTranslateActivityViewModel viewModel;

    private EditText srcEditText;
    private TextView destEditText;
    private FloatingActionButton  translateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_text_translate);

        viewModel = ViewModelProviders.of(this)
              .get(FullTextTranslateActivityViewModel.class);

        translateBtn = findViewById(R.id.btn_translate);
        translateBtn.setEnabled(false);

        srcEditText = findViewById(R.id.edit_text_src);
        srcEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                translateBtn.setEnabled(s.length() != 0);
            }
        });

        destEditText = findViewById(R.id.edit_text_dest);

        viewModel.getNetworkNotification().observe(this, notification -> {
            Toast.makeText(this, notification, Toast.LENGTH_SHORT).show();
        });
    }

    public void translate(View view) {
        setTranslatingState(true);
        viewModel.translate(srcEditText.getText().toString()).observe(this, result -> {
            if (result != null) {
                destEditText.setText(result);
            }
            setTranslatingState(false);
        });
    }

    /**
     * Toggle the tranlsating mode.
     * @param on Is the translating mode turned on.
     */
    private void setTranslatingState(boolean on) {
        if (on) {
            translateBtn.setImageDrawable(getResources().getDrawable(
                  R.drawable.ic_sync, this.getTheme()
            ));
            RotateAnimation rotateAnimation = new RotateAnimation(
                  360,
                  0,
                  RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                  RotateAnimation.RELATIVE_TO_SELF, 0.5f
            );
            rotateAnimation.setDuration(1000);
            rotateAnimation.setRepeatCount(RotateAnimation.INFINITE);
            translateBtn.startAnimation(rotateAnimation);
        } else {
            translateBtn.setImageDrawable(getResources().getDrawable(
                  R.drawable.ic_translate, this.getTheme()
            ));
            translateBtn.clearAnimation();
        }
    }
}
