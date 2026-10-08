package vn.team.autotest.modules.flashcard.scenario;
import vn.team.autotest.automation.base.*; import vn.team.autotest.modules.flashcard.page.FlashcardPage;
public final class FlashcardScenario extends AbstractModuleScenario { protected String route(){return "/flashcards";} protected PwwModulePage page(){return new FlashcardPage(driver(),config());} }
