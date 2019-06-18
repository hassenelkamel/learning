package org.exoplatform.addon.elearning.integration.notification;


import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.addon.elearning.integration.NotificationUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.HTMLEntityEncoder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.gatein.common.text.EntityEncoder;

@TemplateConfigs(templates = {

    @TemplateConfig(pluginId = AddCoursePlugin.ID, template = "war:/notification/templates/mail/AddCoursePlugin.gtmpl")
})
public class MailTemplateProvider extends TemplateProvider {
  //--- Use a dedicated DateFormatter to handle date pattern coming from underlying levels : Wed Mar 15 01:00:00 CET 2017
  // --- Create formatter
  protected DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
  protected static Log log = ExoLogger.getLogger(MailTemplateProvider.class);

  public MailTemplateProvider(InitParams initParams) {
    super(initParams);

    this.templateBuilders.put(PluginKey.key(AddCoursePlugin.ID), new TemplateBuilder());
  }

  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {

      EntityEncoder encoder = HTMLEntityEncoder.getInstance();

      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);
      log.info("Prepare Mail notif for "+ notification.getKey().getId());
      String courseCreator = notification.getValueOwnerParameter(NotificationUtils.COURSE_CREATOR);
      String courseTitle = notification.getValueOwnerParameter(NotificationUtils.COURSE_TITLE);


      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      Identity author = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, courseCreator, true);
      Profile profile = author.getProfile();
      //creator
      templateContext.put("USER", encoder.encode(profile.getFullName()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(profile));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", author.getRemoteId()));
      //receiver
      Identity receiver = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getTo(), true);
      templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
      templateContext.put("LAST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.LAST_NAME).toString()));
      templateContext.put("COURSE_TITLE", encoder.encode(courseTitle));



      templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));
      String subject = TemplateUtils.processSubject(templateContext);

      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();

      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      //
      Identity receiver = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, first.getTo(), true);
      templateContext.put("FIRST_NAME", encoder.encode(receiver.getProfile().getProperty(Profile.FIRST_NAME).toString()));
      templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));

      try {
        writer.append(buildDigestMsg(notifications, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }

    protected String buildDigestMsg(List<NotificationInfo> notifications, TemplateContext templateContext) {
      EntityEncoder encoder = HTMLEntityEncoder.getInstance();

      Map<String, List<NotificationInfo>> map = new HashMap<String, List<NotificationInfo>>();

      StringBuilder sb = new StringBuilder();
      return sb.toString();
    }
  }

}
