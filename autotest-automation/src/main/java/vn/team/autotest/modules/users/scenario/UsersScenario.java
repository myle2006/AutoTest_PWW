package vn.team.autotest.modules.users.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.users.page.UsersPage;
public final class UsersScenario extends AbstractModuleScenario { protected String route(){return "/admin/users";} protected PwwModulePage page(){return new UsersPage(driver(),config());} }
