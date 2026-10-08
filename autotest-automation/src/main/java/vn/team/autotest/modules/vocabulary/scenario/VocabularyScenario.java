package vn.team.autotest.modules.vocabulary.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.vocabulary.page.VocabularyPage;
public final class VocabularyScenario extends AbstractModuleScenario { protected String route(){return "/admin/vocabulary";} protected PwwModulePage page(){return new VocabularyPage(driver(),config());} }
