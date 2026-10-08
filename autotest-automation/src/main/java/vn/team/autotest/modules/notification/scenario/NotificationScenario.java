package vn.team.autotest.modules.notification.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.notification.page.NotificationPage;
public final class NotificationScenario extends AbstractModuleScenario { protected String route(){return "/notifications";} protected PwwModulePage page(){return new NotificationPage(driver(),config());} }
