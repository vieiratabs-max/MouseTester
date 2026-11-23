package com.example.mousetest;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Shows what mouse buttons / keys your mouse sends. No permissions needed. */
public class MainActivity extends Activity {
    private TextView logView;
    private ScrollView scroll;
    private final StringBuilder buf = new StringBuilder();
    private int lines = 0;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(18, 18, 22));
        root.setPadding(24, 24, 24, 24);

        TextView title = new TextView(this);
        title.setText("Mouse Tester\nClick, scroll and press your side buttons. Each event is listed below.");
        title.setTextColor(Color.WHITE);
        title.setTextSize(16);
        root.addView(title);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        Button clear = new Button(this);
        clear.setText("Clear");
        clear.setOnClickListener(v -> { buf.setLength(0); lines = 0; logView.setText(""); });
        Button exit = new Button(this);
        exit.setText("Exit");
        exit.setOnClickListener(v -> finish());
        row.addView(clear);
        row.addView(exit);
        root.addView(row);

        scroll = new ScrollView(this);
        logView = new TextView(this);
        logView.setTextColor(Color.rgb(120, 255, 160));
        logView.setTypeface(Typeface.MONOSPACE);
        logView.setTextSize(13);
        scroll.addView(logView);
        root.addView(scroll, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        setContentView(root);
        log("Ready. Press a side button.");
    }

    private static String buttons(int s) {
        StringBuilder sb = new StringBuilder();
        if ((s & MotionEvent.BUTTON_PRIMARY) != 0) sb.append("LEFT ");
        if ((s & MotionEvent.BUTTON_SECONDARY) != 0) sb.append("RIGHT ");
        if ((s & MotionEvent.BUTTON_TERTIARY) != 0) sb.append("MIDDLE ");
        if ((s & MotionEvent.BUTTON_BACK) != 0) sb.append("SIDE-BACK ");
        if ((s & MotionEvent.BUTTON_FORWARD) != 0) sb.append("SIDE-FORWARD ");
        return sb.length() == 0 ? "none" : sb.toString().trim();
    }

    private void log(String line) {
        buf.append(++lines).append(": ").append(line).append('\n');
        if (lines > 80) {
            int cut = buf.indexOf("\n") + 1;
            buf.delete(0, cut);
        }
        logView.setText(buf);
        scroll.post(() -> scroll.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void logMotion(String kind, MotionEvent e) {
        int a = e.getActionMasked();
        // Skip noisy movement
        if (a == MotionEvent.ACTION_MOVE || a == MotionEvent.ACTION_HOVER_MOVE
                || a == MotionEvent.ACTION_HOVER_ENTER || a == MotionEvent.ACTION_HOVER_EXIT) return;
        String extra = "";
        if (a == MotionEvent.ACTION_SCROLL) {
            extra = " scroll=" + e.getAxisValue(MotionEvent.AXIS_VSCROLL);
        }
        log(kind + " " + MotionEvent.actionToString(a)
                + " buttons=[" + buttons(e.getButtonState()) + "]"
                + " actionButton=" + e.getActionButton()
                + " tool=" + e.getToolType(0) + extra);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        logMotion("TOUCH", e);
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent e) {
        logMotion("MOUSE", e);
        return super.dispatchGenericMotionEvent(e);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getRepeatCount() == 0) {
            String dev = e.getDevice() != null ? e.getDevice().getName() : "?";
            log("KEY " + (e.getAction() == KeyEvent.ACTION_DOWN ? "down" : "up")
                    + " " + KeyEvent.keyCodeToString(e.getKeyCode())
                    + " (" + e.getKeyCode() + ") scan=" + e.getScanCode()
                    + " source=0x" + Integer.toHexString(e.getSource())
                    + " device=" + dev);
        }
        // Keep Back from closing the app so we can see it. Use the Exit button.
        if (e.getKeyCode() == KeyEvent.KEYCODE_BACK) return true;
        return super.dispatchKeyEvent(e);
    }
}
