package com.sanron.statusbarhelper;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.sanron.lib.StatusBarHelper;

public class MainActivity extends AppCompatActivity {

    Toolbar mToolbar;
    Switch mSwitchDark;
    private int currentBackgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mSwitchDark = (Switch) findViewById(R.id.switch_dark);
        setSupportActionBar(mToolbar);
        mSwitchDark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                StatusBarHelper.with(MainActivity.this).setDarkIcon(0.3f);
            }
        });
    }

    public void setFullscreen(View view) {
        StatusBarHelper.with(this).setLayoutFullScreen(true);
    }

    public void setNotFullscreen(View view) {
        StatusBarHelper.with(this).setLayoutFullScreen(false);
    }

    public void setStatusColor(final View view) {
        ColorPickerDialogBuilder
                .with(MainActivity.this)
                .setTitle("Choose color")
                .showColorEdit(true)
                .showAlphaSlider(true)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("ok", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                        currentBackgroundColor = selectedColor;
                        view.setBackgroundColor(selectedColor);
                        StatusBarHelper.with(MainActivity.this).setStatusBarColor(currentBackgroundColor);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .build()
                .show();
    }

    public void setToolbarPadding(View view) {
        StatusBarHelper.with(MainActivity.this).setPaddingTop(mToolbar);
    }

    public void cancelToolbarPadding(View view) {
        StatusBarHelper.with(MainActivity.this).removePaddingTop(mToolbar);
    }
}
