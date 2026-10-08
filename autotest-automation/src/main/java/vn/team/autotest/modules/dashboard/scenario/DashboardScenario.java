package vn.team.autotest.modules.dashboard.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.dashboard.page.DashboardPage;
public final class DashboardScenario extends AbstractModuleScenario { protected String route(){return "/dashboard";} protected PwwModulePage page(){return new DashboardPage(driver(),config());} }
