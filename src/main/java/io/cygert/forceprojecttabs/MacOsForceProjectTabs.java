package io.cygert.forceprojecttabs;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionUiKind;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ActionUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.mac.foundation.Foundation;
import com.intellij.ui.mac.foundation.MacUtil;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class MacOsForceProjectTabs implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        if (!isIdeFullyInitialized(project)) {
            return null;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            var mergeAllWindowsAction = ActionManager.getInstance().getAction("MergeAllWindowsAction");
            if (mergeAllWindowsAction == null) {
                return;
            }
            var firstProject = getFirstProjectOrCurrent(project);
            var fakeEvent = createFakeActionEvent(firstProject);
            ActionUtil.performActionDumbAwareWithCallbacks(mergeAllWindowsAction, fakeEvent);
        });

        return null;
    }

    private boolean isIdeFullyInitialized(@NotNull Project project) {
        var frames = WindowManager.getInstance().getAllProjectFrames();
        var window = WindowManager.getInstance().getFrame(project);
        var tabs = Foundation.invoke(MacUtil.getWindowFromJavaWindow(window), "tabbedWindows");
        int tabCount = Foundation.invoke(tabs, "count").intValue();

        return tabCount >= 2 && frames.length >= tabCount;
    }

    private Project getFirstProjectOrCurrent(Project project) {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        return openProjects.length == 0 ? project : openProjects[0];
    }

    private static AnActionEvent createFakeActionEvent(Project project) {
        var window = WindowManager.getInstance().getFrame(project);
        var dataContext = DataManager.getInstance().getDataContext(window);

        return new AnActionEvent(dataContext, new Presentation(), "",
                ActionUiKind.MAIN_MENU, null, 0, ActionManager.getInstance());
    }
}
