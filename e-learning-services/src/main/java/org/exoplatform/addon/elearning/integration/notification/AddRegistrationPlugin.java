package org.exoplatform.addon.elearning.integration.notification;

import org.exoplatform.addon.elearning.integration.NotificationUtils;
import org.exoplatform.addon.elearning.service.dto.CourseRegistrationDTO;
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

public class AddRegistrationPlugin extends BaseNotificationPlugin {


  public static final Log LOG= ExoLogger.getExoLogger(AddRegistrationPlugin.class);
  public final static String ID ="AddCommentPlugin";

  public final static ArgumentLiteral<CourseRegistrationDTO> REGISTRATION =new ArgumentLiteral<CourseRegistrationDTO>(CourseRegistrationDTO.class, "ticket");






  IdentityManager identityManager;

  public AddRegistrationPlugin(InitParams initParams, IdentityManager identityManager) {
    super(initParams);
    this.identityManager = identityManager;
  }

  public AddRegistrationPlugin(InitParams initParams) {
    super(initParams);
  }


  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

  @Override
  protected NotificationInfo makeNotification(NotificationContext ctx) {

    CourseRegistrationDTO registration =ctx.value(REGISTRATION);
    Set<String> receivers =new HashSet<String>();
    receivers.add(registration.getIdRegistration().toString());
    return NotificationInfo.instance()
                           .setFrom(registration.getDateRegistration())
                           .with(NotificationUtils.REGISTRATION_CREATOR, registration.getIdWorker().toString())
                           .setSendAll(true)
                           .key(getKey()).end();


  }
}
