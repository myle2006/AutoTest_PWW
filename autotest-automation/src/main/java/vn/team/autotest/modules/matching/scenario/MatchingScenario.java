package vn.team.autotest.modules.matching.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.matching.page.MatchingPage;
public final class MatchingScenario extends AbstractModuleScenario { protected String route(){return "/matching";} protected PwwModulePage page(){return new MatchingPage(driver(),config());} }
