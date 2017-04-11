package com.duy.pascal.frontend.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.duy.pascal.frontend.R;
import com.duy.pascal.frontend.code.CodeSample;
import com.duy.pascal.frontend.file.ApplicationFileManager;

import java.io.File;

/**
 * Created by Duy on 10-Apr-17.
 */

public class DialogCreateNewFile extends DialogFragment {
    public static final String TAG = DialogCreateNewFile.class.getSimpleName();
    private EditText mEditFileName;
    private Button btnOK, btnCancel;
    private OnCreateNewFileListener listener;
    private RadioButton checkBoxPas;
    private RadioButton checkBoxInp;
    private ApplicationFileManager mFileManager;

    public static DialogCreateNewFile getInstance() {
        DialogCreateNewFile dialogCreateNewFile = new DialogCreateNewFile();
        return dialogCreateNewFile;
    }

    public void setListener(OnCreateNewFileListener listener) {
        this.listener = listener;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            listener = (OnCreateNewFileListener) getActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFileManager = new ApplicationFileManager(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_new_file, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditFileName = (EditText) view.findViewById(R.id.edit_file_name);
        mEditFileName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    doCreateFile();
                    if (listener != null) listener.onCancel();
                    dismiss();
                    return true;
                }
                return false;
            }
        });

        btnOK = (Button) view.findViewById(R.id.btn_ok);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) listener.onCancel();
                dismiss();
            }
        });
        checkBoxPas = (RadioButton) view.findViewById(R.id.rad_pas);
        checkBoxInp = (RadioButton) view.findViewById(R.id.rad_inp);

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = doCreateFile();
                if (file != null)
                    if (listener != null) {
                        listener.onFileCreated(file);
                        listener.onCancel();
                        dismiss();
                    }
            }
        });

    }

    private File doCreateFile() {
        //get string path of in edit text
        String fileName = mEditFileName.getText().toString();
        if (fileName.isEmpty()) {
            mEditFileName.setError(getString(R.string.enter_new_file_name));
            return null;
        }
        if (checkBoxInp.isChecked()) fileName += ".inp";
        else if (checkBoxPas.isChecked()) fileName += ".pas";
        File file = new File(ApplicationFileManager.getApplicationPath() + fileName);
        if (file.exists()) {
            mEditFileName.setError(getString(R.string.file_exist));
            return null;
        }
        //create new file
        String filePath = mFileManager.createNewFile(ApplicationFileManager.getApplicationPath() + fileName);
        file = new File(filePath);
        if (checkBoxPas.isChecked()) {
            mFileManager.saveFile(file, CodeSample.MAIN);
        }
        return file;
    }

    public interface OnCreateNewFileListener {
        void onFileCreated(File file);

        void onCancel();
    }

}