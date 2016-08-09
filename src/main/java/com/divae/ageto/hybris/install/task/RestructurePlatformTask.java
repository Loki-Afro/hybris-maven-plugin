package com.divae.ageto.hybris.install.task;

import com.divae.ageto.hybris.install.task.CopyFilesTasks.CopyDirectoryContentToDirectoryTask;
import com.divae.ageto.hybris.install.task.CopyFilesTasks.CopyFileToDirectoryTask;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Marvin Haagen
 */
public class RestructurePlatformTask extends AbstractWorkDirectoryTask {

    @Override
    protected void execute(final TaskContext taskContext, final File workDirectory) {
        final File resourcesDirectory = new File("src/main/resources");
        new TaskChainTask(Arrays.<InstallTask>asList( //
                new CopyFileToDirectoryTask(new File("bin/platform/project.properties"), resourcesDirectory), //
                new CopyDirectoryContentToDirectoryTask(new File("bin/platform/resources"), resourcesDirectory), //
                new CopyDirectoryContentToDirectoryTask(new File("bin/platform/bootstrap/resources"),
                        new File(resourcesDirectory + "/bootstrap")), //
                new CreatePomTask("com/divae/ageto/hybris/install/models.pom.xml", "models",
                        Collections.<String, String>emptyMap()) //
        )).execute(taskContext);
    }

}
