/**
 * All rights reserved to Shai Mahfud.
 */

package com.shai_mahfud.mygooglepicturesearch.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shai_mahfud.mygooglepicturesearch.R;

/**
 * An EditText which enables the users to auto clear the input by tapping a button. I
 *
 * @author Shai Mahfud
 */
public class ClearableEditText extends RelativeLayout implements View.OnClickListener,
        TextWatcher {
    // Fields:
    /* The field where the user can type */
    private EditText textBox;
    /* Enables to clear the text in the text box */
    private ImageView clearButton;


    // Constructors:
    public ClearableEditText(Context context) {
        this(context, null, 0);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    // Methods:
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (clearButton.getVisibility() != View.VISIBLE) {
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.clearable_edit_text_clear_button:
                textBox.getText().clear();
                clearButton.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     *
     * @return The input text that currently exist in the text box
     */
    public String getText() {
        return textBox.getText().toString().trim();
    }

    /**
     * Sets input text into the text box
     *
     * @param text The text to set
     */
    public void setText(String text) {
        textBox.append(text);
    }

    /**
     * Dependency injection for the type of action button the text box handles.
     *
     * @param imeOptions Define the action button
     * @param listener Handles the ime action events
     */
    public void setImeAction(int imeOptions, TextView.OnEditorActionListener listener) {
        textBox.setImeOptions(imeOptions);
        textBox.setOnEditorActionListener(listener);
    }

    /*
     * Initializes this component
     *
     * @param ctx The context in which this method is called
     */
    private void init(Context ctx) {
        // Set the layout:
        LayoutInflater li = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        li.inflate(R.layout.clearable_edit_text, this);

        // Capture Views:
        textBox = (EditText) findViewById(R.id.clearable_edit_text_text_box);
        clearButton = (ImageView) findViewById(R.id.clearable_edit_text_clear_button);

        // Once the user starts typing, render the clear button visible:
        textBox.addTextChangedListener(this);

        // If the user taps the clear button, remove the search text:
        clearButton.setOnClickListener(this);
    }
}
