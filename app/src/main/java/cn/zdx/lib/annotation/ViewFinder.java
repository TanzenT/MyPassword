/*
 * Create by 윤규도 on 2017. 11. 26.
 * Copyright (C) 2017. 윤규도. All rights reserved.
 */
package cn.zdx.lib.annotation;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

/**
 * View查找器
 *
 * @author zengdexing
 */
public abstract class ViewFinder {
    public static ViewFinder create(final Activity activity) {
        return new ActivityViewFinder(activity);
    }

    public static ViewFinder create(final View view) {
        return new ViewViewFinder(view);
    }

    public static ViewFinder create(final Dialog dialog) {
        return new DialogViewFinder(dialog);
    }

    public abstract View findViewById(int id);

    public static class DialogViewFinder extends ViewFinder {
        private Dialog dialog;

        public DialogViewFinder(Dialog activity) {
            super();
            this.dialog = activity;
        }

        @Override
        public View findViewById(int id) {
            return dialog.findViewById(id);
        }
    }

    public static class ActivityViewFinder extends ViewFinder {
        private Activity activity;

        public ActivityViewFinder(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        public View findViewById(int id) {
            return activity.findViewById(id);
        }
    }

    public static class ViewViewFinder extends ViewFinder {
        private View view;

        public ViewViewFinder(View view) {
            super();
            this.view = view;
        }

        @Override
        public View findViewById(int id) {
            return view.findViewById(id);
        }
    }
}
