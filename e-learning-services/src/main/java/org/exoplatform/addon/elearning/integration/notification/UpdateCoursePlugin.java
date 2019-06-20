package org.exoplatform.addon.elearning.integration.notification;

import org.exoplatform.addon.elearning.integration.NotificationUtils;
import org.exoplatform.addon.elearning.service.dto.CourseDTO;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManager;

import java.util.HashSet;
import java.util.Set;

public class UpdateCoursePlugin  extends BaseNotificationPlugin {
  private static final Log                       LOG    = ExoLogger.getLogger(UpdateCoursePlugin.class);
  public final static String                     ID     = "UpdateCoursePlugin";
  public final static ArgumentLiteral<CourseDTO> COURSE = new ArgumentLiteral<CourseDTO>(CourseDTO.class, "ticket");
  IdentityManager identityManager;

  public UpdateCoursePlugin(InitParams initParams,IdentityManager identityManager) {
    super(initParams);
    this.identityManager = identityManager;
  }

  public UpdateCoursePlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext notificationContext) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification( NotificationContext ctx) {
    CourseDTO course = ctx.value(COURSE);
    Set<String> receivers = new HashSet<String>();
    receivers.add(course.getUserName());

    return NotificationInfo.instance()

                           .setFrom(course.getUserName())
                           .with(NotificationUtils.COURSE_TITLE, course.getNameCourse())
                           .with(NotificationUtils.COURSE_CREATOR, course.getUserName())
                           .setSendAll(true)
                           .key(getKey()).end();
  }
}
