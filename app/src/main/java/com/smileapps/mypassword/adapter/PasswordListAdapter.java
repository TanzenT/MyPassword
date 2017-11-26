/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */

package com.smileapps.mypassword.adapter;

import android.app.AlertDialog.Builder;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.smileapps.mypassword.R;
import com.smileapps.mypassword.activity.EditPasswordActivity;
import com.smileapps.mypassword.model.Password;
import com.smileapps.mypassword.service.Mainbinder;
import cn.zdx.lib.annotation.FindViewById;
import cn.zdx.lib.annotation.XingAnnotationHelper;

/**
 * 주요 어댑터
 *
 * @author zeus8502
 */
public class PasswordListAdapter extends BaseAdapter {
    /** 하루 지연 */
    private static final long DAY = 1000 * 60 * 60 * 24;
    private List<PasswordItem> passwords = new ArrayList<PasswordItem>();
    private Context context;
    private SimpleDateFormat simpleDateFormatYear = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private int padding;
    private Mainbinder mainbinder;
    private SimpleDateFormat simpleDateFormatMonth = new SimpleDateFormat("MM-dd", Locale.getDefault());
    private String passwordGroup;
    private Comparator<PasswordItem> comparator = new Comparator<PasswordItem>() {
        @Override
        public int compare(PasswordItem lhs, PasswordItem rhs) {
            // 맨 위에 고정
            if (lhs.password.isTop() || rhs.password.isTop()) {
                if (lhs.password.isTop() && rhs.password.isTop()) {
                    return (int) (rhs.password.getCreateDate() - lhs.password.getCreateDate());
                } else if (lhs.password.isTop()) {
                    return -1;
                } else {
                    return 1;
                }
            }
            long value = rhs.password.getCreateDate() - lhs.password.getCreateDate();
            if (value > 0)
                return 1;
            else if (value == 0)
                return 0;
            else
                return -1;
        }
    };

    public PasswordListAdapter(Context context) {
        this.context = context;
        padding = dip2px(6);
    }

