package io.cygert.forceprojecttabs;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.wm.WindowManager;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.openapi.actionSystem.PlatformCoreDataKeys.CONTEXT_COMPONENT;

class MacOsForceProjectTabs implements ProjectActivity {

    @Nullable
    @Override
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        var mergeAllWindowsAction = ActionManager.getInstance().getAction("MergeAllWindowsAction");
        if(mergeAllWindowsAction == null) {
            return null;
        }
        var firstProject = getFirstProjectOrCurrent(project);
        var fakeEvent = createFakeActionEvent(firstProject);
        mergeAllWindowsAction.actionPerformed(fakeEvent);
        return null;
    }

    private Project getFirstProjectOrCurrent(Project project) {
        @NotNull Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        return openProjects.length == 0 ? project : openProjects[0];
    }

    private static AnActionEvent createFakeActionEvent(Project project) {
        DataContext context = dataId ->
                CONTEXT_COMPONENT.getName().equals(dataId) ? WindowManager.getInstance().getFrame(project) : null;

        return new AnActionEvent(null, context, "", new Presentation(), ActionManager.getInstance(), 0);
    }
}
