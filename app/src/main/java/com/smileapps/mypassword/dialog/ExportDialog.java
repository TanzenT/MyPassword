package com.smileapps.mypassword.dialog;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.smileapps.mypassword.R;
import com.smileapps.mypassword.model.AsyncResult;
import com.smileapps.mypassword.model.AsyncSingleTask;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.service.Mainbinder;
import com.smileapps.mypassword.service.OnGetAllPasswordCallback;

/**
 * 내보내기
 *
 * @author zeus8502
 */
public class ExportDialog extends ProgressDialog implements OnGetAllPasswordCallback {
    private Mainbinder mainbinder;

    /** 내보내기 파일 이름 형식 */
    private SimpleDateFormat fileNameFormat;

    public ExportDialog(Context context, Mainbinder mainbinder) {
        super(context);
        this.mainbinder = mainbinder;

        setProgressStyle(ProgressDialog.STYLE_SPINNER);
        setMessage(getString(R.string.export_ing));
        setCancelable(false);

        fileNameFormat = new SimpleDateFormat(getString(R.string.export_filename), Locale.getDefault());
    }

    @Override
    public void show() {
        Builder builder = new Builder(getContext());
        builder.setTitle(R.string.export_to_sd);
        builder.setMessage(R.string.export_dialog_attention);
        builder.setNeutralButton(R.string.i_known, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                ExportDialog.super.show();
                mainbinder.getAllPassword(ExportDialog.this);
            }
        });
        builder.setNegativeButton(R.string.export_dialog_cancle, null);
        builder.show();
    }

    private String getString(int id) {
        return getContext().getString(id);
    }

    /** 파일 이름 저장 */
    private String getFileName() {
        String fileName = fileNameFormat.format(new Date());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        stringBuilder.append(File.separator);
        stringBuilder.append("MyPassword");
        stringBuilder.append(File.separator);
        stringBuilder.append(fileName);
        stringBuilder.append(".mp");
        return stringBuilder.toString();
    }

    @SuppressWarnings("Convert2Diamond")
    @Override
    public void onGetAllPassword(String groupName, final List<Password> passwords) {
        final List<Password> tempPasswords = new ArrayList<>(passwords);
        new AsyncSingleTask<File>() {
            @Override
            protected AsyncResult<File> doInBackground(AsyncResult<File> asyncResult) {
                String fileName = getFileName();
                File file = new File(fileName);
                file.getParentFile().mkdirs();
                file.deleteOnExit();
                PrintWriter printWriter = null;
                try {
                    file.createNewFile();
                    printWriter = new PrintWriter(file);
                    for (Password password : tempPasswords) {
                        printWriter.println(password.toJSON());
                    }
                    asyncResult.setData(file);
                    asyncResult.setResult(0);
                } catch (IOException e) {
                    asyncResult.setResult(-1);
                    e.printStackTrace();
                } finally {
                    if (printWriter != null) {
                        printWriter.close();
                    }
                }
                return asyncResult;
            }

            @Override
            protected void runOnUIThread(AsyncResult<File> asyncResult) {
                dismiss();
                if (asyncResult.getResult() == 0) {
                    String msg = getContext().getString(R.string.export_successful, asyncResult.getData().getName());

                    Builder builder = new Builder(getContext());
                    builder.setMessage(msg);
                    builder.setNeutralButton(R.string.i_known, null);
                    Dialog dialog = builder.create();
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                } else {
                    // 오류!
                    String msg = getContext().getString(R.string.toast_export_failed);
                    Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }
}