    public int dip2px(float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    public void setData(List<Password> passwords, Mainbinder mainbinder) {
        this.mainbinder = mainbinder;
        this.passwords.clear();
        for (Password password : passwords) {
            this.passwords.add(new PasswordItem(password));
        }
        Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return passwords.size();
    }

    @Override
    public PasswordItem getItem(int position) {
        return passwords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void notifyDataSetChanged() {
        for (PasswordItem passwordItem : passwords) {
            passwordItem.initDataString();
        }
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.main_password_item, parent, false);
            convertView.setTag(viewHolder);
            XingAnnotationHelper.findView(viewHolder, convertView);
            viewHolder.copyView.setOnClickListener(viewHolder);
            viewHolder.deleteView.setOnClickListener(viewHolder);
            viewHolder.editView.setOnClickListener(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == 0) {
            convertView.setPadding(padding, padding, padding, padding);
        } else {
            convertView.setPadding(padding, 0, padding, padding);
        }
        PasswordItem passwordItem = getItem(position);
        viewHolder.bindView(passwordItem);
        return convertView;
    }

    private String fomartDate(long createDate) {
        String result = "";
        long currentTime = System.currentTimeMillis();
        long distance = currentTime - createDate;
        if (createDate > currentTime) {
            result = simpleDateFormatYear.format(createDate);
        } else if (distance < 1000 * 60) {
            result = context.getString(R.string.just);
        } else if (distance < 1000 * 60 * 60) {
            String dateString = context.getString(R.string.minute_ago);
            result = String.format(Locale.getDefault(), dateString, distance / (1000 * 60));
        } else if (distance < DAY) {
            String dateString = context.getString(R.string.hour_ago);
            result = String.format(Locale.getDefault(), dateString, distance / (1000 * 60 * 60));
        } else if (distance < DAY * 365) {
            result = simpleDateFormatMonth.format(createDate);
        } else {
            result = simpleDateFormatYear.format(createDate);
        }
        return result;
    }

    public void onNewPassword(Password password) {
        passwords.add(0, new PasswordItem(password));
        Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void onDeletePassword(int id) {
        for (int i = 0; i < passwords.size(); i++) {
            PasswordItem passwordItem = passwords.get(i);
            if (passwordItem.password.getId() == id) {
                passwords.remove(i);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void onUpdatePassword(Password newPassword) {
        boolean needSort = false;
        boolean hasFind = false;
        for (int i = 0; i < passwords.size(); i++) {
            Password oldPassword = passwords.get(i).password;
            if (oldPassword.getId() == newPassword.getId()) {
                if (newPassword.getCreateDate() != 0)
                    oldPassword.setCreateDate(newPassword.getCreateDate());
                if (newPassword.getTitle() != null)
                    oldPassword.setTitle(newPassword.getTitle());
                if (newPassword.getUserName() != null)
                    oldPassword.setUserName(newPassword.getUserName());
                if (newPassword.getPassword() != null)
                    oldPassword.setPassword(newPassword.getPassword());
                if (newPassword.getNote() != null)
                    oldPassword.setNote(newPassword.getNote());
                if (oldPassword.isTop() != newPassword.isTop()) {
                    oldPassword.setTop(newPassword.isTop());
                    needSort = true;
                }
                if (!oldPassword.getGroupName().equals(newPassword.getGroupName()))
                    passwords.remove(i);
                hasFind = true;
                break;
            }
        }

        if (!hasFind) {
            passwords.add(0, new PasswordItem(newPassword));
            needSort = true;
        }

        if (needSort)
            Collections.sort(this.passwords, comparator);
        notifyDataSetChanged();
    }

    public void setPasswordGroup(String passwordGroup) {
        this.passwordGroup = passwordGroup;
    }

    private class ViewHolder implements android.view.View.OnClickListener {
        @FindViewById(R.id.main_item_title)
        public TextView titleView;

        @FindViewById(R.id.main_item_date)
        public TextView dateView;

        @FindViewById(R.id.main_item_name)
        public TextView nameView;

        @FindViewById(R.id.main_item_password)
        public TextView passwordView;

        @FindViewById(R.id.main_item_note)
        public TextView noteView;

        @FindViewById(R.id.main_item_note_container)
        public View noteConainer;

        @FindViewById(R.id.main_item_top)
        public View topIconView;

        @FindViewById(R.id.main_item_copy)
        public View copyView;

        @FindViewById(R.id.main_item_delete)
        public View deleteView;

        @FindViewById(R.id.main_item_edit)
        public View editView;

        private PasswordItem passwordItem;

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.main_item_copy:
                    onCopyClick();
                    break;
                case R.id.main_item_delete:
                    onDeleteClick();
                    break;
                case R.id.main_item_edit:
                    onEditClick();
                    break;
                default:
                    break;
            }
        }

        private void onCopyClick() {
            Builder builder = new Builder(context);
            String[] item = new String[]{context.getResources().getString(R.string.copy_name),
                    context.getResources().getString(R.string.copy_password)};
            builder.setItems(item, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            // 아이디 복사
                            ClipboardManager cmbName = (ClipboardManager) context
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipDataName = ClipData.newPlainText(null, passwordItem.password.getUserName());
                            cmbName.setPrimaryClip(clipDataName);
                            Toast.makeText(context, R.string.copy_name_toast, Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            // 비밀번호 복사
                            ClipboardManager cmbPassword = (ClipboardManager) context
                                    .getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData clipData = ClipData.newPlainText(null, passwordItem.password.getPassword());
                            cmbPassword.setPrimaryClip(clipData);
                            Toast.makeText(context, R.string.copy_password_toast, Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }
            });
            builder.show();
        }

        private void onEditClick() {
            Intent intent = new Intent(context, EditPasswordActivity.class);
            intent.putExtra(EditPasswordActivity.ID, passwordItem.password.getId());
            intent.putExtra(EditPasswordActivity.PASSWORD_GROUP, passwordGroup);
            context.startActivity(intent);
        }

        private void onDeleteClick() {
            Builder builder = new Builder(context);
            builder.setMessage(R.string.alert_delete_message);
            builder.setTitle(passwordItem.password.getTitle());
            builder.setNeutralButton(R.string.yes, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mainbinder.deletePassword(passwordItem.password.getId());
                }
            });
            builder.setNegativeButton(R.string.no, null);
            builder.show();
        }

        void bindView(PasswordItem passwordItem) {
            this.passwordItem = passwordItem;
            titleView.setText(passwordItem.password.getTitle());
            dateView.setText(passwordItem.dataString);
            nameView.setText(passwordItem.password.getUserName());
            passwordView.setText(passwordItem.password.getPassword());

            String note = passwordItem.password.getNote();
            if (TextUtils.isEmpty(note)) {
                noteConainer.setVisibility(View.GONE);
            } else {
                noteConainer.setVisibility(View.VISIBLE);
                noteView.setText(note);
            }

            if (passwordItem.password.isTop()) {
                topIconView.setVisibility(View.VISIBLE);
                dateView.setTextColor(context.getResources().getColor(R.color.title_color));
            } else {
                topIconView.setVisibility(View.GONE);
                dateView.setTextColor(context.getResources().getColor(R.color.text_color));
            }
        }
    }

    public class PasswordItem {
        public String dataString;
        public Password password;

        private PasswordItem(Password password) {
            this.password = password;
            initDataString();
        }
        public void initDataString() {
            dataString = fomartDate(password.getCreateDate());
        }
    }
}
