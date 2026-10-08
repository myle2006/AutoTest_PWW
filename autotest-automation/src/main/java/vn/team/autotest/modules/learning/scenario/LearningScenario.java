package vn.team.autotest.modules.learning.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.learning.page.LearningPage;
public final class LearningScenario extends AbstractModuleScenario { protected String route(){return "/learning";} protected PwwModulePage page(){return new LearningPage(driver(),config());} }
