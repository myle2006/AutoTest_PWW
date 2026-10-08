package vn.team.autotest.modules.wordtype.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.wordtype.page.WordTypePage;
public final class WordTypeScenario extends AbstractModuleScenario { protected String route(){return "/admin/word-types";} protected PwwModulePage page(){return new WordTypePage(driver(),config());} }
