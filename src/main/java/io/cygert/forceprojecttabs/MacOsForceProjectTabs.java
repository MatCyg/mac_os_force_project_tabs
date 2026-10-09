package io.cygert.forceprojecttabs;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.WindowManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Timer;

class MacOsForceProjectTabs implements ProjectActivity {
    private static final int RETRY_DELAY_MS = 200;
    private static final int MAX_RETRIES = 100;

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        ApplicationManager.getApplication().invokeLater(() -> mergeWhenWindowIsReady(project));

        return null;
    }

    private void mergeWhenWindowIsReady(@NotNull Project project) {
        var timer = new Timer(RETRY_DELAY_MS, null);
        var retries = new int[1];
        timer.addActionListener(event -> {
            if (project.isDisposed()) {
                timer.stop();
                return;
            }

            var window = WindowManager.getInstance().getFrame(project);
            if (window != null && window.isShowing()) {
                timer.stop();
                if (WindowManager.getInstance().getAllProjectFrames().length > 1) {
                    var mergeAction = ActionManager.getInstance().getAction("MergeAllWindowsAction");
                    if (mergeAction != null) {
                        ActionManager.getInstance().tryToExecute(mergeAction, null, window,
                                "ForceProjectTabs", true);
                    }
                }
            } else if (++retries[0] >= MAX_RETRIES) {
                timer.stop();
            }
        });
        timer.start();
    }
}
