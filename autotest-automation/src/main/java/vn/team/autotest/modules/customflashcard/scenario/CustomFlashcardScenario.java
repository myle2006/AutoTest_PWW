package vn.team.autotest.modules.customflashcard.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.customflashcard.page.CustomFlashcardPage;
public final class CustomFlashcardScenario extends AbstractModuleScenario { protected String route(){return "/my-flashcards";} protected PwwModulePage page(){return new CustomFlashcardPage(driver(),config());} }
