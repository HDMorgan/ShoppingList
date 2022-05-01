package com.harrydmorgan.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.internal.TextDrawableHelper;

import org.w3c.dom.Text;

public class TextDialog extends AppCompatDialogFragment {
    private EditText editText;
    private final String title;
    private final TextDialogListener listener;


    public TextDialog(String title, TextDialogListener listener) {
        this.title = title;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.text_dialog, null);

        editText = view.findViewById(R.id.dialog_editText);

        builder.setView(view)
                .setTitle(title)
                .setNegativeButton("Cancel", (dialogInterface, i) -> {listener.cancelAction();})
                .setPositiveButton("Set", (dialogInterface, i) -> {
                    listener.setAction(editText.getText().toString());
                });
        return builder.create();
    }

    public interface TextDialogListener {
        void setAction(String textEntered);

        void cancelAction();
    }
}
