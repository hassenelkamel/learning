package org.exoplatform.addon.elearning.integration.notification;

import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import org.exoplatform.addon.elearning.integration.NotificationUtils;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
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
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.webui.utils.TimeConvertUtils;
import org.gatein.common.text.EntityEncoder;

@TemplateConfigs(
    templates = {
        @TemplateConfig( pluginId = AddCoursePlugin.ID, template="war:/notification/templates/web/AddCoursePlugin.gtmpl"),
        @TemplateConfig( pluginId = UpdateCoursePlugin.ID, template="war:/notification/templates/web/UpdateCoursePlugin.gtmpl"),
    }
)


public class WebTemplateProvider extends TemplateProvider {

  protected DateFormat formatter = new SimpleDateFormat("E MMM dd HH:mm:ss Z yyyy");
  protected static Log log = ExoLogger.getLogger(WebTemplateProvider.class);

  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(AddCoursePlugin.ID), new TemplateBuilder());
    this.templateBuilders.put(PluginKey.key(UpdateCoursePlugin.ID), new TemplateBuilder());
  }

  private class TemplateBuilder extends AbstractTemplateBuilder {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String pluginId = notification.getKey().getId();

      String language = getLanguage(notification);
      TemplateContext templateContext = TemplateContext.newChannelInstance(getChannelKey(), pluginId, language);

      String creator = notification.getValueOwnerParameter(NotificationUtils.COURSE_CREATOR);
      String ideaTitle = notification.getValueOwnerParameter(NotificationUtils.COURSE_TITLE);

      Identity identity=null;

      EntityEncoder encoder = HTMLEntityEncoder.getInstance();
      IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
      identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, creator, true);
      Profile profile = identity.getProfile();
      templateContext.put("USER", encoder.encode(profile.getFullName()));
      templateContext.put("AVATAR", profile.getAvatarUrl() != null ? profile.getAvatarUrl() : LinkProvider.PROFILE_DEFAULT_AVATAR_URL);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("COURSE_TITLE", ideaTitle);
      templateContext.put("COURSE_CREATOR",creator);

      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(NotificationMessageUtils.READ_PORPERTY.getKey())) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      Calendar lastModified = Calendar.getInstance();
      lastModified.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgoByTimeServer(lastModified.getTime(),"EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));

      //
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }

  };


}
