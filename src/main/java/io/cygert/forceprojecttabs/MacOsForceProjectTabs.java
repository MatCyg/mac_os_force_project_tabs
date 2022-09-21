package io.cygert.forceprojecttabs;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.wm.WindowManager;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.actionSystem.PlatformCoreDataKeys.CONTEXT_COMPONENT;

class MacOsForceProjectTabs implements StartupActivity.Background {
    @Override
    public void runActivity(@NotNull Project project) {
        var mergeAllWindowsAction = ActionManager.getInstance().getAction("MergeAllWindowsAction");
        if(mergeAllWindowsAction == null) {
            return;
        }
        var firstProject = getFirstProjectOrCurrent(project);
        var fakeEvent = createFakeActionEvent(firstProject);
        mergeAllWindowsAction.actionPerformed(fakeEvent);
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
