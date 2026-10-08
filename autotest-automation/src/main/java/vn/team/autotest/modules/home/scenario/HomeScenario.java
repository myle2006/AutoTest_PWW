package vn.team.autotest.modules.home.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.home.page.HomePage;
public final class HomeScenario extends AbstractModuleScenario { protected String route(){return "/";} protected PwwModulePage page(){return new HomePage(driver(),config());} }
